package crml.compiler.crmlcv2.verification.engine;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
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
 * One case taken as far as it goes.
 *
 * <p>Every case is translated, packaged, loaded, checked and simulated. A case
 * {@link VerificationCase.Mode#WITHOUT_REFERENCE without a reference} ends
 * there: it passes if it simulates, whatever the values. A case
 * {@link VerificationCase.Mode#WITH_REFERENCE with a reference} then compares
 * the model's outputs with the reference, and passes only if every output
 * matches.
 *
 * <p>The outputs are the model's CRML variables, found through the trace and
 * walked into class instances ({@link Outputs}), not whatever names happen to
 * appear in both result files: internal names are the compiler's business. A
 * Clock, Period or Event is compared by what it means, not by the record that
 * implements it ({@link Observation}). An output the reference records but the
 * simulation does not produce is a failure, not a skipped comparison.
 */
public final class Verification {

    /** How far a case got. Declared in pipeline order: each one implies the stages before passed. */
    public enum Outcome {
        /** The fixtures do not allow the case to be verified. */
        FIXTURE_INCOMPLETE,
        TRANSLATION_FAILED,
        LOAD_FAILED,
        CHECK_FAILED,
        SIMULATION_FAILED,
        /** Without a reference: the model simulated, which is all that is asked. */
        SIMULATED,
        /** With a reference: the comparison itself could not be run. */
        COMPARISON_FAILED,
        MISMATCH,
        MISSING_OUTPUTS,
        /** With a reference: no output could be compared, so nothing was verified. */
        NOTHING_COMPARED,
        /** With a reference: everything compared matches, but some outputs could not be compared. */
        PARTIAL_MATCH,
        MATCH
    }

    /** What happened to one output. */
    public enum Status {
        MATCH,
        DIFFERS,
        /** The reference records it, the simulation result does not have it. */
        MISSING,
        /** The translation produced no component for it. */
        DROPPED,
        /** The reference does not record it. */
        NOT_IN_REFERENCE,
        /** Its type has no comparison defined. */
        NO_RULE,
        /** An external, bound by the harness: not an output. */
        INPUT,
        /** Comparable, but the case stopped before the comparison. */
        NOT_REACHED
    }

    /** One output and what happened to it. */
    public static final class OutputResult {
        private final String name;
        private final String kind;
        private String comparedAs = "";
        private Status status;
        private String detail = "";

        OutputResult(String name, String kind, Status status) {
            this.name = name;
            this.kind = kind;
            this.status = status;
        }

        public String name() {
            return name;
        }

        /** The kind of value: scalar, clock, period, event, input, or a type with no rule. */
        public String kind() {
            return kind;
        }

        /** The signals compared, compiled model first, or empty if none was. */
        public String comparedAs() {
            return comparedAs;
        }

        public Status status() {
            return status;
        }

        /** Why it has its status, or empty. */
        public String detail() {
            return detail;
        }
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
    private final List<Outputs.Output> outputs = new ArrayList<>();
    private final List<OutputResult> results = new ArrayList<>();

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

    private boolean withReference() {
        return verificationCase.mode() == VerificationCase.Mode.WITH_REFERENCE;
    }

    private void run() throws IOException, InterruptedException {
        frontEnd = CrmlFrontEnd.run(verificationCase.source());
        if (verificationCase.fixtureProblem() != null) {
            finish(Outcome.FIXTURE_INCOMPLETE, verificationCase.fixtureProblem());
            return;
        }
        TranslationResult translation = frontEnd.translation();
        if (translation == null || !frontEnd.errors().isEmpty()) {
            finish(Outcome.TRANSLATION_FAILED, translationFailure());
            return;
        }

        if (withReference()) {
            // Known from the translation alone, so the report lists the outputs
            // even for a case that stops before comparing.
            for (Outputs.Output output : Outputs.of(frontEnd.model(), translation.trace())) {
                outputs.add(output);
                results.add(new OutputResult(output.name, kind(output), initialStatus(output)));
            }
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

        if (withReference()) {
            compare(Paths.get(resultFile));
        } else {
            finish(Outcome.SIMULATED, "simulated; " + simulation.value("SIMULATION_OPTIONS"));
        }
    }

    private static String kind(Outputs.Output output) {
        return output.kind == Outputs.Kind.NO_RULE ? output.type : output.kind.name().toLowerCase();
    }

    private static Status initialStatus(Outputs.Output output) {
        switch (output.kind) {
            case INPUT:
                return Status.INPUT;
            case NO_RULE:
                return Status.NO_RULE;
            default:
                return output.dropped ? Status.DROPPED : Status.NOT_REACHED;
        }
    }

    // --- comparison --------------------------------------------------------

    private void compare(Path resultFile) throws IOException, InterruptedException {
        Set<String> actual = new LinkedHashSet<>(simulation.values("ACTUAL"));
        Set<String> expected = new LinkedHashSet<>(simulation.values("EXPECTED"));
        double tolerance = 1e-4 * Math.max(1, parse(simulation.value("STOP_TIME")));

        Map<OutputResult, Observation> observed = new HashMap<>();
        for (int i = 0; i < outputs.size(); i++) {
            Outputs.Output output = outputs.get(i);
            OutputResult result = results.get(i);
            if (result.status == Status.INPUT || result.status == Status.NO_RULE) {
                continue;
            }
            Observation observation = Observation.of(output, actual, expected);
            if (observation.unobservable != null) {
                result.status = Status.NOT_IN_REFERENCE;
                result.detail = observation.unobservable;
            } else if (output.dropped) {
                result.status = Status.DROPPED;
                result.detail = "the translation produced no component for it";
            } else if (observation.missing != null) {
                result.status = Status.MISSING;
                result.detail = "the result has no " + observation.missing;
            } else {
                observed.put(result, observation);
                StringBuilder comparedAs = new StringBuilder();
                for (Observation.Check check : observation.checks) {
                    comparedAs.append(comparedAs.length() == 0 ? "" : ", ").append(check.describe());
                }
                result.comparedAs = comparedAs.toString();
            }
        }

        if (!observed.isEmpty()) {
            List<Observation.Check> trajectories = new ArrayList<>();
            List<Observation.Check> steps = new ArrayList<>();
            for (OutputResult result : results) {
                Observation observation = observed.get(result);
                if (observation != null) {
                    for (Observation.Check check : observation.checks) {
                        (check.method == Observation.Method.TRAJECTORY ? trajectories : steps).add(check);
                    }
                }
            }
            comparison = omc.run(directory, "compare.mos", comparisonScript(resultFile, trajectories, steps));
            if (!trajectories.isEmpty() && !comparison.has("DIFF_OK")) {
                finish(Outcome.COMPARISON_FAILED, "diffSimulationResults did not run: " + comparison.errors());
                return;
            }
            Set<String> failedTrajectories = new LinkedHashSet<>(comparison.values("DIFF_FAILED"));
            Map<String, List<double[]>> series = series(comparison.values("SERIES"));
            Set<String> read = new LinkedHashSet<>(comparison.values("SERIES_END"));

            for (OutputResult result : results) {
                Observation observation = observed.get(result);
                if (observation == null) {
                    continue;
                }
                result.status = Status.MATCH;
                for (Observation.Check check : observation.checks) {
                    String difference;
                    if (check.method == Observation.Method.TRAJECTORY) {
                        difference = failedTrajectories.contains(check.actual)
                            ? "differs beyond tolerance; see diff." + check.actual + ".csv" : null;
                    } else {
                        int index = steps.indexOf(check);
                        if (!read.contains("a" + index) || !read.contains("e" + index)) {
                            finish(Outcome.COMPARISON_FAILED, "could not read " + check.describe()
                                + ": " + comparison.errors());
                            return;
                        }
                        Steps got = Steps.of(series.getOrDefault("a" + index, Collections.emptyList()), tolerance);
                        Steps want = Steps.of(series.getOrDefault("e" + index, Collections.emptyList()), tolerance);
                        difference = got.difference(want, tolerance);
                    }
                    if (difference != null) {
                        result.status = Status.DIFFERS;
                        result.detail += (result.detail.isEmpty() ? "" : "; ") + check.describe() + ": " + difference;
                    }
                }
            }
        }

        int matched = count(Status.MATCH);
        int differs = count(Status.DIFFERS);
        int missing = count(Status.MISSING) + count(Status.DROPPED);
        int unobserved = count(Status.NOT_IN_REFERENCE) + count(Status.NO_RULE);
        if (differs > 0) {
            finish(Outcome.MISMATCH, differs + " output(s) differ from the reference"
                + (missing > 0 ? ", " + missing + " missing" : ""));
        } else if (missing > 0) {
            finish(Outcome.MISSING_OUTPUTS, missing + " output(s) the reference records are missing");
        } else if (matched == 0) {
            finish(Outcome.NOTHING_COMPARED, "no output could be compared with the reference"
                + (unobserved > 0 ? " (" + unobserved + " not observable)" : ""));
        } else if (unobserved > 0) {
            finish(Outcome.PARTIAL_MATCH, matched + " output(s) match the reference, "
                + unobserved + " could not be compared");
        } else {
            finish(Outcome.MATCH, matched + " output(s) match the reference");
        }
    }

    /** The {@code @@SERIES <id> <time> <value>} points, by id. */
    private static Map<String, List<double[]>> series(List<String> values) {
        Map<String, List<double[]>> series = new HashMap<>();
        for (String value : values) {
            String[] parts = value.trim().split("\\s+");
            if (parts.length == 3) {
                series.computeIfAbsent(parts[0], k -> new ArrayList<>())
                    .add(new double[] {parse(parts[1]), parse(parts[2])});
            }
        }
        return series;
    }

    private static double parse(String number) {
        try {
            return number == null ? 0 : Double.parseDouble(number.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // --- files and scripts -------------------------------------------------

    /**
     * {@code <out>/<Name>/src/<Name>/package.mo}, the generated model and the
     * harness, each with a {@code within} clause naming that package, so the
     * harness's own package path does not matter.
     */
    private void writePackage(String modelica) throws IOException {
        directory = OUT.resolve(verificationCase.suite()).resolve(verificationCase.name()).toAbsolutePath();
        deleteRecursively(directory);
        // Not directly in the working directory: omc names the simulation
        // executable after the class, and it would collide with the package.
        packageDirectory = directory.resolve("src").resolve(verificationCase.name());
        Files.createDirectories(packageDirectory);

        String name = verificationCase.name();
        write(packageDirectory.resolve("package.mo"), "within ;\npackage " + name + "\nend " + name + ";\n");
        write(packageDirectory.resolve(name + ".mo"), within(modelica, name));
        if (withReference()) {
            // The fixtures may be read from inside the test-resources jar, which
            // omc cannot see, so everything it reads is copied next to the scripts.
            reference = directory.resolve(verificationCase.reference().getFileName().toString());
            Files.copy(verificationCase.reference(), reference);
            Files.copy(crmlLibrary(), directory.resolve("CRML.mo"));
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
        String simulated = verificationCase.name() + "." + verificationCase.simulatedClass();
        StringBuilder script = new StringBuilder("echo(false);\n");
        if (withReference()) {
            // The harnesses are built from CRML.mo, which uses the MSL.
            load(script, "LOAD_MSL", "loadModel(Modelica, {\"4.0.0\"})");
        }
        load(script, "LOAD_CRMLtoModelica", "loadFile(" + OmcRunner.literal(OMCUtil.getCRMLToModelicaFile()) + ")");
        if (withReference()) {
            load(script, "LOAD_CRML", "loadFile(" + OmcRunner.literal(directory.resolve("CRML.mo")) + ")");
        }
        load(script, "LOAD_MODEL", "loadFile(" + OmcRunner.literal(packageDirectory.resolve("package.mo")) + ")");
        script.append("check := checkModel(").append(simulated).append(");\n")
            .append("print(\"@@CHECK\\n\" + check + \"\\n\" + getErrorString() + \"\\n\");\n");
        String prefix = OmcRunner.literal(verificationCase.simulatedClass());
        if (withReference()) {
            // The reference decides the simulated interval; the harness's own
            // experiment annotation is not necessarily the interval it was made with.
            String reference = OmcRunner.literal(this.reference);
            script.append("ref := readSimulationResult(").append(reference).append(", {time});\n")
                .append("stop := ref[1, size(ref, 2)];\n")
                .append("print(\"@@STOP_TIME \" + String(stop) + \"\\n\" + getErrorString() + \"\\n\");\n")
                .append("res := simulate(").append(simulated)
                .append(", stopTime = stop, fileNamePrefix = ").append(prefix).append(");\n");
        } else {
            // No reference to take the interval from: the model's own experiment
            // annotation, or omc's default.
            script.append("res := simulate(").append(simulated)
                .append(", fileNamePrefix = ").append(prefix).append(");\n");
        }
        // A record field cannot be read inside an expression in a script, only
        // assigned: "res.resultFile + ..." fails without a message.
        script.append("resultFile := res.resultFile;\n")
            .append("messages := res.messages;\n")
            .append("options := res.simulationOptions;\n")
            .append("print(\"@@SIMULATION_OPTIONS \" + options + \"\\n\");\n")
            .append("print(\"@@RESULT_FILE \" + resultFile + \"\\n\" + messages + \"\\n\" + getErrorString() + \"\\n\");\n");
        if (withReference()) {
            String reference = OmcRunner.literal(this.reference);
            script.append("if resultFile <> \"\" then\n")
                .append("  actual := readSimulationResultVars(resultFile);\n")
                .append("  for v in actual loop print(\"@@ACTUAL \" + v + \"\\n\"); end for;\n")
                .append("end if;\n")
                .append("expected := readSimulationResultVars(").append(reference).append(");\n")
                .append("for v in expected loop print(\"@@EXPECTED \" + v + \"\\n\"); end for;\n");
        }
        return script.toString();
    }

    private static void load(StringBuilder script, String key, String call) {
        script.append("ok := ").append(call).append(";\n")
            .append("print(\"@@").append(key).append(" \" + String(ok) + \"\\n\" + getErrorString() + \"\\n\");\n")
            .append("if not ok then exit(1); end if;\n");
    }

    /**
     * Scalars through {@code diffSimulationResults}; every other signal printed
     * as its change points, {@code @@SERIES a<i>} for the compiled model and
     * {@code e<i>} for the reference, each closed by {@code @@SERIES_END} so a
     * signal that could not be read is told apart from one that never changes.
     */
    private String comparisonScript(Path resultFile, List<Observation.Check> trajectories,
            List<Observation.Check> steps) {
        StringBuilder script = new StringBuilder("echo(false);\n");
        if (!trajectories.isEmpty()) {
            StringBuilder vars = new StringBuilder();
            for (Observation.Check check : trajectories) {
                vars.append(vars.length() == 0 ? "" : ", ").append(OmcRunner.literal(check.actual));
            }
            script.append("(ok, failed) := diffSimulationResults(").append(OmcRunner.literal(resultFile))
                .append(", ").append(OmcRunner.literal(reference))
                .append(", ").append(OmcRunner.literal(directory.resolve("diff").toString()))
                .append(", vars = {").append(vars).append("});\n")
                .append("print(\"@@DIFF_OK \" + String(ok) + \"\\n\" + getErrorString() + \"\\n\");\n")
                .append("for v in failed loop print(\"@@DIFF_FAILED \" + v + \"\\n\"); end for;\n");
        }
        for (int i = 0; i < steps.size(); i++) {
            printSeries(script, "a" + i, resultFile, steps.get(i).actual);
            printSeries(script, "e" + i, reference, steps.get(i).expected);
        }
        return script.toString();
    }

    private static void printSeries(StringBuilder script, String id, Path file, String variable) {
        script.append("r := readSimulationResult(").append(OmcRunner.literal(file))
            .append(", {time, ").append(variable).append("});\n")
            .append("for i in 1:size(r, 2) loop\n")
            .append("  if i == 1 or r[2, i] <> r[2, i - 1] then\n")
            .append("    print(\"@@SERIES ").append(id)
            .append(" \" + String(r[1, i], significantDigits = 15) + \" \" + String(r[2, i], significantDigits = 15) + \"\\n\");\n")
            .append("  end if;\n")
            .append("end for;\n")
            .append("print(\"@@SERIES_END ").append(id).append("\\n\" + getErrorString() + \"\\n\");\n");
    }

    private static Path crmlLibrary() {
        return CompilerRoot.RESOURCES.resolve("modelica_libraries").resolve("CRML.mo");
    }

    // --- results -----------------------------------------------------------

    /** {@code summary.txt} in the working directory: the mode, the outcome and every output. */
    private void writeSummary() throws IOException {
        if (directory == null) {
            return;
        }
        StringBuilder summary = new StringBuilder()
            .append(verificationCase).append("\n")
            .append(outcome).append(": ").append(reason).append("\n\n");
        for (OutputResult result : results) {
            summary.append(String.format("%-17s %-8s %s%s%s%n", result.status, result.kind, result.name,
                result.comparedAs.isEmpty() || result.comparedAs.equals(result.name) ? "" : "  [" + result.comparedAs + "]",
                result.detail.isEmpty() ? "" : "  -- " + result.detail));
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

    private int count(Status status) {
        int count = 0;
        for (OutputResult result : results) {
            if (result.status == status) {
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

    /** True if the case meets what its mode asks: it simulates, or every output matches. */
    public boolean passed() {
        return outcome == (withReference() ? Outcome.MATCH : Outcome.SIMULATED);
    }

    /**
     * True if the case neither passed nor showed a defect: the fixtures are
     * incomplete, or the reference could not observe some or all outputs.
     */
    public boolean inconclusive() {
        return outcome == Outcome.FIXTURE_INCOMPLETE || outcome == Outcome.NOTHING_COMPARED
            || outcome == Outcome.PARTIAL_MATCH;
    }

    /**
     * Every output of a case with a reference, in model order, with what
     * happened to it; empty without a reference or if the model did not translate.
     */
    public List<OutputResult> outputs() {
        return Collections.unmodifiableList(results);
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
