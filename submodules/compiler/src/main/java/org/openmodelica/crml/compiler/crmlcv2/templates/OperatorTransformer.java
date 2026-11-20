package org.openmodelica.crml.compiler.crmlcv2.templates;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.openmodelica.crml.compiler.crmlcv2.Diagnostic;
import org.openmodelica.crml.compiler.crmlcv2.Diagnostics;
import org.openmodelica.crml.compiler.crmlcv2.GeneratedOperator;
import org.openmodelica.crml.compiler.crmlcv2.Lifter;
import org.openmodelica.crml.compiler.crmlcv2.SetHandle;
import org.openmodelica.crml.compiler.crmlcv2.TransformationContext;
import org.openmodelica.crml.compiler.crmlcv2.util.CRMLCompilerException;
import org.openmodelica.crml.compiler.crmlcv2.util.TypeResolver;
import org.openmodelica.crml.compiler.crmlcv2.util.CRMLCompilerException.UnsupportedConstruct;
import org.openmodelica.crml.model.metamodel.Binding;
import org.openmodelica.crml.model.metamodel.ComputedValue;
import org.openmodelica.crml.model.metamodel.CustomOperator;
import org.openmodelica.crml.model.metamodel.Keyword;
import org.openmodelica.crml.model.metamodel.Value;
import org.openmodelica.crml.model.metamodel.Variable;
import org.openmodelica.crml.modelica.metamodel.ClassDefinition;
import org.openmodelica.crml.modelica.metamodel.ClassKind;
import org.openmodelica.crml.modelica.metamodel.ComponentDeclaration;
import org.openmodelica.crml.modelica.metamodel.RawReason;
import org.openmodelica.crml.modelica.metamodel.Reference;
import org.openmodelica.crml.modelica.metamodel.Expression;
import org.openmodelica.crml.modelica.build.Modelica;

/**
 * CRML templates and user operators to nested Modelica classes, and calls to
 * them to function calls or block instantiations.
 *
 * <h2>Which Modelica class kind an operator becomes</h2>
 *
 * The choice is made mechanically, per operator: transform the body into a
 * scratch class first and look at what came out.
 *
 * <ul>
 * <li>Nothing hoisted - the body is a pure expression over the parameters - so
 *     the operator becomes a {@code function} with one {@code algorithm}
 *     statement, and its calls become ordinary function calls. This is the
 *     cheapest form and the one the runtime library itself uses for its
 *     pure operations (and4, not4, cvBooleanToBoolean4).
 * <li>Something hoisted - the body declared a component or an equation - so a
 *     function is impossible: a Modelica function body is an algorithm, and it
 *     cannot hold components that are defined by equations. The operator
 *     becomes a {@code block}, whose body is exactly what was hoisted plus
 *     {@code out = <expression>}, and its calls become component
 *     instantiations wired with equations.
 * </ul>
 *
 * <p>The legacy AST visitor emitted {@code model} for every operator
 * (crmlVisitorImpl.java:297) with no recorded reason. {@code model} is rejected
 * here: it is strictly weaker than {@code block}, since {@code block} is exactly
 * a class restricted to causal input/output connectors, which is what a CRML
 * operator is. The one place the runtime library itself uses {@code model} with
 * input/output prefixes - {@code Blocks.setAnd} - is listed as a defect in the
 * plan (§8.2(g)), which is the most likely origin of the legacy choice.
 */
public final class OperatorTransformer implements Modelica {

    private final Transformer transformer;

    public OperatorTransformer(Transformer transformer) {
        this.transformer = transformer;
    }

    // --- declaration --------------------------------------------------------

    /**
     * Generates the class for {@code operator} and nests it in the class of its
     * owner (see {@link #ownerContext}), or returns the one already generated.
     */
    public GeneratedOperator declare(TransformationContext ctx, CustomOperator operator) {
        GeneratedOperator existing = ctx.generatedOperator(operator);
        if (existing != null) {
            return existing;
        }
        // An operator that already failed is not retried: the gap is recorded
        // once, however many call sites there are.
        CRMLCompilerException.UnsupportedConstruct failure = ctx.operatorFailure(operator);
        if (failure != null) {
            throw failure;
        }
        if (!ctx.beginVisiting(operator)) {
            throw CRMLCompilerException.unsupported(Diagnostics.error("CustomOperator",
                "operator '" + describe(operator) + "' is defined in terms of itself", operator));
        }
        try {
            return generate(ctx, operator);
        } catch (CRMLCompilerException.UnsupportedConstruct e) {
            ctx.recordOperatorFailure(operator, e);
            throw e;
        } finally {
            ctx.endVisiting(operator);
        }
    }

    private GeneratedOperator generate(TransformationContext ctx, CustomOperator operator) {
        List<Variable> parameters = new ArrayList<Variable>(operator.getVariables());
        TransformationContext owner = ownerContext(ctx, operator);
        String name = owner.allocateClassName(modelicaName(operator));

        if (operator.getDefinition() == null) {
            throw CRMLCompilerException.unsupported(Diagnostics.error("CustomOperator",
                "operator '" + describe(operator) + "' has no definition to translate", operator));
        }

        // Transform the body into a scratch class. If it turns out to be pure,
        // the scratch stays empty and only the expression is kept; if it hoisted
        // anything, the scratch already is the block's body.
        ClassDefinition body = block(name);
        TransformationContext bodyCtx = owner.nested(body);
        bodyCtx.reserveName(GeneratedOperator.OUTPUT_PORT);
        for (Variable parameter : parameters) {
            bodyCtx.reserveName(parameter.getName());
        }
        Expression result = transformer.values().transform(bodyCtx, operator.getDefinition());
        boolean hoisted = !body.getComponents().isEmpty() || !body.getEquations().isEmpty();

        // Parameters first, then the output, then whatever the body hoisted.
        int index = 0;
        for (Variable parameter : parameters) {
            body.getComponents().add(index++, input(owner, parameter, operator));
        }
        body.getComponents().add(index, output(
            resolve(owner, operator.getDomain(), operator, "result"), GeneratedOperator.OUTPUT_PORT));

        if (hoisted) {
            body.getEquations().add(eq(ref(GeneratedOperator.OUTPUT_PORT), result));
        } else {
            body.setKind(ClassKind.FUNCTION);
            body.getStatements().add(assign(ref(GeneratedOperator.OUTPUT_PORT), result));
        }

        GeneratedOperator generated = new GeneratedOperator(body, parameters);
        ctx.registerOperator(operator, generated);
        owner.defineClass(body, operator);
        return generated;
    }

    /**
     * A context targeting the Modelica class the operator's class is nested in:
     * the class generated for the CRML class declaring it, or the model for an
     * operator the model itself declares.
     *
     * <p>Placement follows the declaring scope, not the call site, so the first
     * call to reach an operator cannot decide where it lives. crml.xcore does not
     * let a class own operators yet, so today every operator lands in the model.
     */
    private TransformationContext ownerContext(TransformationContext ctx, CustomOperator operator) {
        EObject container = operator.eContainer();
        if (container instanceof org.openmodelica.crml.model.metamodel.Class) {
            org.openmodelica.crml.model.metamodel.Class clazz = (org.openmodelica.crml.model.metamodel.Class) container;
            ClassDefinition definition = ctx.generatedClass(clazz);
            if (definition == null) {
                throw CRMLCompilerException.unsupported(Diagnostics.error("CustomOperator",
                    "operator '" + describe(operator) + "' is a member of class '" + clazz.getName()
                    + "', which has no generated counterpart", operator));
            }
            return ctx.nested(definition);
        }
        return ctx.root();
    }

    private ComponentDeclaration input(TransformationContext ctx, Variable parameter,
            CustomOperator operator) {
        return input(
            resolve(ctx, parameter.getDomain(), operator, "parameter '" + parameter.getName() + "'"),
            parameter.getName());
    }

    /**
     * An operator whose parameter or result type cannot be resolved is dropped
     * whole: emitting the class with a hole in a declaration would produce
     * invalid Modelica, where dropping it leaves a diagnostic and a placeholder.
     */
    private Reference<ClassDefinition> resolve(TransformationContext ctx,
            org.openmodelica.crml.model.metamodel.TypeReference type, CustomOperator operator, String what) {
        if (type == null) {
            throw CRMLCompilerException.unsupported(Diagnostics.error("CustomOperator",
                "the " + what + " of operator '" + describe(operator) + "' has no declared type", operator));
        }
        Reference<ClassDefinition> reference = transformer.types().resolve(ctx, type);
        if (reference.getTarget() == null && reference.getReason() == RawReason.UNRESOLVED) {
            throw CRMLCompilerException.unsupported(Diagnostics.error("CustomOperator",
                "cannot resolve the type of the " + what + " of operator '" + describe(operator) + "'",
                operator));
        }
        return reference;
    }

    // --- call sites ---------------------------------------------------------

    public Expression transformCall(TransformationContext ctx, ComputedValue call) {
        CustomOperator operator = call.getOperator();
        if (operator == null) {
            throw CRMLCompilerException.unsupported(Diagnostics.error("ComputedValue",
                "the call does not refer to any operator", call));
        }
        GeneratedOperator generated = declare(ctx, operator);

        if (generated.isBlock()) {
            Map<String, Expression> inputs = new LinkedHashMap<String, Expression>();
            for (Variable parameter : generated.parameters()) {
                inputs.put(parameter.getName(),
                    transformer.values().transform(ctx, argument(call, operator, parameter)));
            }
            // The generated class itself, not its name: this is the reference
            // that makes renaming it impossible to get wrong.
            return transformer.blocks().instantiate(ctx, resolvedRef(generated.definition()),
                generated.name() + "_", inputs, GeneratedOperator.OUTPUT_PORT, call);
        }

        List<Expression> arguments = new ArrayList<Expression>();
        for (Variable parameter : generated.parameters()) {
            arguments.add(transformer.values().transform(ctx, argument(call, operator, parameter)));
        }
        return call(resolvedRef(generated.definition()), arguments);
    }

    /**
     * The argument bound to {@code parameter}, found by element identity. Mixfix
     * operators interleave keywords and values, so a binding's position in the
     * list says nothing about which parameter it fills.
     */
    private Value argument(ComputedValue call, CustomOperator operator, Variable parameter) {
        for (Binding binding : call.getBindings()) {
            if (binding.getElement() == parameter) {
                Value value = binding.getValue();
                if (value == null) {
                    throw CRMLCompilerException.unsupported(Diagnostics.error("ComputedValue",
                        "parameter '" + parameter.getName() + "' of operator '" + describe(operator)
                        + "' is bound to nothing", call));
                }
                return value;
            }
        }
        throw CRMLCompilerException.unsupported(Diagnostics.error("ComputedValue",
            "no argument bound to parameter '" + parameter.getName() + "' of operator '"
            + describe(operator) + "'", call));
    }

    /**
     * A call with {@code x.element} arguments: the operator is applied once for
     * each slot of the {@link Lifter}'s region. A function is called per slot; a
     * block is instantiated per slot, through the lifted mode of the context.
     */
    public SetHandle transformLifted(Lifter lifter, ComputedValue call) {
        CustomOperator operator = call.getOperator();
        if (operator == null) {
            throw CRMLCompilerException.unsupported(Diagnostics.error("ComputedValue",
                "the call does not refer to any operator", call));
        }
        GeneratedOperator generated = declare(lifter.outer(), operator);

        Expression value;
        if (generated.isBlock()) {
            Map<String, Expression> inputs = new LinkedHashMap<String, Expression>();
            for (Variable parameter : generated.parameters()) {
                inputs.put(parameter.getName(), lifter.operand(argument(call, operator, parameter)));
            }
            value = transformer.blocks().instantiate(lifter.body(), resolvedRef(generated.definition()),
                generated.name() + "_", inputs, GeneratedOperator.OUTPUT_PORT, call);
        } else {
            List<Expression> arguments = new ArrayList<Expression>();
            for (Variable parameter : generated.parameters()) {
                arguments.add(lifter.operand(argument(call, operator, parameter)));
            }
            value = call(resolvedRef(generated.definition()), arguments);
        }
        return lifter.close(value, generated.resultType(),
            TypeResolver.resolveBuiltin(operator.getDomain()), call);
    }

    // --- naming -------------------------------------------------------------

    /**
     * A Modelica class name for a mixfix operator, built from its keywords:
     * {@code 'becomes false'} becomes {@code op_becomes_false}.
     *
     * <p>The {@code op_} prefix and the sanitising are not cosmetic. A generated
     * class is referred to in call position, where a name is written verbatim -
     * an operator spelled {@code 'or'} would otherwise produce {@code or(...)},
     * and {@code or} is a Modelica reserved word. Making the name a plain
     * identifier by construction keeps every use site valid without quoting.
     */
    static String modelicaName(CustomOperator operator) {
        if (operator.getName() != null && !operator.getName().isEmpty()) {
            return sanitize(operator.getName());
        }
        StringBuilder builder = new StringBuilder("op");
        for (Keyword keyword : operator.getKeywords()) {
            builder.append('_').append(sanitize(unquote(keyword.getKeyword())));
        }
        return builder.length() == 2 ? "op_anonymous" : builder.toString();
    }

    private static String unquote(String keyword) {
        if (keyword == null) {
            return "";
        }
        String trimmed = keyword.trim();
        if (trimmed.length() >= 2 && trimmed.charAt(0) == '\'' && trimmed.charAt(trimmed.length() - 1) == '\'') {
            return trimmed.substring(1, trimmed.length() - 1);
        }
        return trimmed;
    }

    private static String sanitize(String text) {
        StringBuilder builder = new StringBuilder(text.length());
        boolean lastWasUnderscore = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            boolean plain = c == '_' || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9');
            if (plain) {
                builder.append(c);
                lastWasUnderscore = c == '_';
            } else if (!lastWasUnderscore) {
                builder.append('_');
                lastWasUnderscore = true;
            }
        }
        return builder.length() == 0 ? "x" : builder.toString();
    }

    /** A readable spelling of the operator for diagnostics. */
    static String describe(CustomOperator operator) {
        if (operator.getName() != null && !operator.getName().isEmpty()) {
            return operator.getName();
        }
        StringBuilder builder = new StringBuilder();
        for (org.openmodelica.crml.model.metamodel.OperatorHeaderElement element : operator.getHeader()) {
            if (builder.length() > 0) {
                builder.append(' ');
            }
            if (element instanceof Keyword) {
                builder.append(((Keyword) element).getKeyword());
            } else if (element instanceof Variable) {
                builder.append(((Variable) element).getName());
            }
        }
        return builder.toString();
    }

    /** Records a diagnostic for an operator that could not be generated. */
    public static void reportFailure(TransformationContext ctx, Diagnostic diagnostic) {
        ctx.reportWithPlaceholder(diagnostic);
    }
}
