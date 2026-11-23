package org.openmodelica.crml.compiler.crmlcv2.verification;

import java.util.List;

import org.openmodelica.crml.compiler.crmlcv2.verification.engine.VerificationCase;

/** The FORML library's test models: those with externals against their references, the *_no_ext ones simulated. */
public class VerificationFORML extends VerificationTest {
    static List<VerificationCase> cases() {
        return VerificationCase.discover("libraries/FORML_test");
    }
}
