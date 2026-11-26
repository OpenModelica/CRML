package org.openmodelica.crml.compiler.crmlcv2.verification.engine.verification;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openmodelica.crml.compiler.crmlcv2.verification.engine.evaluation.Omc;
import org.openmodelica.crml.compiler.crmlcv2.verification.engine.evaluation.OmcRun;

/**
 * Reads result files (.mat) through omc: the names a file records, and the
 * (time, value) points of chosen signals, which Java then compares. Points are
 * printed rather than exported, because omc exports parameters (such as a
 * constant output) only to .mat, which Java cannot read.
 */
final class Signals {

    private Signals() {
    }

    /** The names {@code file} records, as {@code @@NAME} lines; failed if there are none. */
    static OmcRun names(Path directory, String scriptName, Path file) throws IOException, InterruptedException {
        OmcRun run = Omc.run(directory, scriptName,
            "vars := readSimulationResultVars(" + Omc.literal(file) + ");\n"
            + "for v in vars loop print(\"@@NAME \" + v + \"\\n\"); end for;\n"
            + "print(\"@@END\\n\" + getErrorString() + \"\\n\");\n");
        return run.withProblem(run.values("NAME").isEmpty()
            ? "omc cannot read " + file.getFileName() + ": " + run.errors() : null);
    }

    static Set<String> names(OmcRun run) {
        return new LinkedHashSet<>(run.values("NAME"));
    }

    /**
     * The points of {@code signals} in {@code file}, each as {@code @@SIGNAL <name>}
     * followed by one {@code <time> <value>} line per point; failed unless every
     * signal was read.
     */
    static OmcRun read(Path directory, String scriptName, Path file, Collection<String> signals)
            throws IOException, InterruptedException {
        StringBuilder script = new StringBuilder();
        for (String signal : signals) {
            script.append("r := readSimulationResult(").append(Omc.literal(file)).append(", {time, ").append(signal)
                .append("});\n")
                .append("print(\"@@SIGNAL ").append(signal).append("\\n\");\n")
                .append("for i in 1:size(r, 2) loop print(String(r[1, i], significantDigits = 15) + \" \"")
                .append(" + String(r[2, i], significantDigits = 15) + \"\\n\"); end for;\n");
        }
        script.append("print(\"@@END\\n\" + getErrorString() + \"\\n\");\n");
        OmcRun run = Omc.run(directory, scriptName, script.toString());
        return run.withProblem(run.value("END") == null
            ? "could not read " + signals + " from " + file.getFileName() + ": " + run.errors() : null);
    }

    /** The points {@link #read} printed, by signal. */
    static Map<String, List<double[]>> series(OmcRun run) {
        Map<String, List<double[]>> series = new HashMap<>();
        List<double[]> current = null;
        for (String line : run.log().split("\r?\n")) {
            if (line.startsWith("@@SIGNAL ")) {
                current = new ArrayList<>();
                series.put(line.substring("@@SIGNAL ".length()).trim(), current);
            } else if (line.startsWith("@@")) {
                current = null;
            } else if (current != null) {
                String[] parts = line.trim().split("\\s+");
                if (parts.length == 2) {
                    try {
                        current.add(new double[] {Double.parseDouble(parts[0]), Double.parseDouble(parts[1])});
                    } catch (NumberFormatException e) {
                        // not a point: an error message, which errors() reports
                    }
                }
            }
        }
        return series;
    }
}
