package crml.compiler.omc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import crml.compiler.Utilities;
import crml.util.PathUtil;
import crml.util.PathUtil.Option;
import crml.util.SafeResource;

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
    ctx.setVariable("res_file", PathUtil.toString(res_file, Option.UNIX)); // ABSOLUTE is not implemented for String
    ctx.setVariable("ref_file", PathUtil.toString(ref_file, Option.UNIX, Option.ABSOLUTE));// Was: Utilities.toUnixPath(ref_file)
    return TEMPLATE_ENGINE.process("compare_simulation_results", ctx);
  }

  /**
   * Classpath resource of the CRMLtoModelica library (single file),
   * packaged by the packageCRMLtoModelicaLibrary task of the compiler project
   */
  static final String CRML_TO_MODELICA_RESOURCE = "modelica_libraries/CRMLtoModelica.mo";
  /**
   * Classpath resource folder of the CRML library (Modelica package folder),
   * packaged by the packageCRMLLibrary task of the test-resources project
   */
  static final String CRML_LIBRARY_RESOURCE = "modelica_libraries/CRML";

  /** Library folders already emitted by this JVM; the library is extracted once per folder */
  private static final Set<Path> EMITTED_CRML_LIBRARIES = new HashSet<>();

  /**
   * Locates a classpath resource (also inside a jar, see {@link SafeResource})
   */
  private static Path resource(String name) throws IOException {
    try {
      return SafeResource.get(name);
    } catch (IllegalArgumentException e) {
      throw new IOException("Could not find resource on the classpath: " + name, e);
    }
  }

  /** Location of the emitted CRMLtoModelica library for a given output folder */
  public static Path getCRMLToModelicaFile(Path out_dir) {
    return out_dir.resolve("../crml2modelica.mo").normalize();
  }

  /** Location of the emitted CRML library (its package.mo) for a given output folder */
  public static Path getCRMLLibrary(Path out_dir) {
    return out_dir.resolve("../CRML/package.mo").normalize();
  }

  /**
   * Extracts the CRMLtoModelica library from the classpath
   * @return the emitted file
   */
  public static Path emitCRMLToModelicaFile(Path out_dir) throws IOException {
    Path target = getCRMLToModelicaFile(out_dir);
    Files.createDirectories(target.getParent());
    Files.copy(resource(CRML_TO_MODELICA_RESOURCE), target, StandardCopyOption.REPLACE_EXISTING);
    return target;
  }

  /**
   * Extracts the CRML library package from the classpath
   * (provided by the test-resources project)
   * @return the package.mo of the emitted library, to be loaded by omc
   */
  public static Path emitCRMLLibrary(Path out_dir) throws IOException {
    Path packageMo = getCRMLLibrary(out_dir);
    Path libDir = packageMo.getParent();
    synchronized (EMITTED_CRML_LIBRARIES) {
      if (EMITTED_CRML_LIBRARIES.contains(libDir) && Files.exists(packageMo))
        return packageMo;

      Path source = resource(CRML_LIBRARY_RESOURCE);
      List<Path> files;
      try (Stream<Path> walk = Files.walk(source)) {
        files = walk.filter(Files::isRegularFile).collect(Collectors.toList());
      }
      for (Path f : files) {
        // relative path as String, since source may be on a different file system (jar)
        Path target = libDir.resolve(source.relativize(f).toString());
        Files.createDirectories(target.getParent());
        Files.copy(f, target, StandardCopyOption.REPLACE_EXISTING);
      }

      if (!Files.exists(packageMo))
        throw new IOException("The packaged CRML library has no package.mo: " + packageMo);
      EMITTED_CRML_LIBRARIES.add(libDir);
      return packageMo;
    }
  }

  public static String generateSimulationScript(String stripped_file_name, Path verifModelFolder, Path out_dir) throws IOException {
    Path crml2Modelica = emitCRMLToModelicaFile(out_dir); //Export file from jar to host

    Path verif_model = verifModelFolder.resolve(stripped_file_name);
    boolean verifModelExists = Files.exists(verif_model);

    // The CRML library is only needed (and loaded) to simulate the verification model
    Path crmlLib = verifModelExists ? emitCRMLLibrary(out_dir) : getCRMLLibrary(out_dir);

    if (verifModelExists) {
      for (Path f : Files.list(verif_model).collect(java.util.stream.Collectors.toList()))
        Files.copy(f, out_dir.resolve(f.getFileName().toString()), StandardCopyOption.REPLACE_EXISTING);
    }


    Context ctx = new Context();
    ctx.setVariable("crml2ModelicaPath", PathUtil.toString(crml2Modelica, Option.UNIX, Option.ABSOLUTE)); //Was: Utilities.toUnixPath(crml2Modelica.getAbsolutePath())
    ctx.setVariable("stripped_file_name", stripped_file_name);
    ctx.setVariable("crmlLibPath", PathUtil.toString(crmlLib, Option.UNIX, Option.ABSOLUTE)); // Was: Utilities.toUnixPath(crmlLib.getAbsolutePath())
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
   
    OMCFilesLog msg = new OMCFilesLog(mos_file.toPath(), mo_file.toPath(), getCRMLToModelicaFile(out_dir), getCRMLLibrary(out_dir));

 
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
