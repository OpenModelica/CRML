package org.openmodelica.crml.model.base.typing.operators.binary;

import org.openmodelica.crml.model.base.typing.BaseType;
import org.openmodelica.crml.model.base.typing.operators.BinaryRule;
import org.openmodelica.crml.model.metamodel.BuiltinType;

/**
 * {@code x at e}: the value of {@code x} at an Event, of the type of
 * {@code x}, whatever it is.
 *
 * <p>TODO: decide what the right operand is. The CRML reference table has an
 * Event ({@code AT,*,2,*,EVENT}), the metamodel documents {@code x at c} as
 * the value at the ticks of a Clock. Only an Event is accepted until then.
 */
public final class SamplingRule implements BinaryRule {

    public static final SamplingRule INSTANCE = new SamplingRule();

    private SamplingRule() {
    }

    @Override
    public BaseType result(BaseType lhs, BaseType rhs) {
        return rhs != null && rhs.is(BuiltinType.EVENT) ? lhs : null;
    }
}
