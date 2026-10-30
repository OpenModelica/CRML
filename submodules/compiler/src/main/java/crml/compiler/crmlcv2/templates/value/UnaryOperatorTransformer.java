package crml.compiler.crmlcv2.templates.value;

import static crml.compiler.crmlcv2.templates.value.TypeCategories.isBooleanOrUnknown;
import static crml.compiler.crmlcv2.templates.value.TypeCategories.isClockOrUnknown;
import static crml.compiler.crmlcv2.templates.value.TypeCategories.isNumericOrUnknown;
import static crml.compiler.crmlcv2.templates.value.TypeCategories.isPeriodOrUnknown;

import java.util.LinkedHashMap;
import java.util.Map;

import crml.compiler.crmlcv2.Diagnostics;
import crml.compiler.crmlcv2.TransformationContext;
import crml.compiler.crmlcv2.UnsupportedConstruct;
import crml.compiler.crmlcv2.templates.Transformer;
import crml.compiler.crmlcv2.util.TypeResolver;
import crml.model.language.BuiltinType;
import crml.model.language.BuiltinUnaryOperatorKind;
import crml.model.language.UnaryOperator;
import crml.model.modelica.Expression;
import crml.model.modelica.UnaryOperatorKind;
import crml.modelica.build.Modelica;

/**
 * CRML unary operators to Modelica expressions. Type dispatch ported unchanged
 * from the string generator; see {@link BinaryOperatorTransformer}.
 */
public final class UnaryOperatorTransformer implements Modelica {

    private final Transformer transformer;

    public UnaryOperatorTransformer(Transformer transformer) {
        this.transformer = transformer;
    }

    public Expression transform(TransformationContext ctx, UnaryOperator op) {
        Expression operand = transformer.values().transform(ctx, op.getValue());
        BuiltinType t = TypeResolver.resolveBuiltin(op.getValue().getReturnType());
        BuiltinUnaryOperatorKind opType = op.getOptype();

        switch (opType) {
            case SUBEXPRESSION:
                // Parentheses the CRML author wrote. This is the only producer of
                // ParenthesizedExpression; everything else the printer decides.
                return parens(operand);
            case ADD:
                return transformSign(op, opType, operand, t, UnaryOperatorKind.PLUS);
            case SUB:
                return transformSign(op, opType, operand, t, UnaryOperatorKind.MINUS);
            case NOT:
                // Boolean4 is four-valued, so CRML's "not" is a library call, never
                // Modelica's own not.
                return transformNot(op, opType, operand, t);
            case SIN:
                return transformMath(ctx, op, opType, operand, t, "Modelica.Math.sin");
            case ASIN:
                return transformMath(ctx, op, opType, operand, t, "Modelica.Math.asin");
            case COS:
                return transformMath(ctx, op, opType, operand, t, "Modelica.Math.cos");
            case ACOS:
                return transformMath(ctx, op, opType, operand, t, "Modelica.Math.acos");
            case LOG:
                return transformMath(ctx, op, opType, operand, t, "Modelica.Math.log");
            case LOG10:
                return transformMath(ctx, op, opType, operand, t, "Modelica.Math.log10");
            case EXP_OP:
                return transformMath(ctx, op, opType, operand, t, "Modelica.Math.exp");
            case START:
                return transformPeriodEndpoint(op, opType, operand, t, "CRMLtoModelica.Functions.PStart");
            case END:
                return transformPeriodEndpoint(op, opType, operand, t, "CRMLtoModelica.Functions.PEnd");
            case CARD:
                // CardClock(r1: CRMLClock) -> out: Integer, the number of ticks.
                // "card" over a set is a different operation - set cardinality -
                // which CRMLtoModelica.mo does not implement; and a set-typed
                // variable reaches the generator carrying only its element type
                // ("Real {} S" arrives as a Real), so the two cannot be told
                // apart here beyond "this is not a clock".
                if (!isClockOrUnknown(TypeResolver.inferBuiltin(op.getValue()))) {
                    throw new UnsupportedConstruct(Diagnostics.libraryGap("UnaryOperator.CARD",
                        "card over anything but a Clock - set cardinality - has no implementation "
                        + "in CRMLtoModelica.mo", op));
                }
                return transformClockBlock(ctx, op, opType, operand,
                    "CRMLtoModelica.Blocks.CardClock", "card");
            case TICK:
                // ClockTick(r1: CRMLClock) -> out: Event, the time of the last tick.
                return transformClockBlock(ctx, op, opType, operand,
                    "CRMLtoModelica.Blocks.ClockTick", "tick");
            default:
                throw new UnsupportedConstruct(Diagnostics.unsupported(opType,
                    "this operator kind is not produced by the current AST builders", op));
        }
    }

    /**
     * A unary operator backed by a library block. A block cannot sit inside an
     * expression, so it is instantiated as a component on the target class and
     * the expression becomes a reference to its output port.
     */
    private Expression transformClockBlock(TransformationContext ctx, UnaryOperator op,
            BuiltinUnaryOperatorKind opType, Expression operand, String blockType, String prefix) {
        // The looked-through type, not the inferred return type: both blocks
        // take a CRMLClock and nothing else, and typeinference.csv has no row
        // for card or tick on anything but a Clock. "card S" over a set of Reals
        // must be rejected, not quietly wired to CardClock.
        BuiltinType t = TypeResolver.inferBuiltin(op.getValue());
        if (!isClockOrUnknown(t)) {
            throw new UnsupportedConstruct(Diagnostics.incompatibleTypes(opType, t, op));
        }
        Map<String, Expression> inputs = new LinkedHashMap<String, Expression>();
        inputs.put("r1", operand);
        return transformer.blocks().instantiate(ctx, libraryRef(blockType), prefix, inputs,
            "out", op);
    }

    private Expression transformSign(UnaryOperator op, BuiltinUnaryOperatorKind opType,
            Expression operand, BuiltinType t, UnaryOperatorKind sign) {
        if (isNumericOrUnknown(t)) {
            return unary(sign, operand);
        }
        throw new UnsupportedConstruct(Diagnostics.incompatibleTypes(opType, t, op));
    }

    private Expression transformNot(UnaryOperator op, BuiltinUnaryOperatorKind opType,
            Expression operand, BuiltinType t) {
        if (isBooleanOrUnknown(t)) {
            return call(libraryRef("CRMLtoModelica.Functions.not4"), operand);
        }
        throw new UnsupportedConstruct(Diagnostics.incompatibleTypes(opType, t, op));
    }

    private Expression transformMath(TransformationContext ctx, UnaryOperator op,
            BuiltinUnaryOperatorKind opType, Expression operand, BuiltinType t, String function) {
        if (isNumericOrUnknown(t)) {
            // Modelica.Math is the standard library, which CRMLtoModelica.mo does
            // not itself depend on: a generated model using these needs MSL on the
            // load path. The reference records that, and the model is told once.
            ctx.reportOnce("STANDARD_LIBRARY", Diagnostics.info("UnaryOperator",
                "this model calls the Modelica Standard Library (" + function + "), which "
                + "CRMLtoModelica.mo does not depend on; the generated model needs MSL on the "
                + "load path", op));
            return call(mslRef(function), operand);
        }
        throw new UnsupportedConstruct(Diagnostics.incompatibleTypes(opType, t, op));
    }

    private Expression transformPeriodEndpoint(UnaryOperator op, BuiltinUnaryOperatorKind opType,
            Expression operand, BuiltinType t, String function) {
        if (isPeriodOrUnknown(t)) {
            return call(libraryRef(function), operand);
        }
        throw new UnsupportedConstruct(Diagnostics.incompatibleTypes(opType, t, op));
    }
}
