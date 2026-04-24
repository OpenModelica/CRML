package crml.compiler.omc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.TimeUnit;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import crml.compiler.Utilities;

/**
 * Wrapper for OpenModelica compiler calls
 */
public class OMCUtil {
    public enum CompileStage {TRANSLATE, SIMULATE, VERIFY};

    private static final TemplateEngine TEMPLATE_ENGINE;

    static {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".mos");
        resolver.setTemplateMode(TemplateMode.TEXT);
        resolver.setCharacterEncoding("UTF-8");
        TEMPLATE_ENGINE = new TemplateEngine();
        TEMPLATE_ENGINE.setTemplateResolver(resolver);
    }

    public static final class OMCFilesLog {
        private final Path[] files;

        public OMCFilesLog(Path... files) {
            this.files = files;
        }

        public Path[] files() { return files; }
    }
    /**
   * Method for comparing omc simulation results to reference files
   * @param res_file simulation result file
   * @param ref_file reference result file
   * @return code for comparison script
   */
  public static String compareSimulationResults(String res_file, Path ref_file) {
    Context ctx = new Context();
    ctx.setVariable("res_file", Utilities.toUnixPath(res_file));
    ctx.setVariable("ref_file", ref_file.toAbsolutePath().toString());// Was: Utilities.toUnixPath(ref_file)
    return TEMPLATE_ENGINE.process("compare_simulation_results", ctx);
  }

  public static Path getCRMLToModelicaFile() {
    try {
      return Paths.get(Thread.currentThread().getContextClassLoader().getResource("modelica_libraries/CRMLtoModelica.mo").toURI());
    } catch (URISyntaxException e) {
      // File is expected to be packages with Gradle.
      throw new RuntimeException(e);
    }
//    String startedFrom = System.getProperty("user.dir");
//    return new File(startedFrom + java.io.File.separator + CompileSettings.CRMLtoModelicaLibrary);
  }

  public static Path getCRMLLibrary() {
    try {
      return Paths.get(Thread.currentThread().getContextClassLoader().getResource("modelica_libraries/CRML.mo").toURI());
    } catch (URISyntaxException e) {
      // File is expected to be packages with Gradle.
      throw new RuntimeException(e);
    }
    //String startedFrom = System.getProperty("user.dir");
    //return new File(startedFrom + java.io.File.separator + CompileSettings.CRMLLibrary);
  }

  public static String generateSimulationScript(String stripped_file_name, Path verifModelFolder, Path out_dir) throws IOException {
    Path crml2Modelica = out_dir.resolve("crml2modelica.mo");
    Files.copy(getCRMLToModelicaFile(), crml2Modelica, StandardCopyOption.REPLACE_EXISTING); //Export file from jar to host
    Path crmlLib = out_dir.resolve("CRML.mo");
    Files.copy(getCRMLLibrary(), crmlLib, StandardCopyOption.REPLACE_EXISTING); //Export file from jar to host

    //File crml2Modelica = getCRMLToModelicaFile().toFile();
    //File crmlLib = getCRMLLibrary().toFile();

    
    if (!Files.exists(crml2Modelica)) // Export failed, this check is legacy only
      throw new IOException("Could not find: " + crml2Modelica.toAbsolutePath().toString());

    if (!Files.exists(crmlLib)) // Export failed, this check is legacy only
      throw new IOException("Could not find: " + crmlLib.toAbsolutePath().toString());

    Path verif_model = verifModelFolder.resolve(stripped_file_name);
    boolean verifModelExists = Files.exists(verif_model);
    if (verifModelExists) {
      for (Path f : Files.list(verif_model).collect(java.util.stream.Collectors.toList()))
        Files.copy(f, out_dir.resolve(f.getFileName().toString()), StandardCopyOption.REPLACE_EXISTING);
    }

    
    Context ctx = new Context();
    ctx.setVariable("crml2ModelicaPath", crml2Modelica.toAbsolutePath().toString()); //Was: Utilities.toUnixPath(crml2Modelica.getAbsolutePath())
    ctx.setVariable("stripped_file_name", stripped_file_name);
    ctx.setVariable("crmlLibPath", crmlLib.toAbsolutePath().toString()); // Was: Utilities.toUnixPath(crmlLib.getAbsolutePath())
    ctx.setVariable("verifModelExists", verifModelExists);
    return TEMPLATE_ENGINE.process("generate_simulation_script", ctx);
  }

  public static OMCmsg compile(String stripped_file_name, Path out_dir, CompileSettings cs) throws ModelicaSimulationException, IOException, InterruptedException {
    
    String filePrefix = out_dir + java.io.File.separator + stripped_file_name;
    String script_file_name =  filePrefix + ".mos";
    
    String mos_text = OMCUtil.generateSimulationScript(stripped_file_name, cs.verifModelFolder, out_dir);
    
    // check if a reference file to compare to is available

    Path ref_file = cs.referenceResFolder.resolve(stripped_file_name + "_verif_ref.mat");
    Path out_ref_file = out_dir.resolve(stripped_file_name + "_verif_ref.mat");
    if(Files.exists(ref_file)){
      Files.copy(ref_file, out_ref_file, StandardCopyOption.REPLACE_EXISTING); //Export file from jar to host
      mos_text +=  OMCUtil.compareSimulationResults(stripped_file_name + "." + stripped_file_name + "_verif_res.mat", out_ref_file);
    }
   
    
    BufferedWriter writer = new BufferedWriter(new FileWriter(script_file_name));
    writer.write(mos_text);
    writer.close();

    // add package support
    writer = new BufferedWriter(new FileWriter(new File(out_dir + java.io.File.separator + "package.mo")));
    
    //writer.write("within " + cs.within + ";\n");
    //writer.write("within " + stripped_file_name + ";\n");
    writer.write("within ;\n");
    writer.write("package " + stripped_file_name + "\n end " + stripped_file_name + "; \n");
    writer.close();

    String omc = locateOMC();
    
    // calling omc
    cs.processBuilder.directory(out_dir.toFile());
    cs.processBuilder.command(omc, stripped_file_name + ".mos");

    Process process = cs.processBuilder.redirectErrorStream(true).start();

    InputStream inputStream = process.getInputStream();
    // OutputStream outputStream = process.getOutputStream();
    // Not needed because we redirected via redirectErrorStream
    // InputStream errorStream = process.getErrorStream();
    
    File mos_file = new File(script_file_name);
    File mo_file = new File (filePrefix + ".mo");
   
    OMCFilesLog msg = new OMCFilesLog(mos_file.toPath(), mo_file.toPath(), getCRMLToModelicaFile(), getCRMLLibrary());

 
    String omcOutput = checkInputStream(inputStream);

		// wait for is *AFTER* we consume the output, otherwise process is blocked on output write!
    boolean isFinished = process.waitFor(30, TimeUnit.SECONDS);
    if (!isFinished)
       throw new ModelicaSimulationException("Simulation timed out [30 seconds]: " 	+ omc + stripped_file_name + ".mos");

    return new OMCmsg(msg,omcOutput);
    
  }

    public static String checkInputStream(InputStream inputStream) throws IOException {

    StringBuffer buffer = new StringBuffer();
        try(BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(inputStream))) {
            String line;
            while((line = bufferedReader.readLine()) != null) {
                buffer.append(line + "\n");
            }

        }

    return buffer.toString();
  }

  public static String locateOMC() {
    String omc = "omc";
    // check where is OMC
    if (Utilities.isWindows())
      omc = omc + ".exe";
      File omcFile = Utilities.getFileInPath(omc);
    if (omcFile != null) {
      return omcFile.getAbsolutePath();
    }

    // check if we have an OPENMODELICAHOME defined!
        String omhome = System.getenv("OPENMODELICAHOME");
    if (omhome != null) {
      File om = new File(omhome + File.separator + "bin" + File.separator + omc);
      if (om.exists())
        return om.getAbsolutePath(); // FIXME - check return for 0
    }
        // failed miserably to detect OMC, have a leap of faith   
    System.out.println("Could not detect OMC, searched in:\n\t$PATH=[" + System.getenv("PATH") + "]" + "\nand in:\n\t$OPENMODELICAHOME/bin/ = [" + omhome + "]");
        return omc;
    }
}
