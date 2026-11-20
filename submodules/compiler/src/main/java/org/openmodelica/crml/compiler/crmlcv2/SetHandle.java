package org.openmodelica.crml.compiler.crmlcv2;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.openmodelica.crml.compiler.crmlcv2.util.TypeResolver;
import org.openmodelica.crml.model.metamodel.BuiltinType;
import org.openmodelica.crml.modelica.metamodel.BinaryOperatorKind;
import org.openmodelica.crml.modelica.metamodel.ClassDefinition;
import org.openmodelica.crml.modelica.metamodel.ComponentReference;
import org.openmodelica.crml.modelica.metamodel.Expression;
import org.openmodelica.crml.modelica.metamodel.Reference;
import org.openmodelica.crml.modelica.build.Modelica;

/**
 * What the generator knows about a collection in the generated Modelica: its
 * size, its elements, which of its slots currently hold a real element, and the
 * type of an element.
 *
 * <p>A collection is a fixed-size buffer, because Modelica array sizes are
 * fixed. A slot is <em>active</em> when it holds a real element; an inactive
 * slot is there, but whatever is computed from it must be ignored. A plain
 * array is always fully active.
 *
 * <p>Every accessor returns a new tree on each call, so a result can be put
 * anywhere in the Modelica object model without stealing it from another place.
 *
 * <p><b>Temporary design, to be reworked.</b> The three kinds are factory
 * methods over one class on purpose, to keep this small until the lifting
 * generator has been evaluated. Open for that evaluation: whether the kinds
 * should be subclasses, whether attribute projection ({@code set.attr.element})
 * fits as a decorator on {@link #element}, and whether {@link #size()} can
 * always be {@code size(values(), 1)}.
 */
public final class SetHandle implements Modelica {

    private enum Kind {
        ARRAY,
        PERIODS,
        LIFTED
    }

    private static final String PERIOD_FIELD = "period";
    private static final String COUNTER_FIELD = "counter";

    private final Kind kind;
    /** The array, the CRMLPeriods record, or the values of a lifted result. */
    private final ComponentReference base;
    private final ComponentReference mask;
    private final ComponentReference size;
    private final Reference<ClassDefinition> elementType;
    private final BuiltinType elementBuiltin;

    private SetHandle(Kind kind, ComponentReference base, ComponentReference mask, ComponentReference size,
            Reference<ClassDefinition> elementType, BuiltinType elementBuiltin) {
        this.kind = kind;
        this.base = base;
        this.mask = mask;
        this.size = size;
        this.elementType = elementType;
        this.elementBuiltin = elementBuiltin;
    }

    /**
     * A plain array: {@code size(a, 1)}, {@code a[k]}, always active.
     *
     * @param elementBuiltin the CRML type of an element, or null if unknown
     */
    public static SetHandle array(ComponentReference array, Reference<ClassDefinition> elementType,
            BuiltinType elementBuiltin) {
        return new SetHandle(Kind.ARRAY, array, null, null, elementType, elementBuiltin);
    }

    /**
     * A {@code CRMLPeriods} record: {@code size(P.period, 1)},
     * {@code P.period[k]}, active while {@code k <= P.counter}.
     *
     * <p>This assumes a library contract: {@code CRMLPeriods} has a buffer
     * {@code CRMLPeriod period[...]} and an {@code Integer counter}, the number
     * of periods opened so far, with entries {@code 1..counter} valid. The
     * {@code CRMLtoModelica.mo} bundled with the compiler does not provide these
     * fields - a possible library error - so until a library does, generated
     * code that lifts over {@code Periods} does not pass {@code checkModel}.
     */
    public static SetHandle periods(ComponentReference periods) {
        return new SetHandle(Kind.PERIODS, periods, null, null, null, BuiltinType.PERIOD);
    }

    /**
     * The result of a lifted application: {@code size} slots, {@code values[k]},
     * active where {@code mask[k]}.
     *
     * @param elementBuiltin the CRML type of a result, or null if unknown
     */
    public static SetHandle lifted(ComponentReference values, ComponentReference mask, ComponentReference size,
            Reference<ClassDefinition> elementType, BuiltinType elementBuiltin) {
        return new SetHandle(Kind.LIFTED, values, mask, size, elementType, elementBuiltin);
    }

    /** The number of slots. */
    public Expression size() {
        switch (kind) {
            case PERIODS:
                return call(this.<ClassDefinition>builtinRef("size"), member(base, PERIOD_FIELD), integer(1));
            case LIFTED:
                return EcoreUtil.copy(size);
            default:
                return call(this.<ClassDefinition>builtinRef("size"), EcoreUtil.copy(base), integer(1));
        }
    }

    /** The element in slot {@code k}; {@code k} becomes part of the result. */
    public Expression element(Expression k) {
        return kind == Kind.PERIODS ? subscripted(member(base, PERIOD_FIELD), k) : subscripted(base, k);
    }

    /**
     * Whether slot {@code k} holds a real element, as a Modelica Boolean, or
     * null if every slot always does. {@code k} becomes part of the result.
     */
    public Expression active(Expression k) {
        switch (kind) {
            case PERIODS:
                return binary(BinaryOperatorKind.LE, k, member(base, COUNTER_FIELD));
            case LIFTED:
                return subscripted(mask, k);
            default:
                return null;
        }
    }

    /** The whole array of elements. */
    public Expression values() {
        return kind == Kind.PERIODS ? member(base, PERIOD_FIELD) : EcoreUtil.copy(base);
    }

    /** The whole array of {@link #active} flags, or null if there is no such array. */
    public Expression mask() {
        return mask == null ? null : EcoreUtil.copy(mask);
    }

    /**
     * True if an inactive slot already holds {@code Boolean4.undefined}, the
     * identity of the Boolean4 operators, so a fold over {@link #element} can
     * ignore {@link #active}: the Boolean or Requirement result of a lifted
     * application.
     */
    public boolean inactiveIsUndefined() {
        return kind == Kind.LIFTED
            && (elementBuiltin == BuiltinType.BOOLEAN || elementBuiltin == BuiltinType.REQUIREMENT);
    }

    /** The Modelica type of an element. */
    public Reference<ClassDefinition> elementType() {
        if (kind == Kind.PERIODS) {
            return this.<ClassDefinition>libraryRef(TypeResolver.resolve(BuiltinType.PERIOD));
        }
        return EcoreUtil.copy(elementType);
    }

    /** The CRML type of an element, or null if unknown. */
    public BuiltinType elementBuiltin() {
        return elementBuiltin;
    }
}
