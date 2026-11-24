package org.openmodelica.crml.compiler.crmlcv2.verification.engine.evaluation;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * One omc script and what it printed. Scripts mark the values Java needs with
 * lines {@code @@KEY value}; everything else is omc's own output. {@link #ok()}
 * says whether the script did what it was for, and {@link #reason()} why not.
 */
public final class OmcRun {

    private final Path directory;
    private final String name;
    private final String script;
    private final String log;
    private final boolean timedOut;
    private final String problem;

    OmcRun(Path directory, String name, String script, String log, boolean timedOut) {
        this(directory, name, script, log, timedOut, null);
    }

    private OmcRun(Path directory, String name, String script, String log, boolean timedOut, String problem) {
        this.directory = directory;
        this.name = name;
        this.script = script;
        this.log = log;
        this.timedOut = timedOut;
        this.problem = problem;
    }

    /** This run, failed for {@code problem}, or ok if it is null. */
    public OmcRun withProblem(String problem) {
        return new OmcRun(directory, name, script, log, timedOut, problem);
    }

    /** The working directory it ran in. */
    public Path directory() {
        return directory;
    }

    /** The script's file name, e.g. {@code simulate.mos}. */
    public String name() {
        return name;
    }

    public String script() {
        return script;
    }

    /** The whole of what omc printed. */
    public String log() {
        return log;
    }

    public boolean ok() {
        return problem == null;
    }

    /** Why the run did not do what it was for, or null if it did. */
    public String reason() {
        return problem;
    }

    /** The value of the first {@code @@key} line, or null if there is none. */
    public String value(String key) {
        List<String> values = values(key);
        return values.isEmpty() ? null : values.get(0);
    }

    /** The values of every {@code @@key} line, in order. */
    public List<String> values(String key) {
        List<String> values = new ArrayList<>();
        String prefix = "@@" + key;
        for (String line : log.split("\r?\n")) {
            if (line.equals(prefix) || line.startsWith(prefix + " ")) {
                values.add(line.substring(prefix.length()).trim());
            }
        }
        return values;
    }

    /** The lines after the {@code @@key} line, up to the next marker. */
    public String text(String key) {
        StringBuilder text = new StringBuilder();
        boolean in = false;
        for (String line : log.split("\r?\n")) {
            if (line.startsWith("@@")) {
                in = line.equals("@@" + key) || line.startsWith("@@" + key + " ");
            } else if (in && !line.isEmpty()) {
                text.append(line).append('\n');
            }
        }
        return text.toString().trim();
    }

    /** What omc reported as errors: for a script that stopped before it printed what it was for. */
    public String errors() {
        if (timedOut) {
            return "omc timed out";
        }
        StringBuilder errors = new StringBuilder();
        for (String line : log.split("\r?\n")) {
            if (line.contains("Error:") || line.contains("error:")) {
                errors.append(line.trim()).append('\n');
            }
        }
        return errors.length() == 0 ? "omc stopped without reporting an error" : errors.toString().trim();
    }
}
