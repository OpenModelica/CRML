package crml.compiler.crmlcv2.templates.value;

import static crml.compiler.crmlcv2.templates.value.TypeCategories.involvesClockOrPeriod;
import static crml.compiler.crmlcv2.templates.value.TypeCategories.isBooleanOrUnknown;
import static crml.compiler.crmlcv2.templates.value.TypeCategories.isClockOrUnknown;
import static crml.compiler.crmlcv2.templates.value.TypeCategories.isNumericOrUnknown;
import static crml.compiler.crmlcv2.templates.value.TypeCategories.isStringCompatible;

import java.util.LinkedHashMap;
import java.util.Map;

import crml.compiler.crmlcv2.Diagnostics;
import crml.compiler.crmlcv2.TransformationContext;
import crml.compiler.crmlcv2.UnsupportedConstruct;
import crml.compiler.crmlcv2.templates.Transformer;
import crml.compiler.crmlcv2.util.TypeResolver;
import crml.model.language.BinaryOperator;
import crml.model.language.BuiltinBinaryOperatorKind;
import crml.model.language.BuiltinType;
import crml.model.modelica.BinaryOperatorKind;
import crml.model.modelica.Expression;
import crml.modelica.build.Modelica;

/**
 * CRML binary operators to Modelica expressions.
 *
 * <p>The type-dispatch logic is the string generator's, ported unchanged: it was
 * checked row by row against
 * {@code submodules/language/src/main/resources/res/crml/language/typeinference.csv}
 * and is correct. {@code periodSupported} is true for LT/LE/EQ/NEQ and false for
 * GT/GE, matching exactly the rows where REAL|PERIOD appears; {@code eventSupported}
 * is true for LT/LE/GT/GE and false for EQ/NEQ, matching the EVENT|EVENT rows.
 * Only the construction calls changed: text concatenation became object-model
 * nodes, and the cases with no target became diagnostics instead of exceptions.
 */
public final class BinaryOperatorTransformer implements Modelica {

    private final Transformer transformer;

    public BinaryOperatorTransformer(Transformer transformer) {
        this.transformer = transformer;
    }

    public Expression transform(TransformationContext ctx, BinaryOperator op) {
        Expression lhs = transformer.values().transform(ctx, op.getLhs());
        Expression rhs = transformer.values().transform(ctx, op.getRhs());
        BuiltinType lt = TypeResolver.resolveBuiltin(op.getLhs().getReturnType());
        BuiltinType rt = TypeResolver.resolveBuiltin(op.getRhs().getReturnType());
        BuiltinBinaryOperatorKind opType = op.getOptype();

        switch (opType) {
            case ADD:
                return transformAdd(op, opType, lhs, rhs, lt, rt);
            case SUB:
                return transformSub(op, opType, lhs, rhs, lt, rt);
            case MUL:
                return transformMul(op, opType, lhs, rhs, lt, rt);
            case DIV:
                return transformDiv(op, opType, lhs, rhs, lt, rt);
            case POW:
                // Modelica's ^ yields Real where typeinference.csv says
                // POW,INTEGER,2,INTEGER,INTEGER. Harmless unless downstream code
                // depends on the inferred type; recorded as follow-on work.
                return transformPow(op, opType, lhs, rhs, lt, rt);
            case MOD:
                // Modelica's mod() function, not the legacy OperatorMapping "mod"
                // entry (which pointed at Modelica.Math.exp by mistake).
                return transformMod(op, opType, lhs, rhs, lt, rt);
            case AND:
                return transformAnd(op, opType, lhs, rhs, lt, rt);
            case OR:
                return transformOr(op, opType, lhs, rhs, lt, rt);
            case LT:
                return transformComparison(op, opType, lhs, rhs, lt, rt, BinaryOperatorKind.LT, "<", true, null, true);
            case LE:
                return transformComparison(op, opType, lhs, rhs, lt, rt, BinaryOperatorKind.LE, "<=", true,
                    "CRMLtoModelica.Functions.lEV", true);
            case GT:
                return transformComparison(op, opType, lhs, rhs, lt, rt, BinaryOperatorKind.GT, ">", true, null, false);
            case GE:
                return transformComparison(op, opType, lhs, rhs, lt, rt, BinaryOperatorKind.GE, ">=", true,
                    "CRMLtoModelica.Functions.gEV", false);
            case EQ:
                return transformComparison(op, opType, lhs, rhs, lt, rt, BinaryOperatorKind.EQ, "==", false, null, true);
            case NEQ:
                return transformComparison(op, opType, lhs, rhs, lt, rt, BinaryOperatorKind.NEQ, "<>", false, null, true);
            case FILTER:
                // EventFilter(r1: CRMLClock, r2: Boolean4) -> out: CRMLClock.
                return transformFilter(ctx, op, opType, lhs, rhs, lt, rt);
            case AT:
                // typeinference.csv has "AT,*,2,*,EVENT" and crml.g4 has the rule,
                // and BooleanAtEvent.crml exercises it with a verification harness -
                // so the semantics are defined; CRMLtoModelica.mo simply has no
                // implementation to call.
                throw new UnsupportedConstruct(Diagnostics.unsupported(opType,
                    "CRMLtoModelica.mo has no implementation for 'at', although CRML defines it "
                    + "and the test corpus exercises it", op));
            default:
                throw new UnsupportedConstruct(Diagnostics.unsupported(opType,
                    "this operator kind is not produced by the current AST builders", op));
        }
    }

    // --- block-backed operators ---------------------------------------------

    /**
     * Filtering a clock by a condition. EventFilter is a block, so it is
     * instantiated on the target class and the expression becomes a reference to
     * its output port.
     */
    private Expression transformFilter(TransformationContext ctx, BinaryOperator op,
            BuiltinBinaryOperatorKind opType, Expression lhs, Expression rhs, BuiltinType lt, BuiltinType rt) {
        // The looked-through left type: EventFilter takes a CRMLClock, and
        // typeinference.csv's only FILTER row is CLOCK|BOOLEAN.
        lt = TypeResolver.inferBuiltin(op.getLhs());
        if (!isClockOrUnknown(lt) || !isBooleanOrUnknown(rt)) {
            throw new UnsupportedConstruct(Diagnostics.incompatibleTypes(opType, lt, rt, op));
        }
        Map<String, Expression> inputs = new LinkedHashMap<String, Expression>();
        inputs.put("r1", lhs);
        inputs.put("r2", rhs);
        return transformer.blocks().instantiate(ctx, libraryRef("CRMLtoModelica.Blocks.EventFilter"), "filter",
            inputs, "out", op);
    }

    // --- arithmetic ---------------------------------------------------------

    private Expression transformAdd(BinaryOperator op, BuiltinBinaryOperatorKind opType,
            Expression lhs, Expression rhs, BuiltinType lt, BuiltinType rt) {
        if (isNumericOrUnknown(lt) && isNumericOrUnknown(rt)) {
            return binary(BinaryOperatorKind.ADD, lhs, rhs);
        } else if ((lt == BuiltinType.BOOLEAN || rt == BuiltinType.BOOLEAN) && isBooleanOrUnknown(lt) && isBooleanOrUnknown(rt)) {
            return call(libraryRef("CRMLtoModelica.Functions.add4"), lhs, rhs);
        } else if ((lt == BuiltinType.STRING || rt == BuiltinType.STRING) && isStringCompatible(lt) && isStringCompatible(rt)) {
            // Modelica's + concatenates String.
            return binary(BinaryOperatorKind.ADD, lhs, rhs);
        } else if (involvesClockOrPeriod(lt) || involvesClockOrPeriod(rt)) {
            throw new UnsupportedConstruct(Diagnostics.unsupported(opType,
                "Clock/Period addition needs CRMLtoModelica.Blocks.ClockAdd, whose equation "
                + "section is empty; a Period left operand and the Clock+Integer tick delay "
                + "have no block at all", op));
        } else {
            throw new UnsupportedConstruct(Diagnostics.incompatibleTypes(opType, lt, rt, op));
        }
    }

    private Expression transformSub(BinaryOperator op, BuiltinBinaryOperatorKind opType,
            Expression lhs, Expression rhs, BuiltinType lt, BuiltinType rt) {
        if (isNumericOrUnknown(lt) && isNumericOrUnknown(rt)) {
            return binary(BinaryOperatorKind.SUB, lhs, rhs);
        } else if ((lt == BuiltinType.BOOLEAN || rt == BuiltinType.BOOLEAN) && isBooleanOrUnknown(lt) && isBooleanOrUnknown(rt)) {
            throw new UnsupportedConstruct(Diagnostics.unsupported(opType,
                "Boolean4 subtraction has no implementation in CRMLtoModelica.mo (there is no "
                + "diff4 function, despite the legacy OperatorMapping table)", op));
        } else {
            throw new UnsupportedConstruct(Diagnostics.incompatibleTypes(opType, lt, rt, op));
        }
    }

    private Expression transformMul(BinaryOperator op, BuiltinBinaryOperatorKind opType,
            Expression lhs, Expression rhs, BuiltinType lt, BuiltinType rt) {
        if (isNumericOrUnknown(lt) && isNumericOrUnknown(rt)) {
            return binary(BinaryOperatorKind.MUL, lhs, rhs);
        } else if ((lt == BuiltinType.BOOLEAN || rt == BuiltinType.BOOLEAN) && isBooleanOrUnknown(lt) && isBooleanOrUnknown(rt)) {
            return call(libraryRef("CRMLtoModelica.Functions.mul4"), lhs, rhs);
        } else {
            throw new UnsupportedConstruct(Diagnostics.incompatibleTypes(opType, lt, rt, op));
        }
    }

    private Expression transformDiv(BinaryOperator op, BuiltinBinaryOperatorKind opType,
            Expression lhs, Expression rhs, BuiltinType lt, BuiltinType rt) {
        if (isNumericOrUnknown(lt) && isNumericOrUnknown(rt)) {
            return binary(BinaryOperatorKind.DIV, lhs, rhs);
        } else {
            throw new UnsupportedConstruct(Diagnostics.incompatibleTypes(opType, lt, rt, op));
        }
    }

    private Expression transformPow(BinaryOperator op, BuiltinBinaryOperatorKind opType,
            Expression lhs, Expression rhs, BuiltinType lt, BuiltinType rt) {
        if (isNumericOrUnknown(lt) && isNumericOrUnknown(rt)) {
            return binary(BinaryOperatorKind.POW, lhs, rhs);
        } else {
            throw new UnsupportedConstruct(Diagnostics.incompatibleTypes(opType, lt, rt, op));
        }
    }

    private Expression transformMod(BinaryOperator op, BuiltinBinaryOperatorKind opType,
            Expression lhs, Expression rhs, BuiltinType lt, BuiltinType rt) {
        if (isNumericOrUnknown(lt) && isNumericOrUnknown(rt)) {
            return call(builtinRef("mod"), lhs, rhs);
        } else {
            throw new UnsupportedConstruct(Diagnostics.incompatibleTypes(opType, lt, rt, op));
        }
    }

    // --- logic --------------------------------------------------------------

    private Expression transformAnd(BinaryOperator op, BuiltinBinaryOperatorKind opType,
            Expression lhs, Expression rhs, BuiltinType lt, BuiltinType rt) {
        if (isBooleanOrUnknown(lt) && isBooleanOrUnknown(rt)) {
            return call(libraryRef("CRMLtoModelica.Functions.and4"), lhs, rhs);
        } else {
            throw new UnsupportedConstruct(Diagnostics.incompatibleTypes(opType, lt, rt, op));
        }
    }

    private Expression transformOr(BinaryOperator op, BuiltinBinaryOperatorKind opType,
            Expression lhs, Expression rhs, BuiltinType lt, BuiltinType rt) {
        if (isBooleanOrUnknown(lt) && isBooleanOrUnknown(rt)) {
            return call(libraryRef("CRMLtoModelica.Functions.or4"), lhs, rhs);
        } else {
            throw new UnsupportedConstruct(Diagnostics.incompatibleTypes(opType, lt, rt, op));
        }
    }

    // --- comparisons --------------------------------------------------------

    private Expression transformComparison(BinaryOperator op, BuiltinBinaryOperatorKind opType,
            Expression lhs, Expression rhs, BuiltinType lt, BuiltinType rt,
            BinaryOperatorKind modelicaOp, String symbol,
            boolean eventSupported, String eventFunction, boolean periodSupported) {
        if (isNumericOrUnknown(lt) && isNumericOrUnknown(rt)) {
            return call(libraryRef("CRMLtoModelica.Functions.cvBooleanToBoolean4"), binary(modelicaOp, lhs, rhs));
        } else if (eventSupported && lt == BuiltinType.EVENT && rt == BuiltinType.EVENT) {
            if (eventFunction == null) {
                throw new UnsupportedConstruct(Diagnostics.unsupported(opType,
                    "no CRMLtoModelica.Functions implementation exists for a strict Event "
                    + symbol + " comparison (only lEV/gEV exist)", op));
            }
            return call(libraryRef(eventFunction), lhs, rhs);
        } else if ((lt == BuiltinType.BOOLEAN || rt == BuiltinType.BOOLEAN) && isBooleanOrUnknown(lt) && isBooleanOrUnknown(rt)) {
            throw new UnsupportedConstruct(Diagnostics.unsupported(opType,
                "Boolean4 " + symbol + " comparison has no implementation in CRMLtoModelica.mo "
                + "(the Logical4 package referenced by the legacy OperatorMapping table does not exist)", op));
        } else if (periodSupported
                && ((lt == BuiltinType.REAL && rt == BuiltinType.PERIOD) || (lt == BuiltinType.PERIOD && rt == BuiltinType.REAL))) {
            throw new UnsupportedConstruct(Diagnostics.unsupported(opType,
                "Real/Period " + symbol + " comparison has no implementation in CRMLtoModelica.mo "
                + "(realPeriodeq/realPeriodleq do not exist)", op));
        } else {
            throw new UnsupportedConstruct(Diagnostics.incompatibleTypes(opType, lt, rt, op));
        }
    }
}
