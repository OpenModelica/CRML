package org.openmodelica.crml.compiler.crmlcv2.verification.engine;

import org.openmodelica.crml.compiler.crmlcv2.verification.engine.evaluation.OmcRun;
import org.openmodelica.crml.compiler.crmlcv2.verification.engine.evaluation.Simulation;
import org.openmodelica.crml.compiler.crmlcv2.verification.engine.evaluation.Translation;
import org.openmodelica.crml.compiler.crmlcv2.verification.engine.verification.VerificationResult;

/**
 * Told what came of each stage of {@link Evaluator#run}, whether it succeeded or
 * not; decides what that means, and whether the run goes on.
 *
 * <p>Each callback returns true to continue. The run also stops after a stage
 * that failed, and where the fixtures end, since the next stage would have
 * nothing to work with. A callback may throw - a JUnit assertion, say - which
 * ends the run and propagates out of {@link Evaluator#run}.
 */
public interface Monitor {

    boolean translated(Translation translation);

    boolean checked(OmcRun check);

    boolean simulated(Simulation simulation);

    void verified(VerificationResult result);
}
