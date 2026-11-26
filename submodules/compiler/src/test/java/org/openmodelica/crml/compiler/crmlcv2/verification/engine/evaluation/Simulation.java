package org.openmodelica.crml.compiler.crmlcv2.verification.engine.evaluation;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

/**
 * A simulation of the case's class: the omc run, the result file, and the
 * interval, which is the reference's if there is one.
 */
public final class Simulation {

    private final Workspace workspace;
    private final List<OmcRun> runs;
    private final OmcRun run;
    private final Path resultFile;
    private final String stopTime;

    Simulation(Workspace workspace, List<OmcRun> runs, OmcRun run, Path resultFile, String stopTime) {
        this.workspace = workspace;
        this.runs = Collections.unmodifiableList(runs);
        this.run = run;
        this.resultFile = resultFile;
        this.stopTime = stopTime;
    }

    public Workspace workspace() {
        return workspace;
    }

    /** Every omc run, in order: reading the reference's interval, if there is one, then the simulation. */
    public List<OmcRun> runs() {
        return runs;
    }

    /** The simulation's run. */
    public OmcRun run() {
        return run;
    }

    public boolean ok() {
        return run.ok();
    }

    public String reason() {
        return run.reason();
    }

    /** The result file, or null if the simulation failed. */
    public Path resultFile() {
        return resultFile;
    }

    /** The reference copied into the working directory, or null if there is none. */
    public Path reference() {
        return workspace.reference;
    }

    /** The end of the simulated interval as omc printed it, or null for the model's own. */
    public String stopTime() {
        return stopTime;
    }
}
