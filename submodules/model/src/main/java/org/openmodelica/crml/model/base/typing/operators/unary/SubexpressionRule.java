package org.openmodelica.crml.model.base.typing.operators.unary;

import org.openmodelica.crml.model.base.typing.BaseType;
import org.openmodelica.crml.model.base.typing.operators.UnaryRule;

/** Parentheses: the type of the enclosed expression, whatever it is. */
public final class SubexpressionRule implements UnaryRule {

    public static final SubexpressionRule INSTANCE = new SubexpressionRule();

    private SubexpressionRule() {
    }

    @Override
    public BaseType result(BaseType operand) {
        return operand;
    }
}
