package crml.compiler.crmlcv2.templates.value;

import crml.compiler.crmlcv2.scopes.FunctionScope;
import crml.compiler.crmlcv2.scopes.Scope;
import crml.model.language.BuiltinBinaryOperatorKind;
import crml.model.language.BuiltinType;
import crml.model.language.BuiltinUnaryOperatorKind;

public class ValueGenUtil {

    // --- Modelica text helpers -----------------------------------------------
    // These embed operand Scopes inline into a larger expression, so they need
    // each operand's reference() (its short, standalone-expression form), not
    // its toModelica() (which may include helper declarations/equations that
    // only make sense hoisted out to the host ModelScope).

    public static FunctionScope infix(Scope lhs, Scope rhs, String symbol){
        return new FunctionScope("(" + reference(lhs) + " " + symbol + " " + reference(rhs) + ")");
    }

    public static FunctionScope call(String function, Scope... args){
        StringBuilder builder = new StringBuilder(function).append("(");
        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(reference(args[i]));
        }
        return new FunctionScope(builder.append(")").toString());
    }

    public static FunctionScope comparisonToBoolean4(Scope lhs, Scope rhs, String symbol){
        return new FunctionScope("CRMLtoModelica.Functions.cvBooleanToBoolean4(" + reference(lhs) + " " + symbol + " " + reference(rhs) + ")");
    }

    public static FunctionScope prefix(String symbol, Scope operand){
        return new FunctionScope(symbol + reference(operand));
    }

    public static FunctionScope parenthesize(Scope operand){
        return new FunctionScope("(" + reference(operand) + ")");
    }

    private static String reference(Scope scope){
        String ref = scope.reference();
        if (ref == null) {
            throw new IllegalStateException(
                scope.getClass().getSimpleName() + " has no short reference and cannot be embedded inline");
        }
        return ref;
    }

    // --- operand type categories ---------------------------------------------
    // "OrUnknown" predicates let generation proceed when type inference hasn't
    // resolved an operand's type (getReturnType() only yields a type today for
    // Constant values); a definitively-known incompatible type still fails fast.

    public static boolean isNumericOrUnknown(BuiltinType t){
        return t == null || t == BuiltinType.INTEGER || t == BuiltinType.REAL;
    }

    public static boolean isBooleanOrUnknown(BuiltinType t){
        return t == null || t == BuiltinType.BOOLEAN;
    }

    public static boolean isStringCompatible(BuiltinType t){
        return t == null || t == BuiltinType.STRING || t == BuiltinType.INTEGER || t == BuiltinType.REAL;
    }

    public static boolean involvesClockOrPeriod(BuiltinType t){
        return t == BuiltinType.CLOCK || t == BuiltinType.PERIOD;
    }

    public static boolean isPeriodOrUnknown(BuiltinType t){
        return t == null || t == BuiltinType.PERIOD;
    }

    public static RuntimeException incompatibleTypes(BuiltinBinaryOperatorKind opType, BuiltinType lt, BuiltinType rt){
        return new RuntimeException("Operator " + opType + " is not defined for operand types (" + lt + ", " + rt + ")");
    }

    public static RuntimeException unsupported(BuiltinBinaryOperatorKind opType, String reason){
        return new RuntimeException("Cannot generate Modelica for operator " + opType + ": " + reason);
    }

    public static RuntimeException incompatibleTypes(BuiltinUnaryOperatorKind opType, BuiltinType t){
        return new RuntimeException("Operator " + opType + " is not defined for operand type " + t);
    }

    public static RuntimeException unsupported(BuiltinUnaryOperatorKind opType, String reason){
        return new RuntimeException("Cannot generate Modelica for operator " + opType + ": " + reason);
    }
}