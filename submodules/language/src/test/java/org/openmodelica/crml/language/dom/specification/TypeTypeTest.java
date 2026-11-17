package org.openmodelica.crml.language.dom.specification;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openmodelica.crml.language.util.SpecsRoot;
import org.openmodelica.crml.testutil.ModelExpectation;
import org.openmodelica.crml.testutil.ReportedTest;

public class TypeTypeTest extends DomSpecificationTest {

    static List<Arguments> fileNameSource() {
        List<Arguments> tests = new ArrayList<>();
        tests.addAll(ReportedTest.fileNameSourceHelperDom(SpecsRoot.RESOURCES.resolve("type")));
        return tests;
    }

    @ParameterizedTest
    @MethodSource("fileNameSource")
    void translateToDom(final Path fileName, final ModelExpectation expectation, final Boolean isDisabled) throws IOException {
        checkDom(fileName, expectation, isDisabled);
    }
}
