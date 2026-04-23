package ctests;

import static org.junit.jupiter.api.Assertions.fail;
import java.io.IOException;
import java.nio.file.Path;

import crml.compiler.omc.OMCUtil;
import crml.compiler.omc.OMCmsg;
import crml.compiler.Utilities;
import crml.compiler.omc.OMCUtil.CompileStage;
import crml.compiler.omc.OMCUtil.OMCFilesLog;
import crml.compiler.omc.CompileSettings;
import crml.compiler.omc.ModelicaSimulationException;

public class Util {
	/**
	 * General method for test cases
	 * @param fileName
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws ModelicaSimulationException
	 */
	static OMCmsg runTest(final Path fileName, 
						final CompileSettings cs,
						final CompileStage stage) 
							throws InterruptedException, IOException, ModelicaSimulationException {
		
		String stripped_file_name = Utilities.stripNameEndingAndPath(fileName);
		Path out_dir = cs.outputFolder.resolve(stripped_file_name);

		// try compiling crml to modelica
		try {
    		
<<<<<<< HEAD:src/test/java/ctests/Util.java
			crml.CRMLC.parse_file(cs.testFolderIn, fileName, out_dir, 
=======
			crml.compiler.CRMLC.parse_file(fileName, out_dir, 
>>>>>>> fixes:submodules/compiler/src/test/java/ctests/Util.java
				true, false, true, stripped_file_name, false);
			
    	} catch (Exception e) {
			fail("Unable to translate " + fileName + " to Modelica\n", e);
		}

		if (stage == CompileStage.TRANSLATE) {
			Path fullName = out_dir.resolve(stripped_file_name + ".mo") ;
			//File mo_file = fullName.toFile();
			//String files = p(join(a(fullName.toAbsolutePath().toString()).withHref(mo_file.toURI().toString()), br())).render();
			//new OMCFilesLog(null, )
			System.out.println("STAGE + " + CompileStage.TRANSLATE);
			return new OMCmsg(new OMCFilesLog(fullName), "");
		}
			
		OMCmsg ret = OMCUtil.compile(stripped_file_name, out_dir, cs);

		return ret;
		
		}

		
}
