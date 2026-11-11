package crml.modelica.print;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import crml.model.modelica.BinaryOperatorKind;
import crml.model.modelica.ClassDefinition;
import crml.model.modelica.ComponentDeclaration;
import crml.model.modelica.ComponentReference;
import crml.model.modelica.Expression;
import crml.model.modelica.ForEquation;
import crml.model.modelica.ModelicaElement;
import crml.model.modelica.ModelicaFactory;
import crml.model.modelica.SimpleEquation;
import crml.model.modelica.UnaryOperatorKind;
import crml.model.modelica.Variability;
import crml.model.modelica.Visibility;
import crml.modelica.build.Modelica;

/**
 * Covers each metamodel class at least once, with exact expected text. Uses a
 * fixed "\n" line separator so the expectations are platform-independent.
 */
public class ModelicaPrinterTest implements Modelica {

    private static final PrinterOptions OPTIONS = PrinterOptions.DEFAULT.withLineSeparator("\n");

    private static String print(ModelicaElement node) {
        return ModelicaPrinter.print(node, OPTIONS);
    }

    private static String expr(Expression expression) {
        return print(expression);
    }

    // --- expressions --------------------------------------------------------

    @Test
    public void printsLiterals() {
        assertEquals("3", expr(integer(3)));
        assertEquals("2.5", expr(real(2.5)));
        assertEquals("1e-3", expr(real("1e-3")));
        assertEquals("\"x\"", expr(string("x")));
        assertEquals("\"a\\\"b\"", expr(string("a\"b")));
        assertEquals("true", expr(bool(true)));
        assertEquals("false", expr(bool(false)));
    }

    @Test
    public void printsComponentReferences() {
        assertEquals("a", expr(ref("a")));
        assertEquals("a.b.c", expr(ref("a.b.c")));

        crml.model.modelica.ComponentReference subscripted = ModelicaFactory.eINSTANCE.createComponentReference();
        subscripted.getParts().add(part("ps", integer(1)));
        subscripted.getParts().add(part("period"));
        assertEquals("ps[1].period", expr(subscripted));
    }

    @Test
    public void printsCallsAndConstructors() {
        assertEquals("CRMLtoModelica.Functions.and4(a, b)",
            expr(call(libraryRef("CRMLtoModelica.Functions.and4"), ref("a"), ref("b"))));
        assertEquals("time", expr(ref("time")));
        assertEquals("{a, b, c}", expr(array(ref("a"), ref("b"), ref("c"))));
        assertEquals("(a)", expr(parens(ref("a"))));
    }

    @Test
    public void printsIfExpression() {
        assertEquals("if c == CRMLtoModelica.Types.Boolean4.true4 then a else b",
            expr(ifExpr(binary(BinaryOperatorKind.EQ, ref("c"), ref("CRMLtoModelica.Types.Boolean4.true4")),
                ref("a"), ref("b"))));
    }

    @Test
    public void printsUnaryExpressions() {
        assertEquals("-a", expr(unary(UnaryOperatorKind.MINUS, ref("a"))));
        assertEquals("+a", expr(unary(UnaryOperatorKind.PLUS, ref("a"))));
        // "-a * b" would parse as -(a * b), so the operand of a sign is parenthesised
        // whenever it binds no tighter than a term.
        assertEquals("-(-a)", expr(unary(UnaryOperatorKind.MINUS, unary(UnaryOperatorKind.MINUS, ref("a")))));
    }

    // --- precedence ---------------------------------------------------------

    @Test
    public void leavesTighterOperandsUnparenthesised() {
        // a + b * c
        assertEquals("a + b * c", expr(binary(BinaryOperatorKind.ADD, ref("a"),
            binary(BinaryOperatorKind.MUL, ref("b"), ref("c")))));
    }

    @Test
    public void addsExactlyOnePairWhereNeeded() {
        // (a + b) * c
        assertEquals("(a + b) * c", expr(binary(BinaryOperatorKind.MUL,
            binary(BinaryOperatorKind.ADD, ref("a"), ref("b")), ref("c"))));
    }

    @Test
    public void keepsLeftAssociativeChainsFlat() {
        // (a + b) + c must print as a + b + c
        assertEquals("a + b + c", expr(binary(BinaryOperatorKind.ADD,
            binary(BinaryOperatorKind.ADD, ref("a"), ref("b")), ref("c"))));
        // but a - (b - c) keeps its parentheses
        assertEquals("a - (b - c)", expr(binary(BinaryOperatorKind.SUB, ref("a"),
            binary(BinaryOperatorKind.SUB, ref("b"), ref("c")))));
    }

    @Test
    public void parenthesisesNonAssociativeOperators() {
        // Relations do not chain in Modelica.
        assertEquals("(a < b) < c", expr(binary(BinaryOperatorKind.LT,
            binary(BinaryOperatorKind.LT, ref("a"), ref("b")), ref("c"))));
        // ^ takes primaries on both sides.
        assertEquals("(a ^ b) ^ c", expr(binary(BinaryOperatorKind.POW,
            binary(BinaryOperatorKind.POW, ref("a"), ref("b")), ref("c"))));
    }

    @Test
    public void parenthesisesLooserOperandsOfLogicalOperators() {
        assertEquals("a or b and c", expr(binary(BinaryOperatorKind.OR, ref("a"),
            binary(BinaryOperatorKind.AND, ref("b"), ref("c")))));
        assertEquals("(a or b) and c", expr(binary(BinaryOperatorKind.AND,
            binary(BinaryOperatorKind.OR, ref("a"), ref("b")), ref("c"))));
    }

    @Test
    public void parenthesisesIfExpressionUsedAsAnOperand() {
        assertEquals("(if c then a else b) + d", expr(binary(BinaryOperatorKind.ADD,
            ifExpr(ref("c"), ref("a"), ref("b")), ref("d"))));
    }

    @Test
    public void keepsAuthoredParenthesesEvenWhenRedundant() {
        assertEquals("(a) + b", expr(binary(BinaryOperatorKind.ADD, parens(ref("a")), ref("b"))));
    }

    // --- identifiers --------------------------------------------------------

    @Test
    public void leavesPlainIdentifiersUnquoted() {
        assertEquals("model Simple\nend Simple;\n", print(model("Simple")));
    }

    @Test
    public void quotesHeaderAndEndConsistently() {
        assertEquals("model 'a model'\nend 'a model';\n", print(model("a model")));
        // A reserved word is quoted too, in both places.
        assertEquals("model 'block'\nend 'block';\n", print(model("block")));
    }

    @Test
    public void quotesComponentNames() {
        ClassDefinition cls = model("M");
        cls.getComponents().add(component(builtinRef("Real"), "a b"));
        assertEquals("model M\n    Real 'a b';\nend M;\n", print(cls));
    }

    // --- declarations -------------------------------------------------------

    @Test
    public void printsComponentModifiersAndBinding() {
        ComponentDeclaration component = component(libraryRef("CRMLtoModelica.Types.Event"), "e");
        component.getModifications().add(mod("b", ref("x")));
        assertEquals("CRMLtoModelica.Types.Event e(b = x);\n", print(component));

        ComponentDeclaration bound = component(builtinRef("Real"), "r", binary(BinaryOperatorKind.ADD, ref("a"), ref("b")));
        assertEquals("Real r = a + b;\n", print(bound));

        ComponentDeclaration constant = component(builtinRef("Integer"), "n", integer(3));
        constant.setVariability(Variability.CONSTANT);
        constant.setComment("a count");
        assertEquals("constant Integer n = 3 \"a count\";\n", print(constant));
    }

    @Test
    public void printsArrayDimensions() {
        ComponentDeclaration fixed = component(libraryRef("CRMLtoModelica.Types.Boolean4"), "bs");
        fixed.getArrayDimensions().add(dimension(integer(3)));
        assertEquals("CRMLtoModelica.Types.Boolean4 bs[3];\n", print(fixed));

        ComponentDeclaration open = component(libraryRef("CRMLtoModelica.Types.Boolean4"), "bs");
        open.getArrayDimensions().add(dimension(null));
        assertEquals("CRMLtoModelica.Types.Boolean4 bs[:];\n", print(open));
    }

    @Test
    public void printsCausalityAndVisibility() {
        ClassDefinition blk = block("B");
        blk.getComponents().add(input(builtinRef("Real"), "r1"));
        blk.getComponents().add(output(builtinRef("Real"), "out"));
        ComponentDeclaration hidden = component(builtinRef("Real"), "h");
        hidden.setVisibility(Visibility.PROTECTED);
        blk.getComponents().add(hidden);
        assertEquals(
            "block B\n"
          + "    input Real r1;\n"
          + "    output Real out;\n"
          + "protected\n"
          + "    Real h;\n"
          + "end B;\n",
            print(blk));
    }

    // --- sections -----------------------------------------------------------

    @Test
    public void omitsSectionKeywordsWhenThereIsNothingInThem() {
        ClassDefinition cls = model("M");
        cls.getComponents().add(component(builtinRef("Real"), "a"));
        assertEquals("model M\n    Real a;\nend M;\n", print(cls));
    }

    @Test
    public void printsEquationSection() {
        ClassDefinition cls = model("M");
        cls.getComponents().add(component(builtinRef("Real"), "a"));
        crml.model.modelica.Equation equation = eq(ref("a"), integer(1));
        equation.setComment("bound");
        cls.getEquations().add(equation);
        assertEquals(
            "model M\n"
          + "    Real a;\n"
          + "equation\n"
          + "    a = 1 \"bound\";\n"
          + "end M;\n",
            print(cls));
    }

    @Test
    public void printsAlgorithmSection() {
        ClassDefinition fn = function("f");
        fn.getComponents().add(input(builtinRef("Real"), "x"));
        fn.getComponents().add(output(builtinRef("Real"), "out"));
        fn.getStatements().add(assign(ref("out"), binary(BinaryOperatorKind.MUL, ref("x"), integer(2))));
        assertEquals(
            "function f\n"
          + "    input Real x;\n"
          + "    output Real out;\n"
          + "algorithm\n"
          + "    out := x * 2;\n"
          + "end f;\n",
            print(fn));
    }

    // --- classes ------------------------------------------------------------

    @Test
    public void printsPartialExtendsNestedAndPlaceholders() {
        ClassDefinition cls = model("Outer");
        cls.setPartial(Boolean.TRUE);
        cls.setComment("a model");
        // The extends target is a real nested class, so the printed name is
        // recomputed from containment rather than stored.
        ClassDefinition system = model("System");
        cls.getNestedClasses().add(system);
        cls.getExtendsClauses().add(extendsClause(resolvedRef(system)));
        cls.getPlaceholders().add(placeholder("AT: no CRMLtoModelica implementation"));
        assertEquals(
            "partial model Outer \"a model\"\n"
          + "    extends System;\n"
          + "    model System\n"
          + "    end System;\n"
          + "    // AT: no CRMLtoModelica implementation\n"
          + "end Outer;\n",
            print(cls));
    }

    // --- parameters and for-equations ---------------------------------------

    /** P1 */
    @Test
    public void printsParameter() {
        ComponentDeclaration n = parameter(builtinRef("Integer"), "n",
            call(builtinRef("size"), ref("P.period"), integer(1)));
        assertEquals(Variability.PARAMETER, n.getVariability());
        assertEquals("parameter Integer n = size(P.period, 1);\n", print(n));
    }

    /** P2 */
    @Test
    public void printsForEquation() {
        ClassDefinition cls = model("M");
        cls.getComponents().add(component(builtinRef("Real"), "a"));
        cls.getEquations().add(eq(ref("a"), integer(1)));
        cls.getEquations().add(forEq("i", integer(1), ref("n"), Arrays.asList(
            eq(subscripted(ref("x"), ref("i")), integer(0)),
            eq(subscripted(ref("y"), ref("i")), subscripted(ref("x"), ref("i"))))));
        assertEquals(
            "model M\n"
          + "    Real a;\n"
          + "equation\n"
          + "    a = 1;\n"
          + "    for i in 1:n loop\n"
          + "        x[i] = 0;\n"
          + "        y[i] = x[i];\n"
          + "    end for;\n"
          + "end M;\n",
            print(cls));
    }

    /** P3 */
    @Test
    public void printsNestedForEquations() {
        Expression index = binary(BinaryOperatorKind.ADD,
            binary(BinaryOperatorKind.MUL,
                binary(BinaryOperatorKind.SUB, ref("i1"), integer(1)),
                ref("n2")),
            ref("i2"));
        ForEquation inner = forEq("i2", integer(1), ref("n2"), Arrays.asList(
            eq(subscripted(ref("out"), index), integer(0))));
        ForEquation outer = forEq("i1", integer(1), ref("n1"), Arrays.asList(inner));
        outer.setComment("region");
        assertEquals(
            "for i1 in 1:n1 loop\n"
          + "    for i2 in 1:n2 loop\n"
          + "        out[(i1 - 1) * n2 + i2] = 0;\n"
          + "    end for;\n"
          + "end for \"region\";\n",
            print(outer));
    }

    /** P4 */
    @Test
    public void printsSubscriptedMembersAndRangeBounds() {
        ComponentReference a = ref("a");
        ComponentReference element = member(subscripted(a, ref("i")), "x");
        assertEquals("a[i].x", expr(element));
        assertEquals("a", expr(a), "the base is copied, not changed");

        ComponentReference period = ref("P.period");
        assertEquals("P.period[k]", expr(subscripted(period, ref("k"))));
        assertEquals("P.period[k].isOpen", expr(member(subscripted(period, ref("k")), "isOpen")));
        assertEquals("P.period", expr(period));

        assertEquals(
            "out[p] = if m[p] then x else y;\n",
            print(eq(subscripted(ref("out"), ref("p")),
                ifExpr(subscripted(ref("m"), ref("p")), ref("x"), ref("y")))));

        assertEquals(
            "for i in 1:n + 1 loop\n"
          + "end for;\n",
            print(forEq("i", integer(1), binary(BinaryOperatorKind.ADD, ref("n"), integer(1)),
                Collections.<SimpleEquation>emptyList())));

        assertEquals(
            "for i in (if b then 1 else 2):(if b then n else m) loop\n"
          + "end for;\n",
            print(forEq("i", ifExpr(ref("b"), integer(1), integer(2)), ifExpr(ref("b"), ref("n"), ref("m")),
                Collections.<SimpleEquation>emptyList())));
    }

    // --- determinism --------------------------------------------------------

    @Test
    public void printingIsDeterministic() {
        ClassDefinition cls = model("M");
        cls.getComponents().add(component(builtinRef("Real"), "a", binary(BinaryOperatorKind.ADD, ref("b"), ref("c"))));
        cls.getEquations().add(eq(ref("a"), call(libraryRef("CRMLtoModelica.Functions.not4"), ref("b"))));
        assertEquals(print(cls), print(cls));
    }

    @Test
    public void honoursPrinterOptions() {
        ClassDefinition cls = model("M");
        cls.getComponents().add(component(builtinRef("Real"), "a"));
        assertEquals("model M\r\n  Real a;\r\nend M;\r\n",
            ModelicaPrinter.print(cls, new PrinterOptions("  ", "\r\n")));
    }
}
