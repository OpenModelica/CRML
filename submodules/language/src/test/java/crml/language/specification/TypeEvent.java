package crml.language.specification;

import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import crml.language.specification.util.BaseSpecificationTest;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.params.ParameterizedTest;

public class TypeEvent extends BaseSpecificationTest {
    static List<Arguments> fileNameSource() {
        return BaseSpecificationTest.fileNameSourceHelper2(RESOURCES.resolve("event"));
    }

    @ParameterizedTest
    @MethodSource("fileNameSource")
    public void simulateTestFile(final Path fileName, final Boolean isValid, final Boolean isDisabled) throws IOException {
        emit(fileName, "CRML model");
        Assumptions.assumeFalse(isDisabled);
        var parsed = parse(fileName);
        emit(parsed.syntax(), "Syntax Errors");
        assertEquals(isValid, !parsed.syntax().hasErrors());
    }
}
