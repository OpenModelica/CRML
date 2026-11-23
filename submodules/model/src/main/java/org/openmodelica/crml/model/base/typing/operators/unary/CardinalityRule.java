package org.openmodelica.crml.model.base.typing.operators.unary;

import static org.openmodelica.crml.model.base.typing.operators.util.SignatureTable.oneOf;
import static org.openmodelica.crml.model.metamodel.BuiltinType.CLOCK;
import static org.openmodelica.crml.model.metamodel.BuiltinType.INTEGER;

import org.openmodelica.crml.model.base.typing.BaseType;
import org.openmodelica.crml.model.base.typing.operators.util.SignatureTable;
import org.openmodelica.crml.model.base.typing.operators.util.TableUnaryRule;
import org.openmodelica.crml.model.metamodel.BuiltinType;

/**
 * {@code card}: the number of ticks of a Clock, or the number of elements of
 * a set of any type. It reduces a set to one Integer.
 */
public final class CardinalityRule extends TableUnaryRule {

    public static final CardinalityRule INSTANCE = new CardinalityRule();

    private CardinalityRule() {
        super(SignatureTable.builder(1)
            .row(INTEGER, oneOf(CLOCK))
            .build());
    }

    @Override
    public BaseType reduce(BaseType element) {
        return BaseType.of(BuiltinType.INTEGER);
    }
}
