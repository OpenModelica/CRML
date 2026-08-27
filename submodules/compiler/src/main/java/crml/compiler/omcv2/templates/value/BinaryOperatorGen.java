package crml.compiler.omcv2.templates.value;

import crml.compiler.omcv2.scopes.ModelScope;
import crml.compiler.omcv2.scopes.Scope;
import crml.compiler.omcv2.templates.ValueGen;
import crml.compiler.omcv2.util.TypeResolver;
import crml.model.language.BinaryOperator;
import crml.model.language.BuiltinBinaryOperatorKind;
import crml.model.language.BuiltinType;

import static crml.compiler.omcv2.templates.value.ValueGenUtil.call;
import static crml.compiler.omcv2.templates.value.ValueGenUtil.comparisonToBoolean4;
import static crml.compiler.omcv2.templates.value.ValueGenUtil.incompatibleTypes;
import static crml.compiler.omcv2.templates.value.ValueGenUtil.infix;
import static crml.compiler.omcv2.templates.value.ValueGenUtil.involvesClockOrPeriod;
import static crml.compiler.omcv2.templates.value.ValueGenUtil.isBooleanOrUnknown;
import static crml.compiler.omcv2.templates.value.ValueGenUtil.isNumericOrUnknown;
import static crml.compiler.omcv2.templates.value.ValueGenUtil.isStringCompatible;
import static crml.compiler.omcv2.templates.value.ValueGenUtil.unsupported;

public class BinaryOperatorGen {

    public static Scope generate(ModelScope host, BinaryOperator op){
        Scope lhs = ValueGen.generate(host, op.getLhs());
        Scope rhs = ValueGen.generate(host, op.getRhs());
        BuiltinType lt = TypeResolver.resolveBuiltin(op.getLhs().getReturnType());
        BuiltinType rt = TypeResolver.resolveBuiltin(op.getRhs().getReturnType());
        BuiltinBinaryOperatorKind opType = op.getOptype();

        switch (opType) {
            case BuiltinBinaryOperatorKind.ADD:
                return generateAdd(opType, lhs, rhs, lt, rt);
            case BuiltinBinaryOperatorKind.SUB:
                return generateSub(opType, lhs, rhs, lt, rt);
            case BuiltinBinaryOperatorKind.MUL:
                return generateMul(opType, lhs, rhs, lt, rt);
            case BuiltinBinaryOperatorKind.DIV:
                return generateDiv(opType, lhs, rhs, lt, rt);
            case BuiltinBinaryOperatorKind.POW:
                return generatePow(opType, lhs, rhs, lt, rt);
            case BuiltinBinaryOperatorKind.MOD:
                // Modelica's mod() function, not the legacy OperatorMapping "mod" entry
                // (which pointed at Modelica.Math.exp by mistake).
                return generateMod(opType, lhs, rhs, lt, rt);
            case BuiltinBinaryOperatorKind.AND:
                return generateAnd(opType, lhs, rhs, lt, rt);
            case BuiltinBinaryOperatorKind.OR:
                return generateOr(opType, lhs, rhs, lt, rt);
            case BuiltinBinaryOperatorKind.LT:
                return generateComparison(opType, lhs, rhs, lt, rt, "<", true, null, true);
            case BuiltinBinaryOperatorKind.LE:
                return generateComparison(opType, lhs, rhs, lt, rt, "<=", true, "CRMLtoModelica.Functions.lEV", true);
            case BuiltinBinaryOperatorKind.GT:
                return generateComparison(opType, lhs, rhs, lt, rt, ">", true, null, false);
            case BuiltinBinaryOperatorKind.GE:
                return generateComparison(opType, lhs, rhs, lt, rt, ">=", true, "CRMLtoModelica.Functions.gEV", false);
            case BuiltinBinaryOperatorKind.EQ:
                return generateComparison(opType, lhs, rhs, lt, rt, "==", false, null, true);
            case BuiltinBinaryOperatorKind.NEQ:
                return generateComparison(opType, lhs, rhs, lt, rt, "<>", false, null, true);
            case BuiltinBinaryOperatorKind.AT:
                // No implementation exists anywhere to model this on: the legacy visitor never
                // handled it and no CRMLtoModelica library function backs it (crml.g4 notes
                // "Moved to binary operator. Why was it separate?" with no follow-up).
                throw new UnsupportedOperationException(
                    "AT operator is not implemented: its Modelica semantics are undefined");
            default:
                throw new UnsupportedOperationException(
                    "Binary operator kind " + opType + " is not produced by the current AST builders");
        }
    }

    // --- arithmetic -----------------------------------------------------

    private static Scope generateAdd(BuiltinBinaryOperatorKind opType, Scope lhs, Scope rhs, BuiltinType lt, BuiltinType rt){
        if (isNumericOrUnknown(lt) && isNumericOrUnknown(rt)) {
            return infix(lhs, rhs, "+");
        } else if ((lt == BuiltinType.BOOLEAN || rt == BuiltinType.BOOLEAN) && isBooleanOrUnknown(lt) && isBooleanOrUnknown(rt)) {
            return call("CRMLtoModelica.Functions.add4", lhs, rhs);
        } else if ((lt == BuiltinType.STRING || rt == BuiltinType.STRING) && isStringCompatible(lt) && isStringCompatible(rt)) {
            return infix(lhs, rhs, "+");
        } else if (involvesClockOrPeriod(lt) || involvesClockOrPeriod(rt)) {
            throw unsupported(opType, "Clock/Period addition requires instantiating the "
                + "CRMLtoModelica.Blocks.ClockAdd block, which this generator does not yet support");
        } else {
            throw incompatibleTypes(opType, lt, rt);
        }
    }

    private static Scope generateSub(BuiltinBinaryOperatorKind opType, Scope lhs, Scope rhs, BuiltinType lt, BuiltinType rt){
        if (isNumericOrUnknown(lt) && isNumericOrUnknown(rt)) {
            return infix(lhs, rhs, "-");
        } else if ((lt == BuiltinType.BOOLEAN || rt == BuiltinType.BOOLEAN) && isBooleanOrUnknown(lt) && isBooleanOrUnknown(rt)) {
            throw unsupported(opType, "Boolean4 subtraction has no implementation in the CRMLtoModelica "
                + "runtime library (there is no diff4 function, despite the legacy OperatorMapping table)");
        } else {
            throw incompatibleTypes(opType, lt, rt);
        }
    }

    private static Scope generateMul(BuiltinBinaryOperatorKind opType, Scope lhs, Scope rhs, BuiltinType lt, BuiltinType rt){
        if (isNumericOrUnknown(lt) && isNumericOrUnknown(rt)) {
            return infix(lhs, rhs, "*");
        } else if ((lt == BuiltinType.BOOLEAN || rt == BuiltinType.BOOLEAN) && isBooleanOrUnknown(lt) && isBooleanOrUnknown(rt)) {
            return call("CRMLtoModelica.Functions.mul4", lhs, rhs);
        } else {
            throw incompatibleTypes(opType, lt, rt);
        }
    }

    private static Scope generateDiv(BuiltinBinaryOperatorKind opType, Scope lhs, Scope rhs, BuiltinType lt, BuiltinType rt){
        if (isNumericOrUnknown(lt) && isNumericOrUnknown(rt)) {
            return infix(lhs, rhs, "/");
        } else {
            throw incompatibleTypes(opType, lt, rt);
        }
    }

    private static Scope generatePow(BuiltinBinaryOperatorKind opType, Scope lhs, Scope rhs, BuiltinType lt, BuiltinType rt){
        if (isNumericOrUnknown(lt) && isNumericOrUnknown(rt)) {
            return infix(lhs, rhs, "^");
        } else {
            throw incompatibleTypes(opType, lt, rt);
        }
    }

    private static Scope generateMod(BuiltinBinaryOperatorKind opType, Scope lhs, Scope rhs, BuiltinType lt, BuiltinType rt){
        if (isNumericOrUnknown(lt) && isNumericOrUnknown(rt)) {
            return call("mod", lhs, rhs);
        } else {
            throw incompatibleTypes(opType, lt, rt);
        }
    }

    // --- logic ------------------------------------------------------------

    private static Scope generateAnd(BuiltinBinaryOperatorKind opType, Scope lhs, Scope rhs, BuiltinType lt, BuiltinType rt){
        if (isBooleanOrUnknown(lt) && isBooleanOrUnknown(rt)) {
            return call("CRMLtoModelica.Functions.and4", lhs, rhs);
        } else {
            throw incompatibleTypes(opType, lt, rt);
        }
    }

    private static Scope generateOr(BuiltinBinaryOperatorKind opType, Scope lhs, Scope rhs, BuiltinType lt, BuiltinType rt){
        if (isBooleanOrUnknown(lt) && isBooleanOrUnknown(rt)) {
            return call("CRMLtoModelica.Functions.or4", lhs, rhs);
        } else {
            throw incompatibleTypes(opType, lt, rt);
        }
    }

    // --- comparisons --------------------------------------------------------
    // Numeric/Boolean/Event/Period combinations follow
    // submodules/language/src/main/resources/res/crml/language/typeinference.csv.
    // eventFunction/periodSupported are null/false where the CSV allows the
    // combination but no backing CRMLtoModelica implementation exists.

    private static Scope generateComparison(BuiltinBinaryOperatorKind opType, Scope lhs, Scope rhs, BuiltinType lt, BuiltinType rt,
            String symbol, boolean eventSupported, String eventFunction, boolean periodSupported){
        if (isNumericOrUnknown(lt) && isNumericOrUnknown(rt)) {
            return comparisonToBoolean4(lhs, rhs, symbol);
        } else if (eventSupported && lt == BuiltinType.EVENT && rt == BuiltinType.EVENT) {
            if (eventFunction == null) {
                throw unsupported(opType, "no CRMLtoModelica.Functions implementation exists for a "
                    + "strict Event " + symbol + " comparison (only lEV/gEV exist)");
            } else {
                return call(eventFunction, lhs, rhs);
            }
        } else if ((lt == BuiltinType.BOOLEAN || rt == BuiltinType.BOOLEAN) && isBooleanOrUnknown(lt) && isBooleanOrUnknown(rt)) {
            throw unsupported(opType, "Boolean4 " + symbol + " comparison has no implementation in the "
                + "CRMLtoModelica runtime library (the Logical4 package referenced by the legacy "
                + "OperatorMapping table does not exist)");
        } else if (periodSupported && ((lt == BuiltinType.REAL && rt == BuiltinType.PERIOD) || (lt == BuiltinType.PERIOD && rt == BuiltinType.REAL))) {
            throw unsupported(opType, "Real/Period " + symbol + " comparison requires block instantiation, "
                + "which this generator does not yet support");
        } else {
            throw incompatibleTypes(opType, lt, rt);
        }
    }
}