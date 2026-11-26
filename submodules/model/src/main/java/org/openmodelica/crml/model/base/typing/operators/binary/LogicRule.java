package org.openmodelica.crml.model.base.typing.operators.binary;

import static org.openmodelica.crml.model.base.typing.operators.util.SignatureTable.oneOf;
import static org.openmodelica.crml.model.metamodel.BuiltinType.BOOLEAN;
import static org.openmodelica.crml.model.metamodel.BuiltinType.CLOCK;
import static org.openmodelica.crml.model.metamodel.BuiltinType.EVENT;

import org.openmodelica.crml.model.base.typing.operators.util.SignatureTable;
import org.openmodelica.crml.model.base.typing.operators.util.TableBinaryRule;

/**
 * {@code and} and {@code or}, as the CRML specification examples type them:
 *
 * <ul>
 * <li>two Booleans give a Boolean;</li>
 * <li>a Boolean with an Event gives a Boolean;</li>
 * <li>Events and Clocks, in any combination, give a Clock: the common ticks
 *     for {@code and}, all of them for {@code or}.</li>
 * </ul>
 *
 * <p>Both operators are commutative, so every row holds in both operand
 * orders, also where the examples show only one. A Boolean with a Clock is
 * not defined by the examples and is left out.
 */
public final class LogicRule extends TableBinaryRule {

    public static final LogicRule INSTANCE = new LogicRule();

    private LogicRule() {
        super(SignatureTable.builder(2)
            .row(BOOLEAN, oneOf(BOOLEAN), oneOf(BOOLEAN))
            .row(BOOLEAN, oneOf(BOOLEAN), oneOf(EVENT))
            .row(BOOLEAN, oneOf(EVENT), oneOf(BOOLEAN))
            .row(CLOCK, oneOf(EVENT, CLOCK), oneOf(EVENT, CLOCK))
            .build());
    }
}
