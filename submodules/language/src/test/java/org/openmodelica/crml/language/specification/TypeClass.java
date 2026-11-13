package org.openmodelica.crml.language.specification;

import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openmodelica.crml.language.util.CRMLSyntaxResultsWrapper;
import org.openmodelica.crml.language.util.Parser;
import org.openmodelica.crml.language.util.SpecsRoot;
import org.openmodelica.crml.testutil.ModelExpectation;
import org.openmodelica.crml.testutil.ReportedTest;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.params.ParameterizedTest;

public class TypeClass extends ReportedTest {
    static List<Arguments> fileNameSource() {
        return ReportedTest.fileNameSourceHelper2(SpecsRoot.RESOURCES.resolve("class"));
    }

    @ParameterizedTest
    @MethodSource("fileNameSource")
    public void simulateTestFile(final Path fileName, final ModelExpectation expectation, final Boolean isDisabled) throws IOException {
        emit(fileName, "CRML model");
        Assumptions.assumeFalse(isDisabled);
        Parser.ParserResult parsed = new Parser().parse(fileName);

        emit(CRMLSyntaxResultsWrapper.of(parsed.syntax()), "Syntax Errors");
        emit(parsed.toPrettyTree(), "AST");
        assertEquals(expectation.parses(), !parsed.syntax().hasErrors());
    }
}
