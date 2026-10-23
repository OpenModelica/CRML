package crml.compiler.crmlcv2.templates.value;

import crml.model.language.BuiltinType;

/**
 * Operand type predicates shared by the operator transformers, lifted out of
 * the string generator's {@code ValueGenUtil}.
 *
 * <p>The "OrUnknown" predicates let generation proceed when type inference has
 * not resolved an operand's type - {@code Value#getReturnType()} only yields a
 * type for constants today - while a definitively-known incompatible type still
 * fails.
 */
public final class TypeCategories {

    private TypeCategories() {
    }

    public static boolean isNumericOrUnknown(BuiltinType t) {
        return t == null || t == BuiltinType.INTEGER || t == BuiltinType.REAL;
    }

    public static boolean isBooleanOrUnknown(BuiltinType t) {
        return t == null || t == BuiltinType.BOOLEAN;
    }

    public static boolean isStringCompatible(BuiltinType t) {
        return t == null || t == BuiltinType.STRING || t == BuiltinType.INTEGER || t == BuiltinType.REAL;
    }

    public static boolean involvesClockOrPeriod(BuiltinType t) {
        return t == BuiltinType.CLOCK || t == BuiltinType.PERIOD;
    }

    public static boolean isPeriodOrUnknown(BuiltinType t) {
        return t == null || t == BuiltinType.PERIOD;
    }

    public static boolean isClockOrUnknown(BuiltinType t) {
        return t == null || t == BuiltinType.CLOCK;
    }
}
