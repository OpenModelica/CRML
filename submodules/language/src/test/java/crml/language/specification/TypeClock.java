package crml.language.specification;

import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import crml.language.util.CRMLSyntaxResultsWrapper;
import crml.language.util.Parser;
import crml.language.util.SpecsRoot;
import crml.test.ReportedTest;
import crml.test.TestResourcesRoot;

import org.junit.jupiter.api.Assumptions;

public class TypeClock extends ReportedTest {
    static List<Arguments> fileNameSource() {
        List<Arguments> tests = new ArrayList<>();
        tests.addAll(ReportedTest.fileNameSourceHelper2(SpecsRoot.RESOURCES.resolve("clock")));
        tests.addAll(getDocExamples());
        return tests;
    }
    static List<Arguments> getDocExamples() {
        List<Arguments> tests = new ArrayList<>();
        TestResourcesRoot.listFiles(TestResourcesRoot.RESOURCES.resolve("testModels/spec-doc-examples"), f -> {
            return f.getFileName().toString().startsWith("Clock");
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
