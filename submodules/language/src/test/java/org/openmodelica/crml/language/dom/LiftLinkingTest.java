package org.openmodelica.crml.language.dom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.junit.jupiter.api.Test;
import org.openmodelica.crml.language.dom.util.BuildResult;
import org.openmodelica.crml.language.util.Parser;
import org.openmodelica.crml.model.metamodel.BinaryOperator;
import org.openmodelica.crml.model.metamodel.BuiltinBinaryOperatorKind;
import org.openmodelica.crml.model.metamodel.BuiltinUnaryOperatorKind;
import org.openmodelica.crml.model.metamodel.ComputedValue;
import org.openmodelica.crml.model.metamodel.Liftable;
import org.openmodelica.crml.model.metamodel.ScatteredValue;
import org.openmodelica.crml.model.metamodel.UnaryOperator;
import org.openmodelica.crml.model.metamodel.Variable;
import org.openmodelica.crml.model.metamodel.VariableReference;

/**
 * Linking of {@code x.element} to the application it lifts
 * ({@link org.openmodelica.crml.language.dom.builders.LiftBuilder#link}): parses CRML text,
 * then runs the linker and the operator-call resolution, as the compiler's
 * front end does.
 */
public class LiftLinkingTest {

    private static final String PRELUDE =
          "Operator [ Boolean ] 'f' Boolean a 'with' Boolean b = a and b;\n"
        + "Operator [ Boolean ] 'g' Boolean a = not a;\n"
        + "Operator [ Boolean ] 'h' Boolean a 'with' Boolean b = a and b;\n"
        + "Boolean b1 is true;\n"
        + "Boolean P1 is {b1};\n"
        + "Boolean P2 is {b1};\n";

    /** Builds a model that defines {@code Boolean r is <expression>;} and returns the object model. */
    private static EObject build(String expression) {
        Parser.ParserResult parsed = new Parser().parse(
            "model M is {\n" + PRELUDE + "Boolean r is " + expression + ";\n};");
        assertFalse(parsed.syntax().hasErrors(), "syntax errors: " + parsed.syntax().errors());
        DOMVisitor visitor = new DOMVisitor();
        BuildResult result = visitor.build(parsed.ast());
        EObject dom = ((BuildResult.SingleBuildResult<?>) result).result();
        visitor.linker();
        visitor.resolveOperatorCalls();
        return dom;
    }

    private static <T> List<T> all(EObject root, Class<T> type) {
        List<T> found = new ArrayList<>();
        TreeIterator<EObject> it = EcoreUtil.getAllContents(root, true);
        while (it.hasNext()) {
            EObject object = it.next();
            if (type.isInstance(object)) {
                found.add(type.cast(object));
            }
        }
        return found;
    }

    /** The {@code name.element} in the model; fails unless there is exactly one. */
    private static ScatteredValue lift(EObject root, String name) {
        ScatteredValue found = null;
        for (ScatteredValue lift : all(root, ScatteredValue.class)) {
            if (lift.getData() instanceof VariableReference) {
                Variable variable = ((VariableReference) lift.getData()).getVariable();
                if (variable != null && name.equals(variable.getName())) {
                    assertNull(found, "more than one " + name + ".element");
                    found = lift;
                }
            }
        }
        assertNotNull(found, "no " + name + ".element in the model");
        return found;
    }

    /** The call to the operator whose first keyword is {@code keyword}. */
    private static ComputedValue call(EObject root, String keyword) {
        ComputedValue found = null;
        for (ComputedValue call : all(root, ComputedValue.class)) {
            if (call.getOperator() != null && !call.getOperator().getKeywords().isEmpty()
                    && call.getOperator().getKeywords().get(0).getKeyword().contains(keyword)) {
                assertNull(found, "more than one call to " + keyword);
                found = call;
            }
        }
        assertNotNull(found, "no call to " + keyword);
        return found;
    }

    private static List<ScatteredValue> lifts(Liftable application) {
        return new ArrayList<>(application.getLifts());
    }

    private static List<ScatteredValue> list(ScatteredValue... lifts) {
        List<ScatteredValue> list = new ArrayList<>();
        for (ScatteredValue lift : lifts) {
            list.add(lift);
        }
        return list;
    }

    /** L1: two lifts of one call are both linked, in formal order. */
    @Test
    void twoLiftsOfOneCall() {
        EObject dom = build("'f' P1.element 'with' P2.element");
        ComputedValue f = call(dom, "f");
        ScatteredValue p1 = lift(dom, "P1");
        ScatteredValue p2 = lift(dom, "P2");
        assertSame(f, p1.getMethod());
        assertSame(f, p2.getMethod());
        assertEquals(list(p1, p2), lifts(f));
    }

    /** L2 (experimental): parentheses around a lift are ignored. */
    @Test
    void parenthesisedLift() {
        EObject dom = build("'f' (P1.element) 'with' P2");
        ComputedValue f = call(dom, "f");
        ScatteredValue p1 = lift(dom, "P1");
        assertSame(f, p1.getMethod());
        assertEquals(list(p1), lifts(f));
        for (UnaryOperator operator : all(dom, UnaryOperator.class)) {
            if (operator.getOptype() == BuiltinUnaryOperatorKind.SUBEXPRESSION) {
                assertTrue(operator.getLifts().isEmpty(), "parentheses never receive lifts");
            }
        }
    }

    /** L3: a lift links to a built-in unary operator. */
    @Test
    void unaryOperator() {
        EObject dom = build("not P1.element");
        ScatteredValue p1 = lift(dom, "P1");
        assertTrue(p1.getMethod() instanceof UnaryOperator, "method: " + p1.getMethod());
        UnaryOperator not = (UnaryOperator) p1.getMethod();
        assertEquals(BuiltinUnaryOperatorKind.NOT, not.getOptype());
        assertEquals(list(p1), lifts(not));
    }

    /** L4: the lifts of a binary operator are in operand order, lhs before rhs. */
    @Test
    void binaryOperatorOrder() {
        EObject dom = build("P2.element < P1.element");
        ScatteredValue p1 = lift(dom, "P1");
        ScatteredValue p2 = lift(dom, "P2");
        assertTrue(p2.getMethod() instanceof BinaryOperator, "method: " + p2.getMethod());
        BinaryOperator less = (BinaryOperator) p2.getMethod();
        assertEquals(BuiltinBinaryOperatorKind.LT, less.getOptype());
        assertSame(less, p1.getMethod());
        assertSame(p2, LiftTestSupport.unparen(less.getLhs()));
        assertSame(p1, LiftTestSupport.unparen(less.getRhs()));
        assertEquals(list(p2, p1), lifts(less));
    }

    /** L5: the operand of an aggregator is a set, not a lift. */
    @Test
    void aggregatorOperand() {
        EObject dom = build("and (P1.element)");
        ScatteredValue p1 = lift(dom, "P1");
        assertNull(p1.getMethod());
        for (Liftable liftable : all(dom, Liftable.class)) {
            assertTrue(liftable.getLifts().isEmpty());
        }
    }

    /** L6 (experimental): no link under a value that is not an application. */
    @Test
    void otherContainers() {
        String[] expressions = {
            "if P1.element then true else false",
            "new Clock P1.element",
            "{P1.element}",
        };
        for (String expression : expressions) {
            EObject dom = build(expression);
            assertNull(lift(dom, "P1").getMethod(), expression);
            for (Liftable liftable : all(dom, Liftable.class)) {
                assertTrue(liftable.getLifts().isEmpty(), expression);
            }
        }
    }

    /** L7: chaining; each lift links to the call it is a direct argument of. */
    @Test
    void chaining() {
        EObject dom = build("'h' ('g' P1.element).element 'with' P2.element");
        ComputedValue g = call(dom, "g");
        ComputedValue h = call(dom, "h");
        ScatteredValue p1 = lift(dom, "P1");
        ScatteredValue p2 = lift(dom, "P2");
        ScatteredValue outer = null;
        for (ScatteredValue lift : all(dom, ScatteredValue.class)) {
            if (lift != p1 && lift != p2) {
                assertNull(outer, "more than three lifts");
                outer = lift;
            }
        }
        assertNotNull(outer);
        assertSame(g, LiftTestSupport.unparen(outer.getData()));
        assertSame(g, p1.getMethod());
        assertEquals(list(p1), lifts(g));
        assertSame(h, outer.getMethod());
        assertSame(h, p2.getMethod());
        assertEquals(list(outer, p2), lifts(h));
    }

    private static final class LiftTestSupport {
        static EObject unparen(EObject value) {
            while (value instanceof UnaryOperator
                    && ((UnaryOperator) value).getOptype() == BuiltinUnaryOperatorKind.SUBEXPRESSION) {
                value = ((UnaryOperator) value).getValue();
            }
            return value;
        }
    }
}
