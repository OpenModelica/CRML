package org.openmodelica.crml.model.base.typing.operators.unary;

import static org.openmodelica.crml.model.base.typing.operators.util.SignatureTable.oneOf;
import static org.openmodelica.crml.model.metamodel.BuiltinType.BOOLEAN;

import org.openmodelica.crml.model.base.typing.operators.util.SignatureTable;
import org.openmodelica.crml.model.base.typing.operators.util.TableUnaryRule;

/** {@code not}: a Boolean to a Boolean. */
public final class NotRule extends TableUnaryRule {

    public static final NotRule INSTANCE = new NotRule();

    private NotRule() {
        super(SignatureTable.builder(1)
            .row(BOOLEAN, oneOf(BOOLEAN))
            .build());
    }
}
