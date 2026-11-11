package crml.compiler.crmlcv2.templates.value;

import static crml.compiler.crmlcv2.templates.value.TypeCategories.involvesClockOrPeriod;
import static crml.compiler.crmlcv2.templates.value.TypeCategories.isBooleanOrUnknown;
import static crml.compiler.crmlcv2.templates.value.TypeCategories.isClockOrUnknown;
import static crml.compiler.crmlcv2.templates.value.TypeCategories.isNumericOrUnknown;
import static crml.compiler.crmlcv2.templates.value.TypeCategories.isStringCompatible;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import crml.compiler.crmlcv2.Diagnostics;
import crml.compiler.crmlcv2.Lifter;
import crml.compiler.crmlcv2.SetHandle;
import crml.compiler.crmlcv2.TransformationContext;
import crml.compiler.crmlcv2.templates.Transformer;
import crml.compiler.crmlcv2.util.CRMLCompilerException;
import crml.compiler.crmlcv2.util.TypeResolver;
import crml.model.language.BinaryOperator;
import crml.model.language.BuiltinBinaryOperatorKind;
import crml.model.language.BuiltinType;
import crml.model.modelica.BinaryOperatorKind;
import crml.model.modelica.ClassDefinition;
import crml.model.modelica.ComponentDeclaration;
import crml.model.modelica.ComponentReference;
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
        if (op.getLhs() == null) {
            // "and (...)": no left operand. Not isAggregator(), which also asks
            // the operand whether it is a collection, and no value says yes.
            return aggregate(ctx, op);
        }
        Expression lhs = transformer.values().transform(ctx, op.getLhs());
        Expression rhs = transformer.values().transform(ctx, op.getRhs());
        BuiltinType lt = TypeResolver.resolveBuiltin(op.getLhs().getReturnType());
        BuiltinType rt = TypeResolver.resolveBuiltin(op.getRhs().getReturnType());
        return apply(ctx, op, lhs, rhs, lt, rt);
    }

    /**
     * An operator with at least one {@code x.element} operand: applied once for
     * each slot of the {@link Lifter}'s region. A block-backed operator (filter)
     * gets an instance per slot, through the lifted mode of the context.
     */
    public SetHandle transformLifted(Lifter lifter, BinaryOperator op) {
        Expression lhs = lifter.operand(op.getLhs());
        Expression rhs = lifter.operand(op.getRhs());
        BuiltinType lt = lifter.operandBuiltin(op.getLhs());
        BuiltinType rt = lifter.operandBuiltin(op.getRhs());
        Expression value = apply(lifter.body(), op, lhs, rhs, lt, rt);
        BuiltinType result = TypeResolver.resultBuiltin(op, lt, rt);
        return lifter.close(value, transformer.types().builtinReference(result), result, op);
    }

    // --- aggregators ---------------------------------------------------------

    /**
     * An aggregator: an operator with no left operand, folded over the elements
     * of a collection. {@code and (S)} becomes
     *
     * <pre>
     * Boolean4 and_acc1[n + 1];
     * and_acc1[1] = undefined;
     * for i1 in 1:n loop
     *     and_acc1[i1 + 1] = and4(and_acc1[i1], S[i1]);
     * end for;
     * </pre>
     *
     * and the result is {@code and_acc1[n + 1]}. {@code undefined} is the
     * identity of and4 and or4, so it starts the fold, stands in for every slot
     * of the collection that holds no element, and is the result for an empty
     * collection. Never lifted itself: an {@code x.element} operand of an
     * aggregator is the collection it folds.
     */
    private Expression aggregate(TransformationContext ctx, BinaryOperator op) {
        BuiltinBinaryOperatorKind opType = op.getOptype();
        String prefix;
        String function;
        switch (opType) {
            case AND:
                prefix = "and";
                function = "CRMLtoModelica.Functions.and4";
                break;
            case OR:
                prefix = "or";
                function = "CRMLtoModelica.Functions.or4";
                break;
            case ADD:
            case SUB:
            case MUL:
                throw CRMLCompilerException.unsupported(Diagnostics.notYetImplemented(
                    "BinaryOperator." + opType + ".aggregate",
                    "aggregating a set with " + opType + " is not implemented yet", op));
            default:
                // Not an aggregator in the grammar: an operand is missing.
                transformer.values().transform(ctx, op.getLhs());
                throw CRMLCompilerException.unreachable();
        }

        SetHandle set = transformer.scatteredValues().collect(ctx, op.getRhs());
        BuiltinType elementType = set.elementBuiltin();
        if (elementType != null && elementType != BuiltinType.BOOLEAN && elementType != BuiltinType.REQUIREMENT) {
            throw CRMLCompilerException.unsupported(Diagnostics.incompatibleTypes(opType, null, elementType, op));
        }

        String undefined = "CRMLtoModelica.Types.Boolean4.undefined";
        ComponentDeclaration accumulator = component(
            this.<ClassDefinition>libraryRef(TypeResolver.resolve(BuiltinType.BOOLEAN)),
            ctx.allocateName(prefix + "_acc"));
        accumulator.getArrayDimensions().add(dimension(binary(BinaryOperatorKind.ADD, set.size(), integer(1))));
        ComponentReference acc = ctx.declare(accumulator, op);
        ctx.equate(eq(subscripted(acc, integer(1)), ref(undefined)), op);

        String i = ctx.allocateName("i");
        Expression element = set.element(ref(i));
        Expression active = set.inactiveIsUndefined() ? null : set.active(ref(i));
        if (active != null) {
            element = ifExpr(active, element, ref(undefined));
        }
        ctx.equate(forEq(i, integer(1), set.size(), Collections.singletonList(
            eq(subscripted(acc, binary(BinaryOperatorKind.ADD, ref(i), integer(1))),
                call(this.<ClassDefinition>libraryRef(function), subscripted(acc, ref(i)), element)))), op);

        return subscripted(acc, binary(BinaryOperatorKind.ADD, set.size(), integer(1)));
    }

    /**
     * The operator applied to operands that are already translated.
     *
     * @param lt the type of the left operand, or null if unknown
     * @param rt the type of the right operand, or null if unknown
     */
    private Expression apply(TransformationContext ctx, BinaryOperator op, Expression lhs, Expression rhs,
            BuiltinType lt, BuiltinType rt) {
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
                throw CRMLCompilerException.unsupported(Diagnostics.unsupported(opType,
                    "CRMLtoModelica.mo has no implementation for 'at', although CRML defines it "
                    + "and the test corpus exercises it", op));
            default:
                throw CRMLCompilerException.unsupported(Diagnostics.unsupported(opType,
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
            throw CRMLCompilerException.unsupported(Diagnostics.incompatibleTypes(opType, lt, rt, op));
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
            throw CRMLCompilerException.unsupported(Diagnostics.unsupported(opType,
                "Clock/Period addition needs CRMLtoModelica.Blocks.ClockAdd, whose equation "
                + "section is empty; a Period left operand and the Clock+Integer tick delay "
                + "have no block at all", op));
        } else {
            throw CRMLCompilerException.unsupported(Diagnostics.incompatibleTypes(opType, lt, rt, op));
        }
    }

    private Expression transformSub(BinaryOperator op, BuiltinBinaryOperatorKind opType,
            Expression lhs, Expression rhs, BuiltinType lt, BuiltinType rt) {
        if (isNumericOrUnknown(lt) && isNumericOrUnknown(rt)) {
            return binary(BinaryOperatorKind.SUB, lhs, rhs);
        } else if ((lt == BuiltinType.BOOLEAN || rt == BuiltinType.BOOLEAN) && isBooleanOrUnknown(lt) && isBooleanOrUnknown(rt)) {
            throw CRMLCompilerException.unsupported(Diagnostics.unsupported(opType,
                "Boolean4 subtraction has no implementation in CRMLtoModelica.mo (there is no "
                + "diff4 function, despite the legacy OperatorMapping table)", op));
        } else {
            throw CRMLCompilerException.unsupported(Diagnostics.incompatibleTypes(opType, lt, rt, op));
        }
    }

    private Expression transformMul(BinaryOperator op, BuiltinBinaryOperatorKind opType,
            Expression lhs, Expression rhs, BuiltinType lt, BuiltinType rt) {
        // Looked through: a Boolean variable or operator call carries no return
        // type, and two unknowns would otherwise become Modelica's * on Boolean4.
        lt = TypeResolver.inferBuiltin(op.getLhs());
        rt = TypeResolver.inferBuiltin(op.getRhs());
        if (isNumericOrUnknown(lt) && isNumericOrUnknown(rt)) {
            return binary(BinaryOperatorKind.MUL, lhs, rhs);
        } else if ((lt == BuiltinType.BOOLEAN || rt == BuiltinType.BOOLEAN) && isBooleanOrUnknown(lt) && isBooleanOrUnknown(rt)) {
            return call(libraryRef("CRMLtoModelica.Functions.mul4"), lhs, rhs);
        } else {
            throw CRMLCompilerException.unsupported(Diagnostics.incompatibleTypes(opType, lt, rt, op));
        }
    }

    private Expression transformDiv(BinaryOperator op, BuiltinBinaryOperatorKind opType,
            Expression lhs, Expression rhs, BuiltinType lt, BuiltinType rt) {
        if (isNumericOrUnknown(lt) && isNumericOrUnknown(rt)) {
            return binary(BinaryOperatorKind.DIV, lhs, rhs);
        } else {
            throw CRMLCompilerException.unsupported(Diagnostics.incompatibleTypes(opType, lt, rt, op));
        }
    }

    private Expression transformPow(BinaryOperator op, BuiltinBinaryOperatorKind opType,
            Expression lhs, Expression rhs, BuiltinType lt, BuiltinType rt) {
        if (isNumericOrUnknown(lt) && isNumericOrUnknown(rt)) {
            return binary(BinaryOperatorKind.POW, lhs, rhs);
        } else {
            throw CRMLCompilerException.unsupported(Diagnostics.incompatibleTypes(opType, lt, rt, op));
        }
    }

    private Expression transformMod(BinaryOperator op, BuiltinBinaryOperatorKind opType,
            Expression lhs, Expression rhs, BuiltinType lt, BuiltinType rt) {
        if (isNumericOrUnknown(lt) && isNumericOrUnknown(rt)) {
            return call(builtinRef("mod"), lhs, rhs);
        } else {
            throw CRMLCompilerException.unsupported(Diagnostics.incompatibleTypes(opType, lt, rt, op));
        }
    }

    // --- logic --------------------------------------------------------------

    private Expression transformAnd(BinaryOperator op, BuiltinBinaryOperatorKind opType,
            Expression lhs, Expression rhs, BuiltinType lt, BuiltinType rt) {
        if (isBooleanOrUnknown(lt) && isBooleanOrUnknown(rt)) {
            return call(libraryRef("CRMLtoModelica.Functions.and4"), lhs, rhs);
        } else {
            throw CRMLCompilerException.unsupported(Diagnostics.incompatibleTypes(opType, lt, rt, op));
        }
    }

    private Expression transformOr(BinaryOperator op, BuiltinBinaryOperatorKind opType,
            Expression lhs, Expression rhs, BuiltinType lt, BuiltinType rt) {
        if (isBooleanOrUnknown(lt) && isBooleanOrUnknown(rt)) {
            return call(libraryRef("CRMLtoModelica.Functions.or4"), lhs, rhs);
        } else {
            throw CRMLCompilerException.unsupported(Diagnostics.incompatibleTypes(opType, lt, rt, op));
        }
    }

    // --- comparisons --------------------------------------------------------

    private Expression transformComparison(BinaryOperator op, BuiltinBinaryOperatorKind opType,
            Expression lhs, Expression rhs, BuiltinType lt, BuiltinType rt,
            BinaryOperatorKind modelicaOp, String symbol,
            boolean eventSupported, String eventFunction, boolean periodSupported) {
        // Event operands are looked through: "tick C >= P start" carries no
        // return type, and would otherwise fall into the numeric branch and
        // become Modelica's >= on two Event records. Only Event is inferred, so
        // numeric and Boolean comparisons dispatch exactly as before.
        boolean events = TypeResolver.inferBuiltin(op.getLhs()) == BuiltinType.EVENT
            && TypeResolver.inferBuiltin(op.getRhs()) == BuiltinType.EVENT;
        if (!events && isNumericOrUnknown(lt) && isNumericOrUnknown(rt)) {
            return call(libraryRef("CRMLtoModelica.Functions.cvBooleanToBoolean4"), binary(modelicaOp, lhs, rhs));
        } else if (eventSupported && events) {
            if (eventFunction == null) {
                throw CRMLCompilerException.unsupported(Diagnostics.unsupported(opType,
                    "no CRMLtoModelica.Functions implementation exists for a strict Event "
                    + symbol + " comparison (only lEV/gEV exist)", op));
            }
            return call(libraryRef(eventFunction), lhs, rhs);
        } else if ((lt == BuiltinType.BOOLEAN || rt == BuiltinType.BOOLEAN) && isBooleanOrUnknown(lt) && isBooleanOrUnknown(rt)) {
            throw CRMLCompilerException.unsupported(Diagnostics.unsupported(opType,
                "Boolean4 " + symbol + " comparison has no implementation in CRMLtoModelica.mo "
                + "(the Logical4 package referenced by the legacy OperatorMapping table does not exist)", op));
        } else if (periodSupported
                && ((lt == BuiltinType.REAL && rt == BuiltinType.PERIOD) || (lt == BuiltinType.PERIOD && rt == BuiltinType.REAL))) {
            throw CRMLCompilerException.unsupported(Diagnostics.unsupported(opType,
                "Real/Period " + symbol + " comparison has no implementation in CRMLtoModelica.mo "
                + "(realPeriodeq/realPeriodleq do not exist)", op));
        } else {
            throw CRMLCompilerException.unsupported(Diagnostics.incompatibleTypes(opType, lt, rt, op));
        }
    }
}
