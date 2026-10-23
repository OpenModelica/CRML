package crml.compiler.crmlcv2.specification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import crml.compiler.crmlcv2.Diagnostic;
import crml.compiler.crmlcv2.TranslationResult;
import crml.compiler.util.CodeWrapper;
import crml.compiler.util.CompilerRoot;
import crml.compiler.util.CrmlFrontEnd;
import crml.compiler.util.DiagnosticsWrapper;
import crml.compiler.util.ObjectModelWrapper;
import crml.compiler.util.ThrowableWrapper;
import crml.compiler.util.TraceWrapper;
import crml.test.ReportedTest;
import crml.test.TestResourcesRoot;

/**
 * Base for the OMCv2 (OM to Modelica) specification report: parses a CRML model,
 * builds its object model and feeds it to {@link crml.compiler.crmlcv2.OMCv2}, reporting the object
 * model, the generated Modelica, the CRML-to-Modelica trace and the diagnostics.
 * Subclasses only provide the subset of {@link #SPEC_DOC_EXAMPLES} they cover via
 * a static {@code fileNameSource()}.
 *
 * <p>The translation no longer throws when it meets a construct it cannot map,
 * so "did not throw" would be a vacuous assertion. What is asserted instead is
 * that no ERROR diagnostic was recorded: by the severity convention on
 * {@link Diagnostic.Severity}, an ERROR means a construct that does have a
 * Modelica mapping and is implemented here still failed to translate - a defect
 * - while a construct CRML allows but CRMLtoModelica.mo (or this milestone) does
 * not implement is a WARNING, and shows up in the diagnostics table.
 */
public abstract class OMCv2SpecificationTest extends ReportedTest {
    protected static final Path TEST_MODELS = CompilerRoot.RESOURCES.resolve("testModels");
    protected static final Path SPEC_DOC_EXAMPLES = TEST_MODELS.resolve("spec-doc-examples");

    protected static List<Path> docExamples(String prefix) {
        return TestResourcesRoot.listFiles(SPEC_DOC_EXAMPLES,
            f -> f.getFileName().toString().startsWith(prefix));
    }

    /** Every .crml directly under {@code testModels/<first>/<rest...>}. */
    protected static List<Path> modelsIn(String first, String... rest) {
        Path directory = TEST_MODELS.resolve(first);
        for (String segment : rest) {
            directory = directory.resolve(segment);
        }
        return CompilerRoot.fileNameSourceHelper(directory);
    }

    @ParameterizedTest
    @MethodSource("fileNameSource")
    public void translateToModelica(final Path fileName) throws IOException {
        emit(fileName, "CRML model");

        CrmlFrontEnd run = CrmlFrontEnd.run(fileName);
        if (run.hasSyntaxErrors()) {
            emit(CodeWrapper.of(String.join(System.lineSeparator(), run.syntaxErrors())), "Syntax errors");
        }
        Assumptions.assumeFalse(run.hasSyntaxErrors(), "Model failed to parse");

        if (run.dom() != null) {
            emit(ObjectModelWrapper.of(run.dom()), "Object Model");
        }
        if (run.error() == null) {
            Assumptions.assumeTrue(run.model() != null, "Definition is not a model (e.g. a library)");
        }

        TranslationResult result = run.translation();
        if (result != null) {
            emit(CodeWrapper.of(result.text()), "Modelica");
            emit(TraceWrapper.of(result.trace()), "Trace");
            emit(DiagnosticsWrapper.of(result.diagnostics()), "Diagnostics");
        }
        if (run.error() != null) {
            emit(ThrowableWrapper.of(run.error()), "Error");
        }

        List<Diagnostic> errors = run.errors();
        assertNull(run.error(), "OMCv2 translation threw an exception");
        assertEquals(0, errors.size(), "OMCv2 recorded ERROR diagnostics: " + errors);
    }
}
