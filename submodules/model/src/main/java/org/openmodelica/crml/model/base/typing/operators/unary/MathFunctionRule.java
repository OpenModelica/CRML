package org.openmodelica.crml.model.base.typing.operators.unary;

import static org.openmodelica.crml.model.base.typing.operators.util.SignatureTable.oneOf;
import static org.openmodelica.crml.model.metamodel.BuiltinType.INTEGER;
import static org.openmodelica.crml.model.metamodel.BuiltinType.REAL;

import org.openmodelica.crml.model.base.typing.operators.util.SignatureTable;
import org.openmodelica.crml.model.base.typing.operators.util.TableUnaryRule;

/**
 * The mathematical functions {@code log10}, {@code cos}, {@code acos},
 * {@code sin}, {@code asin}, {@code exp} and {@code log}: a number to a Real.
 */
public final class MathFunctionRule extends TableUnaryRule {

    public static final MathFunctionRule INSTANCE = new MathFunctionRule();

    private MathFunctionRule() {
        super(SignatureTable.builder(1)
            .row(REAL, oneOf(INTEGER, REAL))
            .build());
    }
}
