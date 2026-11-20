package org.openmodelica.crml.compiler.crmlcv2.verification;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.openmodelica.crml.compiler.crmlcv2.verification.engine.OmcRunner;
import org.openmodelica.crml.compiler.crmlcv2.verification.engine.Verification;
import org.openmodelica.crml.compiler.crmlcv2.verification.engine.VerificationCase;
import org.openmodelica.crml.compiler.omc.OMCUtil;

/**
 * Lifting ({@code x.element}) end to end: the generated Modelica must pass
 * {@code checkModel} and simulate.
 *
 * <p>The cases are the models in {@code testModels/lifting}, which lift over
 * set variables and so work with the current {@code CRMLtoModelica.mo}:
 * {@code LiftOneAxis_no_ext} (a user function, a user block and a built-in
 * operator over one set, each aggregated) and {@code LiftTwoAxes_no_ext} (two
 * axes, and chaining).
 *
 * <p>{@code LiftClockBlock_no_ext} is a known failure: it lifts a user block
 * that builds a clock, and OpenModelica 1.26.7 does not simulate an array of
 * instances of such a block (the system comes out under-determined, although
 * {@code checkModel} passes). The model's own comment has the details.
 *
 * <p>Lifting over {@code Periods} needs a library in which {@code CRMLPeriods}
 * has the buffer {@code period[]} and its fill level {@code counter}
 * (see {@link org.openmodelica.crml.compiler.crmlcv2.SetHandle#periods}). The bundled library has
 * neither, so {@link #checkOverNoExt} is skipped until one does.
 *
 * <p>Not named {@code Verification*}, so {@code make test-verification} does not
 * run it. Run it with
 * {@code ./gradlew :compiler:verificationTest --tests "*LiftCheckModelTest"}.
 */
public class LiftCheckModelTest extends VerificationTest {

    private static final long TIMEOUT_SECONDS = 300;

    static List<VerificationCase> cases() {
        return VerificationCase.discover("lifting");
    }

    /**
     * {@code ETL_test/CheckOver_no_ext.crml}, the model lifting was specified
     * for. Skipped until the library provides the {@code Periods} contract.
     *
     * <p>Once it runs, it checks that the model simulates. Comparing it with
     * {@code CheckOver_no_ext_ref.mat} is not done here: the verification
     * engine only simulates a {@code *_no_ext} model, it does not compare it.
     */
    @Test
    public void checkOverNoExt() throws IOException, InterruptedException {
        Assumptions.assumeTrue(OmcRunner.available(), "omc not found");
        Assumptions.assumeTrue(libraryHasPeriodsBuffer(),
            "CRMLtoModelica.mo: CRMLPeriods has no period[] and counter, so code lifted over Periods "
            + "cannot pass checkModel");

        VerificationCase checkOver = null;
        for (VerificationCase candidate : VerificationCase.discover("libraries/ETL_test")) {
            if (candidate.name().equals("CheckOver_no_ext")) {
                checkOver = candidate;
            }
        }
        assertTrue(checkOver != null, "CheckOver_no_ext.crml not found");
        Verification verification = Verification.run(checkOver, TIMEOUT_SECONDS);
        assertTrue(verification.passed(), verification.outcome() + ": " + verification.reason());
    }

    /** True if the record CRMLPeriods of the bundled library declares period[] and counter. */
    private static boolean libraryHasPeriodsBuffer() throws IOException {
        String library = new String(Files.readAllBytes(OMCUtil.getCRMLToModelicaFile()), StandardCharsets.UTF_8);
        int start = library.indexOf("record CRMLPeriods");
        int end = start < 0 ? -1 : library.indexOf("end CRMLPeriods;", start);
        if (end < 0) {
            return false;
        }
        String record = library.substring(start, end);
        return record.contains("period[") && record.contains("counter");
    }
}
