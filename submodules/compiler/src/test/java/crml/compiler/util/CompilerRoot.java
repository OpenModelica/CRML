package crml.compiler.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import crml.test.TestResourcesRoot;

public class CompilerRoot {
    public static Path RESOURCES = TestResourcesRoot.RESOURCES;
    public static Path OUT = Paths.get("build","testSuiteGenerated");

	/**
	 * Method for feeding the list of files into the parametrized test
	 * @param source Source files for the test
	 * @return
	 */
	public static List<Path> fileNameSourceHelper(Path source) {
		return TestResourcesRoot.fileNameSourceHelper(source);
	}
}