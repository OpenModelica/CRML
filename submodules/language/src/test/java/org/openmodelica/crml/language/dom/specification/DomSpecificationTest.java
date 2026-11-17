package org.openmodelica.crml.language.dom.specification;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.Assumptions;
import org.openmodelica.crml.language.util.CRMLSyntaxResultsWrapper;
import org.openmodelica.crml.language.util.DOMWrapper;
import org.openmodelica.crml.language.util.DiagnosticsWrapper;
import org.openmodelica.crml.language.util.DomRun;
import org.openmodelica.crml.language.workspace.InspectingWorkspace.Stage;
import org.openmodelica.crml.testutil.ModelExpectation;
import org.openmodelica.crml.testutil.ReportedTest;

/**
 * The check shared by the DOM specification tests: a model is parsed, built
 * and linked (operator calls are not resolved here), and the report shows the
 * model, its syntax errors, AST, DOM and errors.
 */
public abstract class DomSpecificationTest extends ReportedTest {

    /**
     * Parses, builds and links {@code fileName}. A correct model must do so
     * without errors; for other models, only the report is made, since
     * semantic validation does not exist yet.
     */
    protected void checkDom(final Path fileName, final ModelExpectation expectation, final Boolean isDisabled)
            throws IOException {
        emit(fileName, "CRML model");
        Assumptions.assumeFalse(isDisabled);

        DomRun run = DomRun.of(fileName, Stage.LINK);
        emit(CRMLSyntaxResultsWrapper.of(run.parsed().syntax()), "Syntax Errors");
        emit(run.parsed().toPrettyTree(), "AST");

        Assumptions.assumeFalse(run.parsed().syntax().hasErrors(), "Model failed to parse");

        if (run.dom() != null) {
            emit(DOMWrapper.of(run.dom()), "DOM");
        }
        if (!run.errors().isEmpty()) {
            emit(DiagnosticsWrapper.of(run.errors()), "Error");
        }

        // TODO: check that validation rejects the model once it is implemented.
        Assumptions.assumeTrue(expectation == ModelExpectation.CORRECT,
                "Semantic validation is not implemented yet");
        assertTrue(run.errors().isEmpty(), "DOM translation reported errors:" + run.errorText());
    }
}
