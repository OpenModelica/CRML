package org.openmodelica.crml.compiler.crmlcv2.templates;

import org.openmodelica.crml.compiler.crmlcv2.Diagnostic;
import org.openmodelica.crml.compiler.crmlcv2.Diagnostics;
import org.openmodelica.crml.compiler.crmlcv2.TransformationContext;
import org.openmodelica.crml.compiler.crmlcv2.util.CRMLCompilerException;
import org.openmodelica.crml.model.metamodel.CustomOperator;
import org.openmodelica.crml.model.metamodel.Model;
import org.openmodelica.crml.model.metamodel.Operator;
import org.openmodelica.crml.model.metamodel.Variable;
import org.openmodelica.crml.modelica.metamodel.ClassDefinition;
import org.openmodelica.crml.modelica.build.Modelica;

/**
 * A CRML {@code Model} becomes one Modelica {@code model} class.
 *
 * <p>The class is {@code partial} whenever any of its variables is unbound: the
 * verification harnesses in {@code verificationModels/**} all do
 * {@code extends <Name>;} and then bind those variables, and a Modelica class
 * with unbound components is not a legal standalone class. The string generator
 * never marked anything partial.
 */
public final class ModelTransformer implements Modelica {

    private final Transformer transformer;

    public ModelTransformer(Transformer transformer) {
        this.transformer = transformer;
    }

    public TransformationContext transform(Model model) {
        ClassDefinition definition = model(model.getName());
        definition.setPartial(Boolean.valueOf(hasUnboundVariable(model)));

        TransformationContext ctx = TransformationContext.of(definition);
        ctx.linkRoot(model, definition);

        // Before anything is generated, so no allocated name can collide with a
        // name the model itself uses.
        for (Variable variable : model.getAllVariables()) {
            ctx.reserveName(variable.getName());
        }
        for (org.openmodelica.crml.model.metamodel.Set<?> set : model.getSets()) {
            ctx.reserveName(set.getName());
        }


        // Classes first, in two passes. Every class is shelled and registered
        // before any body is populated, so a member or a super-class naming a
        // class declared later in the model still finds an object to point at -
        // which is what removes the need for a link phase entirely.
        for (org.openmodelica.crml.model.metamodel.Class clazz : model.getAllClasses()) {
            ctx.reserveClassName(clazz.getName());
            ClassDefinition shell = transformer.classes().shell(clazz);
            ctx.registerClass(clazz, shell);
            ctx.defineClass(shell, clazz);
        }
        for (org.openmodelica.crml.model.metamodel.Class clazz : model.getAllClasses()) {
            transformer.classes().populate(ctx, clazz, ctx.generatedClass(clazz));
        }

        // Operators next: a variable's definition may call one, and every
        // operator the model declares is emitted whether or not it is called.
        for (Operator operator : model.getAllOperators()) {
            if (operator instanceof CustomOperator) {
                try {
                    transformer.operators().declare(ctx, (CustomOperator) operator);
                } catch (CRMLCompilerException.UnsupportedConstruct e) {
                    Diagnostic cause = e.diagnostic();
                    ctx.reportWithPlaceholder(cause.withMessage("operator '"
                        + OperatorTransformer.describe((CustomOperator) operator) + "' was dropped: " + cause.message(),
                        cause.crmlSource() == null ? operator : cause.crmlSource()));
                }
            } else {
                ctx.reportWithPlaceholder(Diagnostics.libraryGap("Operator." + operator.eClass().getName(),
                    operator.eClass().getName() + " has no Modelica mapping", operator));
            }
        }

        for (Variable variable : model.getAllVariables()) {
            transformer.variables().transform(ctx, variable);
        }

        for (org.openmodelica.crml.model.metamodel.Set<?> set : model.getSets()) {
            transformer.sets().declare(ctx, set);
        }

        /**
         * Note: Imported operators and classes are handled via AllOperators and AllClasses
         */

        for (org.openmodelica.crml.model.metamodel.Object object : model.getObjects()) {
            ctx.reportWithPlaceholder(Diagnostics.libraryGap("Object",
                "Object has no Modelica mapping and is never built by any DOM builder", object));
        }
        if (model.getFrame() != null) {
            ctx.reportWithPlaceholder(Diagnostics.libraryGap("Model.frame",
                "a model frame has no Modelica mapping", model.getFrame()));
        }

        return ctx;
    }

    private static boolean hasUnboundVariable(Model model) {
        for (Variable variable : model.getVariables()) {
            if (VariableTransformer.isUnbound(variable)) {
                return true;
            }
        }
        return false;
    }
}
