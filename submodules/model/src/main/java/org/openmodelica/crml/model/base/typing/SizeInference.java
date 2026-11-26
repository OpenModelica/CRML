package org.openmodelica.crml.model.base.typing;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.openmodelica.crml.model.metamodel.BuiltinType;
import org.openmodelica.crml.model.metamodel.IfValue;
import org.openmodelica.crml.model.metamodel.Liftable;
import org.openmodelica.crml.model.metamodel.ScatteredValue;
import org.openmodelica.crml.model.metamodel.UnaryOperator;
import org.openmodelica.crml.model.metamodel.Value;
import org.openmodelica.crml.model.metamodel.Variable;
import org.openmodelica.crml.model.metamodel.VariableReference;
import org.openmodelica.crml.model.metamodel.util.CRMLSwitch;

/**
 * The implementation of {@code Value.inferSize()} of {@code crml.xcore}: the
 * maximum number of elements a value holds, known before the model runs, as
 * the size of a Modelica array is.
 *
 * <ul>
 * <li>A single value holds one element, except a Periods, which has room for
 *     {@link #PERIODS_CAPACITY} Periods.</li>
 * <li>A set literal holds its elements; a reference to a collection variable,
 *     as many as the variable's definition.</li>
 * <li>{@code x.element} without a method holds as many as {@code x}.</li>
 * <li>An application with {@code .element} operands holds one result per
 *     combination of their elements: the product of their sizes.</li>
 * <li>A unary operator applied element-wise holds as many as its operand.</li>
 * <li>{@code if c then a else b} holds as many as the larger branch.</li>
 * </ul>
 *
 * <p>Null when the size cannot be computed, for example for an external
 * collection variable, or a value of unknown type. Like {@link TypeInference},
 * nothing is cached or reported.
 */
public final class SizeInference extends CRMLSwitch<Integer> {

    /**
     * The number of Periods a Periods has room for: the size of
     * {@code CRMLPeriods.period} in {@code CRMLtoModelica.mo}.
     */
    public static final int PERIODS_CAPACITY = 50;

    /** Variables whose definitions are being sized, to stop at a cycle. */
    private final Set<Variable> visiting = new HashSet<>();

    private SizeInference() {
    }

    /** The maximum number of elements of the given value, or null if it cannot be computed. */
    public static Integer infer(Value value) {
        return new SizeInference().size(value);
    }

    private Integer size(Value value) {
        if (value == null) {
            return null;
        }
        ValueType type = TypeInference.infer(value);
        if (type.isUnknown()) {
            return null;
        }
        if (type.isSingle()) {
            return type.getBase().is(BuiltinType.PERIODS) ? PERIODS_CAPACITY : 1;
        }
        if (value instanceof Liftable && !((Liftable) value).getLifts().isEmpty()) {
            return liftedSize((Liftable) value);
        }
        return doSwitch(value);
    }

    /** The product of the sizes of the data of the lifts. */
    private Integer liftedSize(Liftable application) {
        int product = 1;
        for (ScatteredValue lift : application.getLifts()) {
            Integer elements = elementCount(lift.getData());
            if (elements == null) {
                return null;
            }
            try {
                product = Math.multiplyExact(product, elements);
            } catch (ArithmeticException overflow) {
                return null;
            }
        }
        return product;
    }

    /**
     * The number of elements {@code .element} ranges over: the size of a set,
     * or the capacity of a Periods.
     */
    private Integer elementCount(Value collection) {
        ValueType type = TypeInference.infer(collection);
        if (type.isSet()) {
            return size(collection);
        }
        if (type.isSingle(BuiltinType.PERIODS)) {
            return PERIODS_CAPACITY;
        }
        return null;
    }

    // ---------------------------------------------------------------------
    // Sets, by kind of value. Only reached for a value whose type is a set.
    // ---------------------------------------------------------------------

    @Override
    public <D> Integer caseSet(org.openmodelica.crml.model.metamodel.Set<D> object) {
        return object.getElements().size();
    }

    @Override
    public Integer caseVariableReference(VariableReference object) {
        Variable variable = object.getVariable();
        if (variable == null || !visiting.add(variable)) {
            return null;
        }
        try {
            return size(variable.getDefinition());
        } finally {
            visiting.remove(variable);
        }
    }

    @Override
    public Integer caseScatteredValue(ScatteredValue object) {
        return elementCount(object.getData());
    }

    @Override
    public Integer caseUnaryOperator(UnaryOperator object) {
        return size(object.getValue());
    }

    @Override
    public Integer caseIfValue(IfValue object) {
        Integer then = size(object.getThen());
        Integer otherwise = size(object.getOthervise());
        return then == null || otherwise == null ? null : Math.max(then, otherwise);
    }

    @Override
    public Integer defaultCase(EObject object) {
        return null;
    }
}
