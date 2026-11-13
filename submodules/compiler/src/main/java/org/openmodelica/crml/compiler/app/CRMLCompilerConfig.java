package org.openmodelica.crml.compiler.app;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

/**
 * Command line options of the CRML compiler, read with JCommander.
 *
 * <p>Use {@link #parse(String...)} to build an instance; it validates the
 * options and throws {@link ParameterException} for wrong ones.
 */
public final class CRMLCompilerConfig {

    /** Name the program is called by in the usage text. */
    public static final String PROGRAM_NAME = "crmlc";

    @Parameter(description = "<.crml files or directories>")
    private List<Path> inputs = new ArrayList<>();

    @Parameter(names = { "-o", "--output" },
            description = "Directory the .mo files are written to. A file given directly is written as "
                    + "<output>/<name>.mo; the files found in a directory keep their path relative to it.")
    private Path outputDirectory = Paths.get("generated");

    @Parameter(names = "--within",
            description = "Package the generated classes are placed in, written as the 'within' clause. "
                    + "Empty means the top level.")
    private String within = "";

    @Parameter(names = { "-r", "--recursive" },
            description = "Also look for .crml files in the subdirectories of a directory.")
    private boolean recursive = false;

    @Parameter(names = { "-v", "--verbose" },
            description = "Also print the warnings and notes of the translation, and every file written.")
    private boolean verbose = false;

    @Parameter(names = "--print-ast",
            description = "Print the parse tree of each file on standard output, also for a file with syntax errors.")
    private boolean printAst = false;

    @Parameter(names = "--print-dom",
            description = "Print the CRML object model of each file on standard output, after linking and operator "
                    + "resolution; if these fail, the model as far as it was built.")
    private boolean printDom = false;

    @Parameter(names = "--stacktrace",
            description = "Print the stack trace of an exception raised while compiling a file.")
    private boolean stacktrace = false;

    @Parameter(names = { "-h", "--help" }, help = true, description = "Print this help and exit.")
    private boolean help = false;

    private CRMLCompilerConfig() {
    }

    /**
     * Reads the command line arguments.
     *
     * @param args the arguments of {@code main}
     * @return the options; when {@link #help()} is set the others are not validated
     * @throws ParameterException if an option is unknown or malformed, or no input is given
     */
    public static CRMLCompilerConfig parse(String... args) {
        CRMLCompilerConfig config = new CRMLCompilerConfig();
        JCommander.newBuilder()
                .programName(PROGRAM_NAME)
                .addObject(config)
                .build()
                .parse(args);
        // JCommander passes an unknown option to the main parameter as an input.
        for (Path input : config.inputs) {
            if (input.toString().startsWith("-") && !Files.exists(input)) {
                throw new ParameterException("Unknown option: " + input);
            }
        }
        if (!config.help && config.inputs.isEmpty()) {
            throw new ParameterException("No input given: name at least one .crml file or directory.");
        }
        return config;
    }

    /** Text describing every option, as printed by {@code --help}. */
    public static String usage() {
        CRMLCompilerConfig config = new CRMLCompilerConfig();
        JCommander commander = JCommander.newBuilder()
                .programName(PROGRAM_NAME)
                .addObject(config)
                .build();
        StringBuilder out = new StringBuilder();
        commander.getUsageFormatter().usage(out);
        return out.toString();
    }

    /** The files and directories named on the command line, in order. */
    public List<Path> inputs() {
        return inputs;
    }

    public Path outputDirectory() {
        return outputDirectory;
    }

    /** The package of the {@code within} clause; empty for the top level. */
    public String within() {
        return within.trim();
    }

    public boolean recursive() {
        return recursive;
    }

    public boolean verbose() {
        return verbose;
    }

    public boolean printAst() {
        return printAst;
    }

    public boolean printDom() {
        return printDom;
    }

    public boolean stacktrace() {
        return stacktrace;
    }

    public boolean help() {
        return help;
    }
}
