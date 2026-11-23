package org.openmodelica.crml.model.base.typing.operators.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openmodelica.crml.model.base.typing.BaseType;

/**
 * One row of a {@link SignatureTable}: the base types of the operands, in
 * order, and the base type of the result.
 */
public final class Signature {

    private final List<BaseType> parameters;
    private final BaseType result;

    Signature(List<BaseType> parameters, BaseType result) {
        this.parameters = Collections.unmodifiableList(new ArrayList<>(parameters));
        this.result = result;
    }

    public List<BaseType> getParameters() {
        return parameters;
    }

    public BaseType getResult() {
        return result;
    }

    /** {@code (Integer, Real) -> Real}. */
    @Override
    public String toString() {
        StringBuilder text = new StringBuilder("(");
        for (int i = 0; i < parameters.size(); i++) {
            if (i > 0) {
                text.append(", ");
            }
            text.append(parameters.get(i));
        }
        return text.append(") -> ").append(result).toString();
    }
}
