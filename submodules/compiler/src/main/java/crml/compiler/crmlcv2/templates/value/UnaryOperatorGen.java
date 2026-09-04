package crml.compiler.crmlcv2.templates.value;

import static crml.compiler.crmlcv2.templates.value.ValueGenUtil.call;
import static crml.compiler.crmlcv2.templates.value.ValueGenUtil.incompatibleTypes;
import static crml.compiler.crmlcv2.templates.value.ValueGenUtil.isBooleanOrUnknown;
import static crml.compiler.crmlcv2.templates.value.ValueGenUtil.isNumericOrUnknown;
import static crml.compiler.crmlcv2.templates.value.ValueGenUtil.isPeriodOrUnknown;
import static crml.compiler.crmlcv2.templates.value.ValueGenUtil.parenthesize;
import static crml.compiler.crmlcv2.templates.value.ValueGenUtil.prefix;
import static crml.compiler.crmlcv2.templates.value.ValueGenUtil.unsupported;

import crml.compiler.crmlcv2.scopes.ModelScope;
import crml.compiler.crmlcv2.scopes.Scope;
import crml.compiler.crmlcv2.templates.ValueGen;
import crml.compiler.crmlcv2.util.TypeResolver;
import crml.model.language.BuiltinType;
import crml.model.language.BuiltinUnaryOperatorKind;
import crml.model.language.UnaryOperator;

public class UnaryOperatorGen {

    public static Scope generate(ModelScope host, UnaryOperator op){
        Scope operand = ValueGen.generate(host, op.getValue());
        BuiltinType t = TypeResolver.resolveBuiltin(op.getValue().getReturnType());
        BuiltinUnaryOperatorKind opType = op.getOptype();

        switch (opType) {
            case SUBEXPRESSION:
                // Explicit parenthesisation around a sub-expression; passes the
                // operand's type through unchanged, so no type check applies.
                return parenthesize(operand);
            case ADD:
                return generateSign(opType, operand, t, "+");
            case SUB:
                return generateSign(opType, operand, t, "-");
            case NOT:
                return generateNot(opType, operand, t);
            case SIN:
                return generateMath(opType, operand, t, "Modelica.Math.sin");
            case ASIN:
                return generateMath(opType, operand, t, "Modelica.Math.asin");
            case COS:
                return generateMath(opType, operand, t, "Modelica.Math.cos");
            case ACOS:
                return generateMath(opType, operand, t, "Modelica.Math.acos");
            case LOG:
                return generateMath(opType, operand, t, "Modelica.Math.log");
            case LOG10:
                return generateMath(opType, operand, t, "Modelica.Math.log10");
            case EXP_OP:
                return generateMath(opType, operand, t, "Modelica.Math.exp");
            case START:
                return generatePeriodEndpoint(opType, operand, t, "CRMLtoModelica.Functions.PStart");
            case END:
                return generatePeriodEndpoint(opType, operand, t, "CRMLtoModelica.Functions.PEnd");
            case CARD:
                // CRMLtoModelica.Blocks.CardClock is a block, not a function: it needs
                // to be instantiated as a component on the host model, which this
                // generator does not yet support (see Scope.reference()).
                throw unsupported(opType, "requires instantiating the "
                    + "CRMLtoModelica.Blocks.CardClock block, which this generator does not yet support");
            case TICK:
                throw unsupported(opType, "requires instantiating the "
                    + "CRMLtoModelica.Blocks.ClockTick block, which this generator does not yet support");
            default:
                throw new UnsupportedOperationException(
                    "Unary operator kind " + opType + " is not produced by the current AST builders");
        }
    }

    private static Scope generateSign(BuiltinUnaryOperatorKind opType, Scope operand, BuiltinType t, String symbol){
        if (isNumericOrUnknown(t)) {
            return prefix(symbol, operand);
        } else {
            throw incompatibleTypes(opType, t);
        }
    }

    private static Scope generateNot(BuiltinUnaryOperatorKind opType, Scope operand, BuiltinType t){
        if (isBooleanOrUnknown(t)) {
            return call("CRMLtoModelica.Functions.not4", operand);
        } else {
            throw incompatibleTypes(opType, t);
        }
    }

    private static Scope generateMath(BuiltinUnaryOperatorKind opType, Scope operand, BuiltinType t, String function){
        if (isNumericOrUnknown(t)) {
            return call(function, operand);
        } else {
            throw incompatibleTypes(opType, t);
        }
    }

    private static Scope generatePeriodEndpoint(BuiltinUnaryOperatorKind opType, Scope operand, BuiltinType t, String function){
        if (isPeriodOrUnknown(t)) {
            return call(function, operand);
        } else {
            throw incompatibleTypes(opType, t);
        }
    }
}