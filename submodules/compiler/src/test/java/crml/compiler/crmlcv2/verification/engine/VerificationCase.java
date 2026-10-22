package crml.compiler.crmlcv2.verification.engine;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import crml.compiler.util.CompilerRoot;

/**
 * One CRML model, with whatever the fixtures provide to check its simulation.
 *
 * <p>The fixtures live in three parallel trees under the test resources, keyed
 * by the suite path (e.g. {@code libraries/ETL_test}) and the model name:
 * <pre>
 * testModels/&lt;suite&gt;/&lt;Name&gt;.crml
 * verificationModels/&lt;suite&gt;/&lt;Name&gt;/   harness: &lt;Name&gt;_verif.mo extends &lt;Name&gt; and binds
 *                                         its externals from &lt;Name&gt;_externals.mo
 * refResults/&lt;suite&gt;/&lt;Name&gt;_verif_ref.mat   reference for the harness
 * refResults/&lt;suite&gt;/&lt;Name&gt;_ref.mat         reference for a model simulated on its
 *                                         own (by convention named *_no_ext)
 * </pre>
 * The references are Dymola simulations of hand-written reference models
 * (libraries/modelica/CRML_test) in the same harness, so they are independent
 * of any CRML compiler.
 */
public final class VerificationCase {

    /** How the model is simulated. */
    public enum Mode {
        /** The model has externals: simulate the harness that binds them. */
        HARNESS,
        /** The model binds everything itself: simulate it directly. */
        STANDALONE
    }

    private static final Path TEST_MODELS = CompilerRoot.RESOURCES.resolve("testModels");
    private static final Path VERIFICATION_MODELS = CompilerRoot.RESOURCES.resolve("verificationModels");
    private static final Path REFERENCES = CompilerRoot.RESOURCES.resolve("refResults");

    private final String suite;
    private final String name;
    private final Path source;
    private final Mode mode;
    private final Path harness;
    private final Path reference;

    private VerificationCase(String suite, String name, Path source, Mode mode, Path harness, Path reference) {
        this.suite = suite;
        this.name = name;
        this.source = source;
        this.mode = mode;
        this.harness = harness;
        this.reference = reference;
    }

    /** Every model of {@code suite}, e.g. {@code "libraries/ETL_test"}. */
    public static List<VerificationCase> discover(String suite) {
        List<VerificationCase> cases = new ArrayList<>();
        for (Path source : CompilerRoot.fileNameSourceHelper(TEST_MODELS.resolve(suite))) {
            cases.add(of(suite, source));
        }
        return cases;
    }

    static VerificationCase of(String suite, Path source) {
        String file = source.getFileName().toString();
        String name = file.substring(0, file.length() - ".crml".length());
        Path harness = VERIFICATION_MODELS.resolve(suite).resolve(name);
        if (Files.isDirectory(harness)) {
            return new VerificationCase(suite, name, source, Mode.HARNESS, harness,
                REFERENCES.resolve(suite).resolve(name + "_verif_ref.mat"));
        }
        return new VerificationCase(suite, name, source, Mode.STANDALONE, null,
            REFERENCES.resolve(suite).resolve(name + "_ref.mat"));
    }

    public String suite() {
        return suite;
    }

    /** The model name, which is also the name of the generated class. */
    public String name() {
        return name;
    }

    public Path source() {
        return source;
    }

    public Mode mode() {
        return mode;
    }

    /** The harness directory, or null for a standalone model. */
    public Path harness() {
        return harness;
    }

    /** The class to simulate, unqualified. */
    public String simulatedClass() {
        return mode == Mode.HARNESS ? name + "_verif" : name;
    }

    /** Where the reference result is expected; it may not exist. */
    public Path reference() {
        return reference;
    }

    public boolean hasReference() {
        return Files.isRegularFile(reference);
    }

    @Override
    public String toString() {
        return suite + "/" + name;
    }
}
