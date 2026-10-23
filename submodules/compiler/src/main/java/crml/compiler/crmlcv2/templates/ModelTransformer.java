package crml.compiler.crmlcv2.templates;

import crml.compiler.crmlcv2.Diagnostics;
import crml.compiler.crmlcv2.TransformationContext;
import crml.compiler.crmlcv2.UnsupportedConstruct;
import crml.model.language.CustomOperator;
import crml.model.language.Model;
import crml.model.language.Operator;
import crml.model.language.Variable;
import crml.model.modelica.ClassDefinition;
import crml.modelica.build.Modelica;

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
        for (Variable variable : model.getVariables()) {
            ctx.reserveName(variable.getName());
        }
        for (crml.model.language.Set<?> set : model.getSets()) {
            ctx.reserveName(set.getName());
        }

        // Classes first, in two passes. Every class is shelled and registered
        // before any body is populated, so a member or a super-class naming a
        // class declared later in the model still finds an object to point at -
        // which is what removes the need for a link phase entirely.
        for (crml.model.language.Class clazz : model.getClasses()) {
            ctx.reserveClassName(clazz.getName());
            ClassDefinition shell = transformer.classes().shell(clazz);
            ctx.registerClass(clazz, shell);
            ctx.defineClass(shell, clazz);
        }
        for (crml.model.language.Class clazz : model.getClasses()) {
            transformer.classes().populate(ctx, clazz, ctx.generatedClass(clazz));
        }

        // Operators next: a variable's definition may call one, and every
        // operator the model declares is emitted whether or not it is called.
        for (Operator operator : model.getOperators()) {
            if (operator instanceof CustomOperator) {
                try {
                    transformer.operators().declare(ctx, (CustomOperator) operator);
                } catch (UnsupportedConstruct e) {
                    ctx.reportWithPlaceholder(e.diagnostic());
                }
            } else {
                ctx.reportWithPlaceholder(Diagnostics.libraryGap("Operator." + operator.eClass().getName(),
                    operator.eClass().getName() + " has no Modelica mapping", operator));
            }
        }

        for (Variable variable : model.getVariables()) {
            transformer.variables().transform(ctx, variable);
        }

        for (crml.model.language.Set<?> set : model.getSets()) {
            transformer.sets().declare(ctx, set);
        }
        for (crml.model.language.Dependency dependency : model.getSuperlibs()) {
            ctx.reportWithPlaceholder(Diagnostics.libraryGap("Library.superlibs",
                "library dependencies are never resolved (RootBuilder.parse returns an empty list), "
                + "so nothing can be emitted for this one", dependency));
        }
        for (crml.model.language.Object object : model.getObjects()) {
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
