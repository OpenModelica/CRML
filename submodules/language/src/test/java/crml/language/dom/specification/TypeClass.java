package crml.language.dom.specification;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import crml.language.dom.DOMVisitor;
import crml.language.dom.util.BuildResult;
import crml.language.util.DOMWrapper;
import crml.language.util.CRMLSyntaxResultsWrapper;
import crml.language.util.ThrowableWrapper;
import crml.language.util.Parser;
import crml.language.util.SpecsRoot;
import crml.test.ReportedTest;

public class TypeClass extends ReportedTest {

    static List<Arguments> fileNameSource() {
        return ReportedTest.fileNameSourceHelper2(SpecsRoot.RESOURCES.resolve("class"));
    }

    @ParameterizedTest
    @MethodSource("fileNameSource")
    public void translateToDom(final Path fileName, final Boolean isValid, final Boolean isDisabled) throws IOException {
        emit(fileName, "CRML model");
        Assumptions.assumeFalse(isDisabled);
        Assumptions.assumeTrue(isValid);

        Parser.ParserResult parsed = new Parser().parse(fileName);
        emit(CRMLSyntaxResultsWrapper.of(parsed.syntax()), "Syntax Errors");
        emit(parsed.toPrettyTree(), "AST");

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
            emit(DOMWrapper.of(dom), "DOM");
        }
        if (error != null) {
            emit(ThrowableWrapper.of(error), "Error");
        }

        assertNull(error, "DOM translation threw an exception");
    }
}
