package org.openmodelica.crml.model.base.typing.operators.binary;

import static org.openmodelica.crml.model.base.typing.operators.util.SignatureTable.oneOf;
import static org.openmodelica.crml.model.metamodel.BuiltinType.BOOLEAN;
import static org.openmodelica.crml.model.metamodel.BuiltinType.EVENT;
import static org.openmodelica.crml.model.metamodel.BuiltinType.INTEGER;
import static org.openmodelica.crml.model.metamodel.BuiltinType.PERIOD;
import static org.openmodelica.crml.model.metamodel.BuiltinType.REAL;

import org.openmodelica.crml.model.base.typing.operators.util.SignatureTable;
import org.openmodelica.crml.model.base.typing.operators.util.TableBinaryRule;

/**
 * The comparisons {@code <}, {@code <=}, {@code >}, {@code >=}, {@code ==}
 * and {@code <>}, which give a Boolean.
 *
 * <p>All compare two numbers and two Booleans. As in the CRML reference
 * table, a time with a Period is accepted by {@code <}, {@code <=},
 * {@code ==} and {@code <>}, and two Events by the four orderings.
 *
 * <p>The table has no row for a time with a Period under {@code >} and
 * {@code >=}, nor for two Events under {@code ==} and {@code <>}. Whether
 * CRML allows them is not decided, so they are left out and infer as unknown.
 */
public final class ComparisonRule extends TableBinaryRule {

    public static final ComparisonRule LT = new ComparisonRule(common()
        .row(BOOLEAN, oneOf(REAL), oneOf(PERIOD))
        .row(BOOLEAN, oneOf(EVENT), oneOf(EVENT)));

    public static final ComparisonRule LE = new ComparisonRule(common()
        .row(BOOLEAN, oneOf(REAL), oneOf(PERIOD))
        .row(BOOLEAN, oneOf(EVENT), oneOf(EVENT)));

    public static final ComparisonRule GT = new ComparisonRule(common()
        .row(BOOLEAN, oneOf(EVENT), oneOf(EVENT)));

    public static final ComparisonRule GE = new ComparisonRule(common()
        .row(BOOLEAN, oneOf(EVENT), oneOf(EVENT)));

    public static final ComparisonRule EQ = new ComparisonRule(common()
        .row(BOOLEAN, oneOf(REAL), oneOf(PERIOD)));

    public static final ComparisonRule NEQ = new ComparisonRule(common()
        .row(BOOLEAN, oneOf(REAL), oneOf(PERIOD)));

    private ComparisonRule(SignatureTable.Builder table) {
        super(table.build());
    }

    /** The rows shared by all six: two numbers, and two Booleans. */
    private static SignatureTable.Builder common() {
        return SignatureTable.builder(2)
            .row(BOOLEAN, oneOf(INTEGER, REAL), oneOf(INTEGER, REAL))
            .row(BOOLEAN, oneOf(BOOLEAN), oneOf(BOOLEAN));
    }
}
