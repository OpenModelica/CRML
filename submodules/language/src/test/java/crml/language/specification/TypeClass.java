package crml.language.specification;

import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import crml.language.util.CRMLSyntaxResultsWrapper;
import crml.language.util.Parser;
import crml.language.util.SpecsRoot;
import crml.test.ReportedTest;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.params.ParameterizedTest;

public class TypeClass extends ReportedTest {
    static List<Arguments> fileNameSource() {
        return ReportedTest.fileNameSourceHelper2(SpecsRoot.RESOURCES.resolve("class"));
    }

    @ParameterizedTest
    @MethodSource("fileNameSource")
    public void simulateTestFile(final Path fileName, final Boolean isValid, final Boolean isDisabled) throws IOException {
        emit(fileName, "CRML model");
        Assumptions.assumeFalse(isDisabled);
        Parser.ParserResult parsed = new Parser().parse(fileName);

        emit(CRMLSyntaxResultsWrapper.of(parsed.syntax()), "Syntax Errors");
        emit(parsed.toPrettyTree(), "AST");
        assertEquals(isValid, !parsed.syntax().hasErrors());
    }
}
