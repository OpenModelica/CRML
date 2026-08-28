package crml.compiler.omcv2.specification;

import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import crml.compiler.omcv2.OMCv2;
import crml.compiler.util.CodeWrapper;
import crml.compiler.util.CompilerRoot;
import crml.compiler.util.ObjectModelWrapper;
import crml.compiler.util.ThrowableWrapper;
import crml.language.dom.DOMVisitor;
import crml.language.dom.util.BuildResult;
import crml.language.util.Parser;
import crml.model.language.Model;
import crml.test.ReportedTest;
import crml.test.TestResourcesRoot;

/**
 * Base for the OMCv2 (OM to Modelica) specification report: parses a CRML model,
 * builds its object model and feeds it to {@link OMCv2}, reporting the object
 * model and the generated Modelica as code blocks. Subclasses only provide the
 * subset of {@link #SPEC_DOC_EXAMPLES} they cover via a static {@code fileNameSource()}.
 */
public abstract class OMCv2SpecificationTest extends ReportedTest {
    protected static final Path SPEC_DOC_EXAMPLES =
        CompilerRoot.RESOURCES.resolve("testModels").resolve("spec-doc-examples");

    protected static List<Path> docExamples(String prefix) {
        return TestResourcesRoot.listFiles(SPEC_DOC_EXAMPLES,
            f -> f.getFileName().toString().startsWith(prefix));
    }

    @ParameterizedTest
    @MethodSource("fileNameSource")
    public void translateToModelica(final Path fileName) throws IOException {
        emit(fileName, "CRML model");

        Parser.ParserResult parsed = new Parser().parse(fileName);
        Assumptions.assumeFalse(parsed.syntax().hasErrors(), "Model failed to parse");

        Throwable error = null;
        EObject dom = null;
        try {
            DOMVisitor visitor = new DOMVisitor();
            BuildResult result = visitor.build(parsed.ast());
            if (result instanceof BuildResult.SingleBuildResult) {
                dom = ((BuildResult.SingleBuildResult<?>) result).result();
            }
            visitor.linker();
        } catch (Throwable e) {
            error = e;
        }

        if (dom != null) {
            emit(ObjectModelWrapper.of(dom), "Object Model");
        }

        if (error == null) {
            Assumptions.assumeTrue(dom instanceof Model, "Definition is not a model (e.g. a library)");
            try {
                String modelica = new OMCv2().translate((Model) dom);
                emit(CodeWrapper.of(modelica), "Modelica");
            } catch (Throwable e) {
                error = e;
            }
        }

        if (error != null) {
            emit(ThrowableWrapper.of(error), "Error");
        }

        assertNull(error, "OMCv2 translation threw an exception");
    }
}
