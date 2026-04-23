package crml.language.util;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SpecsRoot {
    public static Path RESOURCES;
    public static Path OUT;
    static {
		try {
			RESOURCES = Paths.get(Thread.currentThread().getContextClassLoader().getResource("specs/specRoot.txt").toURI()).getParent();
			System.out.println("Resources: "+RESOURCES);
			OUT = Path.of("build","testSuiteGenerated");
			System.out.println("Output:"+OUT);
		} catch (URISyntaxException e) {
			// Test configuration is expected to be correct, 
			// therefore, we just wrap potential errors in a runtime exception to hide it in code.
			throw new RuntimeException("Incorrect project setup. Resources not found.");
		}
	}
}