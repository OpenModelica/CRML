package crml.compiler.crmlcv2;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import crml.model.language.BinaryOperator;
import crml.model.language.BuiltinBinaryOperatorKind;
import crml.model.language.BuiltinType;
import crml.model.language.BuiltinTypeReference;
import crml.model.language.BuiltinUnaryOperatorKind;
import crml.model.language.IntegrateValue;
import crml.model.language.LanguageFactory;
import crml.model.language.Model;
import crml.model.language.PeriodsValue;
import crml.model.language.UnaryOperator;
import crml.model.language.Value;
import crml.model.language.Variable;
import crml.model.language.VariableReference;

/**
 * The constructs that cannot stay inside an expression: they instantiate a
 * library block, or build a record together with its {@code _build} companion,
 * and the expression becomes a reference to what was hoisted.
 *
 * <p>Several of these are unreachable from the corpus because crml.g4 rejects
 * the syntax that produces them - "c filter (...)" is not parsed inside an
 * operator body or a variable definition - so they are exercised on hand-built
 * object models here.
 */
public class BlockBackedConstructsTest {

    private static final LanguageFactory F = LanguageFactory.eINSTANCE;

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

    private static Model model(String name, Variable... variables) {
        Model model = F.createModel();
        model.setName(name);
        for (Variable variable : variables) {
            model.getVariables().add(variable);
        }
        return model;
    }

    private static UnaryOperator unary(BuiltinUnaryOperatorKind kind, Value operand) {
        UnaryOperator op = F.createUnaryOperator();
        op.setOptype(kind);
        op.setValue(operand);
        return op;
    }

    @Test
    public void cardInstantiatesCardClock() {
        Variable c = variable("c", BuiltinType.CLOCK, null);
        String text = new OMCv2().translate(
            model("M", c, variable("n", BuiltinType.INTEGER, unary(BuiltinUnaryOperatorKind.CARD, refTo(c)))));

        assertTrue(text.contains("CRMLtoModelica.Blocks.CardClock card1;"), text);
        assertTrue(text.contains("card1.r1 = c;"), text);
        assertTrue(text.contains("Integer n = card1.out;"), text);
    }

    @Test
    public void tickInstantiatesClockTick() {
        Variable c = variable("c", BuiltinType.CLOCK, null);
        String text = new OMCv2().translate(
            model("M", c, variable("e", BuiltinType.EVENT, unary(BuiltinUnaryOperatorKind.TICK, refTo(c)))));

        assertTrue(text.contains("CRMLtoModelica.Blocks.ClockTick tick1;"), text);
        assertTrue(text.contains("tick1.r1 = c;"), text);
        assertTrue(text.contains("CRMLtoModelica.Types.Event e = tick1.out;"), text);
    }

    /** card over anything but a clock has no implementation to call. */
    @Test
    public void cardOverANonClockIsDiagnosed() {
        Variable s = variable("S", BuiltinType.REAL, null);
        TranslationResult result = new OMCv2().translateModel(
            model("M", s, variable("n", BuiltinType.INTEGER, unary(BuiltinUnaryOperatorKind.CARD, refTo(s)))));

        assertFalse(result.hasErrors(), result.diagnostics().toString());
        assertTrue(result.diagnostics().toString().contains("set cardinality"), result.diagnostics().toString());
        assertFalse(result.text().contains("CardClock"), result.text());
    }

    @Test
    public void filterInstantiatesEventFilter() {
        Variable c = variable("c", BuiltinType.CLOCK, null);
        Variable b = variable("b", BuiltinType.BOOLEAN, null);

        BinaryOperator filter = F.createBinaryOperator();
        filter.setOptype(BuiltinBinaryOperatorKind.FILTER);
        filter.setLhs(refTo(c));
        filter.setRhs(refTo(b));

        String text = new OMCv2().translate(model("M", c, b, variable("f", BuiltinType.CLOCK, filter)));

        assertTrue(text.contains("CRMLtoModelica.Blocks.EventFilter filter1;"), text);
        assertTrue(text.contains("filter1.r1 = c;"), text);
        assertTrue(text.contains("filter1.r2 = b;"), text);
        assertTrue(text.contains("CRMLtoModelica.Types.CRMLClock f = filter1.out;"), text);
    }

    /** Integrate's third input has a default upstream, so it is left unbound. */
    @Test
    public void integrationInstantiatesIntegrate() {
        Variable a = variable("a", BuiltinType.BOOLEAN, null);
        Variable p = variable("P", BuiltinType.PERIOD, null);

        IntegrateValue integrate = F.createIntegrateValue();
        integrate.setIntegrand(refTo(a));
        integrate.setInterval(refTo(p));

        String text = new OMCv2().translate(model("M", a, p, variable("r", BuiltinType.BOOLEAN, integrate)));

        assertTrue(text.contains("CRMLtoModelica.Blocks.Integrate integrate1;"), text);
        assertTrue(text.contains("integrate1.r1 = a;"), text);
        assertTrue(text.contains("integrate1.r2 = P;"), text);
        assertFalse(text.contains("integrate1.a ="), text);
    }

    /**
     * A period literal over events builds a CRMLPeriod, whose two boundary
     * fields are named start_event/close_event and typed Types.Event - all three
     * of PeriodsGen's defects.
     */
    @Test
    public void aPeriodOverEventsBuildsACRMLPeriod() {
        Variable e1 = variable("e1", BuiltinType.EVENT, null);
        Variable e2 = variable("e2", BuiltinType.EVENT, null);

        PeriodsValue periods = F.createPeriodsValue();
        periods.setIsStartInclusive(Boolean.TRUE);
        periods.setIsEndInclusive(Boolean.FALSE);
        periods.setStartValue(refTo(e1));
        periods.setEndValue(refTo(e2));

        String text = new OMCv2().translate(model("M", e1, e2, variable("P", BuiltinType.PERIOD, periods)));

        assertTrue(text.contains("CRMLtoModelica.Types.CRMLPeriod p1(isLeftBoundaryIncluded = true, "
            + "isRightBoundaryIncluded = false, start_event = e1, close_event = e2);"), text);
        assertTrue(text.contains("CRMLtoModelica.Types.CRMLPeriod_build p1_init(P = p1);"), text);
        assertTrue(text.contains("CRMLtoModelica.Types.CRMLPeriod P = p1;"), text);
    }

    /** A period literal over clocks builds a CRMLPeriods, whose boundaries are clocks. */
    @Test
    public void aPeriodOverClocksBuildsACRMLPeriods() {
        Variable c1 = variable("c1", BuiltinType.CLOCK, null);
        Variable c2 = variable("c2", BuiltinType.CLOCK, null);

        PeriodsValue periods = F.createPeriodsValue();
        periods.setIsStartInclusive(Boolean.TRUE);
        periods.setIsEndInclusive(Boolean.TRUE);
        periods.setStartValue(refTo(c1));
        periods.setEndValue(refTo(c2));

        String text = new OMCv2().translate(model("M", c1, c2, variable("P", BuiltinType.PERIODS, periods)));

        assertTrue(text.contains("CRMLtoModelica.Types.CRMLPeriods ps1(isLeftBoundaryIncluded = true, "
            + "isRightBoundaryIncluded = true, start_event = c1, close_event = c2);"), text);
        assertTrue(text.contains("CRMLtoModelica.Types.CRMLPeriods_build ps1_init(ps = ps1);"), text);
    }

    /** A Boolean boundary is wrapped in an Event construction first. */
    @Test
    public void aBooleanPeriodBoundaryIsWrappedInAnEvent() {
        Variable b1 = variable("b1", BuiltinType.BOOLEAN, null);
        Variable b2 = variable("b2", BuiltinType.BOOLEAN, null);

        PeriodsValue periods = F.createPeriodsValue();
        periods.setIsStartInclusive(Boolean.TRUE);
        periods.setIsEndInclusive(Boolean.TRUE);
        periods.setStartValue(refTo(b1));
        periods.setEndValue(refTo(b2));

        String text = new OMCv2().translate(model("M", b1, b2, variable("P", BuiltinType.PERIOD, periods)));

        assertTrue(text.contains("CRMLtoModelica.Types.Event e1(b = b1);"), text);
        assertTrue(text.contains("CRMLtoModelica.Types.Event e2(b = b2);"), text);
        assertTrue(text.contains("start_event = e1, close_event = e2"), text);
    }

    /**
     * A generated name never takes a name the model itself uses: a model with
     * its own variable "c1" must not produce "c1 = c1".
     */
    @Test
    public void generatedNamesAvoidTheModelsOwnNames() {
        Variable b = variable("b", BuiltinType.BOOLEAN, null);
        crml.model.language.ConstructorValue constructor = F.createConstructorValue();
        constructor.setDomain(type(BuiltinType.CLOCK));
        constructor.setValue(refTo(b));

        String text = new OMCv2().translate(model("M", b, variable("c1", BuiltinType.CLOCK, constructor)));

        assertFalse(text.contains("c1 = c1;"), text);
        assertTrue(text.contains("CRMLtoModelica.Types.CRMLClock c2(b = b);"), text);
        assertTrue(text.contains("CRMLtoModelica.Types.CRMLClock c1 = c2;"), text);
    }

    /** A constructor with an argument list instantiates a class. */
    @Test
    public void aConstructorWithBindingsBecomesAComponentWithModifications() {
        Variable member = variable("consumer", BuiltinType.BOOLEAN, null);
        Variable source = variable("b", BuiltinType.BOOLEAN, null);

        crml.model.language.Class clazz = F.createClass();
        clazz.setName("Contract");
        clazz.getVariables().add(member);

        // A type reference per use: TypeReference is a contained feature, so one
        // shared node would be moved out of the first owner by the second.
        crml.model.language.UserTypereference constructedType = F.createUserTypereference();
        constructedType.setDomain(clazz);
        crml.model.language.UserTypereference declaredType = F.createUserTypereference();
        declaredType.setDomain(clazz);

        crml.model.language.ConstructorValue constructor = F.createConstructorValue();
        constructor.setDomain(constructedType);
        crml.model.language.Binding binding = F.createBinding();
        binding.setElement(member);
        binding.setValue(refTo(source));
        constructor.getBindings().add(binding);

        Variable instance = F.createVariable();
        instance.setName("contract");
        instance.setDomain(declaredType);
        instance.setDefinition(constructor);

        Model model = model("M", source, instance);
        model.getClasses().add(clazz);

        String text = new OMCv2().translate(model);

        assertTrue(text.contains("Contract inst1(consumer = b);"), text);
        assertTrue(text.contains("Contract contract = inst1;"), text);
    }
}
