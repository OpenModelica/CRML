package org.openmodelica.crml.compiler.crmlcv2.verification.engine.evaluation;

import java.io.IOException;

import org.openmodelica.crml.compiler.crmlcv2.verification.engine.discovery.TestCase;
import org.openmodelica.crml.compiler.util.CrmlFrontEnd;

/** Runs the translation pipeline - parse, DOM, link, OMCv2 - on a case's model. */
public final class Translator {

    private Translator() {
    }

    public static Translation translate(TestCase testCase) throws IOException {
        return new Translation(CrmlFrontEnd.run(testCase.source()));
    }
}
