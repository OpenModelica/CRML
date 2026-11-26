package org.openmodelica.crml.model.base.typing.operators;

import org.openmodelica.crml.model.base.typing.BaseType;

/**
 * The typing rule of a built-in unary operator, on base types. Multiplicity
 * and lifting are handled by the caller, {@code TypeInference}.
 */
public interface UnaryRule {

    /**
     * The result base type for a single operand of the given base type, or
     * null if the operator does not accept it.
     */
    BaseType result(BaseType operand);

    /**
     * The result base type when the operand is a set of the given base type
     * and the operator reduces the set to one value, as {@code card} does; null
     * if it does not. An operator that does not reduce applies element-wise.
     */
    default BaseType reduce(BaseType element) {
        return null;
    }
}
