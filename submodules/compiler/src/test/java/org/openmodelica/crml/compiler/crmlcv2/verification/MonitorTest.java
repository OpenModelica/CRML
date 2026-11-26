package org.openmodelica.crml.compiler.crmlcv2.verification;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openmodelica.crml.compiler.crmlcv2.verification.engine.Evaluator;
import org.openmodelica.crml.compiler.crmlcv2.verification.engine.Monitor;
import org.openmodelica.crml.compiler.crmlcv2.verification.engine.discovery.Level;
import org.openmodelica.crml.compiler.crmlcv2.verification.engine.discovery.TestCase;
import org.openmodelica.crml.compiler.crmlcv2.verification.engine.evaluation.Omc;
import org.openmodelica.crml.compiler.crmlcv2.verification.engine.evaluation.OmcRun;
import org.openmodelica.crml.compiler.crmlcv2.verification.engine.evaluation.Simulation;
import org.openmodelica.crml.compiler.crmlcv2.verification.engine.evaluation.Translation;
import org.openmodelica.crml.compiler.crmlcv2.verification.engine.verification.OutputResult;
import org.openmodelica.crml.compiler.crmlcv2.verification.engine.verification.VerificationResult;
import org.openmodelica.crml.compiler.util.CodeWrapper;
import org.openmodelica.crml.compiler.util.DiagnosticsWrapper;
import org.openmodelica.crml.compiler.util.OutputsWrapper;
import org.openmodelica.crml.compiler.util.ResourceDiagnosticsWrapper;
import org.openmodelica.crml.compiler.util.ThrowableWrapper;
import org.openmodelica.crml.testutil.ReportedTest;

/**
 * End-to-end check of the OMCv2 compiler: each CRML model of a suite is
 * translated, checked and simulated by OpenModelica, and its outputs verified
 * against a reference, as far as the suite requires and the fixtures allow.
 *
 * <p>This class is the {@link Monitor} of every case: the engine reports each
 * stage to it, and each callback emits what the stage produced, asserts on it,
 * and answers whether to go on. The rules:
 * <ul>
 * <li>an error at any stage fails the case, and ends it there;</li>
 * <li>a translation that dropped elements fails the case, at the end, so the
 * later stages still run and show what they show;</li>
 * <li>an output that differs from the reference, or that the result lacks,
 * fails the case, and so does a verification that compared nothing; outputs the
 * reference does not record are only listed;</li>
 * <li>a case whose fixtures cannot reach the required level is skipped, unless
 * it failed on the way.</li>
 * </ul>
 * A suite that wants other rules overrides a callback. JUnit makes a new
 * instance for every case, so the state of one case is in fields.
 *
 * <p>The required level is the suite's {@link #level()}. To run a suite to
 * another level, change it there.
 *
 * <p>Needs omc, so it is tagged and runs from {@code verificationTest}, not
 * {@code test}; without omc every case that goes beyond translation is skipped.
 */
@Tag("omc")
public abstract class MonitorTest extends ReportedTest implements Monitor {

    private Level target;
    /** Found, but not a reason to stop. */
    private final List<String> defects = new ArrayList<>();

    /** The level every case must reach, or null for as far as its fixtures allow. */
    protected abstract Level level();

    @ParameterizedTest
    @MethodSource("cases")
    public void verify(TestCase testCase) throws IOException, InterruptedException {
        Level required = level() == null ? testCase.maxLevel() : level();
        target = Level.min(required, testCase.maxLevel());
        emit(testCase.source(), "CRML model");
        emit(testCase.describeFixtures(), "Fixtures");
        emit("required " + required + "; fixtures allow " + testCase.maxLevel()
            + (target == required ? "" : "; runs to " + target), "Levels");
        if (target.isAtLeast(Level.CHECK)) {
            Assumptions.assumeTrue(Omc.available(), "omc not found");
        }

        Evaluator.run(testCase, this);

        assertTrue(defects.isEmpty(), String.join("; ", defects));
        Assumptions.assumeTrue(testCase.allows(required), testCase.missingFor(required));
    }

    @Override
    public boolean translated(Translation translation) {
        if (translation.ok()) {
            emit(CodeWrapper.of(translation.modelica()), "Modelica");
            emit(DiagnosticsWrapper.of(translation.diagnostics()), "Diagnostics");
        }
        if (!translation.frontEnd().diagnostics().isEmpty()) {
            emit(ResourceDiagnosticsWrapper.of(translation.frontEnd().diagnostics()), "Errors");
        }
        if (translation.frontEnd().translationError() != null) {
            emit(ThrowableWrapper.of(translation.frontEnd().translationError()), "Error");
        }
        assertTrue(translation.ok(), "translation failed: " + translation.reason());
        if (!translation.dropped().isEmpty()) {
            defects.add("translation dropped elements: " + translation.dropped());
        }
        return target.isAtLeast(Level.CHECK);
    }

    @Override
    public boolean checked(OmcRun check) {
        emit(check.directory(), "Working directory");
        emitRun(check);
        assertTrue(check.ok(), check.reason());
        return target.isAtLeast(Level.SIMULATE);
    }

    @Override
    public boolean simulated(Simulation simulation) {
        for (OmcRun run : simulation.runs()) {
            emitRun(run);
        }
        assertTrue(simulation.ok(), simulation.reason());
        return target.isAtLeast(Level.VERIFY);
    }

    @Override
    public void verified(VerificationResult result) {
        for (OmcRun run : result.runs()) {
            emitRun(run);
        }
        if (!result.outputs().isEmpty()) {
            emit(OutputsWrapper.of(result.outputs()), "Outputs");
            write(result);
        }
        assertTrue(result.ok(), result.reason());

        List<String> differ = names(result, OutputResult.Status.DIFFERS);
        List<String> missing = names(result, OutputResult.Status.MISSING);
        missing.addAll(names(result, OutputResult.Status.DROPPED));
        assertTrue(differ.isEmpty() && missing.isEmpty(), "outputs differ from the reference: " + differ
            + "; missing: " + missing);
        assertTrue(result.count(OutputResult.Status.MATCH) > 0, "no output could be compared with the reference ("
            + result.count(OutputResult.Status.NOT_IN_REFERENCE) + " not in it)");
    }

    // --- helpers -----------------------------------------------------------

    private void emitRun(OmcRun run) {
        emit(CodeWrapper.of(run.script()), "Script " + run.name());
        emit(CodeWrapper.of(run.log()), "omc output (" + run.name() + ")");
    }

    private static List<String> names(VerificationResult result, OutputResult.Status status) {
        List<String> names = new ArrayList<>();
        for (OutputResult output : result.outputs()) {
            if (output.status() == status) {
                names.add(output.name());
            }
        }
        return names;
    }

    /** {@code summary.txt} in the working directory: one line per output. */
    private static void write(VerificationResult result) {
        try {
            Files.write(result.directory().resolve("summary.txt"), result.summary().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new java.io.UncheckedIOException(e);
        }
    }
}
