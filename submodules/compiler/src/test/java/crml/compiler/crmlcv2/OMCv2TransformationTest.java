package crml.compiler.crmlcv2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import crml.model.language.BinaryOperator;
import crml.model.language.BooleanConstant;
import crml.model.language.BooleanLiteral;
import crml.model.language.BuiltinBinaryOperatorKind;
import crml.model.language.BuiltinType;
import crml.model.language.BuiltinTypeReference;
import crml.model.language.ConstructorValue;
import crml.model.language.LanguageFactory;
import crml.model.language.Model;
import crml.model.language.Value;
import crml.model.language.Variable;
import crml.model.language.VariableReference;
import crml.model.trace.TraceLink;
import crml.model.trace.TraceLinkKind;

/**
 * Unit-level checks of the object-model transformation, on hand-built CRML
 * models. The specification report covers the corpus; this covers the
 * properties that report cannot assert directly.
 */
public class OMCv2TransformationTest {

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

    @Test
    public void marksAModelWithUnboundVariablesPartial() {
        Variable unbound = variable("b", BuiltinType.BOOLEAN, null);
        String text = new OMCv2().translate(model("M", unbound));
        assertTrue(text.startsWith("partial model M"), text);
    }

    @Test
    public void leavesAFullyBoundModelNonPartial() {
        BooleanConstant constant = F.createBooleanConstant();
        constant.setValue(BooleanLiteral.TRUE);
        String text = new OMCv2().translate(model("M", variable("b", BuiltinType.BOOLEAN, constant)));
        assertTrue(text.startsWith("model M"), text);
        assertFalse(text.contains("partial"), text);
    }

    @Test
    public void emitsNoEquationKeywordWithoutEquations() {
        String text = new OMCv2().translate(model("M", variable("b", BuiltinType.BOOLEAN, null)));
        assertFalse(text.contains("equation"), text);
    }

    /**
     * The record and its {@code _build} companion must both keep their
     * modification values. Each of the two declarations needs its own reference
     * node: EMF containment means handing the same node to a second parent
     * silently removes it from the first.
     */
    @Test
    public void clockConstructorKeepsBothModificationValues() {
        Variable b = variable("b", BuiltinType.BOOLEAN, null);
        ConstructorValue constructor = F.createConstructorValue();
        constructor.setDomain(type(BuiltinType.CLOCK));
        constructor.setValue(refTo(b));

        String text = new OMCv2().translate(model("M", b, variable("c", BuiltinType.CLOCK, constructor)));

        assertTrue(text.contains("CRMLtoModelica.Types.CRMLClock c1(b = b);"), text);
        assertTrue(text.contains("CRMLtoModelica.Types.CRMLClock_build c1_init(clock = c1);"), text);
        assertTrue(text.contains("CRMLtoModelica.Types.CRMLClock c = c1;"), text);
    }

    /**
     * Allocated names restart for each translation. The generator this replaces
     * held a static allocator, so a model's generated names depended on how many
     * models had been compiled earlier in the same JVM.
     */
    @Test
    public void allocatedNamesAreIndependentOfEarlierTranslations() {
        String first = translateOneClockModel();
        String second = translateOneClockModel();
        assertEquals(first, second);
        assertTrue(first.contains("c1"), first);
    }

    private static String translateOneClockModel() {
        Variable b = variable("b", BuiltinType.BOOLEAN, null);
        ConstructorValue constructor = F.createConstructorValue();
        constructor.setDomain(type(BuiltinType.CLOCK));
        constructor.setValue(refTo(b));
        return new OMCv2().translate(model("M", b, variable("c", BuiltinType.CLOCK, constructor)));
    }

    @Test
    public void recordsATraceLinkForEveryDeclarationAndTheModelItself() {
        Variable b = variable("b", BuiltinType.BOOLEAN, null);
        Model source = model("M", b);
        TranslationResult result = new OMCv2().translateModel(source);

        boolean sawModel = false;
        boolean sawVariable = false;
        for (TraceLink link : result.trace().getLinks()) {
            if (link.getKind() == TraceLinkKind.MODEL_TO_CLASS && link.getCrml() == source) {
                sawModel = true;
            }
            if (link.getKind() == TraceLinkKind.VARIABLE_TO_COMPONENT && link.getCrml() == b) {
                sawVariable = true;
            }
        }
        assertTrue(sawModel, "no MODEL_TO_CLASS link");
        assertTrue(sawVariable, "no VARIABLE_TO_COMPONENT link");
    }

    /**
     * An unmapped construct is reached, diagnosed and marked in the output
     * instead of aborting the translation of the whole model.
     */
    @Test
    public void unmappedConstructIsDiagnosedRatherThanThrown() {
        Variable b = variable("b", BuiltinType.BOOLEAN, null);
        Variable e = variable("e", BuiltinType.EVENT, null);

        BinaryOperator at = F.createBinaryOperator();
        at.setOptype(BuiltinBinaryOperatorKind.AT);
        at.setLhs(refTo(b));
        at.setRhs(refTo(e));

        TranslationResult result = new OMCv2().translateModel(
            model("M", b, e, variable("b_at_e", BuiltinType.BOOLEAN, at)));

        assertEquals(1, result.diagnostics().size(), result.diagnostics().toString());
        Diagnostic diagnostic = result.diagnostics().get(0);
        assertEquals("BinaryOperator.AT", diagnostic.constructKind());
        assertEquals(Diagnostic.Severity.WARNING, diagnostic.severity());

        String text = result.text();
        // The other two variables survive; only the one with the hole is dropped.
        assertTrue(text.contains("CRMLtoModelica.Types.Boolean4 b;"), text);
        assertTrue(text.contains("CRMLtoModelica.Types.Event e;"), text);
        assertFalse(text.contains("b_at_e ="), text);
        assertTrue(text.contains("// BinaryOperator.AT:"), text);
    }
}
