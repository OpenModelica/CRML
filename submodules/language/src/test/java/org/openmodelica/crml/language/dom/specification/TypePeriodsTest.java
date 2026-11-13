package org.openmodelica.crml.language.dom.specification;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openmodelica.crml.language.dom.DOMVisitor;
import org.openmodelica.crml.language.dom.util.BuildResult;
import org.openmodelica.crml.language.util.CRMLSyntaxResultsWrapper;
import org.openmodelica.crml.language.util.DOMWrapper;
import org.openmodelica.crml.language.util.Parser;
import org.openmodelica.crml.language.util.SpecsRoot;
import org.openmodelica.crml.language.util.ThrowableWrapper;
import org.openmodelica.crml.testresources.TestResourcesRoot;
import org.openmodelica.crml.testutil.ModelExpectation;
import org.openmodelica.crml.testutil.ReportedTest;

public class TypePeriodsTest extends ReportedTest {

    static List<Arguments> fileNameSource() {
        List<Arguments> tests = new ArrayList<>();
        tests.addAll(ReportedTest.fileNameSourceHelperDom(SpecsRoot.RESOURCES.resolve("periods")));
        tests.addAll(getDocExamples());
        return tests;
    }

    static List<Arguments> getDocExamples() {
        List<Arguments> tests = new ArrayList<>();
        TestResourcesRoot.listFiles(TestResourcesRoot.RESOURCES.resolve("testModels/spec-doc-examples"), f -> {
            return f.getFileName().toString().startsWith("Periods");
        }).forEach(f -> tests.add(Arguments.of(f, ModelExpectation.CORRECT, false)));
        return tests;
    }

    @ParameterizedTest
    @MethodSource("fileNameSource")
    void translateToDom(final Path fileName, final ModelExpectation expectation, final Boolean isDisabled) throws IOException {
        emit(fileName, "CRML model");
        Assumptions.assumeFalse(isDisabled);

        Parser.ParserResult parsed = new Parser().parse(fileName);
        emit(CRMLSyntaxResultsWrapper.of(parsed.syntax()), "Syntax Errors");
        emit(parsed.toPrettyTree(), "AST");

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
            emit(DOMWrapper.of(dom), "DOM");
        }
        if (error != null) {
            emit(ThrowableWrapper.of(error), "Error");
        }

        // TODO: check that validation rejects the model once it is implemented.
        Assumptions.assumeTrue(expectation == ModelExpectation.CORRECT,
                "Semantic validation is not implemented yet");
        assertNull(error, "DOM translation threw an exception");
    }
}
