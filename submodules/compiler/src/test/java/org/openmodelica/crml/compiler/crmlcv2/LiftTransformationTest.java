package org.openmodelica.crml.compiler.crmlcv2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.junit.jupiter.api.Test;
import org.openmodelica.crml.compiler.crmlcv2.Diagnostic;
import org.openmodelica.crml.compiler.crmlcv2.Lifter;
import org.openmodelica.crml.compiler.crmlcv2.OMCv2;
import org.openmodelica.crml.compiler.crmlcv2.SetHandle;
import org.openmodelica.crml.compiler.crmlcv2.TransformationContext;
import org.openmodelica.crml.compiler.crmlcv2.TranslationResult;
import org.openmodelica.crml.compiler.crmlcv2.templates.Transformer;
import org.openmodelica.crml.compiler.crmlcv2.util.CRMLCompilerException;
import org.openmodelica.crml.compiler.crmlcv2.util.TypeResolver;
import org.openmodelica.crml.language.dom.builders.LiftBuilder;
import org.openmodelica.crml.model.metamodel.BinaryOperator;
import org.openmodelica.crml.model.metamodel.Binding;
import org.openmodelica.crml.model.metamodel.BuiltinBinaryOperatorKind;
import org.openmodelica.crml.model.metamodel.BuiltinType;
import org.openmodelica.crml.model.metamodel.BuiltinTypeReference;
import org.openmodelica.crml.model.metamodel.BuiltinUnaryOperatorKind;
import org.openmodelica.crml.model.metamodel.ComputedValue;
import org.openmodelica.crml.model.metamodel.CustomOperator;
import org.openmodelica.crml.model.metamodel.IfValue;
import org.openmodelica.crml.model.metamodel.IntegerConstant;
import org.openmodelica.crml.model.metamodel.Keyword;
import org.openmodelica.crml.model.metamodel.CRMLFactory;
import org.openmodelica.crml.model.metamodel.Liftable;
import org.openmodelica.crml.model.metamodel.Model;
import org.openmodelica.crml.model.metamodel.ScatteredValue;
import org.openmodelica.crml.model.metamodel.UnaryOperator;
import org.openmodelica.crml.model.metamodel.UserOperator;
import org.openmodelica.crml.model.metamodel.Value;
import org.openmodelica.crml.model.metamodel.Variable;
import org.openmodelica.crml.model.metamodel.VariableReference;
import org.openmodelica.crml.modelica.metamodel.ClassDefinition;
import org.openmodelica.crml.modelica.metamodel.ClassKind;
import org.openmodelica.crml.modelica.metamodel.ComponentDeclaration;
import org.openmodelica.crml.modelica.metamodel.Equation;
import org.openmodelica.crml.modelica.metamodel.ForEquation;
import org.openmodelica.crml.modelica.build.Modelica;
import org.openmodelica.crml.modelica.print.ModelicaPrinter;

/**
 * Lifting: an operator applied to {@code x.element} operands is generated once,
 * as a region of nested {@code for} loops over the elements, and its results
 * form an array with a mask.
 *
 * <p>The object models are built by hand and linked with the front end's
 * {@link LiftBuilder#linkAll}. A case either translates a whole model, where the
 * lifted application is the operand of an aggregator, or - where the result is
 * not something an aggregator takes - asks the transformer for the region alone.
 */
public class LiftTransformationTest implements Modelica {

    private static final CRMLFactory F = CRMLFactory.eINSTANCE;
    private static final String B4 = "CRMLtoModelica.Types.Boolean4";
    private static final String UNDEFINED = "CRMLtoModelica.Types.Boolean4.undefined";

    // --- CRML object model builders --------------------------------------------

    private static BuiltinTypeReference type(BuiltinType builtinType) {
        BuiltinTypeReference reference = F.createBuiltinTypeReference();
        reference.setBuiltinType(builtinType);
        return reference;
    }

    private static Variable variable(String name, BuiltinType domain, Value definition) {
        Variable variable = F.createVariable();
        variable.setName(name);
        variable.setDomain(type(domain));
        variable.setDefinition(definition);
        return variable;
    }

    private static VariableReference refTo(Variable variable) {
        VariableReference reference = F.createVariableReference();
        reference.setVariable(variable);
        return reference;
    }

    private static org.openmodelica.crml.model.metamodel.UserTypereference classType(org.openmodelica.crml.model.metamodel.Class clazz) {
        org.openmodelica.crml.model.metamodel.UserTypereference reference = F.createUserTypereference();
        reference.setDomain(clazz);
        return reference;
    }

    private static Variable classVariable(String name, org.openmodelica.crml.model.metamodel.Class clazz, Value definition) {
        Variable variable = F.createVariable();
        variable.setName(name);
        variable.setDomain(classType(clazz));
        variable.setDefinition(definition);
        return variable;
    }

    /** {@code Type name is { elements };} */
    private static Variable setVariable(String name, BuiltinType domain, Variable... elements) {
        org.openmodelica.crml.model.metamodel.Set<Value> set = F.createSet();
        for (Variable element : elements) {
            set.getElements().add(refTo(element));
        }
        return variable(name, domain, set);
    }

    /** {@code data.element}, not linked yet. */
    private static ScatteredValue element(Value data) {
        ScatteredValue scattered = F.createScatteredValue();
        scattered.setData(data);
        return scattered;
    }

    private static ScatteredValue element(Variable collection) {
        return element(refTo(collection));
    }

    private static UnaryOperator unary(BuiltinUnaryOperatorKind kind, Value operand) {
        UnaryOperator op = F.createUnaryOperator();
        op.setOptype(kind);
        op.setValue(operand);
        return op;
    }

    private static UnaryOperator parens(Value operand) {
        return unary(BuiltinUnaryOperatorKind.SUBEXPRESSION, operand);
    }

    private static BinaryOperator binary(BuiltinBinaryOperatorKind kind, Value lhs, Value rhs) {
        BinaryOperator op = F.createBinaryOperator();
        op.setOptype(kind);
        op.setLhs(lhs);
        op.setRhs(rhs);
        return op;
    }

    /** {@code and (operand)}: an aggregator has no left operand. */
    private static BinaryOperator and(Value operand) {
        return binary(BuiltinBinaryOperatorKind.AND, null, parens(operand));
    }

    private static IntegerConstant integerConstant(int value) {
        IntegerConstant constant = F.createIntegerConstant();
        constant.setValue(value);
        constant.setReturnType(type(BuiltinType.INTEGER));
        return constant;
    }

    private static Keyword keyword(String text) {
        Keyword kw = F.createKeyword();
        kw.setKeyword(text);
        return kw;
    }

    /** An operator {@code 'k1' p1 'k2' p2 ...} with one keyword before each parameter. */
    private static UserOperator operator(BuiltinType result, Object... keywordsAndParameters) {
        UserOperator operator = F.createUserOperator();
        operator.setDomain(type(result));
        for (Object part : keywordsAndParameters) {
            if (part instanceof String) {
                operator.getHeader().add(keyword("'" + part + "'"));
            } else {
                operator.getHeader().add((Variable) part);
            }
        }
        return operator;
    }

    private static ComputedValue call(CustomOperator operator, Value... arguments) {
        ComputedValue computed = F.createComputedValue();
        computed.setOperator(operator);
        for (int i = 0; i < arguments.length; i++) {
            Binding binding = F.createBinding();
            binding.setElement(operator.getVariables().get(i));
            binding.setValue(arguments[i]);
            computed.getBindings().add(binding);
        }
        return computed;
    }

    /** {@code Operator [ Boolean ] 'g' Boolean a = not a;} - a function. */
    private static UserOperator g() {
        Variable a = variable("a", BuiltinType.BOOLEAN, null);
        UserOperator operator = operator(BuiltinType.BOOLEAN, "g", a);
        operator.setDefinition(unary(BuiltinUnaryOperatorKind.NOT, refTo(a)));
        return operator;
    }

    /** {@code Operator [ Boolean ] 'f' Boolean a 'with' Boolean b = a and b;} - a function. */
    private static UserOperator f() {
        Variable a = variable("a", BuiltinType.BOOLEAN, null);
        Variable b = variable("b", BuiltinType.BOOLEAN, null);
        UserOperator operator = operator(BuiltinType.BOOLEAN, "f", a, "with", b);
        operator.setDefinition(binary(BuiltinBinaryOperatorKind.AND, refTo(a), refTo(b)));
        return operator;
    }

    /** {@code Operator [ Boolean ] 'x' Clock x = (card x) > 0;} - hoists a CardClock, so a block. */
    private static UserOperator x() {
        Variable x = variable("x", BuiltinType.CLOCK, null);
        UserOperator operator = operator(BuiltinType.BOOLEAN, "x", x);
        operator.setDefinition(binary(BuiltinBinaryOperatorKind.GT,
            parens(unary(BuiltinUnaryOperatorKind.CARD, refTo(x))), integerConstant(0)));
        return operator;
    }

    /** The model's variables shared by most cases: two Boolean sets and a Clock set. */
    private static final class Fixture {
        final Variable b1 = variable("b1", BuiltinType.BOOLEAN, null);
        final Variable b2 = variable("b2", BuiltinType.BOOLEAN, null);
        final Variable a = setVariable("A", BuiltinType.BOOLEAN, b1, b2);
        final Variable b = setVariable("B", BuiltinType.BOOLEAN, b1, b2);
        final Variable c1 = variable("c1", BuiltinType.CLOCK, null);
        final Variable c2 = variable("c2", BuiltinType.CLOCK, null);
        final Variable c = setVariable("C", BuiltinType.CLOCK, c1, c2);
        final Variable d = setVariable("D", BuiltinType.CLOCK, c1, c2);
        final Variable x = variable("x", BuiltinType.REAL, null);
        final Variable r = setVariable("R", BuiltinType.REAL, x, x);
        final Variable p = variable("P", BuiltinType.PERIODS, null);
        final Model model = F.createModel();

        Fixture(CustomOperator... operators) {
            model.setName("M");
            for (CustomOperator operator : operators) {
                model.getOperators().add(operator);
            }
            Variable[] all = {b1, b2, a, b, c1, c2, c, d, x, r, p};
            for (Variable variable : all) {
                model.getVariables().add(variable);
            }
        }

        /** Adds {@code Boolean name is definition;}, links every lift and translates the model. */
        TranslationResult translate(String name, Value definition) {
            return translate(variable(name, BuiltinType.BOOLEAN, definition));
        }

        TranslationResult translate(Variable... results) {
            for (Variable result : results) {
                model.getVariables().add(result);
            }
            LiftBuilder.linkAll(model);
            return new OMCv2().translateModel(model);
        }
    }

    /** A bare class and a context on it, for asking the transformer for one region. */
    private static final class Direct implements Modelica {
        final Transformer transformer = new Transformer();
        final ClassDefinition target = model("M");
        final TransformationContext ctx = TransformationContext.of(target);

        SetHandle region(Liftable application) {
            LiftBuilder.linkAll(application);
            return transformer.liftables().region(ctx, application);
        }

        String text() {
            return ModelicaPrinter.print(target);
        }
    }

    // --- assertions --------------------------------------------------------------

    private static String print(org.openmodelica.crml.modelica.metamodel.Expression expression) {
        return ModelicaPrinter.print(expression);
    }

    private static void assertContains(String text, String... expected) {
        for (String line : expected) {
            assertTrue(text.contains(line), "missing: " + line + "\nin:\n" + text);
        }
    }

    private static List<String> kinds(TranslationResult result) {
        List<String> kinds = new ArrayList<String>();
        for (Diagnostic diagnostic : result.diagnostics()) {
            kinds.add(diagnostic.constructKind());
        }
        return kinds;
    }

    private static Diagnostic diagnostic(TranslationResult result, String kind) {
        for (Diagnostic diagnostic : result.diagnostics()) {
            if (diagnostic.constructKind().equals(kind)) {
                return diagnostic;
            }
        }
        throw new AssertionError("no " + kind + " in " + result.diagnostics());
    }

    /** The constructKind an operation is rejected with. */
    private static Diagnostic rejection(org.junit.jupiter.api.function.Executable operation) {
        return assertThrows(CRMLCompilerException.UnsupportedConstruct.class, operation).diagnostic();
    }

    private static ComponentDeclaration component(ClassDefinition cls, String name) {
        for (ComponentDeclaration component : cls.getComponents()) {
            if (name.equals(component.getName())) {
                return component;
            }
        }
        return null;
    }

    private static ClassDefinition nested(ClassDefinition cls, String name) {
        for (ClassDefinition nested : cls.getNestedClasses()) {
            if (name.equals(nested.getName())) {
                return nested;
            }
        }
        throw new AssertionError("no class " + name);
    }

    /** The region loops of a class: its for-equations whose body writes a mask. */
    private static List<ForEquation> regions(ClassDefinition cls) {
        List<ForEquation> regions = new ArrayList<ForEquation>();
        for (Equation equation : cls.getEquations()) {
            if (equation instanceof ForEquation && print(firstLhs(body((ForEquation) equation))).contains("_mask")) {
                regions.add((ForEquation) equation);
            }
        }
        return regions;
    }

    /** The equations of the innermost loop of a nest. */
    private static List<Equation> body(ForEquation loop) {
        while (loop.getEquations().size() == 1 && loop.getEquations().get(0) instanceof ForEquation) {
            loop = (ForEquation) loop.getEquations().get(0);
        }
        return loop.getEquations();
    }

    private static org.openmodelica.crml.modelica.metamodel.Expression firstLhs(List<Equation> equations) {
        return ((org.openmodelica.crml.modelica.metamodel.SimpleEquation) equations.get(0)).getLhs();
    }

    /** The number of loops nested in a region, itself included. */
    private static int depth(ForEquation loop) {
        int depth = 1;
        while (loop.getEquations().size() == 1 && loop.getEquations().get(0) instanceof ForEquation) {
            loop = (ForEquation) loop.getEquations().get(0);
            depth++;
        }
        return depth;
    }

    /**
     * The balance of a region: each slot has one equation for the mask, one for
     * the result, and one for each input of each per-slot instance.
     */
    private static void assertRegion(ForEquation region, int axes, int instanceInputsPerSlot) {
        assertEquals(axes, depth(region), "nested loops");
        assertEquals(2 + instanceInputsPerSlot, body(region).size(), "equations per slot");
    }

    /** No trace of a region: nothing was added for one that failed. */
    private static void assertNoRegion(ClassDefinition cls) {
        for (ComponentDeclaration component : cls.getComponents()) {
            String name = component.getName();
            assertFalse(name.contains("_n") && name.matches(".*_n\\d+") || name.contains("_size")
                || name.contains("_out") || name.contains("_mask"), "region component left behind: " + name);
        }
        assertTrue(regions(cls).isEmpty(), "region loop left behind");
    }

    // --- T1, T2: collections -----------------------------------------------------

    /** T1 */
    @Test
    public void aPeriodsVariableIsACollectionOfItsPeriods() {
        Fixture fx = new Fixture();
        Direct direct = new Direct();
        SetHandle periods = direct.transformer.scatteredValues().collect(direct.ctx, element(fx.p));

        assertEquals("size(P.period, 1)", print(periods.size()));
        assertEquals("P.period[k]", print(periods.element(ref("k"))));
        assertEquals("k <= P.counter", print(periods.active(ref("k"))));
        assertEquals("P.period", print(periods.values()));
        assertNull(periods.mask());
        assertEquals(BuiltinType.PERIOD, periods.elementBuiltin());
        assertEquals("CRMLtoModelica.Types.CRMLPeriod", periods.elementType().getRawText());
        // Every accessor hands out a new tree.
        assertTrue(periods.size() != periods.size());
        assertTrue(direct.target.getComponents().isEmpty(), direct.text());
        assertTrue(direct.target.getEquations().isEmpty(), direct.text());
    }

    /** T2 */
    @Test
    public void aSetVariableIsAPlainArray() {
        Fixture fx = new Fixture();
        Direct direct = new Direct();
        SetHandle set = direct.transformer.scatteredValues().collect(direct.ctx, element(fx.a));

        assertEquals("size(A, 1)", print(set.size()));
        assertEquals("A[k]", print(set.element(ref("k"))));
        assertNull(set.active(ref("k")));
        assertEquals("A", print(set.values()));
        assertNull(set.mask());
        assertEquals(BuiltinType.BOOLEAN, set.elementBuiltin());
        assertEquals(B4, set.elementType().getRawText());
        assertTrue(direct.target.getComponents().isEmpty(), direct.text());
        assertTrue(direct.target.getEquations().isEmpty(), direct.text());
    }

    @Test
    public void aSetLiteralIsStoredInAVariable() {
        Fixture fx = new Fixture();
        Direct direct = new Direct();
        org.openmodelica.crml.model.metamodel.Set<Value> literal = F.createSet();
        literal.getElements().add(refTo(fx.b1));
        literal.getElements().add(refTo(fx.b2));
        SetHandle set = direct.transformer.scatteredValues().collect(direct.ctx, literal);

        assertEquals("set1[k]", print(set.element(ref("k"))));
        assertContains(direct.text(), B4 + " set1[2] = {b1, b2};");
    }

    // --- T3 - T7: regions of user operators ----------------------------------------

    /** T3 */
    @Test
    public void aFunctionOverOneAxisIsCalledInOneLoop() {
        // Operator [ Real ] 'sq' Real a = a * a;
        Variable a = variable("a", BuiltinType.REAL, null);
        UserOperator sq = operator(BuiltinType.REAL, "sq", a);
        sq.setDefinition(binary(BuiltinBinaryOperatorKind.MUL, refTo(a), refTo(a)));
        Fixture fx = new Fixture();
        Direct direct = new Direct();

        SetHandle result = direct.region(call(sq, element(fx.r)));

        assertContains(direct.text(),
            "function op_sq",
            "parameter Integer op_sq_n1 = size(R, 1);",
            "parameter Integer op_sq_size1 = op_sq_n1;",
            "Real op_sq_out1[op_sq_size1];",
            "Boolean op_sq_mask1[op_sq_size1];",
            "for i1 in 1:op_sq_n1 loop",
            "op_sq_mask1[i1] = true;",
            "op_sq_out1[i1] = op_sq(R[i1]);");
        assertEquals(4, direct.target.getComponents().size(), "no instance array: " + direct.text());
        assertEquals(1, regions(direct.target).size());
        assertRegion(regions(direct.target).get(0), 1, 0);
        assertEquals("op_sq_size1", print(result.size()));
        assertEquals("op_sq_out1[k]", print(result.element(ref("k"))));
        assertEquals("op_sq_mask1[k]", print(result.active(ref("k"))));
        assertEquals(BuiltinType.REAL, result.elementBuiltin());
    }

    /** T4 */
    @Test
    public void aBlockOverOneAxisIsInstantiatedPerElement() {
        UserOperator x = x();
        Fixture fx = new Fixture(x);
        TranslationResult result = fx.translate("r", and(call(x, element(fx.c))));

        assertFalse(result.hasErrors(), result.diagnostics().toString());
        assertContains(result.text(),
            "block op_x",
            "parameter Integer op_x_n1 = size(C, 1);",
            B4 + " op_x_out1[op_x_size1];",
            "op_x op_x_1[op_x_size1];",
            "for i1 in 1:op_x_n1 loop",
            "op_x_mask1[i1] = true;",
            "op_x_1[i1].x = C[i1];",
            "op_x_out1[i1] = if op_x_mask1[i1] then op_x_1[i1].out else " + UNDEFINED + ";");
        assertRegion(regions(result.modelica()).get(0), 1, 1);
        assertFalse(kinds(result).contains("Lift.InstanceCount"), "one axis: " + result.diagnostics());
    }

    /** T5 */
    @Test
    public void aBlockOverTwoAxesWithABroadcastOperand() {
        // Operator [ Boolean ] 'y' Clock a 'vs' Clock b 'min' Integer n = ((card a) > n) and ((card b) > n);
        Variable a = variable("a", BuiltinType.CLOCK, null);
        Variable b = variable("b", BuiltinType.CLOCK, null);
        Variable n = variable("n", BuiltinType.INTEGER, null);
        UserOperator y = operator(BuiltinType.BOOLEAN, "y", a, "vs", b, "min", n);
        y.setDefinition(binary(BuiltinBinaryOperatorKind.AND,
            parens(binary(BuiltinBinaryOperatorKind.GT, parens(unary(BuiltinUnaryOperatorKind.CARD, refTo(a))), refTo(n))),
            parens(binary(BuiltinBinaryOperatorKind.GT, parens(unary(BuiltinUnaryOperatorKind.CARD, refTo(b))), refTo(n)))));
        Fixture fx = new Fixture(y);
        Variable k = variable("k", BuiltinType.INTEGER, null);
        fx.model.getVariables().add(k);

        TranslationResult result = fx.translate("r", and(call(y, element(fx.c), element(fx.d), refTo(k))));

        assertFalse(result.hasErrors(), result.diagnostics().toString());
        String p = "(i1 - 1) * op_y_vs_min_n2 + i2";
        assertContains(result.text(),
            "parameter Integer op_y_vs_min_n1 = size(C, 1);",
            "parameter Integer op_y_vs_min_n2 = size(D, 1);",
            "parameter Integer op_y_vs_min_size1 = op_y_vs_min_n1 * op_y_vs_min_n2;",
            "op_y_vs_min op_y_vs_min_1[op_y_vs_min_size1];",
            "for i1 in 1:op_y_vs_min_n1 loop",
            "for i2 in 1:op_y_vs_min_n2 loop",
            "op_y_vs_min_mask1[" + p + "] = true;",
            "op_y_vs_min_1[" + p + "].a = C[i1];",
            "op_y_vs_min_1[" + p + "].b = D[i2];",
            // The broadcast operand is the same for every slot.
            "op_y_vs_min_1[" + p + "].n = k;",
            "op_y_vs_min_out1[" + p + "] = if op_y_vs_min_mask1[" + p + "] then op_y_vs_min_1[" + p + "].out else "
                + UNDEFINED + ";");
        assertRegion(regions(result.modelica()).get(0), 2, 3);
        Diagnostic count = diagnostic(result, "Lift.InstanceCount");
        assertEquals(Diagnostic.Severity.INFO, count.severity());
        assertTrue(count.message().contains("op_y_vs_min_n1 * op_y_vs_min_n2"), count.message());
    }

    /** T6 */
    @Test
    public void threeAxesAreIndexedRowMajor() {
        // Operator [ Boolean ] 't' Boolean a 'u' Boolean b 'v' Boolean c = a and (b and c);
        Variable a = variable("a", BuiltinType.BOOLEAN, null);
        Variable b = variable("b", BuiltinType.BOOLEAN, null);
        Variable c = variable("c", BuiltinType.BOOLEAN, null);
        UserOperator t = operator(BuiltinType.BOOLEAN, "t", a, "u", b, "v", c);
        t.setDefinition(binary(BuiltinBinaryOperatorKind.AND, refTo(a),
            parens(binary(BuiltinBinaryOperatorKind.AND, refTo(b), refTo(c)))));
        Fixture fx = new Fixture(t);
        Variable e = setVariable("E", BuiltinType.BOOLEAN, fx.b1, fx.b2);
        fx.model.getVariables().add(e);

        TranslationResult result = fx.translate("r", and(call(t, element(fx.a), element(fx.b), element(e))));

        assertFalse(result.hasErrors(), result.diagnostics().toString());
        String p = "((i1 - 1) * op_t_u_v_n2 + i2 - 1) * op_t_u_v_n3 + i3";
        assertContains(result.text(),
            "parameter Integer op_t_u_v_size1 = op_t_u_v_n1 * op_t_u_v_n2 * op_t_u_v_n3;",
            "op_t_u_v_mask1[" + p + "] = true;",
            "op_t_u_v_out1[" + p + "] = if op_t_u_v_mask1[" + p + "] then op_t_u_v(A[i1], B[i2], E[i3]) else "
                + UNDEFINED + ";");
        assertRegion(regions(result.modelica()).get(0), 3, 0);
        assertFalse(kinds(result).contains("Lift.InstanceCount"), "no instances: " + result.diagnostics());
    }

    /** T7 */
    @Test
    public void theSameCollectionTwiceIsTwoAxes() {
        UserOperator f = f();
        Fixture fx = new Fixture(f);
        TranslationResult result = fx.translate("r", and(call(f, element(fx.a), element(fx.a))));

        assertFalse(result.hasErrors(), result.diagnostics().toString());
        assertContains(result.text(),
            "parameter Integer op_f_with_n1 = size(A, 1);",
            "parameter Integer op_f_with_n2 = size(A, 1);",
            "parameter Integer op_f_with_size1 = op_f_with_n1 * op_f_with_n2;",
            "then op_f_with(A[i1], A[i2]) else");
        assertRegion(regions(result.modelica()).get(0), 2, 0);
    }

    // --- T8, T9: chaining and propagation ------------------------------------------

    /** T8 */
    @Test
    public void aLiftedResultCanBeLiftedOverAgain() {
        UserOperator f = f();
        UserOperator g = g();
        Fixture fx = new Fixture(f, g);
        // and ('f' ('g' A.element).element 'with' B.element)
        TranslationResult result = fx.translate("r",
            and(call(f, element(parens(call(g, element(fx.a)))), element(fx.b))));

        assertFalse(result.hasErrors(), result.diagnostics().toString());
        String text = result.text();
        String p = "(i2 - 1) * op_f_with_n2 + i3";
        assertContains(text,
            "op_g_out1[i1] = if op_g_mask1[i1] then op_g(A[i1]) else " + UNDEFINED + ";",
            "parameter Integer op_f_with_n1 = op_g_size1;",
            "parameter Integer op_f_with_n2 = size(B, 1);",
            "op_f_with_mask1[" + p + "] = op_g_mask1[i2];",
            "op_f_with_out1[" + p + "] = if op_f_with_mask1[" + p + "] then op_f_with(op_g_out1[i2], B[i3]) else "
                + UNDEFINED + ";");
        List<ForEquation> regions = regions(result.modelica());
        assertEquals(2, regions.size());
        assertEquals("op_g_n1", print(regions.get(0).getStop()), "the inner region comes first");
        assertEquals("op_f_with_n1", print(regions.get(1).getStop()));
        assertRegion(regions.get(0), 1, 0);
        assertRegion(regions.get(1), 2, 0);
        assertTrue(text.indexOf("Boolean op_g_mask1[") < text.indexOf("parameter Integer op_f_with_n1"), text);
    }

    /** T9 */
    @Test
    public void aLiftedResultIsNotASingleValue() {
        UserOperator f = f();
        UserOperator g = g();
        Fixture fx = new Fixture(f, g);
        // and ('f' ('g' A.element) 'with' B.element): the result of g is a set, f wants a Boolean.
        TranslationResult result = fx.translate("r",
            and(call(f, parens(call(g, element(fx.a))), element(fx.b))));

        Diagnostic diagnostic = diagnostic(result, "Lift.SetWhereValueExpected");
        assertEquals(Diagnostic.Severity.WARNING, diagnostic.severity());
        assertTrue(diagnostic.crmlSource() instanceof ComputedValue);
        assertNull(component(result.modelica(), "r"), "the variable is dropped");
        assertNoRegion(result.modelica());
    }

    // --- T10, T11 ------------------------------------------------------------------

    /** T10 */
    @Test
    public void aBroadcastOperandIsEvaluatedOnceOutsideTheLoop() {
        // Operator [ Boolean ] 'h' Event e 'w' Boolean b = b;
        Variable e = variable("e", BuiltinType.EVENT, null);
        Variable b = variable("b", BuiltinType.BOOLEAN, null);
        UserOperator h = operator(BuiltinType.BOOLEAN, "h", e, "w", b);
        h.setDefinition(refTo(b));
        Fixture fx = new Fixture(h);
        // and ('h' (tick c1) 'w' A.element)
        TranslationResult result = fx.translate("r",
            and(call(h, parens(unary(BuiltinUnaryOperatorKind.TICK, refTo(fx.c1))), element(fx.a))));

        assertFalse(result.hasErrors(), result.diagnostics().toString());
        String text = result.text();
        assertContains(text,
            "CRMLtoModelica.Blocks.ClockTick tick1;",
            "tick1.r1 = c1;",
            "then op_h_w((tick1.out), A[i1]) else");
        ComponentDeclaration tick = component(result.modelica(), "tick1");
        assertNotNull(tick);
        assertTrue(tick.getArrayDimensions().isEmpty(), "declared once, not per slot");
        assertTrue(text.indexOf("tick1.r1 = c1;") < text.indexOf("for i1 in"), text);
        assertRegion(regions(result.modelica()).get(0), 1, 0);
    }

    /** T11: the driving case, 'check' phi 'over' Periods P = and ('evaluate' phi 'over' P.element). */
    @Test
    public void aLiftInsideAnOperatorBodyMakesTheOperatorABlock() {
        // Operator [ Boolean ] 'evaluate' Boolean phi 'over' Period P = integrate phi on P;
        Variable ePhi = variable("phi", BuiltinType.BOOLEAN, null);
        Variable eP = variable("P", BuiltinType.PERIOD, null);
        UserOperator evaluate = operator(BuiltinType.BOOLEAN, "evaluate", ePhi, "over", eP);
        org.openmodelica.crml.model.metamodel.IntegrateValue integrate = F.createIntegrateValue();
        integrate.setIntegrand(refTo(ePhi));
        integrate.setInterval(refTo(eP));
        evaluate.setDefinition(integrate);
        // Operator [ Boolean ] 'check' Boolean phi 'over' Periods P = and ('evaluate' phi 'over' P.element);
        Variable cPhi = variable("phi", BuiltinType.BOOLEAN, null);
        Variable cP = variable("P", BuiltinType.PERIODS, null);
        UserOperator check = operator(BuiltinType.BOOLEAN, "check", cPhi, "over", cP);
        check.setDefinition(and(call(evaluate, refTo(cPhi), element(cP))));

        Fixture fx = new Fixture(evaluate, check);
        TranslationResult result = fx.translate("r", call(check, refTo(fx.b1), refTo(fx.p)));

        assertFalse(result.hasErrors(), result.diagnostics().toString());
        ClassDefinition block = nested(result.modelica(), "op_check_over");
        assertEquals(ClassKind.BLOCK, block.getKind());
        assertContains(ModelicaPrinter.print(block),
            "parameter Integer op_evaluate_over_n1 = size(P.period, 1);",
            "parameter Integer op_evaluate_over_size1 = op_evaluate_over_n1;",
            B4 + " op_evaluate_over_out1[op_evaluate_over_size1];",
            "Boolean op_evaluate_over_mask1[op_evaluate_over_size1];",
            "op_evaluate_over op_evaluate_over_1[op_evaluate_over_size1];",
            B4 + " and_acc1[op_evaluate_over_size1 + 1];",
            "for i1 in 1:op_evaluate_over_n1 loop",
            "op_evaluate_over_mask1[i1] = i1 <= P.counter;",
            "op_evaluate_over_1[i1].phi = phi;",
            "op_evaluate_over_1[i1].P = P.period[i1];",
            "op_evaluate_over_out1[i1] = if op_evaluate_over_mask1[i1] then op_evaluate_over_1[i1].out else "
                + UNDEFINED + ";",
            "and_acc1[1] = " + UNDEFINED + ";",
            "for i2 in 1:op_evaluate_over_size1 loop",
            "and_acc1[i2 + 1] = CRMLtoModelica.Functions.and4(and_acc1[i2], op_evaluate_over_out1[i2]);",
            "out = and_acc1[op_evaluate_over_size1 + 1];");
        assertRegion(regions(block).get(0), 1, 2);
        // The region is in the operator's class, not in the model.
        assertNoRegion(result.modelica());
        assertContains(result.text(), "op_check_over op_check_over_1;");
    }

    // --- T12: built-in operators ---------------------------------------------------

    /** T12 */
    @Test
    public void aBuiltinFunctionOperatorIsLifted() {
        Fixture fx = new Fixture();
        Direct direct = new Direct();
        SetHandle result = direct.region(unary(BuiltinUnaryOperatorKind.NOT, element(fx.a)));

        assertContains(direct.text(),
            "parameter Integer not_n1 = size(A, 1);",
            "parameter Integer not_size1 = not_n1;",
            B4 + " not_out1[not_size1];",
            "Boolean not_mask1[not_size1];",
            "for i1 in 1:not_n1 loop",
            "not_mask1[i1] = true;",
            "not_out1[i1] = if not_mask1[i1] then CRMLtoModelica.Functions.not4(A[i1]) else " + UNDEFINED + ";");
        assertRegion(regions(direct.target).get(0), 1, 0);
        assertEquals(BuiltinType.BOOLEAN, result.elementBuiltin());
        assertTrue(result.inactiveIsUndefined());
    }

    /** T12 */
    @Test
    public void aBuiltinBinaryOperatorIsLiftedOverEitherOperand() {
        Fixture fx = new Fixture();
        Direct left = new Direct();
        left.region(binary(BuiltinBinaryOperatorKind.LT, element(fx.r), refTo(fx.x)));
        assertContains(left.text(),
            "parameter Integer lt_n1 = size(R, 1);",
            B4 + " lt_out1[lt_size1];",
            "lt_out1[i1] = if lt_mask1[i1] then CRMLtoModelica.Functions.cvBooleanToBoolean4(R[i1] < x) else "
                + UNDEFINED + ";");

        // Both operands: lhs is the first axis, rhs the second.
        Direct both = new Direct();
        both.region(binary(BuiltinBinaryOperatorKind.ADD, element(fx.r), element(fx.r)));
        assertContains(both.text(),
            "parameter Integer add_size1 = add_n1 * add_n2;",
            "Real add_out1[add_size1];",
            "add_out1[(i1 - 1) * add_n2 + i2] = R[i1] + R[i2];");
        assertRegion(regions(both.target).get(0), 2, 0);
    }

    /** T12 */
    @Test
    public void aBlockBackedBuiltinIsInstantiatedPerElement() {
        Fixture fx = new Fixture();
        Direct direct = new Direct();
        SetHandle result = direct.region(unary(BuiltinUnaryOperatorKind.TICK, element(fx.c)));

        assertContains(direct.text(),
            "parameter Integer tick_n1 = size(C, 1);",
            "CRMLtoModelica.Types.Event tick_out1[tick_size1];",
            "CRMLtoModelica.Blocks.ClockTick tick1[tick_size1];",
            "tick1[i1].r1 = C[i1];",
            "tick_out1[i1] = tick1[i1].out;");
        assertRegion(regions(direct.target).get(0), 1, 1);
        assertEquals(BuiltinType.EVENT, result.elementBuiltin());
        assertFalse(result.inactiveIsUndefined());
    }

    // --- T13: diagnostics ----------------------------------------------------------

    /** T13: Lift.NotDirectArgument */
    @Test
    public void aLiftThatIsNotAnOperandIsRejected() {
        UserOperator g = g();
        Fixture fx = new Fixture(g);
        // 'g' (not A.element), with the lift wrongly attached to the call instead of to "not".
        ScatteredValue lift = element(fx.a);
        ComputedValue call = call(g, unary(BuiltinUnaryOperatorKind.NOT, lift));
        call.getLifts().add(lift);
        Direct direct = new Direct();

        Diagnostic diagnostic = rejection(() -> direct.transformer.liftables().region(direct.ctx, call));
        assertEquals("Lift.NotDirectArgument", diagnostic.constructKind());
        assertEquals(Diagnostic.Severity.ERROR, diagnostic.severity());
        assertTrue(diagnostic.crmlSource() == lift);
        assertNoRegion(direct.target);
    }

    /** T13: Lift.NotACollection */
    @Test
    public void liftingOverAScalarIsRejected() {
        UserOperator g = g();
        Fixture fx = new Fixture(g);
        TranslationResult result = fx.translate("r", and(call(g, element(fx.b1))));

        Diagnostic diagnostic = diagnostic(result, "Lift.NotACollection");
        assertEquals(Diagnostic.Severity.ERROR, diagnostic.severity());
        assertTrue(diagnostic.message().contains("BOOLEAN"), diagnostic.message());
        assertNoRegion(result.modelica());
    }

    /** T13: Lift.ElementType */
    @Test
    public void elementsOfTheWrongTypeAreRejected() {
        UserOperator g = g();
        Fixture fx = new Fixture(g);
        // 'g' takes a Boolean; R is a set of Reals.
        TranslationResult result = fx.translate("r", and(call(g, element(fx.r))));

        Diagnostic diagnostic = diagnostic(result, "Lift.ElementType");
        assertEquals(Diagnostic.Severity.ERROR, diagnostic.severity());
        assertTrue(diagnostic.crmlSource() instanceof Binding);
        assertTrue(diagnostic.message().contains("BOOLEAN") && diagnostic.message().contains("REAL"),
            diagnostic.message());
        assertNoRegion(result.modelica());
    }

    /** T13: Lift.Unlinked */
    @Test
    public void aLinkedLiftOutsideItsApplicationIsRejected() {
        UserOperator g = g();
        Fixture fx = new Fixture(g);
        // The lift claims to belong to a call it is not an operand of, and sits where a value is expected.
        ScatteredValue lift = element(fx.a);
        ComputedValue elsewhere = call(g, refTo(fx.b1));
        elsewhere.getLifts().add(lift);
        fx.model.getVariables().add(variable("r", BuiltinType.BOOLEAN, lift));
        TranslationResult result = new OMCv2().translateModel(fx.model);

        Diagnostic diagnostic = diagnostic(result, "Lift.Unlinked");
        assertEquals(Diagnostic.Severity.ERROR, diagnostic.severity());
        assertNoRegion(result.modelica());
    }

    /** T13: Lift.NoApplication (experimental) */
    @Test
    public void aLiftWithoutAnApplicationIsRejected() {
        Fixture fx = new Fixture();
        // if A.element then b1 else b2
        IfValue conditional = F.createIfValue();
        conditional.setCondition(element(fx.a));
        conditional.setThen(refTo(fx.b1));
        conditional.setOthervise(refTo(fx.b2));
        TranslationResult result = fx.translate("r", conditional);

        Diagnostic diagnostic = diagnostic(result, "Lift.NoApplication");
        assertEquals(Diagnostic.Severity.WARNING, diagnostic.severity());
        assertTrue(diagnostic.crmlSource() instanceof ScatteredValue);
        assertTrue(diagnostic.message().contains("IfValue"), diagnostic.message());
        assertFalse(result.hasErrors(), result.diagnostics().toString());
        assertNull(component(result.modelica(), "r"));
        assertNoRegion(result.modelica());
    }

    /** T13: Lift.UnknownResultType */
    @Test
    public void anUnknownResultTypeIsRejected() {
        // U is a set whose elements have no known builtin type.
        Variable u1 = F.createVariable();
        u1.setName("u1");
        org.openmodelica.crml.model.metamodel.Set<Value> elements = F.createSet();
        elements.getElements().add(refTo(u1));
        Variable u = F.createVariable();
        u.setName("U");
        u.setDefinition(elements);
        u.setDomain(type(BuiltinType.BOOLEAN));
        // The declared domain of a set variable is only a fallback; take it away again.
        u.setDomain(null);
        BinaryOperator sum = binary(BuiltinBinaryOperatorKind.ADD, element(u), refTo(u1));

        assertEquals("Lift.UnknownResultType",
            rejection(() -> TypeResolver.resultBuiltin(sum, null, null)).constructKind());

        Direct direct = new Direct();
        Diagnostic diagnostic = rejection(() -> direct.region(sum));
        assertEquals("Lift.UnknownResultType", diagnostic.constructKind());
        assertEquals(Diagnostic.Severity.WARNING, diagnostic.severity());
        assertTrue(diagnostic.crmlSource() == sum);
        assertNoRegion(direct.target);
    }

    @Test
    public void resultTypesFollowTheTypeInferenceTable() {
        assertEquals(BuiltinType.BOOLEAN, TypeResolver.resultBuiltin(unary(BuiltinUnaryOperatorKind.NOT, null), null));
        assertEquals(BuiltinType.INTEGER, TypeResolver.resultBuiltin(unary(BuiltinUnaryOperatorKind.CARD, null), null));
        assertEquals(BuiltinType.EVENT, TypeResolver.resultBuiltin(unary(BuiltinUnaryOperatorKind.TICK, null), null));
        assertEquals(BuiltinType.EVENT, TypeResolver.resultBuiltin(unary(BuiltinUnaryOperatorKind.START, null), null));
        assertEquals(BuiltinType.REAL, TypeResolver.resultBuiltin(unary(BuiltinUnaryOperatorKind.SIN, null), null));
        assertEquals(BuiltinType.INTEGER,
            TypeResolver.resultBuiltin(unary(BuiltinUnaryOperatorKind.SUB, null), BuiltinType.INTEGER));

        BuiltinType b = BuiltinType.BOOLEAN;
        BuiltinType i = BuiltinType.INTEGER;
        BuiltinType r = BuiltinType.REAL;
        assertEquals(b, TypeResolver.resultBuiltin(binary(BuiltinBinaryOperatorKind.LE, null, null), null, null));
        assertEquals(b, TypeResolver.resultBuiltin(binary(BuiltinBinaryOperatorKind.OR, null, null), null, null));
        assertEquals(BuiltinType.CLOCK,
            TypeResolver.resultBuiltin(binary(BuiltinBinaryOperatorKind.FILTER, null, null), null, null));
        assertEquals(i, TypeResolver.resultBuiltin(binary(BuiltinBinaryOperatorKind.ADD, null, null), i, i));
        assertEquals(r, TypeResolver.resultBuiltin(binary(BuiltinBinaryOperatorKind.ADD, null, null), i, r));
        assertEquals(b, TypeResolver.resultBuiltin(binary(BuiltinBinaryOperatorKind.MUL, null, null), b, b));
        assertEquals(BuiltinType.STRING,
            TypeResolver.resultBuiltin(binary(BuiltinBinaryOperatorKind.ADD, null, null), BuiltinType.STRING, i));
        assertEquals(i, TypeResolver.resultBuiltin(binary(BuiltinBinaryOperatorKind.MOD, null, null), i, i));
        // Modelica's / and ^ yield Real whatever the operands.
        assertEquals(r, TypeResolver.resultBuiltin(binary(BuiltinBinaryOperatorKind.DIV, null, null), i, i));
        assertEquals(r, TypeResolver.resultBuiltin(binary(BuiltinBinaryOperatorKind.POW, null, null), i, i));
    }

    // --- T14 -----------------------------------------------------------------------

    /** T14 */
    @Test
    public void twoRegionsOfTheSameOperatorDoNotShareNames() {
        UserOperator g = g();
        Fixture fx = new Fixture(g);
        TranslationResult result = fx.translate(
            variable("r1", BuiltinType.BOOLEAN, and(call(g, element(fx.a)))),
            variable("r2", BuiltinType.BOOLEAN, and(call(g, element(fx.a)))));

        assertFalse(result.hasErrors(), result.diagnostics().toString());
        assertContains(result.text(),
            "parameter Integer op_g_n1 = size(A, 1);",
            "parameter Integer op_g_n2 = size(A, 1);",
            "op_g_out1[i1] =",
            "op_g_out2[i3] =",
            B4 + " r1 = and_acc1[op_g_size1 + 1];",
            B4 + " r2 = and_acc2[op_g_size2 + 1];");
        java.util.Set<String> names = new HashSet<String>();
        for (ComponentDeclaration component : result.modelica().getComponents()) {
            assertTrue(names.add(component.getName()), "declared twice: " + component.getName());
        }
        assertEquals(2, regions(result.modelica()).size());
    }

    // --- T15: aggregators ----------------------------------------------------------

    /** T15 */
    @Test
    public void anAggregatorFoldsALiftedResult() {
        UserOperator g = g();
        Fixture fx = new Fixture(g);
        TranslationResult result = fx.translate("r", and(call(g, element(fx.a))));

        assertFalse(result.hasErrors(), result.diagnostics().toString());
        assertContains(result.text(),
            B4 + " and_acc1[op_g_size1 + 1];",
            "and_acc1[1] = " + UNDEFINED + ";",
            "for i2 in 1:op_g_size1 loop",
            // The mask is already in the values: no "if" here.
            "and_acc1[i2 + 1] = CRMLtoModelica.Functions.and4(and_acc1[i2], op_g_out1[i2]);",
            B4 + " r = and_acc1[op_g_size1 + 1];");
    }

    /** T15 */
    @Test
    public void anAggregatorFoldsASet() {
        Fixture fx = new Fixture();
        BinaryOperator or = binary(BuiltinBinaryOperatorKind.OR, null, refTo(fx.a));
        TranslationResult result = fx.translate("r", or);

        assertFalse(result.hasErrors(), result.diagnostics().toString());
        assertContains(result.text(),
            B4 + " or_acc1[size(A, 1) + 1];",
            "or_acc1[1] = " + UNDEFINED + ";",
            "for i1 in 1:size(A, 1) loop",
            "or_acc1[i1 + 1] = CRMLtoModelica.Functions.or4(or_acc1[i1], A[i1]);",
            B4 + " r = or_acc1[size(A, 1) + 1];");
    }

    /** T15: A.element as the operand of an aggregator is the set A. */
    @Test
    public void anAggregatorFoldsTheElementsOfASet() {
        Fixture fx = new Fixture();
        TranslationResult result = fx.translate("r", and(element(fx.a)));

        assertFalse(result.hasErrors(), result.diagnostics().toString());
        assertContains(result.text(),
            "and_acc1[i1 + 1] = CRMLtoModelica.Functions.and4(and_acc1[i1], A[i1]);");
        assertNoRegion(result.modelica());
    }

    /** T15: and over the Periods of P is ill-typed, and says so. */
    @Test
    public void anAggregatorRejectsElementsThatAreNotBoolean() {
        Fixture fx = new Fixture();
        TranslationResult result = fx.translate("r", and(element(fx.p)));

        Diagnostic diagnostic = diagnostic(result, "BinaryOperator.AND");
        assertEquals(Diagnostic.Severity.ERROR, diagnostic.severity());
        assertTrue(diagnostic.message().contains("PERIOD"), diagnostic.message());
        assertNull(component(result.modelica(), "and_acc1"));
    }

    /**
     * T15: where an inactive slot does not already hold undefined, the fold
     * reads the mask. Here the lifted result is of a class type, so the region
     * cannot know that undefined is a value of it.
     */
    @Test
    public void anAggregatorMasksInactiveSlots() {
        org.openmodelica.crml.model.metamodel.Class k = F.createClass();
        k.setName("K");
        // Operator [ K ] 'id' K a = a;
        Variable a = classVariable("a", k, null);
        UserOperator id = F.createUserOperator();
        id.setDomain(classType(k));
        id.getHeader().add(keyword("'id'"));
        id.getHeader().add(a);
        id.setDefinition(refTo(a));

        Fixture fx = new Fixture(id);
        fx.model.getClasses().add(k);
        Variable k1 = classVariable("k1", k, null);
        Variable k2 = classVariable("k2", k, null);
        org.openmodelica.crml.model.metamodel.Set<Value> elements = F.createSet();
        elements.getElements().add(refTo(k1));
        elements.getElements().add(refTo(k2));
        Variable ks = classVariable("KS", k, elements);
        fx.model.getVariables().add(k1);
        fx.model.getVariables().add(k2);
        fx.model.getVariables().add(ks);

        TranslationResult result = fx.translate("r", and(call(id, element(ks))));

        assertContains(result.text(),
            "K op_id_out1[op_id_size1];",
            // No undefined is written into a result of unknown type ...
            "op_id_out1[i1] = op_id(KS[i1]);",
            // ... so the fold asks the mask.
            "and_acc1[i2 + 1] = CRMLtoModelica.Functions.and4(and_acc1[i2], "
                + "if op_id_mask1[i2] then op_id_out1[i2] else " + UNDEFINED + ");");
    }

    @Test
    public void aggregatorsOtherThanAndAndOrAreNotImplemented() {
        Fixture fx = new Fixture();
        TranslationResult result = fx.translate("r", binary(BuiltinBinaryOperatorKind.ADD, null, refTo(fx.a)));

        Diagnostic diagnostic = diagnostic(result, "BinaryOperator.ADD.aggregate");
        assertEquals(Diagnostic.Severity.WARNING, diagnostic.severity());
        assertFalse(result.hasErrors(), result.diagnostics().toString());
    }

    // --- T16 -----------------------------------------------------------------------

    /** T16 */
    @Test
    public void aModifiedComponentCannotBeDeclaredPerElement() {
        Fixture fx = new Fixture();
        Direct direct = new Direct();
        UnaryOperator application = unary(BuiltinUnaryOperatorKind.NOT, element(fx.a));
        LiftBuilder.linkAll(application);
        Lifter lifter = direct.transformer.liftables().open(direct.ctx, application);
        assertTrue(lifter.body().isLifted());
        assertFalse(lifter.outer().isLifted());
        assertFalse(lifter.body().root().isLifted(), "operator classes are never generated in lifted mode");

        ComponentDeclaration modified = component(this.<ClassDefinition>builtinRef("Real"), "m");
        modified.getModifications().add(mod("start", integer(0)));
        EObject source = application;
        Diagnostic diagnostic = rejection(() -> lifter.body().declare(modified, source));
        assertEquals("Lift.BodyModification", diagnostic.constructKind());
        assertEquals(Diagnostic.Severity.WARNING, diagnostic.severity());
        assertTrue(diagnostic.message().contains("'m'"), diagnostic.message());

        // A binding is fine: it moves into the loop body.
        assertEquals("v[i1]", print(lifter.body().declare(
            component(this.<ClassDefinition>builtinRef("Real"), "v", integer(1)), source)));
        // Nothing reaches the class before close.
        assertTrue(direct.target.getComponents().isEmpty(), direct.text());
        lifter.close(ref("x"), this.<ClassDefinition>builtinRef("Real"), BuiltinType.REAL, source);
        assertContains(direct.text(), "Real v[not_size1];", "v[i1] = 1;", "not_out1[i1] = x;");
    }
}
