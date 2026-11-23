package org.openmodelica.crml.compiler.crmlcv2.verification;

import java.util.List;

import org.openmodelica.crml.compiler.crmlcv2.verification.engine.VerificationCase;

/**
 * The examples of the specification document: those with externals against
 * their references, the *_no_ext ones simulated. Most examples have no harness
 * or reference yet, and are reported as FIXTURE_INCOMPLETE (aborted).
 */
public class VerificationSpecDoc extends VerificationTest {
    static List<VerificationCase> cases() {
        return VerificationCase.discover("spec-doc-examples");
    }
}
