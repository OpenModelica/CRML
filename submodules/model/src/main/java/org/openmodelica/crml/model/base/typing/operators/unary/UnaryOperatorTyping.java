package org.openmodelica.crml.model.base.typing.operators.unary;

import org.openmodelica.crml.model.base.typing.operators.UnaryRule;
import org.openmodelica.crml.model.metamodel.BuiltinUnaryOperatorKind;

/**
 * The {@link UnaryRule} of each built-in unary operator.
 */
public final class UnaryOperatorTyping {

    private UnaryOperatorTyping() {
    }

    /**
     * The rule of the given operator, or null if its typing is not defined
     * yet ({@code par} and {@code pre}).
     */
    public static UnaryRule rule(BuiltinUnaryOperatorKind kind) {
        if (kind == null) {
            return null;
        }
        // Note: Fall-through behaviour of the switch is an intended feature.
        // Do not confuse empty cases with missing inference.
        switch (kind) {
            case NOT:
                return NotRule.INSTANCE;
            case CARD:
                return CardinalityRule.INSTANCE;
            case LOG10:
            case COS:
            case ACOS:
            case SIN:
            case ASIN:
            case EXP_OP:
            case LOG:
                return MathFunctionRule.INSTANCE;
            case START:
            case END:
                return EventRule.PERIOD_BOUNDARY;
            case TICK:
                return EventRule.TICK;
            case SUB:
            case ADD:
                return SignRule.INSTANCE;
            case SUBEXPRESSION:
                return SubexpressionRule.INSTANCE;
            case PAR:
            case PRE:
            default:
                return null;
        }
    }
}
