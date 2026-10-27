package crml.compiler.crmlcv2.verification;

import static org.junit.jupiter.api.Assertions.assertTrue;

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
import crml.compiler.util.OutputsWrapper;
import crml.compiler.util.ThrowableWrapper;
import crml.test.ReportedTest;

/**
 * End-to-end check of the OMCv2 compiler: the Modelica it generates for a CRML
 * model, simulated by OpenModelica. Subclasses provide the cases of one suite
 * via a static {@code cases()}.
 *
 * <p>A case {@link VerificationCase.Mode#WITH_REFERENCE with a reference}
 * passes when every output matches the reference; one
 * {@link VerificationCase.Mode#WITHOUT_REFERENCE without} passes when it
 * simulates. A case that could not be verified - incomplete fixtures, or outputs
 * the reference does not record - is reported as aborted, neither passed nor
 * failed. Cases come grouped by mode, and each one's report entry states its mode.
 *
 * <p>Needs omc, so it is tagged and runs from {@code verificationTest}, not
 * {@code test}; without omc every case is skipped.
 */
@Tag("omc")
public abstract class VerificationTest extends ReportedTest {

    /** Per omc invocation; a simulation includes compiling the generated C code. */
    private static final long TIMEOUT_SECONDS = 300;

    @ParameterizedTest
    @MethodSource("cases")
    public void verify(VerificationCase verificationCase) throws IOException, InterruptedException {
        Assumptions.assumeTrue(OmcRunner.available(), "omc not found");
        emit(verificationCase.source(), "CRML model");

        Verification verification = Verification.run(verificationCase, TIMEOUT_SECONDS);

        emit(String.valueOf(verificationCase.mode()), "Mode");
        if (verificationCase.reference() != null) {
            emit(verificationCase.reference(), "Reference");
        }
        TranslationResult translation = verification.frontEnd().translation();
        if (translation != null) {
            emit(CodeWrapper.of(translation.text()), "Modelica");
            emit(DiagnosticsWrapper.of(translation.diagnostics()), "Diagnostics");
        }
        if (verification.frontEnd().error() != null) {
            emit(ThrowableWrapper.of(verification.frontEnd().error()), "Error");
        }
        if (!verification.outputs().isEmpty()) {
            emit(OutputsWrapper.of(verification.outputs()), "Outputs");
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

        Assumptions.assumeFalse(verification.inconclusive(), verification.outcome() + ": " + verification.reason());
        assertTrue(verification.passed(), verification.outcome() + ": " + verification.reason());
    }

    private void emitScript(Path script, String key) throws IOException {
        if (Files.isRegularFile(script)) {
            emit(CodeWrapper.of(new String(Files.readAllBytes(script), "UTF-8")), key);
        }
    }
}
