package ctests;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.provider.MethodSource;

import crml.compiler.ModelicaSimulationException;
import crml.compiler.OMCmsg;
import crml.compiler.Utilities;
import crml.compiler.OMCUtil.CompileStage;

import org.junit.jupiter.params.ParameterizedTest;


public class FORMLTests  {

    @Nested
    public static class SimulationTests extends ParameterizedSuite {
        @BeforeAll
        public static void setupTestSuite() throws IOException {
            cs.initAllDirs("testModels", "verificationModels", 
                    "refResults", "libraries/FORML_test");
            cs.processBuilder = new ProcessBuilder();
            cs.setOutputSubFolder("FORML_test");
        }
    
    
        @ParameterizedTest
        @MethodSource("fileNameSource")
        public void simulateTestFile(final String fileName) throws InterruptedException, IOException, ModelicaSimulationException {
            emit(Path.of(cs.testFolderIn, fileName), "CRML model");

            OMCmsg ret = Util.runTest(fileName, cs, CompileStage.SIMULATE);
            emit(ret.files, "files");

            if(ret.msg.contains("Failed")||ret.msg.contains("Error"))
			fail("Unable to run Modelica script " + Utilities.getAbsolutePath(fileName) + ".mos", 
			new Throwable( "\n omc fails with the following message: \n" + ret.msg));
		
        }

    }
}
