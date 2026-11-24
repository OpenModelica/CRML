package org.openmodelica.crml.compiler.crmlcv2.verification.engine.discovery;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.openmodelica.crml.compiler.util.CompilerRoot;

/**
 * Stage I: finds a suite's models and pairs each with its fixtures. Only looks
 * at the file system. The fixtures live in three parallel trees under the test
 * resources, keyed by the suite path (e.g. {@code libraries/ETL_test}):
 * <pre>
 * testModels/&lt;suite&gt;/&lt;Name&gt;.crml
 * verificationModels/&lt;suite&gt;/&lt;Name&gt;/        harness: &lt;Name&gt;_verif.mo extends &lt;Name&gt; and binds its externals
 * refResults/&lt;suite&gt;/&lt;Name&gt;_verif_ref.mat    reference for the harness
 * refResults/&lt;suite&gt;/&lt;Name&gt;_no_ext_ref.mat   reference for a *_no_ext model
 * </pre>
 * The references are Dymola simulations of hand-written reference models
 * (libraries/modelica/CRML_test), so they are independent of any CRML compiler.
 */
public final class CaseDiscovery {

    private static final Path TEST_MODELS = CompilerRoot.RESOURCES.resolve("testModels");
    private static final Path VERIFICATION_MODELS = CompilerRoot.RESOURCES.resolve("verificationModels");
    private static final Path REFERENCES = CompilerRoot.RESOURCES.resolve("refResults");

    private CaseDiscovery() {
    }

    /** Every model of {@code suite}: those with externals first, then the {@code *_no_ext} ones. */
    public static List<TestCase> discover(String suite) {
        List<TestCase> withExternals = new ArrayList<>();
        List<TestCase> noExternals = new ArrayList<>();
        for (Path source : CompilerRoot.fileNameSourceHelper(TEST_MODELS.resolve(suite))) {
            TestCase testCase = of(suite, source);
            (testCase.needsHarness() ? withExternals : noExternals).add(testCase);
        }
        withExternals.addAll(noExternals);
        return withExternals;
    }

    static TestCase of(String suite, Path source) {
        String file = source.getFileName().toString();
        String name = file.substring(0, file.length() - ".crml".length());
        Path references = REFERENCES.resolve(suite);
        if (name.endsWith(TestCase.NO_EXTERNALS)) {
            return new TestCase(suite, name, source, null, references.resolve(name + "_ref.mat"));
        }
        return new TestCase(suite, name, source, VERIFICATION_MODELS.resolve(suite).resolve(name),
            references.resolve(name + "_verif_ref.mat"));
    }
}
