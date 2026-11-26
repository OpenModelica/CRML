package org.openmodelica.crml.model.base.typing.operators.util;

import org.openmodelica.crml.model.base.typing.BaseType;
import org.openmodelica.crml.model.base.typing.operators.UnaryRule;

/** A {@link UnaryRule} given by a {@link SignatureTable}. */
public class TableUnaryRule implements UnaryRule {

    private final SignatureTable table;

    protected TableUnaryRule(SignatureTable table) {
        if (table.getArity() != 1) {
            throw new IllegalArgumentException("a unary rule needs a table of arity 1");
        }
        this.table = table;
    }

    public SignatureTable getTable() {
        return table;
    }

    @Override
    public BaseType result(BaseType operand) {
        return table.result(operand);
    }
}
