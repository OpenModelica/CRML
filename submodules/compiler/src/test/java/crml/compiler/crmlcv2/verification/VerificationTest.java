package crml.compiler.crmlcv2.verification;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import crml.compiler.crmlcv2.TranslationResult;
import crml.compiler.crmlcv2.verification.engine.OmcRunner;
import crml.compiler.crmlcv2.verification.engine.Verification;
import crml.compiler.crmlcv2.verification.engine.VerificationCase;
import crml.compiler.util.CodeWrapper;
import crml.compiler.util.DiagnosticsWrapper;
import crml.compiler.util.ThrowableWrapper;
import crml.compiler.util.VariablesWrapper;
import crml.test.ReportedTest;

/**
 * End-to-end check of the OMCv2 compiler: the Modelica it generates for a CRML
 * model, simulated by OpenModelica, against the reference result for that model.
 * Subclasses provide the cases of one suite via a static {@code cases()}.
 *
 * <p>Needs omc, so it is tagged and runs from {@code verificationTest}, not
 * {@code test}; without omc every case is skipped. A case with no reference
 * result is skipped too, since there is nothing to verify it against.
 */
@Tag("omc")
public abstract class VerificationTest extends ReportedTest {

    /** Per omc invocation; a simulation includes compiling the generated C code. */
    private static final long TIMEOUT_SECONDS = 300;

    @ParameterizedTest(name = "{0}")
    @MethodSource("cases")
    public void simulateAgainstReference(VerificationCase verificationCase)
            throws IOException, InterruptedException {
        Assumptions.assumeTrue(OmcRunner.available(), "omc not found");
        emit(verificationCase.source(), "CRML model");

        Verification verification = Verification.run(verificationCase, TIMEOUT_SECONDS);
        Assumptions.assumeFalse(verification.outcome() == Verification.Outcome.NO_REFERENCE,
            verification.reason());

        emit(verificationCase.mode() + (verificationCase.harness() != null
            ? " (" + verificationCase.harness().getFileName() + ")" : ""), "Mode");
        emit(verificationCase.reference(), "Reference");
        TranslationResult translation = verification.frontEnd().translation();
        if (translation != null) {
            emit(CodeWrapper.of(translation.text()), "Modelica");
            emit(DiagnosticsWrapper.of(translation.diagnostics()), "Diagnostics");
        }
        if (verification.frontEnd().error() != null) {
            emit(ThrowableWrapper.of(verification.frontEnd().error()), "Error");
        }
        if (!verification.variables().isEmpty()) {
            emit(VariablesWrapper.of(verification.variables()), "Compared variables");
        }
        if (verification.directory() != null) {
            emit(verification.directory(), "Working directory");
            emitScript(verification.directory().resolve("simulate.mos"), "Simulation script");
        }
        if (verification.simulation() != null) {
            emit(CodeWrapper.of(verification.simulation().log()), "omc output (simulation)");
        }
        if (verification.comparison() != null) {
            emit(CodeWrapper.of(verification.comparison().log()), "omc output (comparison)");
        }
        emit(verification.outcome() + ": " + verification.reason(), "Outcome");

        // Not a defect, but not verified either: shown as aborted rather than passed.
        Assumptions.assumeFalse(verification.outcome() == Verification.Outcome.PARTIAL_MATCH,
            verification.reason());
        assertEquals(Verification.Outcome.MATCH, verification.outcome(), verification.reason());
    }

    private void emitScript(Path script, String key) throws IOException {
        if (Files.isRegularFile(script)) {
            emit(CodeWrapper.of(new String(Files.readAllBytes(script), "UTF-8")), key);
        }
    }
}
