package crml.compiler.crmlcv2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import crml.model.language.BinaryOperator;
import crml.model.language.BuiltinBinaryOperatorKind;
import crml.model.language.BuiltinType;
import crml.model.language.BuiltinTypeReference;
import crml.model.language.BuiltinUnaryOperatorKind;
import crml.model.language.DurationValue;
import crml.model.language.LanguageFactory;
import crml.model.language.Model;
import crml.model.language.ProjectionValue;
import crml.model.language.SequenceValue;
import crml.model.language.UnaryOperator;
import crml.model.language.Value;
import crml.model.language.Variable;
import crml.model.language.VariableReference;

/**
 * The guarantee behind decision #3 of the plan: every CRML construct is reached
 * and produces either Modelica or a recorded diagnostic. Nothing throws out of
 * the transformation and nothing is skipped in silence.
 */
public class DiagnosticFloorTest {

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

    /** Each of these has no Modelica target; all of them must be diagnosed, none may throw. */
    private static List<Value> unmappedValues(Variable operand) {
        List<Value> values = new ArrayList<Value>();

        BinaryOperator at = F.createBinaryOperator();
        at.setOptype(BuiltinBinaryOperatorKind.AT);
        at.setLhs(refTo(operand));
        at.setRhs(refTo(operand));
        values.add(at);

        BinaryOperator with = F.createBinaryOperator();
        with.setOptype(BuiltinBinaryOperatorKind.WITH);
        with.setLhs(refTo(operand));
        with.setRhs(refTo(operand));
        values.add(with);

        UnaryOperator pre = F.createUnaryOperator();
        pre.setOptype(BuiltinUnaryOperatorKind.PRE);
        pre.setValue(refTo(operand));
        values.add(pre);

        DurationValue duration = F.createDurationValue();
        duration.setExp1(refTo(operand));
        duration.setExp2(refTo(operand));
        values.add(duration);

        ProjectionValue projection = F.createProjectionValue();
        projection.setP1(refTo(operand));
        projection.setP2(refTo(operand));
        values.add(projection);

        SequenceValue sequence = F.createSequenceValue();
        sequence.setValue(refTo(operand));
        values.add(sequence);

        return values;
    }

    @Test
    public void everyUnmappedValueIsDiagnosedAndMarkedInTheOutput() {
        Variable b = variable("b", BuiltinType.BOOLEAN, null);
        Model model = F.createModel();
        model.setName("M");
        model.getVariables().add(b);

        List<Value> values = unmappedValues(b);
        for (int i = 0; i < values.size(); i++) {
            model.getVariables().add(variable("v" + i, BuiltinType.BOOLEAN, values.get(i)));
        }

        TranslationResult result = new OMCv2().translateModel(model);

        // One diagnostic per unmapped value, and none of them is an ERROR: each
        // is legal CRML that CRMLtoModelica.mo has no implementation for.
        assertEquals(values.size(), result.diagnostics().size(), result.diagnostics().toString());
        assertFalse(result.hasErrors(), result.diagnostics().toString());

        String text = result.text();
        for (int i = 0; i < values.size(); i++) {
            // The declaration is dropped - an expression hole cannot produce
            // valid Modelica - but the gap is visible in the .mo.
            assertFalse(text.contains("v" + i + " ="), text);
        }
        assertEquals(values.size(), countOccurrences(text, "// "), text);
        // Everything the model could translate still did.
        assertTrue(text.contains("CRMLtoModelica.Types.Boolean4 b;"), text);
    }

    /** Model-level constructs with no mapping are reached too, not skipped. */
    @Test
    public void unmappedModelLevelConstructsAreDiagnosed() {
        Model model = F.createModel();
        model.setName("M");

        crml.model.language.Category category = F.createCategory();
        category.setName("C");
        model.getOperators().add(category);

        crml.model.language.ModelDependency dependency = F.createModelDependency();
        model.getSuperlibs().add(dependency);

        TranslationResult result = new OMCv2().translateModel(model);

        Set<String> kinds = new HashSet<String>();
        for (Diagnostic diagnostic : result.diagnostics()) {
            kinds.add(diagnostic.constructKind());
        }
        assertTrue(kinds.containsAll(Arrays.asList("Operator.Category", "Library.superlibs")), kinds.toString());
        assertEquals(2, countOccurrences(result.text(), "// "), result.text());
    }

    /** A hole in one declaration must not cost the declarations around it. */
    @Test
    public void onlyTheEnclosingDeclarationIsDropped() {
        Variable b = variable("b", BuiltinType.BOOLEAN, null);

        BinaryOperator at = F.createBinaryOperator();
        at.setOptype(BuiltinBinaryOperatorKind.AT);
        at.setLhs(refTo(b));
        at.setRhs(refTo(b));

        Model model = F.createModel();
        model.setName("M");
        model.getVariables().add(b);
        model.getVariables().add(variable("broken", BuiltinType.BOOLEAN, at));
        model.getVariables().add(variable("fine", BuiltinType.BOOLEAN, null));

        String text = new OMCv2().translate(model);

        assertTrue(text.contains("CRMLtoModelica.Types.Boolean4 b;"), text);
        assertTrue(text.contains("CRMLtoModelica.Types.Boolean4 fine;"), text);
        // No declaration for it, but the placeholder names it, so the gap is
        // attributable rather than invisible.
        assertFalse(text.contains("Boolean4 broken"), text);
        assertTrue(text.contains("// BinaryOperator.AT: variable 'broken' was dropped"), text);
    }

    private static int countOccurrences(String text, String needle) {
        int count = 0;
        for (int i = text.indexOf(needle); i >= 0; i = text.indexOf(needle, i + needle.length())) {
            count++;
        }
        return count;
    }
}
