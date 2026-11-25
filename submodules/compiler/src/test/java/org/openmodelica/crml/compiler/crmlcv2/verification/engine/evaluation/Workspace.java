package org.openmodelica.crml.compiler.crmlcv2.verification.engine.evaluation;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.openmodelica.crml.compiler.crmlcv2.verification.engine.discovery.TestCase;
import org.openmodelica.crml.compiler.omc.OMCUtil;
import org.openmodelica.crml.compiler.util.CompilerRoot;

/**
 * The working directory of one case, {@code <out>/verification/<suite>/<Name>/},
 * with everything omc reads copied into it: the fixtures may be inside the
 * test-resources jar, which omc cannot see.
 * <pre>
 * src/&lt;Name&gt;/package.mo, &lt;Name&gt;.mo   the generated model
 * src/&lt;Name&gt;/*.mo                    the harness, if there is one
 * CRML/package.mo, ...               the harness's library, if there is a harness
 * *_ref.mat                          the reference, if there is one
 * *.mos, *.log                       the omc scripts and what omc printed
 * </pre>
 * Every {@code .mo} in the package gets a {@code within} clause naming it. The
 * package is not directly in the working directory: omc names the simulation
 * executable after the class, and it would collide with the package.
 */
public final class Workspace {

    private static final Path OUT = CompilerRoot.OUT.resolve("verification");
    private static final Pattern WITHIN = Pattern.compile("^\\s*within\\b[^;]*;", Pattern.MULTILINE);

    final TestCase testCase;
    final Path directory;
    final Path packageFile;
    /** The package.mo of the copied library of the harness, or null without a harness. */
    final Path crmlLibrary;
    /** The copied reference, or null without one. */
    final Path reference;

    private Workspace(TestCase testCase, Path directory, Path packageFile, Path crmlLibrary, Path reference) {
        this.testCase = testCase;
        this.directory = directory;
        this.packageFile = packageFile;
        this.crmlLibrary = crmlLibrary;
        this.reference = reference;
    }

    /** A fresh working directory with the generated package and whatever fixtures the case has. */
    public static Workspace create(TestCase testCase, Translation translation) throws IOException {
        String name = testCase.name();
        Path directory = OUT.resolve(testCase.suite()).resolve(name).toAbsolutePath();
        deleteRecursively(directory);
        Path packageDirectory = directory.resolve("src").resolve(name);
        Files.createDirectories(packageDirectory);

        Path packageFile = packageDirectory.resolve("package.mo");
        write(packageFile, "within ;\npackage " + name + "\nend " + name + ";\n");
        write(packageDirectory.resolve(name + ".mo"), within(translation.modelica(), name));

        Path crmlLibrary = null;
        if (testCase.hasHarness()) {
            crmlLibrary = OMCUtil.exportCRMLLibrary(directory);
            try (DirectoryStream<Path> files = Files.newDirectoryStream(testCase.harness(), "*.mo")) {
                for (Path file : files) {
                    String text = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
                    write(packageDirectory.resolve(file.getFileName().toString()), within(text, name));
                }
            }
        }
        Path reference = null;
        if (testCase.hasReference()) {
            reference = directory.resolve(testCase.reference().getFileName().toString());
            Files.copy(testCase.reference(), reference);
        }
        return new Workspace(testCase, directory, packageFile, crmlLibrary, reference);
    }

    public Path directory() {
        return directory;
    }

    /** The simulated class, qualified: {@code <Name>.<Name>_verif} with a harness, else {@code <Name>.<Name>}. */
    String simulatedClass() {
        return testCase.name() + "." + testCase.simulatedClass();
    }

    private static String within(String modelica, String packageName) {
        Matcher matcher = WITHIN.matcher(modelica);
        String clause = "within " + packageName + ";";
        return matcher.find() ? matcher.replaceFirst(clause) : clause + "\n" + modelica;
    }

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
