package org.openmodelica.crml.compiler.crmlcv2.verification;

import java.util.List;

import org.openmodelica.crml.compiler.crmlcv2.verification.engine.discovery.CaseDiscovery;
import org.openmodelica.crml.compiler.crmlcv2.verification.engine.discovery.Level;
import org.openmodelica.crml.compiler.crmlcv2.verification.engine.discovery.TestCase;

/**
 * The examples of the specification document, each taken as far as its
 * fixtures allow: verified against its reference if it has one (and a harness,
 * if it needs one), simulated if not, otherwise checked on its own. Most
 * examples have no harness or reference yet.
 */
public class VerificationSpecDoc extends MonitorTest {

    /** As far as each case's fixtures allow. */
    @Override
    protected Level level() {
        return null;
    }

    static List<TestCase> cases() {
        return CaseDiscovery.discover("spec-doc-examples");
    }
}
