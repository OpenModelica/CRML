package org.openmodelica.crml.model.base.typing.operators.binary;

import static org.openmodelica.crml.model.base.typing.operators.util.SignatureTable.oneOf;
import static org.openmodelica.crml.model.metamodel.BuiltinType.BOOLEAN;
import static org.openmodelica.crml.model.metamodel.BuiltinType.CLOCK;

import org.openmodelica.crml.model.base.typing.operators.util.SignatureTable;
import org.openmodelica.crml.model.base.typing.operators.util.TableBinaryRule;

/** {@code c filter b}: the ticks of a Clock at which a Boolean holds, as a Clock. */
public final class FilterRule extends TableBinaryRule {

    public static final FilterRule INSTANCE = new FilterRule();

    private FilterRule() {
        super(SignatureTable.builder(2)
            .row(CLOCK, oneOf(CLOCK), oneOf(BOOLEAN))
            .build());
    }
}
