package org.openmodelica.crml.compiler.crmlcv2.verification.engine.evaluation;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openmodelica.crml.compiler.omc.OMCUtil;

/**
 * Runs OpenModelica scripts in a case's working directory. Each script is
 * written to {@code <name>.mos} and what omc prints to {@code <name>.log}.
 */
public final class Omc {

    /** Per omc run; a simulation includes compiling the generated C code. */
    private static final long TIMEOUT_SECONDS = 300;

    private Omc() {
    }

    /** True when omc can be found, so a case can be skipped rather than fail without it. */
    public static boolean available() {
        String omc = OMCUtil.locateOMC();
        return new File(omc).isAbsolute() && new File(omc).canExecute();
    }

    /** Loads the package (and the harness), and runs {@code checkModel} on the simulated class. */
    public static OmcRun check(Workspace workspace) throws IOException, InterruptedException {
        String script = load(workspace)
            + "check := checkModel(" + workspace.simulatedClass() + ");\n"
            + "print(\"@@CHECK\\n\" + check + \"\\n\" + getErrorString() + \"\\n\");\n";
        OmcRun run = run(workspace.directory, "check.mos", script);
        String problem = loadProblem(run, workspace);
        if (problem == null && !run.text("CHECK").contains("completed successfully")) {
            problem = "check failed: " + run.errors();
        }
        return run.withProblem(problem);
    }

    /**
     * Loads and simulates the simulated class: over the reference's interval if
     * there is a reference (the harness's experiment annotation is not
     * necessarily the interval it was made with), else over the model's own.
     */
    public static Simulation simulate(Workspace workspace) throws IOException, InterruptedException {
        List<OmcRun> runs = new ArrayList<>();
        String stopTime = null;
        if (workspace.reference != null) {
            // A script of its own, so a reference omc cannot read stops the
            // verification, not the simulation.
            OmcRun interval = run(workspace.directory, "reference.mos",
                "ref := readSimulationResult(" + literal(workspace.reference) + ", {time});\n"
                + "print(\"@@STOP_TIME \" + String(ref[1, size(ref, 2)]) + \"\\n\" + getErrorString() + \"\\n\");\n");
            stopTime = interval.value("STOP_TIME");
            runs.add(interval.withProblem(stopTime == null ? interval.errors() : null));
        }
        String script = load(workspace)
            + "res := simulate(" + workspace.simulatedClass() + (stopTime == null ? "" : ", stopTime = " + stopTime)
            + ", fileNamePrefix = " + literal(workspace.testCase.simulatedClass()) + ");\n"
            // A record field cannot be read inside an expression in a script,
            // only assigned: "res.resultFile + ..." fails without a message.
            + "resultFile := res.resultFile;\n"
            + "messages := res.messages;\n"
            + "print(\"@@RESULT_FILE \" + resultFile + \"\\n\" + messages + \"\\n\" + getErrorString() + \"\\n\");\n";
        OmcRun run = run(workspace.directory, "simulate.mos", script);
        String resultFile = run.value("RESULT_FILE");
        String problem = loadProblem(run, workspace);
        if (problem == null && (resultFile == null || resultFile.isEmpty())) {
            problem = "simulation failed: " + run.errors();
        }
        run = run.withProblem(problem);
        runs.add(run);
        return new Simulation(workspace, runs, run, problem == null ? Paths.get(resultFile) : null, stopTime);
    }

    /** Writes {@code script} to {@code directory/fileName} and runs it there. */
    public static OmcRun run(Path directory, String fileName, String script) throws IOException, InterruptedException {
        String text = "echo(false);\n" + script;
        Files.write(directory.resolve(fileName), text.getBytes(StandardCharsets.UTF_8));
        Path logFile = directory.resolve(fileName.replaceAll("\\.mos$", "") + ".log");
        Process process = new ProcessBuilder(OMCUtil.locateOMC(), fileName)
            .directory(directory.toFile())
            .redirectErrorStream(true)
            .redirectOutput(logFile.toFile())
            .start();
        boolean finished = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
        }
        String log = new String(Files.readAllBytes(logFile), StandardCharsets.UTF_8);
        return new OmcRun(directory, fileName, text, finished ? log : log + "\n[timed out after " + TIMEOUT_SECONDS + " s]\n",
            !finished);
    }

    /** A path as an omc string literal. */
    public static String literal(Path path) {
        if (path.getFileSystem() != FileSystems.getDefault()) {
            throw new IllegalArgumentException(path + " is not on disk, so omc cannot read it");
        }
        return literal(path.toAbsolutePath().toString().replace('\\', '/'));
    }

    /** A string as an omc string literal. */
    public static String literal(String text) {
        return "\"" + text.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    // --- loading -----------------------------------------------------------

    /** The libraries and the package, each reported as {@code @@LOADED <what> true|false}; the script stops at the first that fails. */
    private static String load(Workspace workspace) {
        StringBuilder script = new StringBuilder();
        for (String[] library : libraries(workspace)) {
            script.append("ok := ").append(library[1]).append(";\n")
                .append("print(\"@@LOADED ").append(library[0]).append(" \" + String(ok) + \"\\n\");\n")
                .append("if not ok then print(getErrorString() + \"\\n\"); exit(1); end if;\n");
        }
        return script.toString();
    }

    /** Why loading failed, or null if everything loaded. */
    private static String loadProblem(OmcRun run, Workspace workspace) {
        List<String> loaded = run.values("LOADED");
        for (String[] library : libraries(workspace)) {
            if (!loaded.contains(library[0] + " true")) {
                return "load failed: " + library[0] + ": " + run.errors();
            }
        }
        return null;
    }

    /** What is loaded, in order, as {name, call}. */
    private static List<String[]> libraries(Workspace workspace) {
        List<String[]> libraries = new ArrayList<>();
        if (workspace.crmlLibrary != null) {
            // The harnesses are built from the CRML library, which uses the MSL.
            libraries.add(new String[] {"Modelica", "loadModel(Modelica, {\"4.0.0\"})"});
        }
        libraries.add(new String[] {"CRMLtoModelica", "loadFile(" + literal(OMCUtil.getCRMLToModelicaFile()) + ")"});
        if (workspace.crmlLibrary != null) {
            libraries.add(new String[] {"CRML", "loadFile(" + literal(workspace.crmlLibrary) + ")"});
        }
        libraries.add(new String[] {workspace.testCase.name(), "loadFile(" + literal(workspace.packageFile) + ")"});
        return libraries;
    }
}
