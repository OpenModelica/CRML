package org.openmodelica.crml.model.base.typing.operators.unary;

import static org.openmodelica.crml.model.base.typing.operators.util.SignatureTable.oneOf;
import static org.openmodelica.crml.model.metamodel.BuiltinType.INTEGER;
import static org.openmodelica.crml.model.metamodel.BuiltinType.REAL;

import org.openmodelica.crml.model.base.typing.operators.util.SignatureTable;
import org.openmodelica.crml.model.base.typing.operators.util.TableUnaryRule;

/** Unary {@code +} and {@code -}: a number to a number of the same type. */
public final class SignRule extends TableUnaryRule {

    public static final SignRule INSTANCE = new SignRule();

    private SignRule() {
        super(SignatureTable.builder(1)
            .row(INTEGER, oneOf(INTEGER))
            .row(REAL, oneOf(REAL))
            .build());
    }
}
