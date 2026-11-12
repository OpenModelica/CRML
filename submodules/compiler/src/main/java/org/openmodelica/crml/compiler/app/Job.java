package org.openmodelica.crml.compiler.app;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.emf.ecore.EObject;
import org.openmodelica.crml.compiler.crmlcv2.Diagnostic;
import org.openmodelica.crml.compiler.crmlcv2.OMCv2;
import org.openmodelica.crml.compiler.crmlcv2.TranslationResult;
import org.openmodelica.crml.language.dom.DOMVisitor;
import org.openmodelica.crml.language.dom.util.BuildResult;
import org.openmodelica.crml.language.pretty.PrettyPrint;
import org.openmodelica.crml.language.util.ErrorListener.CRMLSyntaxError;
import org.openmodelica.crml.language.util.Parser;
import org.openmodelica.crml.model.metamodel.Model;

/** A file to compile, the file its Modelica is written to, and the compilation itself. */
final class Job {

    static final String CRML_EXTENSION = ".crml";

    /** What became of one file. */
    enum Outcome {
        TRANSLATED,
        /** Not a model, for example a library: there is nothing to translate on its own. */
        SKIPPED,
        FAILED
    }

    private final Path source;
    private final Path target;

    Job(Path source, Path target) {
        this.source = source;
        this.target = target;
    }

    Path source() {
        return source;
    }

    Path target() {
        return target;
    }

    /**
     * Finds the CRML files of one input. A file is taken whatever its
     * extension; a directory contributes its {@code .crml} files, sorted.
     *
     * @param input a file or a directory
     * @param recursive whether to descend into subdirectories
     * @return the files, empty if a directory has no {@code .crml} file
     * @throws NoSuchFileException if the input is neither a file nor a directory
     * @throws IOException if a directory could not be read
     */
    static List<Path> discover(Path input, boolean recursive) throws IOException {
        if (Files.isRegularFile(input)) {
            return Collections.singletonList(input);
        }
        if (!Files.isDirectory(input)) {
            throw new NoSuchFileException(input.toString());
        }
        int depth = recursive ? Integer.MAX_VALUE : 1;
        try (Stream<Path> files = Files.walk(input, depth)) {
            return files
                    .filter(p -> Files.isRegularFile(p) && p.getFileName().toString().endsWith(CRML_EXTENSION))
                    .sorted()
                    .collect(Collectors.toList());
        }
    }

    /**
     * Pairs the files discovered for an input with their Modelica files in
     * the output directory. A file found in a directory keeps its path
     * relative to that directory; a file given directly keeps its name.
     *
     * @param input the input the sources were discovered from
     * @param sources the result of {@link #discover} for that input
     * @param outputDirectory where the Modelica files are written
     */
    static List<Job> prepare(Path input, List<Path> sources, Path outputDirectory) {
        List<Job> jobs = new ArrayList<>(sources.size());
        for (Path source : sources) {
            Path relative = source.equals(input) ? source.getFileName() : input.relativize(source);
            jobs.add(new Job(source, outputDirectory.resolve(moName(relative))));
        }
        return jobs;
    }

    /** {@code a/b/Name.crml} to {@code a/b/Name.mo}. */
    private static Path moName(Path crml) {
        String name = crml.getFileName().toString();
        if (name.endsWith(CRML_EXTENSION)) {
            name = name.substring(0, name.length() - CRML_EXTENSION.length());
        }
        return crml.resolveSibling(name + ".mo");
    }

    /**
     * Runs the whole pipeline on {@link #source} and writes the Modelica to
     * {@link #target}. Problems are printed on {@code err}, prefixed with the
     * source file; the --print-* sections go to {@code out}. A translation
     * with errors is still written, so it can be inspected, but fails.
     */
    Outcome compile(CRMLCompilerConfig config, PrintStream out, PrintStream err) {
        try {
            Parser.ParserResult parsed = new Parser().parse(source);
            if (config.printAst() && parsed.ast() != null) {
                section(out, "AST", parsed.toPrettyTree());
            }
            if (parsed.syntax().hasErrors()) {
                for (CRMLSyntaxError error : parsed.syntax().errors()) {
                    report(err, error.toString());
                }
                return Outcome.FAILED;
            }

            EObject root = buildObjectModel(parsed, config.printDom(), out);

            if (!(root instanceof Model)) {
                report(err, "WARNING skipped: "
                        + (root == null ? "no CRML model was built" : "a " + root.eClass().getName()
                                + " is not a model and cannot be translated on its own"));
                return Outcome.SKIPPED;
            }

            TranslationResult translation = new OMCv2().translateModel((Model) root);
            for (Diagnostic diagnostic : translation.diagnostics()) {
                if (config.verbose() || diagnostic.severity() == Diagnostic.Severity.ERROR) {
                    report(err, diagnostic.toString());
                }
            }

            write(translation.text(), config.within());
            if (translation.hasErrors()) {
                report(err, "ERROR translated with errors, written to " + target);
                return Outcome.FAILED;
            }
            if (config.verbose()) {
                out.println(source + " -> " + target);
            }
            return Outcome.TRANSLATED;
        } catch (Exception | StackOverflowError e) {
            report(err, "ERROR " + (e.getMessage() != null ? e.getMessage() : e.getClass().getName()));
            if (config.stacktrace()) {
                e.printStackTrace(err);
            }
            return Outcome.FAILED;
        }
    }

    /**
     * Builds the CRML object model, links it and resolves the operator calls.
     * With {@code --print-dom} the model is printed even when linking fails,
     * to show what could not be resolved.
     *
     * @return the root of the model, or null if nothing was built
     */
    private EObject buildObjectModel(Parser.ParserResult parsed, boolean printDom, PrintStream out) {
        DOMVisitor visitor = new DOMVisitor();
        EObject root = null;
        try {
            BuildResult built = visitor.build(parsed.ast());
            if (built instanceof BuildResult.SingleBuildResult) {
                root = ((BuildResult.SingleBuildResult<?>) built).result();
            }
            visitor.linker();
            visitor.resolveOperatorCalls();
        } finally {
            if (printDom) {
                section(out, "DOM", root != null ? PrettyPrint.prettyPrint(root) : "(no object model was built)");
            }
        }
        return root;
    }

    private void write(String modelica, String within) throws IOException {
        Path parent = target.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        String text = "within " + within + ";\n" + modelica + "\n";
        Files.write(target, text.getBytes(StandardCharsets.UTF_8));
    }

    /** Prints a labelled block on standard output, for the --print-* options. */
    private void section(PrintStream out, String label, String text) {
        out.println("==== " + label + ": " + source);
        out.println(text);
        out.println("==== end " + label + ": " + source);
    }

    private void report(PrintStream err, String message) {
        err.println(source + ": " + message);
    }
}
