package ctests;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import crml.compiler.omc.OMCUtil.CompileStage;
import crml.compiler.omc.CompileSettings;
import crml.compiler.omc.ModelicaSimulationException;

/**
 * 
 * Test suite for running specification tests added by Audrey
 * 
 * @author Lena B
 *
 */
//@Disabled("temporarily disabled to speed up ETL testing")
public class SpecificationTests extends ParameterizedSuite {
	public static CompileSettings cs;

	static List<Path> fileNameSource() {
        return ParameterizedSuite.fileNameSourceHelper(RESOURCES.resolve("testModels","spec-doc-examples"));
    }

 	@BeforeAll
    public static void setupTestSuite() {
		cs = new CompileSettings(
                RESOURCES.resolve("testModels","spec-doc-examples"),
                RESOURCES.resolve("verificationModels","spec-doc-examples"),
                RESOURCES.resolve("refResults","spec-doc-examples"));
        cs.processBuilder = new ProcessBuilder();
        cs.outputFolder = OUT.resolve("spec-doc-examples");
	}

    @ParameterizedTest
    @MethodSource("fileNameSource")
	public void simulateTestFile(final Path fileName) throws InterruptedException, IOException, ModelicaSimulationException {
	    Util.runTest(fileName, cs, CompileStage.VERIFY);
	}
}
