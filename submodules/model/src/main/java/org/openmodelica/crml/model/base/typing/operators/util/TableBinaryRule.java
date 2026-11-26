package org.openmodelica.crml.model.base.typing.operators.util;

import org.openmodelica.crml.model.base.typing.BaseType;
import org.openmodelica.crml.model.base.typing.operators.BinaryRule;

/** A {@link BinaryRule} given by a {@link SignatureTable}. */
public class TableBinaryRule implements BinaryRule {

    private final SignatureTable table;

    protected TableBinaryRule(SignatureTable table) {
        if (table.getArity() != 2) {
            throw new IllegalArgumentException("a binary rule needs a table of arity 2");
        }
        this.table = table;
    }

    public SignatureTable getTable() {
        return table;
    }

    @Override
    public BaseType result(BaseType lhs, BaseType rhs) {
        return table.result(lhs, rhs);
    }
}
