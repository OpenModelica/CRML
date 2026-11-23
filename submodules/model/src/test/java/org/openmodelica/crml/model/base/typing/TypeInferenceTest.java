package org.openmodelica.crml.model.base.typing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.openmodelica.crml.model.base.typing.Crml.aggregate;
import static org.openmodelica.crml.model.base.typing.Crml.binary;
import static org.openmodelica.crml.model.base.typing.Crml.bool;
import static org.openmodelica.crml.model.base.typing.Crml.call;
import static org.openmodelica.crml.model.base.typing.Crml.construct;
import static org.openmodelica.crml.model.base.typing.Crml.duration;
import static org.openmodelica.crml.model.base.typing.Crml.element;
import static org.openmodelica.crml.model.base.typing.Crml.ifThenElse;
import static org.openmodelica.crml.model.base.typing.Crml.integer;
import static org.openmodelica.crml.model.base.typing.Crml.integrate;
import static org.openmodelica.crml.model.base.typing.Crml.period;
import static org.openmodelica.crml.model.base.typing.Crml.project;
import static org.openmodelica.crml.model.base.typing.Crml.real;
import static org.openmodelica.crml.model.base.typing.Crml.ref;
import static org.openmodelica.crml.model.base.typing.Crml.set;
import static org.openmodelica.crml.model.base.typing.Crml.string;
import static org.openmodelica.crml.model.base.typing.Crml.unary;
import static org.openmodelica.crml.model.metamodel.BuiltinType.BOOLEAN;
import static org.openmodelica.crml.model.metamodel.BuiltinType.CLOCK;
import static org.openmodelica.crml.model.metamodel.BuiltinType.EVENT;
import static org.openmodelica.crml.model.metamodel.BuiltinType.INTEGER;
import static org.openmodelica.crml.model.metamodel.BuiltinType.PERIOD;
import static org.openmodelica.crml.model.metamodel.BuiltinType.PERIODS;
import static org.openmodelica.crml.model.metamodel.BuiltinType.REAL;
import static org.openmodelica.crml.model.metamodel.BuiltinType.STRING;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openmodelica.crml.model.base.typing.operators.util.SignatureTable;
import org.openmodelica.crml.model.metamodel.BinaryOperator;
import org.openmodelica.crml.model.metamodel.BuiltinBinaryOperatorKind;
import org.openmodelica.crml.model.metamodel.BuiltinType;
import org.openmodelica.crml.model.metamodel.BuiltinUnaryOperatorKind;
import org.openmodelica.crml.model.metamodel.IndirectTypeReference;
import org.openmodelica.crml.model.metamodel.SequenceKeyword;
import org.openmodelica.crml.model.metamodel.UserTypereference;
import org.openmodelica.crml.model.metamodel.Value;
import org.openmodelica.crml.model.metamodel.Variable;
import org.openmodelica.crml.testutil.ReportedTest;

/**
 * {@code Value.inferType()} and {@code Value.inferSize()} on models built with
 * the EMF factory.
 *
 * <p>The expected types and sizes are written out by hand from the CRML
 * semantics and the reference sheet {@code typeinference.csv}; none is
 * computed with the inference code or its tables.
 */
public class TypeInferenceTest extends ReportedTest {

    /** Size of a single value. */
    private static final Integer ONE = 1;
    /** No computable size. */
    private static final Integer NONE = null;

    private static final ValueType UNKNOWN = ValueType.UNKNOWN;

    private static ValueType single(BuiltinType type) {
        return ValueType.of(type);
    }

    private static ValueType setOf(BuiltinType type) {
        return ValueType.setOf(type);
    }

    // Short names for the operator kinds; unary and binary share some names.
    private static final BuiltinUnaryOperatorKind U_NEG = BuiltinUnaryOperatorKind.SUB;
    private static final BuiltinBinaryOperatorKind ADD = BuiltinBinaryOperatorKind.ADD;
    private static final BuiltinBinaryOperatorKind MUL = BuiltinBinaryOperatorKind.MUL;

    // ---------------------------------------------------------------------
    // Cases
    // ---------------------------------------------------------------------

    /**
     * A fixture: a model with variables of every type the cases refer to.
     * Each case gets a fresh one.
     */
    static final class Fixture extends Crml {
        final Variable x = external("x", REAL);
        final Variable n = external("n", INTEGER);
        final Variable b = external("b", BOOLEAN);
        final Variable c = external("c", CLOCK);
        final Variable e1 = external("e1", EVENT);
        final Variable e2 = external("e2", EVENT);
        final Variable p1 = external("P1", PERIOD);
        final Variable p = external("P", PERIODS);
        final Variable u = untyped("u");
        final Variable xs = collection("xs", INTEGER, set(integer(1), integer(2), integer(3)));
        final Variable ws = collection("ws", REAL, set(real(1.0), real(2.0)));
        final Variable ys = collection("ys", REAL, null);
    }

    private static final List<Arguments> CASES = new ArrayList<>();

    private static void add(String expression, Function<Fixture, Value> build, ValueType type, Integer size) {
        CASES.add(Arguments.of(expression, build, type, size));
    }

    static {
        // Grounded in constants
        add("true", f -> bool(true), single(BOOLEAN), ONE);
        add("42", f -> integer(42), single(INTEGER), ONE);
        add("4.2", f -> real(4.2), single(REAL), ONE);
        add("\"text\"", f -> string("text"), single(STRING), ONE);
        add("time", f -> Crml.F.createTimeValue(), single(REAL), ONE);

        // Grounded in declared domains
        add("x  (Real x is external)", f -> ref(f.x), single(REAL), ONE);
        add("P  (Periods P is external)", f -> ref(f.p), single(PERIODS), 50);
        add("xs  (Integer collection xs is {1, 2, 3})", f -> ref(f.xs), setOf(INTEGER), 3);
        add("ys  (Real collection ys is external)", f -> ref(f.ys), setOf(REAL), NONE);
        add("u  (variable without a domain)", f -> ref(f.u), UNKNOWN, NONE);
        add("new Event b", f -> construct(EVENT, ref(f.b)), single(EVENT), ONE);

        // Grounded in operator result types
        add("'holds' x  (Template)", f -> call(f.template("holds"), ref(f.x)), single(BOOLEAN), ONE);
        add("'scale' x  (Operator [Real])", f -> call(f.operator("scale", REAL), ref(f.x)), single(REAL), ONE);
        add("call of an unresolved operator", f -> call(null, ref(f.x)), UNKNOWN, NONE);

        // Unary operators
        add("-1", f -> unary(U_NEG, integer(1)), single(INTEGER), ONE);
        add("-1.5", f -> unary(U_NEG, real(1.5)), single(REAL), ONE);
        add("not true", f -> unary(BuiltinUnaryOperatorKind.NOT, bool(true)), single(BOOLEAN), ONE);
        add("not 1", f -> unary(BuiltinUnaryOperatorKind.NOT, integer(1)), UNKNOWN, NONE);
        add("sin 1", f -> unary(BuiltinUnaryOperatorKind.SIN, integer(1)), single(REAL), ONE);
        add("exp 2.0", f -> unary(BuiltinUnaryOperatorKind.EXP_OP, real(2.0)), single(REAL), ONE);
        add("(2)", f -> unary(BuiltinUnaryOperatorKind.SUBEXPRESSION, integer(2)), single(INTEGER), ONE);
        add("card c", f -> unary(BuiltinUnaryOperatorKind.CARD, ref(f.c)), single(INTEGER), ONE);
        add("card xs", f -> unary(BuiltinUnaryOperatorKind.CARD, ref(f.xs)), single(INTEGER), ONE);
        add("P1 start", f -> unary(BuiltinUnaryOperatorKind.START, ref(f.p1)), single(EVENT), ONE);
        add("P1 end", f -> unary(BuiltinUnaryOperatorKind.END, ref(f.p1)), single(EVENT), ONE);
        add("tick c", f -> unary(BuiltinUnaryOperatorKind.TICK, ref(f.c)), single(EVENT), ONE);
        add("- xs  (element-wise)", f -> unary(U_NEG, ref(f.xs)), setOf(INTEGER), 3);
        add("pre x  (not typed yet)", f -> unary(BuiltinUnaryOperatorKind.PRE, ref(f.x)), UNKNOWN, NONE);
        add("not u  (unknown operand)", f -> unary(BuiltinUnaryOperatorKind.NOT, ref(f.u)), UNKNOWN, NONE);

        // Binary operators
        add("1 + 2", f -> binary(ADD, integer(1), integer(2)), single(INTEGER), ONE);
        add("1 + 2.5", f -> binary(ADD, integer(1), real(2.5)), single(REAL), ONE);
        add("2.5 * 2", f -> binary(MUL, real(2.5), integer(2)), single(REAL), ONE);
        add("7 / 2  (Real, as in Modelica)", f -> binary(BuiltinBinaryOperatorKind.DIV, integer(7), integer(2)),
            single(REAL), ONE);
        add("2 ^ 3  (Real, as in Modelica)", f -> binary(BuiltinBinaryOperatorKind.POW, integer(2), integer(3)),
            single(REAL), ONE);
        add("2 ^ 0.5", f -> binary(BuiltinBinaryOperatorKind.POW, integer(2), real(0.5)), single(REAL), ONE);
        add("n mod 2", f -> binary(BuiltinBinaryOperatorKind.MOD, ref(f.n), integer(2)), single(INTEGER), ONE);
        add("\"a\" + 1", f -> binary(ADD, string("a"), integer(1)), single(STRING), ONE);
        add("1.5 + \"a\"", f -> binary(ADD, real(1.5), string("a")), single(STRING), ONE);
        add("true + false", f -> binary(ADD, bool(true), bool(false)), single(BOOLEAN), ONE);
        add("c + 1.5", f -> binary(ADD, ref(f.c), real(1.5)), single(CLOCK), ONE);
        add("P1 + 1.5", f -> binary(ADD, ref(f.p1), real(1.5)), single(CLOCK), ONE);
        add("1 < 2.5", f -> binary(BuiltinBinaryOperatorKind.LT, integer(1), real(2.5)), single(BOOLEAN), ONE);
        add("e1 <= e2", f -> binary(BuiltinBinaryOperatorKind.LE, ref(f.e1), ref(f.e2)), single(BOOLEAN), ONE);
        add("x == P1", f -> binary(BuiltinBinaryOperatorKind.EQ, ref(f.x), ref(f.p1)), single(BOOLEAN), ONE);
        add("x > P1  (not in the reference table)", f -> binary(BuiltinBinaryOperatorKind.GT, ref(f.x), ref(f.p1)),
            UNKNOWN, NONE);
        add("e1 == e2  (not in the reference table)", f -> binary(BuiltinBinaryOperatorKind.EQ, ref(f.e1),
            ref(f.e2)), UNKNOWN, NONE);
        add("true and b", f -> binary(BuiltinBinaryOperatorKind.AND, bool(true), ref(f.b)), single(BOOLEAN), ONE);
        add("b or false", f -> binary(BuiltinBinaryOperatorKind.OR, ref(f.b), bool(false)), single(BOOLEAN), ONE);
        add("true and 1", f -> binary(BuiltinBinaryOperatorKind.AND, bool(true), integer(1)), UNKNOWN, NONE);
        add("b and e1", f -> binary(BuiltinBinaryOperatorKind.AND, ref(f.b), ref(f.e1)), single(BOOLEAN), ONE);
        add("e1 or b  (commuted)", f -> binary(BuiltinBinaryOperatorKind.OR, ref(f.e1), ref(f.b)),
            single(BOOLEAN), ONE);
        add("e1 and e2", f -> binary(BuiltinBinaryOperatorKind.AND, ref(f.e1), ref(f.e2)), single(CLOCK), ONE);
        add("e1 or e2", f -> binary(BuiltinBinaryOperatorKind.OR, ref(f.e1), ref(f.e2)), single(CLOCK), ONE);
        add("c and c", f -> binary(BuiltinBinaryOperatorKind.AND, ref(f.c), ref(f.c)), single(CLOCK), ONE);
        add("e1 and c", f -> binary(BuiltinBinaryOperatorKind.AND, ref(f.e1), ref(f.c)), single(CLOCK), ONE);
        add("c and e1  (commuted)", f -> binary(BuiltinBinaryOperatorKind.AND, ref(f.c), ref(f.e1)),
            single(CLOCK), ONE);
        add("c or e1", f -> binary(BuiltinBinaryOperatorKind.OR, ref(f.c), ref(f.e1)), single(CLOCK), ONE);
        add("b and c  (not defined)", f -> binary(BuiltinBinaryOperatorKind.AND, ref(f.b), ref(f.c)), UNKNOWN, NONE);
        add("c filter b", f -> binary(BuiltinBinaryOperatorKind.FILTER, ref(f.c), ref(f.b)), single(CLOCK), ONE);
        add("x at e1", f -> binary(BuiltinBinaryOperatorKind.AT, ref(f.x), ref(f.e1)), single(REAL), ONE);
        add("b with b  (not typed yet)", f -> binary(BuiltinBinaryOperatorKind.WITH, ref(f.b), ref(f.b)), UNKNOWN, NONE);
        add("x + u  (unknown operand)", f -> binary(ADD, ref(f.x), ref(f.u)), UNKNOWN, NONE);
        add("xs + 1  (set operand, not lifted)", f -> binary(ADD, ref(f.xs), integer(1)), UNKNOWN, NONE);

        // Aggregators
        add("and {true, false}", f -> aggregate(BuiltinBinaryOperatorKind.AND, set(bool(true), bool(false))),
            single(BOOLEAN), ONE);
        add("+ {1, 2, 3}", f -> aggregate(ADD, set(integer(1), integer(2), integer(3))), single(INTEGER), ONE);
        add("< {1, 2}  (result is not an element)", f -> aggregate(BuiltinBinaryOperatorKind.LT,
            set(integer(1), integer(2))), UNKNOWN, NONE);
        add("and b  (not a set)", f -> aggregate(BuiltinBinaryOperatorKind.AND, ref(f.b)), UNKNOWN, NONE);

        // Set literals
        add("{1, 2, 3}", f -> set(integer(1), integer(2), integer(3)), setOf(INTEGER), 3);
        add("{1, 2.5}  (Integer converted to Real)", f -> set(integer(1), real(2.5)), setOf(REAL), 2);
        add("{1, true}  (no common type)", f -> set(integer(1), bool(true)), UNKNOWN, NONE);
        add("{}", f -> set(), UNKNOWN, NONE);
        add("{} of Real  (declared domain)", f -> set(REAL), setOf(REAL), 0);

        // .element and lifting
        add("xs.element  (no application)", f -> element(ref(f.xs)), setOf(INTEGER), 3);
        add("P.element  (no application)", f -> element(ref(f.p)), setOf(PERIOD), 50);
        add("xs.element + 1", f -> binary(ADD, element(ref(f.xs)), integer(1)), setOf(INTEGER), 3);
        add("xs.element * ws.element", f -> binary(MUL, element(ref(f.xs)), element(ref(f.ws))), setOf(REAL), 6);
        add("P.element end", f -> unary(BuiltinUnaryOperatorKind.END, element(ref(f.p))), setOf(EVENT), 50);
        add("'holds' xs.element", f -> call(f.template("holds"), element(ref(f.xs))), setOf(BOOLEAN), 3);
        add("(xs.element + 1).element * 2", f -> binary(MUL,
            element(binary(ADD, element(ref(f.xs)), integer(1))), integer(2)), setOf(INTEGER), 3);
        add("ys.element + 1  (external collection)", f -> binary(ADD, element(ref(f.ys)), integer(1)),
            setOf(REAL), NONE);
        add("x.element + 1  (x is not a collection)", f -> binary(ADD, element(ref(f.x)), integer(1)),
            UNKNOWN, NONE);

        // Sizes through definitions
        add("zs  (Integer collection zs is xs)", f -> ref(f.collection("zs", INTEGER, ref(f.xs))), setOf(INTEGER), 3);
        add("a  (a is b, b is a: a cycle)", f -> {
            Variable a = f.collection("a", INTEGER, null);
            Variable b = f.collection("b2", INTEGER, ref(a));
            a.setDefinition(ref(b));
            return ref(a);
        }, setOf(INTEGER), NONE);

        // Periods
        add("[e1, e2]", f -> period(ref(f.e1), ref(f.e2)), single(PERIOD), ONE);
        add("[b, b]  (Booleans become Events)", f -> period(ref(f.b), ref(f.b)), single(PERIOD), ONE);
        add("[c, c]  (Clocks)", f -> period(ref(f.c), ref(f.c)), single(PERIODS), 50);
        add("[e1, c]", f -> period(ref(f.e1), ref(f.c)), UNKNOWN, NONE);

        // Time operators
        add("integrate x on P1", f -> integrate(ref(f.x), ref(f.p1)), single(REAL), ONE);
        add("integrate 1 on P", f -> integrate(integer(1), ref(f.p)), single(REAL), ONE);
        add("integrate b on P1", f -> integrate(ref(f.b), ref(f.p1)), UNKNOWN, NONE);
        add("duration b on P1", f -> duration(ref(f.b), ref(f.p1)), single(REAL), ONE);
        add("e1 proj c", f -> project(ref(f.e1), ref(f.c)), single(EVENT), ONE);
        add("c proj c", f -> project(ref(f.c), ref(f.c)), single(CLOCK), ONE);

        // Conditionals
        add("if b then 1 else 2", f -> ifThenElse(ref(f.b), integer(1), integer(2)), single(INTEGER), ONE);
        add("if b then 1 else 2.5  (Integer converted to Real)", f -> ifThenElse(ref(f.b), integer(1), real(2.5)),
            single(REAL), ONE);
        add("if b then 1 else true  (no common type)", f -> ifThenElse(ref(f.b), integer(1), bool(true)),
            UNKNOWN, NONE);
        add("if b then 1 else {1}  (single and set)", f -> ifThenElse(ref(f.b), integer(1), set(integer(1))),
            UNKNOWN, NONE);
        add("if b then {1} else {2.5, 3.5}", f -> ifThenElse(ref(f.b), set(integer(1)),
            set(real(2.5), real(3.5))), setOf(REAL), 2);
        add("if 1 then 1 else 2", f -> ifThenElse(integer(1), integer(1), integer(2)), UNKNOWN, NONE);
        add("if b then {1, 2} else {1, 2, 3}", f -> ifThenElse(ref(f.b), set(integer(1), integer(2)),
            set(integer(1), integer(2), integer(3))), setOf(INTEGER), 3);

        // Left by the parser
        add("'some' 'words'  (unresolved Sequence)", f -> {
            SequenceKeyword sequence = Crml.F.createSequenceKeyword();
            sequence.setKeyword("some");
            return sequence;
        }, UNKNOWN, NONE);
    }

    static List<Arguments> cases() {
        return CASES;
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("cases")
    void infers(String expression, Function<Fixture, Value> build, ValueType expectedType, Integer expectedSize) {
        Value value = build.apply(new Fixture());

        ValueType inferredType = value.inferType();
        Integer inferredSize = value.inferSize();
        emit(new InferenceReport(expression, expectedType, inferredType, expectedSize, inferredSize), "Inference");

        assertEquals(expectedType, inferredType, "type of " + expression);
        assertEquals(expectedSize, inferredSize, "size of " + expression);
        assertEquals(expectedType.isSet(), value.isCollection(), "isCollection of " + expression);
    }

    // ---------------------------------------------------------------------
    // Type references, variables and the metamodel operations
    // ---------------------------------------------------------------------

    @Test
    void typeReferences() {
        IndirectTypeReference indirect = Crml.F.createIndirectTypeReference();
        indirect.setReferredType(Crml.builtin(INTEGER));
        assertEquals(single(INTEGER), indirect.toType(), "an indirect reference to Integer");

        IndirectTypeReference loop = Crml.F.createIndirectTypeReference();
        loop.setReferredType(loop);
        assertEquals(UNKNOWN, loop.toType(), "a cycle of indirect references");

        UserTypereference user = Crml.F.createUserTypereference();
        user.setDomain(Crml.F.createClass());
        assertEquals(UNKNOWN, user.toType(), "a class type is not supported yet");
    }

    @Test
    void declaredTypes() {
        Fixture f = new Fixture();
        assertEquals(single(REAL), f.x.declaredType());
        assertEquals(setOf(INTEGER), f.xs.declaredType());
        assertEquals(UNKNOWN, f.u.declaredType());
    }

    @Test
    void aggregators() {
        BinaryOperator folded = aggregate(BuiltinBinaryOperatorKind.AND, set(bool(true), bool(false)));
        assertTrue(folded.isAggregator());
        assertFalse(binary(BuiltinBinaryOperatorKind.AND, bool(true), bool(false)).isAggregator());
        assertFalse(aggregate(BuiltinBinaryOperatorKind.AND, bool(true)).isAggregator());
    }

    @Test
    void valueTypes() {
        assertEquals("INTEGER", single(INTEGER).toString());
        assertEquals("set of REAL", setOf(REAL).toString());
        assertEquals("unknown", UNKNOWN.toString());
        assertTrue(UNKNOWN.isUnknown());
        assertFalse(UNKNOWN.isSet());
        assertFalse(UNKNOWN.isSingle());
        assertEquals(UNKNOWN, UNKNOWN.asSet());
        assertEquals(setOf(REAL), single(REAL).asSet());
    }

    @Test
    void conflictingRowsAreRejected() {
        SignatureTable.Builder table = SignatureTable.builder(1)
            .row(INTEGER, SignatureTable.oneOf(INTEGER, REAL));
        assertThrows(IllegalStateException.class, () -> table.row(REAL, SignatureTable.oneOf(REAL)));
    }
}
