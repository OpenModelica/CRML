package crml.compiler.crmlcv2.verification.engine;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import crml.compiler.util.CompilerRoot;

/**
 * One CRML model, and how it is verified.
 *
 * <p>The fixtures live in three parallel trees under the test resources, keyed
 * by the suite path (e.g. {@code libraries/ETL_test}) and the model name:
 * <pre>
 * testModels/&lt;suite&gt;/&lt;Name&gt;.crml
 * verificationModels/&lt;suite&gt;/&lt;Name&gt;/   harness: &lt;Name&gt;_verif.mo extends &lt;Name&gt; and binds
 *                                         its externals from &lt;Name&gt;_externals.mo
 * refResults/&lt;suite&gt;/&lt;Name&gt;_verif_ref.mat   reference for the harness
 * </pre>
 * The references are Dymola simulations of hand-written reference models
 * (libraries/modelica/CRML_test) in the same harness, so they are independent
 * of any CRML compiler.
 *
 * <p>A model named {@code *_no_ext} binds all its variables itself and has no
 * external reference: it is only simulated. Any {@code *_no_ext_ref.mat} next
 * to it is not used. Every other model is verified against its reference.
 */
public final class VerificationCase {

    /** How a model is verified. */
    public enum Mode {
        /** Simulate the harness that binds the externals, and compare the outputs with the reference. */
        WITH_REFERENCE("with reference"),
        /** Simulate the model on its own; producing correct results is not required. */
        WITHOUT_REFERENCE("without reference");

        private final String label;

        Mode(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private static final String NO_EXTERNALS = "_no_ext";

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

    /**
     * Every model of {@code suite}, e.g. {@code "libraries/ETL_test"}: those
     * verified with a reference first, then those only simulated.
     */
    public static List<VerificationCase> discover(String suite) {
        List<VerificationCase> withReference = new ArrayList<>();
        List<VerificationCase> withoutReference = new ArrayList<>();
        for (Path source : CompilerRoot.fileNameSourceHelper(TEST_MODELS.resolve(suite))) {
            VerificationCase verificationCase = of(suite, source);
            (verificationCase.mode == Mode.WITH_REFERENCE ? withReference : withoutReference).add(verificationCase);
        }
        withReference.addAll(withoutReference);
        return withReference;
    }

    static VerificationCase of(String suite, Path source) {
        String file = source.getFileName().toString();
        String name = file.substring(0, file.length() - ".crml".length());
        if (name.endsWith(NO_EXTERNALS)) {
            return new VerificationCase(suite, name, source, Mode.WITHOUT_REFERENCE, null, null);
        }
        return new VerificationCase(suite, name, source, Mode.WITH_REFERENCE,
            VERIFICATION_MODELS.resolve(suite).resolve(name),
            REFERENCES.resolve(suite).resolve(name + "_verif_ref.mat"));
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

    /** Where the harness is expected, or null for a model verified without a reference. */
    public Path harness() {
        return harness;
    }

    /** The class to simulate, unqualified. */
    public String simulatedClass() {
        return mode == Mode.WITH_REFERENCE ? name + "_verif" : name;
    }

    /** Where the reference result is expected, or null for a model verified without one. */
    public Path reference() {
        return reference;
    }

    /**
     * What the fixtures lack for this case to be verified, or null if they are
     * complete. A model with externals cannot be simulated without the harness
     * that binds them, and there is nothing to verify it against without the
     * reference.
     */
    public String fixtureProblem() {
        if (mode == Mode.WITHOUT_REFERENCE) {
            return null;
        }
        if (!Files.isDirectory(harness)) {
            return "no harness at " + harness + " (a model without one must be named *" + NO_EXTERNALS + ")";
        }
        if (!Files.isRegularFile(reference)) {
            return "no reference result at " + reference;
        }
        return null;
    }

    @Override
    public String toString() {
        return suite + "/" + name + ".crml";
    }
}
