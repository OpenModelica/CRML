package ctests;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import crml.language.util.CRMLSyntaxResultsWrapper;
import crml.language.util.Parser;
import crml.test.ReportedTest;
import crml.util.SafeResource;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.params.ParameterizedTest;


public class FORMLTests extends ReportedTest {

    static List<Arguments> fileNameSource() {
        List<Arguments> tests = new ArrayList<>();
        tests.addAll(ReportedTest.fileNameSourceHelper2(SafeResource.get("testResourcesRoot").getParent().resolve("testModels").resolve("libraries").resolve("FORML_test")));
        tests.add(Arguments.of(SafeResource.get("testResourcesRoot").getParent().resolve("testModels").resolve("libraries").resolve("FORM-L.crml"), true, false));
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
