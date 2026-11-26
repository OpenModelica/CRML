package org.openmodelica.crml.model.base.typing;

import java.util.Objects;

import org.openmodelica.crml.model.metamodel.BuiltinType;

/**
 * The type of a CRML value: a {@link BaseType} and a {@link Multiplicity}, or
 * {@link #UNKNOWN}.
 *
 * <p>This is the computed type, as opposed to a {@code TypeReference}, which
 * is the type as written in the source. It is immutable and not an EMF
 * object, so it can be returned, stored and compared freely.
 *
 * <p>The size of a set is not part of its type; see {@link SizeInference}.
 */
public final class ValueType {

    /**
     * The type of a value whose type cannot be inferred: an incomplete or
     * ill-typed expression, or one whose rules are not implemented yet. Telling
     * these apart is the validator's job.
     */
    public static final ValueType UNKNOWN = new ValueType(null, null);

    private final BaseType base;
    private final Multiplicity multiplicity;

    private ValueType(BaseType base, Multiplicity multiplicity) {
        this.base = base;
        this.multiplicity = multiplicity;
    }

    /** A single element of the given base type. */
    public static ValueType single(BaseType base) {
        return new ValueType(Objects.requireNonNull(base, "base"), Multiplicity.SINGLE);
    }

    /** A set of elements of the given base type. */
    public static ValueType set(BaseType base) {
        return new ValueType(Objects.requireNonNull(base, "base"), Multiplicity.SET);
    }

    /** A single element of the given built-in type. */
    public static ValueType of(BuiltinType type) {
        return single(BaseType.of(type));
    }

    /** A set of elements of the given built-in type. */
    public static ValueType setOf(BuiltinType type) {
        return set(BaseType.of(type));
    }

    /** The given base type as a single value, or {@link #UNKNOWN} if it is null. */
    public static ValueType singleOrUnknown(BaseType base) {
        return base == null ? UNKNOWN : single(base);
    }

    /** The given base type as a set, or {@link #UNKNOWN} if it is null. */
    public static ValueType setOrUnknown(BaseType base) {
        return base == null ? UNKNOWN : set(base);
    }

    public boolean isUnknown() {
        return base == null;
    }

    public boolean isSingle() {
        return multiplicity == Multiplicity.SINGLE;
    }

    public boolean isSet() {
        return multiplicity == Multiplicity.SET;
    }

    /** The base type, null if unknown. */
    public BaseType getBase() {
        return base;
    }

    /** The multiplicity, null if unknown. */
    public Multiplicity getMultiplicity() {
        return multiplicity;
    }

    /** The built-in base type, null if unknown or not a built-in type. */
    public BuiltinType getBuiltinType() {
        return base == null ? null : base.getBuiltinType();
    }

    /** True if this is a single value of the given built-in type. */
    public boolean isSingle(BuiltinType type) {
        return isSingle() && base.is(type);
    }

    /** The same base type as a set; {@link #UNKNOWN} stays unknown. */
    public ValueType asSet() {
        return isUnknown() ? UNKNOWN : set(base);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ValueType)) {
            return false;
        }
        ValueType that = (ValueType) other;
        return Objects.equals(base, that.base) && multiplicity == that.multiplicity;
    }

    @Override
    public int hashCode() {
        return Objects.hash(base, multiplicity);
    }

    /** {@code Integer}, {@code set of Integer} or {@code unknown}. */
    @Override
    public String toString() {
        if (isUnknown()) {
            return "unknown";
        }
        return isSet() ? "set of " + base : base.toString();
    }
}
