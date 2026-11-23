package org.openmodelica.crml.model.base.typing.operators;

import org.openmodelica.crml.model.base.typing.BaseType;

/**
 * The typing rule of a built-in binary operator, on base types. Multiplicity,
 * lifting and aggregation are handled by the caller, {@code TypeInference}.
 */
public interface BinaryRule {

    /**
     * The result base type for single operands of the given base types, or
     * null if the operator does not accept them.
     */
    BaseType result(BaseType lhs, BaseType rhs);
}
