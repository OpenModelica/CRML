package org.openmodelica.crml.model.base.typing.operators.unary;

import static org.openmodelica.crml.model.base.typing.operators.util.SignatureTable.oneOf;
import static org.openmodelica.crml.model.metamodel.BuiltinType.CLOCK;
import static org.openmodelica.crml.model.metamodel.BuiltinType.EVENT;
import static org.openmodelica.crml.model.metamodel.BuiltinType.PERIOD;

import org.openmodelica.crml.model.base.typing.operators.util.SignatureTable;
import org.openmodelica.crml.model.base.typing.operators.util.TableUnaryRule;

/** The operators that give an Event: {@code P start}, {@code P end} and {@code tick c}. */
public final class EventRule extends TableUnaryRule {

    /** {@code start} and {@code end}: the opening and closing event of a Period. */
    public static final EventRule PERIOD_BOUNDARY = new EventRule(SignatureTable.builder(1)
        .row(EVENT, oneOf(PERIOD))
        .build());

    /** {@code tick}: the event of the current tick of a Clock. */
    public static final EventRule TICK = new EventRule(SignatureTable.builder(1)
        .row(EVENT, oneOf(CLOCK))
        .build());

    private EventRule(SignatureTable table) {
        super(table);
    }
}
