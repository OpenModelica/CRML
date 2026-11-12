package org.openmodelica.crml.modelica.build;

import java.util.Arrays;
import java.util.List;

import org.eclipse.emf.ecore.util.EcoreUtil;

import org.openmodelica.crml.modelica.metamodel.ArrayConstructor;
import org.openmodelica.crml.modelica.metamodel.ArrayDimension;
import org.openmodelica.crml.modelica.metamodel.BinaryExpression;
import org.openmodelica.crml.modelica.metamodel.BinaryOperatorKind;
import org.openmodelica.crml.modelica.metamodel.BooleanLiteral;
import org.openmodelica.crml.modelica.metamodel.Causality;
import org.openmodelica.crml.modelica.metamodel.ClassDefinition;
import org.openmodelica.crml.modelica.metamodel.ClassKind;
import org.openmodelica.crml.modelica.metamodel.ComponentDeclaration;
import org.openmodelica.crml.modelica.metamodel.ComponentReference;
import org.openmodelica.crml.modelica.metamodel.Equation;
import org.openmodelica.crml.modelica.metamodel.Expression;
import org.openmodelica.crml.modelica.metamodel.ExtendsClause;
import org.openmodelica.crml.modelica.metamodel.ForEquation;
import org.openmodelica.crml.modelica.metamodel.FunctionCall;
import org.openmodelica.crml.modelica.metamodel.IfExpression;
import org.openmodelica.crml.modelica.metamodel.IntegerLiteral;
import org.openmodelica.crml.modelica.metamodel.ModelicaElement;
import org.openmodelica.crml.modelica.metamodel.ModelicaFactory;
import org.openmodelica.crml.modelica.metamodel.ModificationElement;
import org.openmodelica.crml.modelica.metamodel.ParenthesizedExpression;
import org.openmodelica.crml.modelica.metamodel.Placeholder;
import org.openmodelica.crml.modelica.metamodel.RawReason;
import org.openmodelica.crml.modelica.metamodel.RealLiteral;
import org.openmodelica.crml.modelica.metamodel.Reference;
import org.openmodelica.crml.modelica.metamodel.ReferencePart;
import org.openmodelica.crml.modelica.metamodel.SimpleEquation;
import org.openmodelica.crml.modelica.metamodel.Statement;
import org.openmodelica.crml.modelica.metamodel.StringLiteral;
import org.openmodelica.crml.modelica.metamodel.UnaryExpression;
import org.openmodelica.crml.modelica.metamodel.UnaryOperatorKind;
import org.openmodelica.crml.modelica.metamodel.Variability;
import org.openmodelica.crml.modelica.metamodel.Visibility;

/**
 * Hand-written construction helpers for the Modelica object model. Everything
 * here is a thin wrapper over {@link ModelicaFactory}; the point is that a
 * transformation reads as Modelica rather than as EMF plumbing.
 *
 * <p>Implement this interface to use the helpers unqualified; every method is a
 * default, so an implementor can override any of them.
 */
public interface Modelica {

    ModelicaFactory FACTORY = ModelicaFactory.eINSTANCE;

    // --- references ---------------------------------------------------------

    /**
     * A reference to an element of this tree. There is deliberately no overload
     * taking a name: a resolved reference can only be built from an object
     * already in hand.
     */
    default <T extends ModelicaElement> Reference<T> resolvedRef(T target) {
        Reference<T> reference = FACTORY.createReference();
        reference.setTarget(target);
        return reference;
    }

    /** A reference to something outside this tree. The reason is not optional. */
    default <T extends ModelicaElement> Reference<T> rawRef(String text, RawReason reason) {
        Reference<T> reference = FACTORY.createReference();
        reference.setRawText(text);
        reference.setReason(reason);
        return reference;
    }

    /** A member of CRMLtoModelica.mo. */
    default <T extends ModelicaElement> Reference<T> libraryRef(String name) {
        return rawRef(name, RawReason.EXTERNAL_LIBRARY);
    }

    /** Part of the Modelica language itself. */
    default <T extends ModelicaElement> Reference<T> builtinRef(String name) {
        return rawRef(name, RawReason.MODELICA_BUILTIN);
    }

    /** A member of the Modelica Standard Library. */
    default <T extends ModelicaElement> Reference<T> mslRef(String name) {
        return rawRef(name, RawReason.STANDARD_LIBRARY);
    }

    // --- classes ------------------------------------------------------------

    default ClassDefinition classDefinition(ClassKind kind, String name) {
        ClassDefinition cls = FACTORY.createClassDefinition();
        cls.setKind(kind);
        cls.setName(name);
        return cls;
    }

    default ClassDefinition model(String name) {
        return classDefinition(ClassKind.MODEL, name);
    }

    default ClassDefinition record(String name) {
        return classDefinition(ClassKind.RECORD, name);
    }

    default ClassDefinition block(String name) {
        return classDefinition(ClassKind.BLOCK, name);
    }

    default ClassDefinition function(String name) {
        return classDefinition(ClassKind.FUNCTION, name);
    }

    default ClassDefinition packageDefinition(String name) {
        return classDefinition(ClassKind.PACKAGE, name);
    }

    default ExtendsClause extendsClause(Reference<ClassDefinition> superClass) {
        ExtendsClause clause = FACTORY.createExtendsClause();
        clause.setSuperClass(superClass);
        return clause;
    }

    default Placeholder placeholder(String message) {
        Placeholder placeholder = FACTORY.createPlaceholder();
        placeholder.setMessage(message);
        return placeholder;
    }

    // --- components ---------------------------------------------------------

    default ComponentDeclaration component(Reference<ClassDefinition> declaredType, String name) {
        ComponentDeclaration component = FACTORY.createComponentDeclaration();
        component.setDeclaredType(declaredType);
        component.setName(name);
        component.setVisibility(Visibility.PUBLIC);
        component.setCausality(Causality.NONE);
        return component;
    }

    default ComponentDeclaration component(Reference<ClassDefinition> declaredType, String name,
            Expression binding) {
        ComponentDeclaration component = component(declaredType, name);
        component.setBinding(binding);
        return component;
    }

    /** {@code parameter Type name = binding}. */
    default ComponentDeclaration parameter(Reference<ClassDefinition> declaredType, String name,
            Expression binding) {
        ComponentDeclaration component = component(declaredType, name, binding);
        component.setVariability(Variability.PARAMETER);
        return component;
    }

    default ComponentDeclaration input(Reference<ClassDefinition> declaredType, String name) {
        ComponentDeclaration component = component(declaredType, name);
        component.setCausality(Causality.INPUT);
        return component;
    }

    default ComponentDeclaration output(Reference<ClassDefinition> declaredType, String name) {
        ComponentDeclaration component = component(declaredType, name);
        component.setCausality(Causality.OUTPUT);
        return component;
    }

    default ModificationElement mod(String name, Expression value) {
        ModificationElement modification = FACTORY.createModificationElement();
        modification.setName(name);
        modification.setValue(value);
        return modification;
    }

    /** An array dimension of the given size; a null size prints as ":". */
    default ArrayDimension dimension(Expression size) {
        ArrayDimension dimension = FACTORY.createArrayDimension();
        dimension.setSize(size);
        return dimension;
    }

    // --- equations and statements -------------------------------------------

    default SimpleEquation eq(Expression lhs, Expression rhs) {
        SimpleEquation equation = FACTORY.createSimpleEquation();
        equation.setLhs(lhs);
        equation.setRhs(rhs);
        return equation;
    }

    /** {@code for iterator in start:stop loop body end for}. */
    default ForEquation forEq(String iterator, Expression start, Expression stop,
            List<? extends Equation> body) {
        ForEquation loop = FACTORY.createForEquation();
        loop.setIterator(iterator);
        loop.setStart(start);
        loop.setStop(stop);
        loop.getEquations().addAll(body);
        return loop;
    }

    default Statement assign(ComponentReference target, Expression value) {
        Statement statement = FACTORY.createStatement();
        statement.setTarget(target);
        statement.setValue(value);
        return statement;
    }

    // --- expressions --------------------------------------------------------

    /** A dotted component reference: {@code ref("a.b.c")}. */
    default ComponentReference ref(String dottedName) {
        ComponentReference reference = FACTORY.createComponentReference();
        for (String part : dottedName.split("\\.")) {
            reference.getParts().add(part(part));
        }
        return reference;
    }

    /**
     * Copy of base with subscripts added to its last part: {@code a -> a[i]},
     * {@code a.b -> a.b[i]}. The base is left untouched; the subscripts become
     * part of the copy.
     */
    default ComponentReference subscripted(ComponentReference base, Expression... subscripts) {
        ComponentReference reference = EcoreUtil.copy(base);
        List<ReferencePart> parts = reference.getParts();
        parts.get(parts.size() - 1).getSubscripts().addAll(Arrays.asList(subscripts));
        return reference;
    }

    /** Copy of base with a member appended: {@code a[i] -> a[i].x}. The base is left untouched. */
    default ComponentReference member(ComponentReference base, String member) {
        ComponentReference reference = EcoreUtil.copy(base);
        reference.getParts().add(part(member));
        return reference;
    }

    default ReferencePart part(String name, Expression... subscripts) {
        ReferencePart part = FACTORY.createReferencePart();
        part.setName(name);
        part.getSubscripts().addAll(Arrays.asList(subscripts));
        return part;
    }

    default FunctionCall call(Reference<ClassDefinition> function, Expression... arguments) {
        FunctionCall call = FACTORY.createFunctionCall();
        call.setFunction(function);
        call.getArguments().addAll(Arrays.asList(arguments));
        return call;
    }

    default FunctionCall call(Reference<ClassDefinition> function, List<? extends Expression> arguments) {
        FunctionCall call = FACTORY.createFunctionCall();
        call.setFunction(function);
        call.getArguments().addAll(arguments);
        return call;
    }

    default BinaryExpression binary(BinaryOperatorKind operator, Expression lhs, Expression rhs) {
        BinaryExpression expression = FACTORY.createBinaryExpression();
        expression.setOperator(operator);
        expression.setLhs(lhs);
        expression.setRhs(rhs);
        return expression;
    }

    default UnaryExpression unary(UnaryOperatorKind operator, Expression operand) {
        UnaryExpression expression = FACTORY.createUnaryExpression();
        expression.setOperator(operator);
        expression.setOperand(operand);
        return expression;
    }

    default IfExpression ifExpr(Expression condition, Expression thenExpression, Expression elseExpression) {
        IfExpression expression = FACTORY.createIfExpression();
        expression.setCondition(condition);
        expression.setThenExpression(thenExpression);
        expression.setElseExpression(elseExpression);
        return expression;
    }

    default ArrayConstructor array(Expression... elements) {
        ArrayConstructor constructor = FACTORY.createArrayConstructor();
        constructor.getElements().addAll(Arrays.asList(elements));
        return constructor;
    }

    default ArrayConstructor array(List<? extends Expression> elements) {
        ArrayConstructor constructor = FACTORY.createArrayConstructor();
        constructor.getElements().addAll(elements);
        return constructor;
    }

    /**
     * Parentheses the CRML author wrote. The printer parenthesises on its own
     * wherever precedence requires it, so do not use this defensively.
     */
    default ParenthesizedExpression parens(Expression inner) {
        ParenthesizedExpression expression = FACTORY.createParenthesizedExpression();
        expression.setInner(inner);
        return expression;
    }

    default IntegerLiteral integer(int value) {
        IntegerLiteral literal = FACTORY.createIntegerLiteral();
        literal.setValue(value);
        return literal;
    }

    default RealLiteral real(double value) {
        RealLiteral literal = FACTORY.createRealLiteral();
        literal.setValue(value);
        return literal;
    }

    /** A Real literal that keeps the spelling it had in the CRML source. */
    default RealLiteral real(String literalText) {
        RealLiteral literal = FACTORY.createRealLiteral();
        literal.setLiteral(literalText);
        return literal;
    }

    default StringLiteral string(String value) {
        StringLiteral literal = FACTORY.createStringLiteral();
        literal.setValue(value);
        return literal;
    }

    default BooleanLiteral bool(boolean value) {
        BooleanLiteral literal = FACTORY.createBooleanLiteral();
        literal.setValue(value);
        return literal;
    }
}
