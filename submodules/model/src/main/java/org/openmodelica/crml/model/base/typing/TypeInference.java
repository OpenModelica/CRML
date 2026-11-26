package org.openmodelica.crml.model.base.typing;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.openmodelica.crml.model.base.typing.operators.BinaryRule;
import org.openmodelica.crml.model.base.typing.operators.UnaryRule;
import org.openmodelica.crml.model.base.typing.operators.binary.BinaryOperatorTyping;
import org.openmodelica.crml.model.base.typing.operators.unary.UnaryOperatorTyping;
import org.openmodelica.crml.model.metamodel.BinaryOperator;
import org.openmodelica.crml.model.metamodel.BooleanConstant;
import org.openmodelica.crml.model.metamodel.BuiltinType;
import org.openmodelica.crml.model.metamodel.BuiltinTypeReference;
import org.openmodelica.crml.model.metamodel.ComputedValue;
import org.openmodelica.crml.model.metamodel.ConstructorValue;
import org.openmodelica.crml.model.metamodel.CustomOperator;
import org.openmodelica.crml.model.metamodel.DurationValue;
import org.openmodelica.crml.model.metamodel.IfValue;
import org.openmodelica.crml.model.metamodel.IndirectTypeReference;
import org.openmodelica.crml.model.metamodel.IntegerConstant;
import org.openmodelica.crml.model.metamodel.IntegrateValue;
import org.openmodelica.crml.model.metamodel.Liftable;
import org.openmodelica.crml.model.metamodel.PeriodsValue;
import org.openmodelica.crml.model.metamodel.ProjectionValue;
import org.openmodelica.crml.model.metamodel.RealConstant;
import org.openmodelica.crml.model.metamodel.ScatteredValue;
import org.openmodelica.crml.model.metamodel.StringConstant;
import org.openmodelica.crml.model.metamodel.TimeValue;
import org.openmodelica.crml.model.metamodel.TypeReference;
import org.openmodelica.crml.model.metamodel.UnaryOperator;
import org.openmodelica.crml.model.metamodel.Value;
import org.openmodelica.crml.model.metamodel.Variable;
import org.openmodelica.crml.model.metamodel.VariableReference;
import org.openmodelica.crml.model.metamodel.util.CRMLSwitch;

/**
 * The implementation of {@code Value.inferType()}, {@code TypeReference.toType()}
 * and {@code Variable.declaredType()} of {@code crml.xcore}.
 *
 * <p>Inference is bottom-up and grounded in what the model states: the kind
 * of a constant, the declared domain of a variable, the declared result type
 * of a user operator or template, and the domain of a constructor. It never
 * looks into the definition of a variable, so it always terminates. The
 * built-in operators are typed by the rules in
 * {@link org.openmodelica.crml.model.base.typing.operators}.
 *
 * <p>Nothing is cached: the linker changes the model while it runs, so every
 * call computes its result from the current model. Nothing is reported
 * either: a value that cannot be typed, because it is incomplete, ill-typed
 * or not supported yet, is {@link ValueType#UNKNOWN}, and as soon as an
 * operand is unknown, so is the result. Telling these cases apart is the
 * validator's job.
 *
 * <p>Multiplicity: an application with {@code .element} operands is a set
 * (one result per combination of their elements). A unary operator applied
 * to a set works element-wise, unless it reduces the set (as {@code card}
 * does). A binary operator without a left operand aggregates a set into one
 * value. Other binary operators on sets are not typed yet.
 */
public final class TypeInference extends CRMLSwitch<ValueType> {

    private static final TypeInference INSTANCE = new TypeInference();

    private TypeInference() {
    }

    /** The type of the given value; {@link ValueType#UNKNOWN} for null. */
    public static ValueType infer(Value value) {
        if (value == null) {
            return ValueType.UNKNOWN;
        }
        ValueType type = INSTANCE.doSwitch(value);
        return type == null ? ValueType.UNKNOWN : type;
    }

    /**
     * The type a type reference names, as a single value; {@link ValueType#UNKNOWN}
     * for null, for a cycle of indirect references, and for a user-defined type,
     * which is not supported yet.
     */
    public static ValueType toType(TypeReference reference) {
        Set<TypeReference> seen = new HashSet<>();
        TypeReference current = reference;
        while (current instanceof IndirectTypeReference) {
            if (!seen.add(current)) {
                return ValueType.UNKNOWN;
            }
            current = ((IndirectTypeReference) current).getReferredType();
        }
        if (current instanceof BuiltinTypeReference) {
            return ValueType.of(((BuiltinTypeReference) current).getBuiltinType());
        }
        // A UserTypereference to a Class becomes a class base type here once
        // complex types are supported.
        return ValueType.UNKNOWN;
    }

    /**
     * The declared type of a variable: its domain, as a set if the variable
     * is declared a collection.
     */
    public static ValueType declaredType(Variable variable) {
        if (variable == null) {
            return ValueType.UNKNOWN;
        }
        ValueType domain = toType(variable.getDomain());
        return Boolean.TRUE.equals(variable.getCollection()) ? domain.asSet() : domain;
    }

    /**
     * The type of one element of a value of the given type: the base type of
     * a set, or a Period of a Periods; {@link ValueType#UNKNOWN} otherwise.
     */
    public static ValueType elementType(ValueType collection) {
        if (collection.isSet()) {
            return ValueType.single(collection.getBase());
        }
        if (collection.isSingle(BuiltinType.PERIODS)) {
            return ValueType.of(BuiltinType.PERIOD);
        }
        return ValueType.UNKNOWN;
    }

    // ---------------------------------------------------------------------
    // Grounded values
    // ---------------------------------------------------------------------

    @Override
    public ValueType caseBooleanConstant(BooleanConstant object) {
        return ValueType.of(BuiltinType.BOOLEAN);
    }

    @Override
    public ValueType caseIntegerConstant(IntegerConstant object) {
        return ValueType.of(BuiltinType.INTEGER);
    }

    @Override
    public ValueType caseRealConstant(RealConstant object) {
        return ValueType.of(BuiltinType.REAL);
    }

    @Override
    public ValueType caseStringConstant(StringConstant object) {
        return ValueType.of(BuiltinType.STRING);
    }

    @Override
    public ValueType caseTimeValue(TimeValue object) {
        return ValueType.of(BuiltinType.REAL);
    }

    @Override
    public ValueType caseVariableReference(VariableReference object) {
        return declaredType(object.getVariable());
    }

    @Override
    public ValueType caseConstructorValue(ConstructorValue object) {
        return toType(object.getDomain());
    }

    /** A call to a user operator or template: the operator's declared result type. */
    @Override
    public ValueType caseComputedValue(ComputedValue object) {
        CustomOperator operator = object.getOperator();
        ValueType result = operator == null ? ValueType.UNKNOWN : toType(operator.getDomain());
        return lifted(object, result);
    }

    // ---------------------------------------------------------------------
    // Sets
    // ---------------------------------------------------------------------

    /**
     * A set literal: its declared element domain if it has one, else the
     * {@link #common(ValueType, ValueType) common type} of its elements, as
     * Modelica types an array constructor ({@code {1, 2.5}} is a set of Real);
     * unknown if the elements have none, or if the set is empty and has no
     * domain.
     */
    @Override
    public <D> ValueType caseSet(org.openmodelica.crml.model.metamodel.Set<D> object) {
        if (object.getDomain() != null) {
            return toType(object.getDomain()).asSet();
        }
        ValueType shared = null;
        for (Object element : object.getElements()) {
            ValueType type = element instanceof Value ? infer((Value) element) : ValueType.UNKNOWN;
            if (!type.isSingle()) {
                return ValueType.UNKNOWN;
            }
            shared = shared == null ? type : common(shared, type);
        }
        return shared == null ? ValueType.UNKNOWN : shared.asSet();
    }

    /**
     * {@code x.element}: with a method, one element of {@code x}; without
     * one, the set of the elements of {@code x}.
     */
    @Override
    public ValueType caseScatteredValue(ScatteredValue object) {
        ValueType element = elementType(infer(object.getData()));
        return object.getMethod() == null ? element.asSet() : element;
    }

    // ---------------------------------------------------------------------
    // Built-in operators
    // ---------------------------------------------------------------------

    @Override
    public ValueType caseUnaryOperator(UnaryOperator object) {
        UnaryRule rule = UnaryOperatorTyping.rule(object.getOptype());
        ValueType operand = infer(object.getValue());
        if (rule == null || operand.isUnknown()) {
            return ValueType.UNKNOWN;
        }
        if (operand.isSet()) {
            BaseType reduced = rule.reduce(operand.getBase());
            if (reduced != null) {
                return lifted(object, ValueType.single(reduced));
            }
            return lifted(object, ValueType.setOrUnknown(rule.result(operand.getBase())));
        }
        return lifted(object, ValueType.singleOrUnknown(rule.result(operand.getBase())));
    }

    @Override
    public ValueType caseBinaryOperator(BinaryOperator object) {
        BinaryRule rule = BinaryOperatorTyping.rule(object.getOptype());
        if (rule == null) {
            return ValueType.UNKNOWN;
        }
        ValueType rhs = infer(object.getRhs());
        if (object.getLhs() == null) {
            // An aggregator, "and ({a, b})": the operator folded over a set.
            if (!rhs.isSet()) {
                return ValueType.UNKNOWN;
            }
            BaseType element = rhs.getBase();
            BaseType folded = rule.result(element, element);
            return folded != null && folded.equals(element) ? ValueType.single(folded) : ValueType.UNKNOWN;
        }
        ValueType lhs = infer(object.getLhs());
        if (lhs.isUnknown() || rhs.isUnknown() || lhs.isSet() || rhs.isSet()) {
            return ValueType.UNKNOWN;
        }
        return lifted(object, ValueType.singleOrUnknown(rule.result(lhs.getBase(), rhs.getBase())));
    }

    // ---------------------------------------------------------------------
    // Other expressions
    // ---------------------------------------------------------------------

    /**
     * {@code [e1, e2]}: a Period between two Events (or Booleans, which
     * become Events), or a Periods between two Clocks.
     */
    @Override
    public ValueType casePeriodsValue(PeriodsValue object) {
        ValueType start = infer(object.getStartValue());
        ValueType end = infer(object.getEndValue());
        if (start.isSingle(BuiltinType.CLOCK) && end.isSingle(BuiltinType.CLOCK)) {
            return ValueType.of(BuiltinType.PERIODS);
        }
        if (isEventLike(start) && isEventLike(end)) {
            return ValueType.of(BuiltinType.PERIOD);
        }
        return ValueType.UNKNOWN;
    }

    /** {@code integrate x on P}: a number integrated over a Period or Periods, a Real. */
    @Override
    public ValueType caseIntegrateValue(IntegrateValue object) {
        ValueType integrand = infer(object.getIntegrand());
        ValueType interval = infer(object.getInterval());
        boolean numeric = integrand.isSingle(BuiltinType.INTEGER) || integrand.isSingle(BuiltinType.REAL);
        return numeric && isPeriodLike(interval) ? ValueType.of(BuiltinType.REAL) : ValueType.UNKNOWN;
    }

    /** {@code duration b on P}: how long a Boolean holds in a Period or Periods, a Real. */
    @Override
    public ValueType caseDurationValue(DurationValue object) {
        ValueType condition = infer(object.getExp1());
        ValueType interval = infer(object.getExp2());
        return condition.isSingle(BuiltinType.BOOLEAN) && isPeriodLike(interval)
            ? ValueType.of(BuiltinType.REAL) : ValueType.UNKNOWN;
    }

    /** {@code e proj c}: an Event or a Clock projected on a Clock, of the type of {@code e}. */
    @Override
    public ValueType caseProjectionValue(ProjectionValue object) {
        ValueType projected = infer(object.getP1());
        ValueType clock = infer(object.getP2());
        boolean projectable = projected.isSingle(BuiltinType.EVENT) || projected.isSingle(BuiltinType.CLOCK);
        return projectable && clock.isSingle(BuiltinType.CLOCK) ? projected : ValueType.UNKNOWN;
    }

    /**
     * {@code if c then a else b}: the {@link #common(ValueType, ValueType) common type}
     * of both branches, as in a Modelica if-expression
     * ({@code if c then 1 else 2.5} is a Real).
     */
    @Override
    public ValueType caseIfValue(IfValue object) {
        ValueType condition = infer(object.getCondition());
        if (!condition.isSingle(BuiltinType.BOOLEAN)) {
            return ValueType.UNKNOWN;
        }
        return common(infer(object.getThen()), infer(object.getOthervise()));
    }

    /** Anything else, such as a Sequence left by the parser: unknown. */
    @Override
    public ValueType defaultCase(EObject object) {
        return ValueType.UNKNOWN;
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    /**
     * The type of an application with the given per-combination result: a
     * set of it if the application has {@code .element} operands.
     */
    private static ValueType lifted(Liftable application, ValueType result) {
        if (result.isUnknown() || application.getLifts().isEmpty()) {
            return result;
        }
        return result.isSet() ? ValueType.UNKNOWN : result.asSet();
    }

    /**
     * The type two alternatives share, as Modelica decides it for the branches
     * of an if-expression and the elements of an array constructor: the type
     * itself if both have it, Real for an Integer and a Real (the Integer is
     * converted), and unknown otherwise. The multiplicities must be the same.
     */
    private static ValueType common(ValueType a, ValueType b) {
        if (a.isUnknown() || b.isUnknown() || a.getMultiplicity() != b.getMultiplicity()) {
            return ValueType.UNKNOWN;
        }
        if (a.equals(b)) {
            return a;
        }
        if (isNumber(a.getBase()) && isNumber(b.getBase())) {
            BaseType real = BaseType.of(BuiltinType.REAL);
            return a.isSet() ? ValueType.set(real) : ValueType.single(real);
        }
        return ValueType.UNKNOWN;
    }

    private static boolean isNumber(BaseType base) {
        return base.is(BuiltinType.INTEGER) || base.is(BuiltinType.REAL);
    }

    private static boolean isEventLike(ValueType type) {
        return type.isSingle(BuiltinType.EVENT) || type.isSingle(BuiltinType.BOOLEAN);
    }

    private static boolean isPeriodLike(ValueType type) {
        return type.isSingle(BuiltinType.PERIOD) || type.isSingle(BuiltinType.PERIODS);
    }
}
