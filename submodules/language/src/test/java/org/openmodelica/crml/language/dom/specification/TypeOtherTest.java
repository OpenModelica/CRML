package org.openmodelica.crml.language.dom.specification;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openmodelica.crml.testresources.TestResourcesRoot;
import org.openmodelica.crml.testutil.ModelExpectation;
import org.openmodelica.crml.testutil.ReportedTest;

public class TypeOtherTest extends DomSpecificationTest {

    static List<Arguments> fileNameSource() {
        return getDocExamples();
    }

    static List<Arguments> getDocExamples() {
        List<String> testedElsewhere = Arrays.asList(
                "Boolean", "Clock", "Event", "Integer", "Operator",
                "Period", "Real", "String", "Template");
        List<Arguments> tests = new ArrayList<>();
        TestResourcesRoot.listFiles(TestResourcesRoot.RESOURCES.resolve("testModels/spec-doc-examples"), f -> {
            String name = f.getFileName().toString();
            return testedElsewhere.stream().noneMatch(name::startsWith);
        }).forEach(f -> tests.add(Arguments.of(f, ModelExpectation.CORRECT, false)));
        return tests;
    }

    @ParameterizedTest
    @MethodSource("fileNameSource")
    void translateToDom(final Path fileName, final ModelExpectation expectation, final Boolean isDisabled) throws IOException {
        checkDom(fileName, expectation, isDisabled);
    }
}
