package org.openmodelica.crml.model.base.typing;

import org.openmodelica.crml.model.metamodel.BinaryOperator;
import org.openmodelica.crml.model.metamodel.BooleanConstant;
import org.openmodelica.crml.model.metamodel.BooleanLiteral;
import org.openmodelica.crml.model.metamodel.BuiltinBinaryOperatorKind;
import org.openmodelica.crml.model.metamodel.BuiltinType;
import org.openmodelica.crml.model.metamodel.BuiltinTypeReference;
import org.openmodelica.crml.model.metamodel.BuiltinUnaryOperatorKind;
import org.openmodelica.crml.model.metamodel.CRMLFactory;
import org.openmodelica.crml.model.metamodel.ComputedValue;
import org.openmodelica.crml.model.metamodel.ConstructorValue;
import org.openmodelica.crml.model.metamodel.CustomOperator;
import org.openmodelica.crml.model.metamodel.DurationValue;
import org.openmodelica.crml.model.metamodel.IfValue;
import org.openmodelica.crml.model.metamodel.IntegerConstant;
import org.openmodelica.crml.model.metamodel.IntegrateValue;
import org.openmodelica.crml.model.metamodel.Liftable;
import org.openmodelica.crml.model.metamodel.Model;
import org.openmodelica.crml.model.metamodel.PeriodsValue;
import org.openmodelica.crml.model.metamodel.ProjectionValue;
import org.openmodelica.crml.model.metamodel.RealConstant;
import org.openmodelica.crml.model.metamodel.ScatteredValue;
import org.openmodelica.crml.model.metamodel.Set;
import org.openmodelica.crml.model.metamodel.StringConstant;
import org.openmodelica.crml.model.metamodel.Template;
import org.openmodelica.crml.model.metamodel.UnaryOperator;
import org.openmodelica.crml.model.metamodel.UserOperator;
import org.openmodelica.crml.model.metamodel.Value;
import org.openmodelica.crml.model.metamodel.Variable;
import org.openmodelica.crml.model.metamodel.VariableReference;

/**
 * Builds CRML model fragments with {@link CRMLFactory}, as the parser would,
 * for the type inference tests. Variables are declared in one {@link Model},
 * so that they are contained as in a parsed model.
 */
class Crml {

    static final CRMLFactory F = CRMLFactory.eINSTANCE;

    final Model model = F.createModel();

    Crml() {
        model.setName("M");
    }

    // ---------------------------------------------------------------------
    // Declarations
    // ---------------------------------------------------------------------

    /** {@code T name is external;} */
    Variable external(String name, BuiltinType type) {
        return declare(name, builtin(type), false, null);
    }

    /** {@code T name is definition;} */
    Variable variable(String name, BuiltinType type, Value definition) {
        return declare(name, builtin(type), false, definition);
    }

    /** A collection variable of element type {@code type}: {@code T name is definition;} */
    Variable collection(String name, BuiltinType type, Value definition) {
        return declare(name, builtin(type), true, definition);
    }

    /** A variable with no domain. */
    Variable untyped(String name) {
        return declare(name, null, false, null);
    }

    private Variable declare(String name, BuiltinTypeReference domain, boolean collection, Value definition) {
        Variable variable = F.createVariable();
        variable.setName(name);
        variable.setDomain(domain);
        variable.setCollection(collection);
        variable.setDefinition(definition);
        variable.setExternal(definition == null);
        model.getVariables().add(variable);
        return variable;
    }

    /** {@code Template ... = ...}: the parser gives a template a Boolean domain. */
    Template template(String name) {
        Template template = F.createTemplate();
        template.setName(name);
        template.setDomain(builtin(BuiltinType.BOOLEAN));
        model.getOperators().add(template);
        return template;
    }

    /** {@code Operator [type] ...}. */
    UserOperator operator(String name, BuiltinType type) {
        UserOperator operator = F.createUserOperator();
        operator.setName(name);
        operator.setDomain(builtin(type));
        model.getOperators().add(operator);
        return operator;
    }

    // ---------------------------------------------------------------------
    // Values
    // ---------------------------------------------------------------------

    static BuiltinTypeReference builtin(BuiltinType type) {
        BuiltinTypeReference reference = F.createBuiltinTypeReference();
        reference.setBuiltinType(type);
        return reference;
    }

    static BooleanConstant bool(boolean value) {
        BooleanConstant constant = F.createBooleanConstant();
        constant.setValue(value ? BooleanLiteral.TRUE : BooleanLiteral.FALSE);
        return constant;
    }

    static IntegerConstant integer(int value) {
        IntegerConstant constant = F.createIntegerConstant();
        constant.setValue(value);
        return constant;
    }

    static RealConstant real(double value) {
        RealConstant constant = F.createRealConstant();
        constant.setValue(value);
        constant.setLiteral(Double.toString(value));
        return constant;
    }

    static StringConstant string(String value) {
        StringConstant constant = F.createStringConstant();
        constant.setRawString(value);
        return constant;
    }

    static VariableReference ref(Variable variable) {
        VariableReference reference = F.createVariableReference();
        reference.setVariable(variable);
        return reference;
    }

    static UnaryOperator unary(BuiltinUnaryOperatorKind kind, Value operand) {
        UnaryOperator operator = F.createUnaryOperator();
        operator.setOptype(kind);
        operator.setValue(operand);
        return lift(operator, operand);
    }

    static BinaryOperator binary(BuiltinBinaryOperatorKind kind, Value lhs, Value rhs) {
        BinaryOperator operator = F.createBinaryOperator();
        operator.setOptype(kind);
        operator.setLhs(lhs);
        operator.setRhs(rhs);
        return lift(operator, lhs, rhs);
    }

    /** An aggregator: a binary operator with no left operand. */
    static BinaryOperator aggregate(BuiltinBinaryOperatorKind kind, Value operand) {
        return binary(kind, null, operand);
    }

    /** A call to a user operator or template, with the given arguments. */
    static ComputedValue call(CustomOperator operator, Value... arguments) {
        ComputedValue call = F.createComputedValue();
        call.setOperator(operator);
        for (Value argument : arguments) {
            org.openmodelica.crml.model.metamodel.Binding binding = F.createBinding();
            binding.setValue(argument);
            call.getBindings().add(binding);
        }
        return lift(call, arguments);
    }

    /**
     * {@code data.element}. Used as an operand of {@link #unary}, {@link #binary}
     * or {@link #call}, it becomes a lift of that application, as the linker does.
     */
    static ScatteredValue element(Value data) {
        ScatteredValue element = F.createScatteredValue();
        element.setData(data);
        return element;
    }

    /** The operands that are {@code .element} lift the application. */
    private static <T extends Liftable> T lift(T application, Value... operands) {
        for (Value operand : operands) {
            if (operand instanceof ScatteredValue) {
                ((ScatteredValue) operand).setMethod(application);
            }
        }
        return application;
    }

    /** {@code { elements }}. */
    static Set<Value> set(Value... elements) {
        Set<Value> set = F.createSet();
        for (Value element : elements) {
            set.getElements().add(element);
        }
        return set;
    }

    /** A set with a declared element domain. */
    static Set<Value> set(BuiltinType domain, Value... elements) {
        Set<Value> set = set(elements);
        set.setDomain(builtin(domain));
        return set;
    }

    static PeriodsValue period(Value start, Value end) {
        PeriodsValue period = F.createPeriodsValue();
        period.setStartValue(start);
        period.setEndValue(end);
        return period;
    }

    static IntegrateValue integrate(Value integrand, Value interval) {
        IntegrateValue integrate = F.createIntegrateValue();
        integrate.setIntegrand(integrand);
        integrate.setInterval(interval);
        return integrate;
    }

    static DurationValue duration(Value condition, Value interval) {
        DurationValue duration = F.createDurationValue();
        duration.setExp1(condition);
        duration.setExp2(interval);
        return duration;
    }

    static ProjectionValue project(Value projected, Value clock) {
        ProjectionValue projection = F.createProjectionValue();
        projection.setP1(projected);
        projection.setP2(clock);
        return projection;
    }

    static IfValue ifThenElse(Value condition, Value then, Value otherwise) {
        IfValue value = F.createIfValue();
        value.setCondition(condition);
        value.setThen(then);
        value.setOthervise(otherwise);
        return value;
    }

    static ConstructorValue construct(BuiltinType type, Value value) {
        ConstructorValue constructor = F.createConstructorValue();
        constructor.setDomain(builtin(type));
        constructor.setValue(value);
        return constructor;
    }
}
