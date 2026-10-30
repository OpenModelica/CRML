package crml.compiler.crmlcv2.verification.engine;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import crml.compiler.omc.OMCUtil;

/**
 * Runs an OpenModelica script and hands back what it reported.
 *
 * <p>Scripts report through marker lines, {@code @@KEY value}, which this
 * collects per key; any other line is attached to the marker before it. That
 * keeps the decision of what succeeded in Java, on values the script printed on
 * purpose, rather than in pattern matching over omc's free-form output.
 */
public final class OmcRunner {

    /** What a script printed, by marker. */
    public static final class Output {
        private final String log;
        private final Map<String, List<String>> values = new LinkedHashMap<>();
        private final Map<String, StringBuilder> texts = new LinkedHashMap<>();

        Output(String log) {
            this.log = log;
            String current = null;
            for (String line : log.split("\r?\n", -1)) {
                if (line.startsWith("@@")) {
                    int space = line.indexOf(' ');
                    current = space < 0 ? line.substring(2) : line.substring(2, space);
                    String value = space < 0 ? "" : line.substring(space + 1).trim();
                    values.computeIfAbsent(current, k -> new ArrayList<>()).add(value);
                    texts.computeIfAbsent(current, k -> new StringBuilder());
                } else if (current != null && !line.isEmpty()) {
                    texts.get(current).append(line).append('\n');
                }
            }
        }

        /** The whole of what omc printed. */
        public String log() {
            return log;
        }

        boolean has(String key) {
            return values.containsKey(key);
        }

        /** The value of the first {@code @@key} marker, or null if there was none. */
        String value(String key) {
            List<String> all = values.get(key);
            return all == null ? null : all.get(0);
        }

        /** The values of every {@code @@key} marker, in order. */
        List<String> values(String key) {
            List<String> all = values.get(key);
            return all == null ? new ArrayList<>() : all;
        }

        /**
         * The lines omc reported as errors, for a run that stopped before it
         * printed the marker that would have explained it.
         */
        String errors() {
            StringBuilder errors = new StringBuilder();
            for (String line : log.split("\r?\n")) {
                if (line.contains("Error:") || line.contains("error:")) {
                    errors.append(line.trim()).append('\n');
                }
            }
            return errors.length() == 0 ? "omc stopped without reporting an error" : errors.toString().trim();
        }

        /** The unmarked lines printed after {@code @@key} markers. */
        String text(String key) {
            StringBuilder text = texts.get(key);
            return text == null ? "" : text.toString().trim();
        }
    }

    private final long timeoutSeconds;

    OmcRunner(long timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    /** True when omc can be found, so the suite can skip rather than fail without it. */
    public static boolean available() {
        String omc = OMCUtil.locateOMC();
        return new File(omc).isAbsolute() && new File(omc).canExecute();
    }

    /** Writes {@code script} to {@code directory/fileName} and runs it there. */
    Output run(Path directory, String fileName, String script) throws IOException, InterruptedException {
        Path scriptFile = directory.resolve(fileName);
        Files.write(scriptFile, script.getBytes(StandardCharsets.UTF_8));

        Path logFile = directory.resolve(fileName.replaceAll("\\.mos$", "") + ".log");
        Process process = new ProcessBuilder(OMCUtil.locateOMC(), fileName)
            .directory(directory.toFile())
            .redirectErrorStream(true)
            .redirectOutput(logFile.toFile())
            .start();
        if (!process.waitFor(timeoutSeconds, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            String log = new String(Files.readAllBytes(logFile), StandardCharsets.UTF_8);
            return new Output(log + "\n@@TIMEOUT " + timeoutSeconds + "\n");
        }
        return new Output(new String(Files.readAllBytes(logFile), StandardCharsets.UTF_8));
    }

    /** A path as an omc string literal. */
    static String literal(Path path) {
        if (path.getFileSystem() != java.nio.file.FileSystems.getDefault()) {
            throw new IllegalArgumentException(path + " is not on disk, so omc cannot read it");
        }
        return "\"" + path.toAbsolutePath().toString().replace('\\', '/').replace("\"", "\\\"") + "\"";
    }

    /** A string as an omc string literal. */
    static String literal(String text) {
        return "\"" + text.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}
