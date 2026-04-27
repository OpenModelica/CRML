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
import crml.compiler.omc.OMCmsg;
import crml.compiler.util.CompilerRoot;
import crml.compiler.util.FilesWrapper;
import crml.test.ReportedTest;

/**
 * 
 * Test suite for running specification tests added by Audrey
 * 
 * @author Lena B
 *
 */
//@Disabled("temporarily disabled to speed up ETL testing")
public class SpecificationTests extends ReportedTest {
    public static CompileSettings cs;
    
    static List<Path> fileNameSource() {
        System.out.println(CompilerRoot.RESOURCES.toString());
        return CompilerRoot.fileNameSourceHelper(CompilerRoot.RESOURCES.resolve("testModels").resolve("spec-doc-examples"));
    }

 	@BeforeAll
    public static void setupTestSuite() {
        System.out.println(CompilerRoot.RESOURCES.toString());
		cs = new CompileSettings(
                CompilerRoot.RESOURCES.resolve("testModels").resolve("spec-doc-examples"),
                CompilerRoot.RESOURCES.resolve("verificationModels").resolve("spec-doc-examples"),
                CompilerRoot.RESOURCES.resolve("refResults").resolve("spec-doc-examples"));
        cs.processBuilder = new ProcessBuilder();
        cs.outputFolder = CompilerRoot.OUT.resolve("spec-doc-examples");
	}

    @ParameterizedTest
    @MethodSource("fileNameSource")
	public void simulateTestFile(final Path fileName) throws InterruptedException, IOException, ModelicaSimulationException {
	    emit(fileName, "CRML model");
                OMCmsg ret = Util.runTest(fileName, cs, CompileStage.VERIFY);
        emit(FilesWrapper.of(ret.files), "Files");
        emit(ret.msg, "OMC message");
	}
}
