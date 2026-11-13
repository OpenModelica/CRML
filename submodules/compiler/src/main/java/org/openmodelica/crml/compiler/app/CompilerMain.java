package org.openmodelica.crml.compiler.app;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openmodelica.crml.compiler.crmlcv2.OMCv2;

import com.beust.jcommander.ParameterException;

/**
 * Entry point of the CRML compiler: translates {@code .crml} files to Modelica.
 *
 * <p>Each file goes through the whole pipeline: parsing, building and linking
 * the CRML object model, resolving the operator calls, and the translation by
 * {@link OMCv2}. Problems are printed on the error stream, prefixed with the
 * file they belong to. A file whose translation reports errors is still
 * written, so the result can be inspected, but counts as failed.
 *
 * <p>Exit status: {@value #EXIT_OK} when every file was translated,
 * {@value #EXIT_FAILED} when a file failed or an input was missing,
 * {@value #EXIT_USAGE} for wrong command line options.
 */
public final class CompilerMain {

    public static final int EXIT_OK = 0;
    public static final int EXIT_FAILED = 1;
    public static final int EXIT_USAGE = 2;

    private final CRMLCompilerConfig config;
    private final PrintStream out;
    private final PrintStream err;
    /** Set by {@link #collectJobs()} when an input does not exist or a directory could not be read. */
    private boolean inputsMissing;

    private CompilerMain(CRMLCompilerConfig config, PrintStream out, PrintStream err) {
        this.config = config;
        this.out = out;
        this.err = err;
    }

    public static void main(String[] args) {
        // UTF-8 whatever the locale: the object model printer draws its tree
        // with box-drawing characters, which a POSIX locale turns into '?'.
        System.exit(run(utf8(FileDescriptor.out), utf8(FileDescriptor.err), args));
    }

    private static PrintStream utf8(FileDescriptor descriptor) {
        try {
            return new PrintStream(new FileOutputStream(descriptor), true, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("UTF-8 is always supported", e);
        }
    }

    /**
     * Runs the compiler without exiting the JVM.
     *
     * @return the exit status
     */
    public static int run(PrintStream out, PrintStream err, String... args) {
        CRMLCompilerConfig config;
        try {
            config = CRMLCompilerConfig.parse(args);
        } catch (ParameterException e) {
            err.println(CRMLCompilerConfig.PROGRAM_NAME + ": " + e.getMessage());
            err.print(CRMLCompilerConfig.usage());
            return EXIT_USAGE;
        }
        if (config.help()) {
            out.print(CRMLCompilerConfig.usage());
            return EXIT_OK;
        }
        return new CompilerMain(config, out, err).compileAll();
    }

    private int compileAll() {
        List<Job> jobs = collectJobs();
        if (jobs.isEmpty()) {
            err.println(CRMLCompilerConfig.PROGRAM_NAME + ": no " + Job.CRML_EXTENSION + " file to compile");
            return EXIT_FAILED;
        }

        int translated = 0;
        int skipped = 0;
        int failed = 0;
        Set<Path> targets = new HashSet<>();
        for (Job job : jobs) {
            Path target = job.target().toAbsolutePath().normalize();
            if (!targets.add(target)) {
                report(job.source(), "ERROR not compiled: another input is also written to " + job.target());
                failed++;
                continue;
            }
            switch (job.compile(config, out, err)) {
                case TRANSLATED:
                    translated++;
                    break;
                case SKIPPED:
                    skipped++;
                    break;
                default:
                    failed++;
            }
        }

        out.println(jobs.size() + (jobs.size() == 1 ? " file: " : " files: ") + translated + " translated, "
                + failed + " failed" + (skipped > 0 ? ", " + skipped + " skipped (not a model)" : "")
                + "; output in " + config.outputDirectory());
        return failed == 0 && !inputsMissing ? EXIT_OK : EXIT_FAILED;
    }

    /**
     * Lists the files to compile, in command line order; the files of a
     * directory are sorted. Inputs that do not exist or cannot be read are
     * reported and set {@link #inputsMissing}.
     *
     * @return the jobs, empty if there is nothing to compile
     */
    private List<Job> collectJobs() {
        List<Job> jobs = new ArrayList<>();
        for (Path input : config.inputs()) {
            try {
                List<Path> sources = Job.discover(input, config.recursive());
                jobs.addAll(Job.prepare(input, sources, config.outputDirectory()));
            } catch (NoSuchFileException e) {
                report(input, "ERROR no such file or directory");
                inputsMissing = true;
            } catch (IOException e) {
                report(input, "ERROR cannot read the directory: " + e.getMessage());
                inputsMissing = true;
            }
        }
        return jobs;
    }

    private void report(Path file, String message) {
        err.println(file + ": " + message);
    }
}
