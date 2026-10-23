package crml.compiler.crmlcv2.verification;

import java.util.List;

import crml.compiler.crmlcv2.verification.engine.VerificationCase;

/** The ETL library's test models, simulated against their reference results. */
public class VerificationETL extends VerificationTest {
    static List<VerificationCase> cases() {
        return VerificationCase.discover("libraries/ETL_test");
    }
}
