package crml.compiler.omc;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CompileSettings {
    
    public Path testFolderIn;
	public Path verifModelFolder;
    public Path referenceResFolder;
    public Path outputFolder = Paths.get("build/testSuiteGenerated"); // if not set explicitly
    public String within = null; //Used in OMCUtil

    //static final String defaultOutputRoot = "build/testSuiteGenerated";
	//static final String CRMLtoModelicaLibrary = "libraries/modelica/CRMLtoModelica.mo";
	//static final String CRMLLibrary = "libraries/modelica/CRML.mo";


    public ProcessBuilder processBuilder; // used for running omc commands


    public CompileSettings(){
        this(null, null, null);
    }
    public CompileSettings(Path testFolder, Path verificationFolder, Path refernceFolder){
        this.testFolderIn = testFolder;
        this.verifModelFolder = verificationFolder;
        this.referenceResFolder = refernceFolder;
    }
    /*public void initAllDirs(String testF, String verifF, String refResF, String subFolder){
        //File sf =  new File(subFolder);
        testFolderIn = getResourcePath(testF).resolve(subFolder);
        verifModelFolder = getResourcePath(verifF).resolve(subFolder);
        referenceResFolder = getResourcePath(refResF).resolve(subFolder);
    }*/

     /*public void initTestDir(String testF){
        
        testFolderIn = getResourcePath(testF);
      
    }*/

    /**
     * Puts the tests in a sub-folder in the default putput directory
     */
    /*public void setOutputSubFolder(String subFolder){
        outputFolder = Paths.get(defaultOutputRoot, subFolder);
    }*/

    private static Path getResourcePath(String dirName){
        System.out.println(Thread.currentThread().getContextClassLoader().toString());

        URL url = Thread.currentThread().getContextClassLoader().getResource(dirName);
        if(url != null) {
            return Paths.get(url.getPath());
        } else {
            return Paths.get("src/test/resources/", dirName);
        }            
	}
}
