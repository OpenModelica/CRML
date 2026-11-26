package org.openmodelica.crml.compiler.crmlcv2.templates.value;

import org.openmodelica.crml.model.metamodel.BuiltinType;

/**
 * Operand type predicates shared by the operator transformers, lifted out of
 * the string generator's {@code ValueGenUtil}.
 *
 * <p>The "OrUnknown" predicates let generation proceed when
 * {@code Value#inferType()} cannot type an operand, while a definitively-known
 * incompatible type still fails.
 */
public final class TypeCategories {

    private TypeCategories() {
    }

    public static boolean isNumericOrUnknown(BuiltinType t) {
        return t == null || t == BuiltinType.INTEGER || t == BuiltinType.REAL;
    }

    /** Boolean or Requirement: both are a Boolean4 in Modelica. */
    public static boolean isBoolean(BuiltinType t) {
        return t == BuiltinType.BOOLEAN || t == BuiltinType.REQUIREMENT;
    }

    public static boolean isBooleanOrUnknown(BuiltinType t) {
        return t == null || isBoolean(t);
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
