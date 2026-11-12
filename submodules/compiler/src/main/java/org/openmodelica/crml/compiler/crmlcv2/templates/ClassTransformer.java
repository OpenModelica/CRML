package org.openmodelica.crml.compiler.crmlcv2.templates;

import org.openmodelica.crml.compiler.crmlcv2.Diagnostic;
import org.openmodelica.crml.compiler.crmlcv2.Diagnostics;
import org.openmodelica.crml.compiler.crmlcv2.TransformationContext;
import org.openmodelica.crml.compiler.crmlcv2.util.CRMLCompilerException;
import org.openmodelica.crml.model.metamodel.Variable;
import org.openmodelica.crml.modelica.metamodel.ClassDefinition;
import org.openmodelica.crml.modelica.metamodel.ComponentDeclaration;
import org.openmodelica.crml.modelica.metamodel.Expression;
import org.openmodelica.crml.modelica.metamodel.Reference;
import org.openmodelica.crml.modelica.metamodel.Variability;
import org.openmodelica.crml.modelica.build.Modelica;

/**
 * A CRML class becomes a nested Modelica {@code model}.
 *
 * <p>Not a {@code record}: a CRML class variable may carry an expression
 * ("Boolean nostart is (...)", "Requirement noStartProb is ..."), and those
 * become equations, which a Modelica record cannot hold. Where a class happens
 * to have only external variables a record would work, but mixing record and
 * model in one extends hierarchy is illegal in Modelica, so every class is a
 * model.
 *
 * <p>A CRML partial class - one that "cannot have any instances because [it is]
 * incompletely defined" - is a Modelica {@code partial} model.
 */
public final class ClassTransformer implements Modelica {

    private final Transformer transformer;

    public ClassTransformer(Transformer transformer) {
        this.transformer = transformer;
    }

    /**
     * The empty class, name and kind only. Every class is shelled before any
     * body is populated, so a member or a super-class that names a class
     * declared later in the model still resolves to an object.
     */
    public ClassDefinition shell(org.openmodelica.crml.model.metamodel.Class clazz) {
        ClassDefinition definition = model(clazz.getName());
        definition.setPartial(Boolean.valueOf(Boolean.TRUE.equals(clazz.getPartial())));
        return definition;
    }

    /** Fills in the extends clauses and members of an already-shelled class. */
    public void populate(TransformationContext ctx, org.openmodelica.crml.model.metamodel.Class clazz,
            ClassDefinition definition) {
        for (org.openmodelica.crml.model.metamodel.Class superClass : clazz.getSuperClasses()) {
            ClassDefinition superDefinition = ctx.generatedClass(superClass);
            if (superDefinition == null) {
                ctx.report(Diagnostics.error("Class.superClasses",
                    "class '" + clazz.getName() + "' extends a class with no generated counterpart",
                    clazz));
                continue;
            }
            definition.getExtendsClauses().add(extendsClause(resolvedRef(superDefinition)));
        }

        TransformationContext body = ctx.nested(definition);
        for (Variable variable : clazz.getVariables()) {
            body.reserveName(variable.getName());
        }
        for (Variable variable : clazz.getVariables()) {
            declare(body, variable);
        }
    }

    /**
     * One component per class variable, and an equation for each variable that
     * has a definition. The definition becomes an equation rather than a binding
     * because that is what makes the enclosing class a model rather than a
     * record.
     */
    private void declare(TransformationContext ctx, Variable variable) {
        Reference<ClassDefinition> declaredType = transformer.types().resolve(ctx, variable.getDomain());

        Expression definition = null;
        if (variable.getDefinition() != null && !ValueTransformer.isDeclarationOnly(variable.getDefinition())) {
            try {
                definition = transformer.values().transform(ctx, variable.getDefinition());
            } catch (CRMLCompilerException.UnsupportedConstruct e) {
                Diagnostic cause = e.diagnostic();
                ctx.reportWithPlaceholder(new Diagnostic(cause.severity(), cause.constructKind(),
                    "member '" + variable.getName() + "' was dropped: " + cause.message(),
                    cause.crmlSource() == null ? variable : cause.crmlSource()));
                return;
            }
        }

        ComponentDeclaration component = component(declaredType, variable.getName());
        if (Boolean.TRUE.equals(variable.getConstant())) {
            component.setVariability(Variability.CONSTANT);
        }
        ctx.declare(component, variable);

        if (definition != null) {
            ctx.equate(eq(ref(variable.getName()), definition), variable);
        }
    }
}
