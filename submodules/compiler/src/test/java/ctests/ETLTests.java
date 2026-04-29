package ctests;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.provider.MethodSource;

import crml.compiler.omc.OMCmsg;
import crml.compiler.util.CompilerRoot;
import crml.compiler.util.FilesWrapper;
import crml.test.ReportedTest;
import crml.test.SharedParameter;
import crml.util.PathUtil;
import crml.util.PathUtil.Option;
import crml.compiler.Utilities;
import crml.compiler.omc.OMCUtil.CompileStage;
import crml.compiler.omc.CompileSettings;
import crml.compiler.omc.ModelicaSimulationException;

import org.junit.jupiter.params.ParameterizedTest;


public class ETLTests extends ReportedTest {
    public static CompileSettings cs;
    
    static List<Path> fileNameSource() {
        return CompilerRoot.fileNameSourceHelper(CompilerRoot.RESOURCES.resolve("testModels").resolve("libraries").resolve("ETL_test"));
    }
 
    @BeforeAll
    public static void setupTestSuite() {
        cs = new CompileSettings(
                CompilerRoot.RESOURCES.resolve("testModels").resolve("libraries").resolve("ETL_test"),
                CompilerRoot.RESOURCES.resolve("verificationModels").resolve("libraries").resolve("ETL_test"),
                CompilerRoot.RESOURCES.resolve("refResults").resolve("libraries").resolve("ETL_test"));
        cs.processBuilder = new ProcessBuilder();
        cs.outputFolder = CompilerRoot.OUT.resolve("ETL_test");

        System.out.println(cs.testFolderIn);
    }
    
    
    @ParameterizedTest
    @MethodSource("fileNameSource")
    public void simulateTestFile(final Path fileName) throws InterruptedException, IOException, ModelicaSimulationException {
        emit(fileName, "CRML model");
        OMCmsg ret = Util.runTest(fileName, cs, CompileStage.SIMULATE);

        emit(FilesWrapper.of(ret.files), "files");
        emit(ret.msg, "OMC message");
        
        //files = ret.files;
        if(ret.msg.contains("Failed")||ret.msg.contains("Error"))
		    fail("Unable to run Modelica script " +  PathUtil.toString(fileName, Option.ABSOLUTE) + ".mos", 
		            new Throwable( "\n omc fails with the following message: \n" + ret.msg));
	
    }
}
