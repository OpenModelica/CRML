package org.openmodelica.crml.compiler.crmlcv2.templates;

import org.eclipse.emf.ecore.EObject;
import org.openmodelica.crml.compiler.crmlcv2.Diagnostics;
import org.openmodelica.crml.compiler.crmlcv2.SetHandle;
import org.openmodelica.crml.compiler.crmlcv2.TransformationContext;
import org.openmodelica.crml.compiler.crmlcv2.util.CRMLCompilerException;
import org.openmodelica.crml.compiler.crmlcv2.util.TypeResolver;
import org.openmodelica.crml.model.metamodel.BuiltinType;
import org.openmodelica.crml.model.metamodel.Liftable;
import org.openmodelica.crml.model.metamodel.ScatteredValue;
import org.openmodelica.crml.model.metamodel.Set;
import org.openmodelica.crml.model.metamodel.Value;
import org.openmodelica.crml.model.metamodel.Variable;
import org.openmodelica.crml.model.metamodel.VariableReference;
import org.openmodelica.crml.modelica.metamodel.ArrayConstructor;
import org.openmodelica.crml.modelica.metamodel.ClassDefinition;
import org.openmodelica.crml.modelica.metamodel.ComponentDeclaration;
import org.openmodelica.crml.modelica.metamodel.ComponentReference;
import org.openmodelica.crml.modelica.metamodel.Expression;
import org.openmodelica.crml.modelica.metamodel.RawReason;
import org.openmodelica.crml.modelica.metamodel.Reference;
import org.openmodelica.crml.modelica.build.Modelica;

/**
 * Values in <em>set positions</em>, and {@code x.element} itself.
 *
 * <p>A value is in a set position when a collection is expected: the
 * {@code x} of {@code x.element}, and the operand of an aggregator
 * ({@code and (...)}). {@link #collect} translates such a value to a
 * {@link SetHandle}. Everywhere else a single value is expected, and
 * {@link ValueTransformer} translates it.
 *
 * <p>An {@code x.element} that lifts an application is never translated on its
 * own: the application's {@link org.openmodelica.crml.compiler.crmlcv2.Lifter} replaces it with
 * the element of the current slot. So {@link #transform}, which
 * {@link ValueTransformer} calls for an {@code x.element} it meets, only ever
 * reports why that one is not valid there.
 *
 * <p><b>Experimental, subject to change:</b> an {@code x.element} that is not an
 * operand of an operator or an aggregator - under {@code if},
 * {@code integrate}, {@code new}, a set literal, ... - is reported as
 * {@code Lift.NoApplication}.
 */
public final class ScatteredValueTransformer implements Modelica {

    private final Transformer transformer;

    public ScatteredValueTransformer(Transformer transformer) {
        this.transformer = transformer;
    }

    /**
     * Translates a value in a set position. May hoist into {@code ctx}: the
     * region of a lifted application, or a variable holding a set literal or a
     * computed {@code Periods}.
     */
    public SetHandle collect(TransformationContext ctx, Value value) {
        if (value == null) {
            throw CRMLCompilerException.unsupported(Diagnostics.error("Value",
                "a value is missing from the object model", null));
        }
        Value v = LiftableTransformer.unparen(value);

        // A lifted application: its result is the collection (chaining).
        if (v instanceof Liftable && !((Liftable) v).getLifts().isEmpty()) {
            return transformer.liftables().region(ctx, (Liftable) v);
        }
        // x.element without an application: the set of elements of x. The linker
        // leaves only the operand of an aggregator like this on purpose.
        if (v instanceof ScatteredValue && ((ScatteredValue) v).getMethod() == null) {
            return collect(ctx, ((ScatteredValue) v).getData());
        }
        if (v instanceof Set<?>) {
            return collectLiteral(ctx, (Set<?>) v);
        }
        if (v instanceof VariableReference) {
            Variable variable = ((VariableReference) v).getVariable();
            if (variable != null && variable.getDefinition() instanceof Set<?>) {
                // Declared as an array of its element type by VariableTransformer.
                Set<?> set = (Set<?>) variable.getDefinition();
                Reference<ClassDefinition> elementType = transformer.sets().elementType(set);
                if (elementType == null) {
                    elementType = transformer.types().resolve(ctx, variable.getDomain());
                }
                return SetHandle.array(ref(variable.getName()), elementType, TypeResolver.elementBuiltin(v));
            }
        }
        if (TypeResolver.inferBuiltin(v) == BuiltinType.PERIODS) {
            Expression periods = transformer.values().transform(ctx, v);
            if (!(periods instanceof ComponentReference)) {
                String name = ctx.allocateName("periods");
                periods = ctx.declare(component(
                    this.<ClassDefinition>libraryRef(TypeResolver.resolve(BuiltinType.PERIODS)), name, periods), v);
            }
            return SetHandle.periods((ComponentReference) periods);
        }
        BuiltinType type = TypeResolver.inferBuiltin(v);
        throw CRMLCompilerException.unsupported(Diagnostics.error("Lift.NotACollection",
            "a " + v.eClass().getName() + " of type " + (type == null ? "unknown" : type.toString())
            + " is not a collection: expected a set, a Periods or a lifted application", v));
    }

    /** A set literal is stored in a variable, so its elements can be indexed. */
    private SetHandle collectLiteral(TransformationContext ctx, Set<?> set) {
        Reference<ClassDefinition> elementType = transformer.sets().elementType(set);
        if (elementType == null || elementType.getReason() == RawReason.UNRESOLVED) {
            throw CRMLCompilerException.unsupported(Diagnostics.error("Lift.NotACollection",
                "a Set of type unknown is not a collection that can be stored: the type of its "
                + "elements cannot be determined", set));
        }
        ArrayConstructor elements = transformer.sets().literal(ctx, set);
        ComponentDeclaration component = component(elementType, ctx.allocateName("set"), elements);
        component.getArrayDimensions().add(dimension(integer(elements.getElements().size())));
        ComponentReference reference = ctx.declare(component, set);
        return SetHandle.array(reference, transformer.sets().elementType(set), TypeResolver.elementBuiltin(set));
    }

    /**
     * An {@code x.element} met where a single value is expected. Always throws.
     */
    public Expression transform(TransformationContext ctx, ScatteredValue sv) {
        if (sv.getMethod() != null) {
            // A linked lift is consumed by the Lifter of its application and
            // never reaches the value dispatch: a defect in the compiler.
            throw CRMLCompilerException.unsupported(Diagnostics.error("Lift.Unlinked",
                "an '.element' operand of a " + sv.getMethod().eClass().getName()
                + " was translated outside the lifting of that application", sv));
        }
        EObject container = sv.eContainer();
        throw CRMLCompilerException.unsupported(Diagnostics.notYetImplemented("Lift.NoApplication",
            "'.element' is only supported as an operand of an operator or of an aggregator, not directly in "
            + (container == null ? "nothing" : "a " + container.eClass().getName()), sv));
    }
}
