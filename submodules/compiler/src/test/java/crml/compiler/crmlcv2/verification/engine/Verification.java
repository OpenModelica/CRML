package crml.compiler.crmlcv2.verification.engine;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import crml.compiler.crmlcv2.TranslationResult;
import crml.compiler.omc.OMCUtil;
import crml.compiler.util.CompilerRoot;
import crml.compiler.util.CrmlFrontEnd;

/**
 * One case taken as far as it goes: translate, package, load and check,
 * simulate, compare with the reference.
 *
 * <p>The comparison is over the model's own CRML variables, found through the
 * trace, not over whatever names happen to appear in both result files.
 * Internal names (block instances, hoisted helpers) are the compiler's business
 * and may differ from the reference model's; a CRML variable is what the
 * requirement means, and the reference observes it under the same name. A CRML
 * variable the reference observes but the simulation does not produce is a
 * failure, not a skipped comparison.
 *
 * <p>Class instances and sets are walked down to their scalar members (see
 * {@link ComparedVariables}), and only scalars are compared, by name. A Clock, Period,
 * Periods or Event has a different representation in the compiler's library
 * and in the reference models (a record or a block instance on one side, a
 * scalar or a differently shaped record on the other), so comparing them needs
 * a definition of equality per type; until there is one they are reported as
 * not comparable, and a case that has any is at best a partial match.
 */
public final class Verification {

    /** How far a case got. Declared in pipeline order: each one implies the stages before passed. */
    public enum Outcome {
        NO_REFERENCE,
        TRANSLATION_FAILED,
        LOAD_FAILED,
        CHECK_FAILED,
        SIMULATION_FAILED,
        NOTHING_TO_COMPARE,
        MISSING_VARIABLES,
        MISMATCH,
        /** Everything compared matches, but some variables could not be compared. */
        PARTIAL_MATCH,
        MATCH
    }

    /** What happened to one compared variable. */
    public enum VariableStatus {
        MATCH,
        DIFFERS,
        /** The reference observes it, the simulation result does not have it. */
        MISSING,
        /** The translation dropped the CRML variable, so no component exists. */
        DROPPED,
        /** The CRML variable exists but the reference does not observe it. */
        NOT_IN_REFERENCE,
        /** The variable's type has no comparison defined yet. */
        NOT_COMPARABLE,
        /** Comparable, but the case stopped before the comparison. */
        NOT_REACHED
    }

    private static final Path OUT = CompilerRoot.OUT.resolve("verification");
    private static final Pattern WITHIN = Pattern.compile("^\\s*within\\b[^;]*;", Pattern.MULTILINE);

    private final VerificationCase verificationCase;
    private final OmcRunner omc;

    private CrmlFrontEnd frontEnd;
    private Path directory;
    private Path packageDirectory;
    private Path reference;
    private OmcRunner.Output simulation;
    private OmcRunner.Output comparison;
    private Outcome outcome;
    private String reason = "";
    private final List<ComparedVariables.Entry> entries = new ArrayList<>();
    private final Map<String, VariableStatus> variables = new LinkedHashMap<>();

    private Verification(VerificationCase verificationCase, OmcRunner omc) {
        this.verificationCase = verificationCase;
        this.omc = omc;
    }

    public static Verification run(VerificationCase verificationCase, long timeoutSeconds)
            throws IOException, InterruptedException {
        Verification verification = new Verification(verificationCase, new OmcRunner(timeoutSeconds));
        verification.run();
        verification.writeSummary();
        return verification;
    }

    private void run() throws IOException, InterruptedException {
        frontEnd = CrmlFrontEnd.run(verificationCase.source());
        if (!verificationCase.hasReference()) {
            finish(Outcome.NO_REFERENCE, "no reference result at " + verificationCase.reference());
            return;
        }
        TranslationResult translation = frontEnd.translation();
        if (translation == null || !frontEnd.errors().isEmpty()) {
            finish(Outcome.TRANSLATION_FAILED, translationFailure());
            return;
        }

        // Known from the translation alone, so the report lists the variables
        // that would be compared even for a case that stops before comparing.
        for (ComparedVariables.Entry entry : ComparedVariables.of(frontEnd.model(), translation.trace())) {
            entries.add(entry);
            variables.put(entry.name, !entry.comparable ? VariableStatus.NOT_COMPARABLE
                : entry.dropped ? VariableStatus.DROPPED
                : VariableStatus.NOT_REACHED);
        }

        writePackage(translation.text());
        simulation = omc.run(directory, "simulate.mos", simulationScript());
        if (simulation.has("TIMEOUT")) {
            finish(Outcome.SIMULATION_FAILED, "omc timed out");
            return;
        }
        for (String library : new String[] {"LOAD_MSL", "LOAD_CRMLtoModelica", "LOAD_CRML", "LOAD_MODEL"}) {
            if (simulation.has(library) && !"true".equals(simulation.value(library))) {
                finish(Outcome.LOAD_FAILED, library + ": " + simulation.text(library));
                return;
            }
        }
        if (!simulation.has("CHECK") || !simulation.text("CHECK").contains("completed successfully")) {
            finish(Outcome.CHECK_FAILED, simulation.errors());
            return;
        }
        String resultFile = simulation.value("RESULT_FILE");
        if (resultFile == null || resultFile.isEmpty()) {
            finish(Outcome.SIMULATION_FAILED, simulation.errors());
            return;
        }

        compare(Paths.get(resultFile));
    }

    private void compare(Path resultFile) throws IOException, InterruptedException {
        Set<String> actual = new LinkedHashSet<>(simulation.values("ACTUAL"));
        List<String> expected = simulation.values("EXPECTED");
        List<String> compared = new ArrayList<>();
        for (ComparedVariables.Entry entry : entries) {
            if (!entry.comparable) {
                variables.put(entry.name, VariableStatus.NOT_COMPARABLE);
            } else if (!expected.contains(entry.name)) {
                variables.put(entry.name, VariableStatus.NOT_IN_REFERENCE);
            } else if (entry.dropped) {
                variables.put(entry.name, VariableStatus.DROPPED);
            } else if (!actual.contains(entry.name)) {
                variables.put(entry.name, VariableStatus.MISSING);
            } else {
                variables.put(entry.name, VariableStatus.MATCH);
                compared.add(entry.name);
            }
        }

        if (!compared.isEmpty()) {
            comparison = omc.run(directory, "compare.mos", comparisonScript(resultFile, compared));
            for (String failed : comparison.values("DIFF_FAILED")) {
                variables.put(failed, VariableStatus.DIFFERS);
            }
            if (!comparison.has("DIFF_OK")) {
                finish(Outcome.SIMULATION_FAILED, "the comparison did not run: " + comparison.errors());
                return;
            }
        }

        int differs = count(VariableStatus.DIFFERS);
        int missing = count(VariableStatus.MISSING) + count(VariableStatus.DROPPED);
        if (differs > 0) {
            finish(Outcome.MISMATCH, differs + " variable(s) differ from the reference"
                + (missing > 0 ? ", " + missing + " missing" : ""));
        } else if (missing > 0) {
            finish(Outcome.MISSING_VARIABLES, missing + " variable(s) the reference observes are missing");
        } else if (compared.isEmpty()) {
            finish(Outcome.NOTHING_TO_COMPARE, "none of the model's variables could be compared with the reference"
                + (count(VariableStatus.NOT_COMPARABLE) > 0
                    ? " (" + count(VariableStatus.NOT_COMPARABLE) + " not comparable)" : ""));
        } else if (count(VariableStatus.NOT_COMPARABLE) > 0) {
            finish(Outcome.PARTIAL_MATCH, compared.size() + " variable(s) match the reference, "
                + count(VariableStatus.NOT_COMPARABLE) + " could not be compared");
        } else {
            finish(Outcome.MATCH, compared.size() + " variable(s) match the reference");
        }
    }

    // --- files and scripts -------------------------------------------------

    /**
     * {@code <out>/<Name>/package.mo}, the generated model and the harness, each
     * with a {@code within} clause naming that package, so the harness's own
     * package path does not matter.
     */
    private void writePackage(String modelica) throws IOException {
        directory = OUT.resolve(verificationCase.suite()).resolve(verificationCase.name()).toAbsolutePath();
        deleteRecursively(directory);
        // Not directly in the working directory: omc names the simulation
        // executable after the class, and it would collide with the package.
        packageDirectory = directory.resolve("src").resolve(verificationCase.name());
        Files.createDirectories(packageDirectory);

        String name = verificationCase.name();
        // The fixtures may be read from inside the test-resources jar, which omc
        // cannot see, so everything it reads is copied next to the scripts.
        reference = directory.resolve(verificationCase.reference().getFileName().toString());
        Files.copy(verificationCase.reference(), reference);
        if (verificationCase.mode() == VerificationCase.Mode.HARNESS) {
            Files.copy(crmlLibrary(), directory.resolve("CRML.mo"));
        }

        write(packageDirectory.resolve("package.mo"), "within ;\npackage " + name + "\nend " + name + ";\n");
        write(packageDirectory.resolve(name + ".mo"), within(modelica, name));
        if (verificationCase.harness() != null) {
            try (DirectoryStream<Path> files = Files.newDirectoryStream(verificationCase.harness(), "*.mo")) {
                for (Path file : files) {
                    String text = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
                    write(packageDirectory.resolve(file.getFileName().toString()), within(text, name));
                }
            }
        }
    }

    private static String within(String modelica, String packageName) {
        Matcher matcher = WITHIN.matcher(modelica);
        String clause = "within " + packageName + ";";
        return matcher.find() ? matcher.replaceFirst(clause) : clause + "\n" + modelica;
    }

    private String simulationScript() {
        boolean harness = verificationCase.mode() == VerificationCase.Mode.HARNESS;
        String simulated = verificationCase.name() + "." + verificationCase.simulatedClass();
        String reference = OmcRunner.literal(this.reference);
        StringBuilder script = new StringBuilder("echo(false);\n");
        if (harness) {
            // The harnesses are built from CRML.mo, which uses the MSL.
            load(script, "LOAD_MSL", "loadModel(Modelica, {\"4.0.0\"})");
        }
        load(script, "LOAD_CRMLtoModelica", "loadFile(" + OmcRunner.literal(OMCUtil.getCRMLToModelicaFile()) + ")");
        if (harness) {
            load(script, "LOAD_CRML", "loadFile(" + OmcRunner.literal(directory.resolve("CRML.mo")) + ")");
        }
        load(script, "LOAD_MODEL", "loadFile(" + OmcRunner.literal(packageDirectory.resolve("package.mo")) + ")");
        script.append("check := checkModel(").append(simulated).append(");\n")
            .append("print(\"@@CHECK\\n\" + check + \"\\n\" + getErrorString() + \"\\n\");\n")
            // The reference decides the simulated interval; the harness's own
            // experiment annotation is not necessarily the interval it was made with.
            .append("ref := readSimulationResult(").append(reference).append(", {time});\n")
            .append("stop := ref[1, size(ref, 2)];\n")
            .append("print(\"@@STOP_TIME \" + String(stop) + \"\\n\" + getErrorString() + \"\\n\");\n")
            .append("res := simulate(").append(simulated)
            .append(", stopTime = stop, fileNamePrefix = ").append(OmcRunner.literal(verificationCase.simulatedClass()))
            .append(");\n")
            // A record field cannot be read inside an expression in a script,
            // only assigned: "res.resultFile + ..." fails without a message.
            .append("resultFile := res.resultFile;\n")
            .append("messages := res.messages;\n")
            .append("print(\"@@RESULT_FILE \" + resultFile + \"\\n\" + messages + \"\\n\" + getErrorString() + \"\\n\");\n")
            .append("if resultFile <> \"\" then\n")
            .append("  actual := readSimulationResultVars(resultFile);\n")
            .append("  for v in actual loop print(\"@@ACTUAL \" + v + \"\\n\"); end for;\n")
            .append("end if;\n")
            .append("expected := readSimulationResultVars(").append(reference).append(");\n")
            .append("for v in expected loop print(\"@@EXPECTED \" + v + \"\\n\"); end for;\n");
        return script.toString();
    }

    private static void load(StringBuilder script, String key, String call) {
        script.append("ok := ").append(call).append(";\n")
            .append("print(\"@@").append(key).append(" \" + String(ok) + \"\\n\" + getErrorString() + \"\\n\");\n")
            .append("if not ok then exit(1); end if;\n");
    }

    private String comparisonScript(Path resultFile, List<String> compared) {
        StringBuilder vars = new StringBuilder();
        for (String variable : compared) {
            vars.append(vars.length() == 0 ? "" : ", ").append(OmcRunner.literal(variable));
        }
        return "echo(false);\n"
            + "(ok, failed) := diffSimulationResults(" + OmcRunner.literal(resultFile) + ", "
            + OmcRunner.literal(reference) + ", "
            + OmcRunner.literal(directory.resolve("diff").toString()) + ", vars = {" + vars + "});\n"
            + "print(\"@@DIFF_OK \" + String(ok) + \"\\n\" + getErrorString() + \"\\n\");\n"
            + "for v in failed loop print(\"@@DIFF_FAILED \" + v + \"\\n\"); end for;\n";
    }

    private static Path crmlLibrary() {
        return CompilerRoot.RESOURCES.resolve("modelica_libraries").resolve("CRML.mo");
    }

    // --- results -----------------------------------------------------------

    /** {@code summary.txt} in the working directory: the outcome and every variable's status. */
    private void writeSummary() throws IOException {
        if (directory == null) {
            return;
        }
        StringBuilder summary = new StringBuilder()
            .append(verificationCase).append(" (").append(verificationCase.mode()).append(")\n")
            .append(outcome).append(": ").append(reason).append("\n\n");
        for (Map.Entry<String, VariableStatus> entry : variables.entrySet()) {
            summary.append(String.format("%-18s %s%n", entry.getValue(), entry.getKey()));
        }
        write(directory.resolve("summary.txt"), summary.toString());
    }

    private void finish(Outcome outcome, String reason) {
        this.outcome = outcome;
        this.reason = reason == null ? "" : reason;
    }

    private String translationFailure() {
        if (frontEnd.hasSyntaxErrors()) {
            return "syntax errors: " + frontEnd.syntaxErrors();
        }
        if (frontEnd.error() != null) {
            return "threw " + frontEnd.error();
        }
        if (frontEnd.model() == null) {
            return "the definition is not a model";
        }
        return "ERROR diagnostics: " + frontEnd.errors();
    }

    private int count(VariableStatus status) {
        int count = 0;
        for (VariableStatus value : variables.values()) {
            if (value == status) {
                count++;
            }
        }
        return count;
    }

    public VerificationCase verificationCase() {
        return verificationCase;
    }

    public CrmlFrontEnd frontEnd() {
        return frontEnd;
    }

    public Outcome outcome() {
        return outcome;
    }

    public String reason() {
        return reason;
    }

    /**
     * Every variable the case compares or would compare, in model order, with
     * its status; empty if the model did not translate.
     */
    public Map<String, VariableStatus> variables() {
        return variables;
    }

    /** The case's working directory, or null if nothing was written. */
    public Path directory() {
        return directory;
    }

    /** The simulation script's output, or null if it did not run. */
    public OmcRunner.Output simulation() {
        return simulation;
    }

    /** The comparison script's output, or null if it did not run. */
    public OmcRunner.Output comparison() {
        return comparison;
    }

    // --- helpers -----------------------------------------------------------

    private static void write(Path file, String text) throws IOException {
        Files.write(file, text.getBytes(StandardCharsets.UTF_8));
    }

    private static void deleteRecursively(Path directory) throws IOException {
        if (!Files.exists(directory)) {
            return;
        }
        try (Stream<Path> walk = Files.walk(directory)) {
            for (Path path : (Iterable<Path>) walk.sorted(Comparator.reverseOrder())::iterator) {
                Files.delete(path);
            }
        }
    }
}
