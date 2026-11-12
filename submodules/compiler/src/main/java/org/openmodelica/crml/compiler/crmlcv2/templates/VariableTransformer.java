package org.openmodelica.crml.compiler.crmlcv2.templates;

import org.openmodelica.crml.compiler.crmlcv2.Diagnostic;
import org.openmodelica.crml.compiler.crmlcv2.TransformationContext;
import org.openmodelica.crml.compiler.crmlcv2.util.CRMLCompilerException;
import org.openmodelica.crml.model.metamodel.Set;
import org.openmodelica.crml.model.metamodel.Variable;
import org.openmodelica.crml.modelica.metamodel.ClassDefinition;
import org.openmodelica.crml.modelica.metamodel.ComponentDeclaration;
import org.openmodelica.crml.modelica.metamodel.Expression;
import org.openmodelica.crml.modelica.metamodel.Reference;
import org.openmodelica.crml.modelica.metamodel.Variability;
import org.openmodelica.crml.modelica.build.Modelica;

/**
 * A CRML {@code Variable} becomes one Modelica component declaration, with the
 * variable's definition as its binding where it has one.
 *
 * <p>This is the declaration boundary: if the definition cannot be translated,
 * the whole declaration is dropped - an expression hole cannot produce valid
 * Modelica - and the gap is recorded once, naming this variable.
 */
public final class VariableTransformer implements Modelica {

    private final Transformer transformer;

    public VariableTransformer(Transformer transformer) {
        this.transformer = transformer;
    }

    public void transform(TransformationContext ctx, Variable variable) {
        Reference<ClassDefinition> declaredType = transformer.types().resolve(ctx, variable.getDomain());

        Expression binding = null;
        if (variable.getDefinition() != null && !ValueTransformer.isDeclarationOnly(variable.getDefinition())) {
            try {
                binding = transformer.values().transform(ctx, variable.getDefinition());
            } catch (CRMLCompilerException.UnsupportedConstruct e) {
                Diagnostic cause = e.diagnostic();
                ctx.reportWithPlaceholder(new Diagnostic(cause.severity(), cause.constructKind(),
                    "variable '" + variable.getName() + "' was dropped: " + cause.message(),
                    cause.crmlSource() == null ? variable : cause.crmlSource()));
                return;
            }
        }

        ComponentDeclaration declaration = component(declaredType, variable.getName(), binding);
        if (Boolean.TRUE.equals(variable.getConstant())) {
            declaration.setVariability(Variability.CONSTANT);
        }
        if (variable.getDefinition() instanceof Set<?>) {
            // A set-valued variable is an array of its element type, which is not
            // its declared CRML domain: "Periods P3 is { P1, P2, Pn }" declares a
            // set of Periods, and its elements are Periods, not CRMLPeriods.
            Set<?> set = (Set<?>) variable.getDefinition();
            Reference<ClassDefinition> elementType = transformer.sets().elementType(set);
            if (elementType != null) {
                declaration.setDeclaredType(elementType);
            }
            declaration.getArrayDimensions().add(dimension(integer(SetTransformer.sizeOf(set))));
        }
        ctx.declare(declaration, variable);
    }

    /**
     * True when the variable has no value bound to it, so a class containing it
     * cannot be instantiated on its own.
     */
    public static boolean isUnbound(Variable variable) {
        return variable.getDefinition() == null
            || ValueTransformer.isDeclarationOnly(variable.getDefinition());
    }
}
