package ctests;

import java.io.IOException;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import crml.compiler.omc.OMCUtil.CompileStage;
import crml.compiler.util.CompilerRoot;
import crml.compiler.omc.CompileSettings;
import crml.compiler.omc.ModelicaSimulationException;


public class UseCaseTests {
    static CompileSettings cs = new CompileSettings();

    @BeforeAll
    public static void setupTestSuite() {
		cs = new CompileSettings(
                CompilerRoot.RESOURCES.resolve("..").resolve("..").resolve("..").resolve("..").resolve("crml_tutorial"),
                null,
                null);
        cs.processBuilder = new ProcessBuilder();
        cs.outputFolder = CompilerRoot.OUT.resolve("use_cases");
	}

    @DisplayName("Traffic lights use-case test")
	@Test
	void testTraficLight () throws InterruptedException, IOException, ModelicaSimulationException{
		String filePath = "traffic_light/";
		Util.runTest(Paths.get(filePath), cs, CompileStage.TRANSLATE);
	}

	/** @DisplayName("Pumping System use-case test")
	@Test
	void testPumpingSystem () throws InterruptedException, IOException, ModelicaSimulationException{
		String filePath = "pumping_system/";
		Util.runTest(filePath, cs, CompileStage.TRANSLATE);
	} */


	
}
