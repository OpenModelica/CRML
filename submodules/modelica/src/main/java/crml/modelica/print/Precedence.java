package crml.modelica.print;

import crml.model.modelica.BinaryExpression;
import crml.model.modelica.BinaryOperatorKind;
import crml.model.modelica.Expression;
import crml.model.modelica.IfExpression;
import crml.model.modelica.UnaryExpression;

/**
 * The Modelica expression grammar's binding strengths, used to insert the
 * minimum number of parentheses. Levels mirror the production hierarchy of
 * Modelica 3.x:
 *
 * <pre>
 *   expression            if-then-else                     IF
 *   logical_expression    or                               OR
 *   logical_term          and                              AND
 *   relation              &lt; &lt;= &gt; &gt;= == &lt;&gt;  (non-associative) RELATIONAL
 *   arithmetic_expression + -  and the leading unary sign   ADDITIVE
 *   term                  * /                              MULTIPLICATIVE
 *   factor                ^     (non-associative)           POWER
 *   primary               literals, references, calls, ()   PRIMARY
 * </pre>
 */
public final class Precedence {

    public static final int IF = 1;
    public static final int OR = 2;
    public static final int AND = 3;
    public static final int RELATIONAL = 4;
    public static final int ADDITIVE = 5;
    public static final int MULTIPLICATIVE = 6;
    public static final int POWER = 7;
    public static final int PRIMARY = 8;

    private Precedence() {
    }

    /** The binding strength of the expression's own top-level operator. */
    public static int of(Expression expression) {
        if (expression instanceof BinaryExpression) {
            return of(((BinaryExpression) expression).getOperator());
        }
        if (expression instanceof UnaryExpression) {
            // "[ add_op ] term { add_op term }": a leading sign binds at the
            // additive level and takes a whole term as its operand.
            return ADDITIVE;
        }
        if (expression instanceof IfExpression) {
            return IF;
        }
        // Literals, component references, function calls, array constructors and
        // explicit parentheses are all primaries.
        return PRIMARY;
    }

    public static int of(BinaryOperatorKind operator) {
        if (operator == null) {
            return PRIMARY;
        }
        switch (operator) {
            case OR:
                return OR;
            case AND:
                return AND;
            case EQ:
            case NEQ:
            case LT:
            case LE:
            case GT:
            case GE:
                return RELATIONAL;
            case ADD:
            case SUB:
                return ADDITIVE;
            case MUL:
            case DIV:
                return MULTIPLICATIVE;
            case POW:
                return POWER;
            default:
                return PRIMARY;
        }
    }

    /** Relations and {@code ^} are non-associative: both operands need parentheses at equal strength. */
    public static boolean isNonAssociative(BinaryOperatorKind operator) {
        return of(operator) == RELATIONAL || operator == BinaryOperatorKind.POW;
    }

    /**
     * Whether an operand of a binary expression must be parenthesised.
     *
     * @param operator the enclosing operator
     * @param operand  the operand being printed
     * @param leftSide true when the operand is the left-hand one
     */
    public static boolean needsParens(BinaryOperatorKind operator, Expression operand, boolean leftSide) {
        int parent = of(operator);
        int child = of(operand);
        if (child > parent) {
            return false;
        }
        if (child < parent) {
            return true;
        }
        // Equal strength: only a left-associative operator may keep its left
        // operand bare.
        return isNonAssociative(operator) || !leftSide;
    }

    /**
     * Whether the operand of a unary sign must be parenthesised. The operand of
     * "[ add_op ] term" is a term, so anything binding no tighter than
     * additive - another sign, a sum, a relation, an if-expression - needs
     * parentheses.
     */
    public static boolean unaryOperandNeedsParens(Expression operand) {
        return of(operand) <= ADDITIVE;
    }

    /**
     * Whether an if-expression's condition must be parenthesised. Modelica's
     * "if" takes an expression, but a nested if-expression there is unreadable
     * and ambiguous to a human, so it gets parentheses.
     */
    public static boolean conditionNeedsParens(Expression condition) {
        return of(condition) <= IF;
    }
}
