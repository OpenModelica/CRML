package org.openmodelica.crml.compiler.crmlcv2.verification;

import java.util.List;

import org.openmodelica.crml.compiler.crmlcv2.verification.engine.discovery.CaseDiscovery;
import org.openmodelica.crml.compiler.crmlcv2.verification.engine.discovery.Level;
import org.openmodelica.crml.compiler.crmlcv2.verification.engine.discovery.TestCase;

/** The ETL library's test models: each verified against its reference. A model without its fixtures is skipped. */
public class VerificationETL extends MonitorTest {

    @Override
    protected Level level() {
        return Level.VERIFY;
    }

    static List<TestCase> cases() {
        return CaseDiscovery.discover("libraries/ETL_test");
    }
}
