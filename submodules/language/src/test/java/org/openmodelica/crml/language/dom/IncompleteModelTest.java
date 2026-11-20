package org.openmodelica.crml.language.dom;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openmodelica.crml.testresources.TestResourcesRoot;

/**
 * Models with syntax errors, as an editor sees them while they are being
 * written: the cases of {@code testModels/incomplete} in
 * {@code :test-resources}. Each case states in its {@code expected.constraints}
 * what of the partial DOM must be there, checked as described in
 * {@link CaseFolderTest}.
 */
public class IncompleteModelTest extends CaseFolderTest {

    static final Path CASES = TestResourcesRoot.RESOURCES.resolve("testModels").resolve("incomplete");

    static List<Arguments> cases() throws IOException {
        return casesIn(CASES);
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("cases")
    void parseAndBuild(final String caseName, final Path caseDir) throws IOException {
        check(caseDir);
    }
}
