package crml.compiler.omc.runtime;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OMCRunner {
    private static final Logger logger = LogManager.getLogger(OMCRunner.class);
    
    private final static String cmdlet = System.getProperty("os.name").toLowerCase().contains("win") ? "omc.exe"
            : "omc";
    private static final Path omc = locateOMC();
    private Path wd;

    public ProcessBuilder build(Path script) {
        ProcessBuilder processBuilder = new ProcessBuilder();
        if (wd != null)
            processBuilder.directory(wd.toFile());
        processBuilder.command(omc.toAbsolutePath().toString(), script.toAbsolutePath().toString());
        return processBuilder;
    }

    public static Path locateOMC() {
        Path omc = findOnPath().orElseGet(OMCRunner::findFromEnv);
        if(omc==null){
            logger.warn("OMC was not found on PATH or with environment variable OPENMODELICAHOME."); 
        }
        return omc;
    }

    public static Path findFromEnv() {
        String omhome = System.getenv("OPENMODELICAHOME");
        if (omhome == null) {
            return null;
        }
        return Paths.get(omhome, "bin", cmdlet);
    }

    public static Optional<Path> findOnPath() {
        String os = System.getProperty("os.name").toLowerCase();
        String finder = os.contains("win") ? "where" : "which";

        Scanner sc = null;
        try {
            Process process = new ProcessBuilder(finder, cmdlet)
                    .redirectErrorStream(true)
                    .start();
            sc = new Scanner(process.getInputStream()).useDelimiter("\\A");
            String output = sc.hasNext() ? sc.next().trim() : "";
            int exit = process.waitFor();

            if (exit == 0 && !output.isEmpty()) {
                String firstLine = output.split("\n")[0];
                return Optional.of(Paths.get(firstLine));
            }
        } catch (IOException |InterruptedException e) {

        } finally {
            if (sc != null) sc.close();
        } 

        return Optional.empty();
    }
}
