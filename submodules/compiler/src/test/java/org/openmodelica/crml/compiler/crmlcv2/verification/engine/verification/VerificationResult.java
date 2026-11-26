package org.openmodelica.crml.compiler.crmlcv2.verification.engine.verification;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import org.openmodelica.crml.compiler.crmlcv2.verification.engine.evaluation.OmcRun;

/**
 * Every output of the model and what came of comparing it with the reference,
 * and the omc runs that read the signals. {@link #ok()} unless the signals could
 * not be read; whether the outputs are acceptable is the monitor's call.
 */
public final class VerificationResult {

    private final Path directory;
    private final List<OutputResult> outputs;
    private final List<OmcRun> runs;
    private final String problem;

    VerificationResult(Path directory, List<OutputResult> outputs,
            List<OmcRun> runs, String problem) {
        this.directory = directory;
        this.outputs = Collections.unmodifiableList(outputs);
        this.runs = Collections.unmodifiableList(runs);
        this.problem = problem;
    }

    /** The case's working directory. */
    public Path directory() {
        return directory;
    }

    /** Every output, in model order; empty if the signals could not be read. */
    public List<OutputResult> outputs() {
        return outputs;
    }

    /** The omc runs that read the names and points of the signals, in order. */
    public List<OmcRun> runs() {
        return runs;
    }

    public boolean ok() {
        return problem == null;
    }

    /** Why the signals could not be read, or null. */
    public String reason() {
        return problem;
    }

    public int count(OutputResult.Status status) {
        int count = 0;
        for (OutputResult output : outputs) {
            if (output.status() == status) {
                count++;
            }
        }
        return count;
    }

    /** One line per output. */
    public String summary() {
        StringBuilder summary = new StringBuilder();
        for (OutputResult output : outputs) {
            summary.append(output).append('\n');
        }
        return summary.toString();
    }
}
