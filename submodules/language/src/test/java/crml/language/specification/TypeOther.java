package crml.language.specification;

import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import crml.language.util.CRMLSyntaxResultsWrapper;
import crml.language.util.Parser;
import crml.test.ReportedTest;
import crml.test.TestResourcesRoot;

import org.junit.jupiter.api.Assumptions;

public class TypeOther extends ReportedTest {
    static List<Arguments> fileNameSource() {
        return getDocExamples();
    }

    static List<Arguments> getDocExamples() {
        List<String> testedElsewhere = Arrays.asList(
                "Boolean", "Clock", "Event", "Integer", "Operator",
                "Period", "Real", "String", "Template"); // Period covers both Period and Periods
        List<Arguments> tests = new ArrayList<>();
        TestResourcesRoot.listFiles(TestResourcesRoot.RESOURCES.resolve("testModels/spec-doc-examples"), f -> {
            String name = f.getFileName().toString();
            return testedElsewhere.stream().noneMatch(name::startsWith);
        }).forEach(f -> {
            tests.add(Arguments.of(f, true, false));
        });
        return tests;
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
