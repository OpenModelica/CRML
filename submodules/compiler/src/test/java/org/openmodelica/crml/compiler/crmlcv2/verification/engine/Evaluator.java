package org.openmodelica.crml.compiler.crmlcv2.verification.engine;

import java.io.IOException;

import org.openmodelica.crml.compiler.crmlcv2.verification.engine.discovery.Level;
import org.openmodelica.crml.compiler.crmlcv2.verification.engine.discovery.TestCase;
import org.openmodelica.crml.compiler.crmlcv2.verification.engine.evaluation.Omc;
import org.openmodelica.crml.compiler.crmlcv2.verification.engine.evaluation.OmcRun;
import org.openmodelica.crml.compiler.crmlcv2.verification.engine.evaluation.Simulation;
import org.openmodelica.crml.compiler.crmlcv2.verification.engine.evaluation.Translation;
import org.openmodelica.crml.compiler.crmlcv2.verification.engine.evaluation.Translator;
import org.openmodelica.crml.compiler.crmlcv2.verification.engine.evaluation.Workspace;
import org.openmodelica.crml.compiler.crmlcv2.verification.engine.verification.Verifier;

/**
 * Takes a case through translate, check, simulate and verify, and reports each
 * stage to a {@link Monitor}. It decides nothing: whether a result is
 * acceptable, and whether to go on, is the monitor's call.
 */
public final class Evaluator {

    private Evaluator() {
    }

    /** Runs {@code testCase} as far as {@code monitor} and its fixtures allow. */
    public static void run(TestCase testCase, Monitor monitor) throws IOException, InterruptedException {
        Translation translation = Translator.translate(testCase);
        if (!monitor.translated(translation) || !translation.ok()) {
            return;
        }

        Workspace workspace = Workspace.create(testCase, translation);
        OmcRun check = Omc.check(workspace);
        if (!monitor.checked(check) || !check.ok() || !testCase.allows(Level.SIMULATE)) {
            return;
        }

        Simulation simulation = Omc.simulate(workspace);
        if (!monitor.simulated(simulation) || !simulation.ok() || !testCase.allows(Level.VERIFY)) {
            return;
        }

        monitor.verified(Verifier.verify(translation, simulation));
    }
}
