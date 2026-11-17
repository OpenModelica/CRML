package org.openmodelica.crml.language.dom.specification;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openmodelica.crml.language.util.SpecsRoot;
import org.openmodelica.crml.testresources.TestResourcesRoot;
import org.openmodelica.crml.testutil.ModelExpectation;
import org.openmodelica.crml.testutil.ReportedTest;

public class TypeIntegerTest extends DomSpecificationTest {

    static List<Arguments> fileNameSource() {
        List<Arguments> tests = new ArrayList<>();
        tests.addAll(ReportedTest.fileNameSourceHelperDom(SpecsRoot.RESOURCES.resolve("integer")));
        tests.addAll(getDocExamples());
        return tests;
    }

    static List<Arguments> getDocExamples() {
        List<Arguments> tests = new ArrayList<>();
        TestResourcesRoot.listFiles(TestResourcesRoot.RESOURCES.resolve("testModels/spec-doc-examples"), f -> {
            return f.getFileName().toString().startsWith("Integer");
        }).forEach(f -> tests.add(Arguments.of(f, ModelExpectation.CORRECT, false)));
        return tests;
    }

    @ParameterizedTest
    @MethodSource("fileNameSource")
    void translateToDom(final Path fileName, final ModelExpectation expectation, final Boolean isDisabled) throws IOException {
        checkDom(fileName, expectation, isDisabled);
    }
}
