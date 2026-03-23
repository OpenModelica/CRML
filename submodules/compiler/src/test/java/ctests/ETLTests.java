package ctests;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.provider.MethodSource;

import crml.compiler.omc.OMCmsg;
import crml.compiler.Utilities;
import crml.compiler.omc.OMCUtil.CompileStage;
import crml.compiler.omc.CompileSettings;
import crml.compiler.omc.ModelicaSimulationException;

import org.junit.jupiter.params.ParameterizedTest;


public class ETLTests extends ParameterizedSuite {
    //public static String files="blanc";
    public static CompileSettings cs;
    
    static List<Path> fileNameSource() {
        return ParameterizedSuite.fileNameSourceHelper(RESOURCES.resolve("testModels","libraries","ETL_test"));
    }
 
    @BeforeAll
    public static void setupTestSuite() {
        cs = new CompileSettings(
                RESOURCES.resolve("testModels","libraries","ETL_test"),
                RESOURCES.resolve("verificationModels","libraries","ETL_test"),
                RESOURCES.resolve("refResults","libraries","ETL_test"));
        cs.processBuilder = new ProcessBuilder();
        cs.outputFolder = OUT.resolve("ETL_test");

        System.out.println(cs.testFolderIn);
    }
    
    
    @ParameterizedTest
    @MethodSource("fileNameSource")
    public void simulateTestFile(final Path fileName) throws InterruptedException, IOException, ModelicaSimulationException {
        OMCmsg ret = Util.runTest(fileName, cs, CompileStage.SIMULATE);

        emit(ret.files);

        //files = ret.files;
        if(ret.msg.contains("Failed")||ret.msg.contains("Error"))
		    fail("Unable to run Modelica script " +  Utilities.getAbsolutePath(fileName) + ".mos", 
		            new Throwable( "\n omc fails with the following message: \n" + ret.msg));
	
    }
}
