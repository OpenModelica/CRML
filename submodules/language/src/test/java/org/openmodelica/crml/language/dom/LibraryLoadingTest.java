package org.openmodelica.crml.language.dom;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openmodelica.crml.testresources.TestResourcesRoot;

/**
 * Models spread over several files: the cases of
 * {@code testModels/library-loading} in {@code :test-resources}, checked as
 * described in {@link CaseFolderTest}.
 */
public class LibraryLoadingTest extends CaseFolderTest {

    static final Path CASES = TestResourcesRoot.RESOURCES.resolve("testModels").resolve("library-loading");

    static List<Arguments> cases() throws IOException {
        return casesIn(CASES);
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("cases")
    void parseAndBuild(final String caseName, final Path caseDir) throws IOException {
        check(caseDir);
    }
}
