package org.openmodelica.crml.compiler.crmlcv2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.resource.Resource;
import org.junit.jupiter.api.Test;
import org.openmodelica.crml.language.workspace.CrmlDiagnostic;
import org.openmodelica.crml.language.workspace.CrmlResource;
import org.openmodelica.crml.language.workspace.CrmlWorkspace;
import org.openmodelica.crml.language.workspace.SourcePosition;
import org.openmodelica.crml.model.metamodel.BinaryOperator;
import org.openmodelica.crml.model.metamodel.Model;

/**
 * A dropped element fails the translation, and the error is marked on the
 * CRML object that triggered it: on its resource, at its position.
 */
public class TranslationErrorMarkingTest {

    /** Line 5 holds the one variable that cannot be translated: CRMLtoModelica.mo has no 'at'. */
    private static final String SOURCE = "model M is {\n"
        + "\tBoolean b is external;\n"
        + "\tEvent e is external;\n"
        + "\tBoolean kept is not b;\n"
        + "\tBoolean sampled is b at e;\n"
        + "};\n";

    private static CrmlResource load() {
        CrmlWorkspace workspace = new CrmlWorkspace();
        CrmlResource resource = workspace.load(SOURCE);
        workspace.resolve();
        assertTrue(resource.getErrors().isEmpty(), resource.getErrors().toString());
        return resource;
    }

    private static List<CrmlDiagnostic> translationErrors(CrmlResource resource) {
        List<CrmlDiagnostic> found = new ArrayList<CrmlDiagnostic>();
        for (Resource.Diagnostic error : resource.getErrors()) {
            if (error instanceof CrmlDiagnostic && ((CrmlDiagnostic) error).getKind() == CrmlDiagnostic.Kind.TRANSLATION) {
                found.add((CrmlDiagnostic) error);
            }
        }
        return found;
    }

    @Test
    public void aDroppedVariableIsAnErrorAtItsTrigger() {
        CrmlResource resource = load();

        TranslationResult result = new OMCv2().translateModel((Model) resource.getRoot());

        // The translation fails, with the reason kept.
        assertTrue(result.hasErrors(), result.diagnostics().toString());
        assertEquals(1, result.diagnostics().size(), result.diagnostics().toString());
        Diagnostic diagnostic = result.diagnostics().get(0);
        assertEquals(Diagnostic.Severity.ERROR, diagnostic.severity());
        assertEquals(Diagnostic.Category.UNSUPPORTED, diagnostic.category());
        assertTrue(diagnostic.crmlSource() instanceof BinaryOperator, String.valueOf(diagnostic.crmlSource()));

        // Its trigger is found in the text through the source map: 'b at e' on line 5.
        SourcePosition position = SourcePosition.of(diagnostic.crmlSource());
        assertNotNull(position, "the trigger was loaded from the text");
        assertEquals(5, position.getLine());
        assertEquals(20, position.getColumn());

        // The trigger is marked in the CRML model, at the same position.
        List<CrmlDiagnostic> marks = translationErrors(resource);
        assertEquals(1, marks.size(), resource.getErrors().toString());
        assertEquals(position.getLine(), marks.get(0).getLine());
        assertEquals(position.getColumn(), marks.get(0).getColumn());
        assertTrue(marks.get(0).getMessage().contains("BinaryOperator.AT"), marks.get(0).getMessage());
        assertTrue(resource.isComplete(), "a translation error leaves the DOM complete");

        // The rest is still translated, and the gap is still visible in the .mo.
        String text = result.text();
        assertTrue(text.contains("kept ="), text);
        assertTrue(text.contains("// BinaryOperator.AT: variable 'sampled' was dropped"), text);
    }

    @Test
    public void translatingAgainMarksTheTriggerOnce() {
        CrmlResource resource = load();
        Model model = (Model) resource.getRoot();

        new OMCv2().translateModel(model);
        TranslationResult again = new OMCv2().translateModel(model);

        assertTrue(again.hasErrors());
        assertEquals(1, translationErrors(resource).size(), resource.getErrors().toString());
    }
}
