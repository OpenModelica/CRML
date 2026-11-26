package org.openmodelica.crml.model.base.typing;

import java.util.EnumMap;
import java.util.Map;

import org.openmodelica.crml.model.metamodel.BuiltinType;

/**
 * The kind of the elements of a value, without its {@link Multiplicity}: a
 * built-in type today, a CRML class later.
 *
 * <p>Immutable, compared with {@link #equals(Object)}. The variants are
 * closed: the constructor is package-private, and each variant is a nested
 * class. A class type would be added as another variant holding a reference
 * (never a containment) to its {@code Domain}.
 */
public abstract class BaseType {

    private static final Map<BuiltinType, BaseType> BUILTINS = new EnumMap<>(BuiltinType.class);

    static {
        for (BuiltinType type : BuiltinType.values()) {
            BUILTINS.put(type, new Builtin(type));
        }
    }

    BaseType() {
    }

    /** The base type of a built-in type. */
    public static BaseType of(BuiltinType type) {
        if (type == null) {
            throw new IllegalArgumentException("no built-in type");
        }
        return BUILTINS.get(type);
    }

    /** The built-in type this base type is, or null if it is not one. */
    public BuiltinType getBuiltinType() {
        return null;
    }

    /** True if this base type is the given built-in type. */
    public boolean is(BuiltinType type) {
        return getBuiltinType() == type;
    }

    /** A built-in type of CRML. */
    private static final class Builtin extends BaseType {
        private final BuiltinType type;

        Builtin(BuiltinType type) {
            this.type = type;
        }

        @Override
        public BuiltinType getBuiltinType() {
            return type;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof Builtin && ((Builtin) other).type == type;
        }

        @Override
        public int hashCode() {
            return type.hashCode();
        }

        @Override
        public String toString() {
            return type.getName();
        }
    }
}
