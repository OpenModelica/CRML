package org.openmodelica.crml.model.base.typing.operators.binary;

import org.openmodelica.crml.model.base.typing.operators.BinaryRule;
import org.openmodelica.crml.model.metamodel.BuiltinBinaryOperatorKind;

/**
 * The {@link BinaryRule} of each built-in binary operator.
 */
public final class BinaryOperatorTyping {

    private BinaryOperatorTyping() {
    }

    /**
     * The rule of the given operator, or null if its typing is not defined
     * yet ({@code with}, {@code master}, {@code on}, {@code par} and the
     * two-operand {@code log}).
     */
    public static BinaryRule rule(BuiltinBinaryOperatorKind kind) {
        if (kind == null) {
            return null;
        }
        switch (kind) {
            case AND:
            case OR:
                return LogicRule.INSTANCE;
            case ADD:
                return ArithmeticRule.ADD;
            case SUB:
                return ArithmeticRule.SUB;
            case MUL:
                return ArithmeticRule.MUL;
            case DIV:
                return ArithmeticRule.DIV;
            case MOD:
                return ArithmeticRule.MOD;
            case POW:
                return ArithmeticRule.POW;
            case LT:
                return ComparisonRule.LT;
            case LE:
                return ComparisonRule.LE;
            case GT:
                return ComparisonRule.GT;
            case GE:
                return ComparisonRule.GE;
            case EQ:
                return ComparisonRule.EQ;
            case NEQ:
                return ComparisonRule.NEQ;
            case FILTER:
                return FilterRule.INSTANCE;
            case AT:
                return SamplingRule.INSTANCE;
            case WITH:
            case MASTER:
            case ON:
            case PAR:
            case LOG:
            default:
                return null;
        }
    }
}
