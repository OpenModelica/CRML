package org.openmodelica.crml.compiler.crmlcv2.verification.engine.evaluation;

import java.util.List;

import org.openmodelica.crml.compiler.crmlcv2.Diagnostic;
import org.openmodelica.crml.compiler.util.CrmlFrontEnd;
import org.openmodelica.crml.model.metamodel.Model;
import org.openmodelica.crml.modelica.trace.metamodel.TraceModel;

/**
 * What the translation pipeline made of a model. {@link #ok()} if it produced
 * Modelica; elements it could not translate are in {@link #dropped()}.
 */
public final class Translation {

    private final CrmlFrontEnd frontEnd;

    Translation(CrmlFrontEnd frontEnd) {
        this.frontEnd = frontEnd;
    }

    /** Everything the front end reported: syntax errors, diagnostics, what it threw. */
    public CrmlFrontEnd frontEnd() {
        return frontEnd;
    }

    public boolean ok() {
        return frontEnd.translation() != null;
    }

    /** The generated Modelica, or null if there is none. */
    public String modelica() {
        return ok() ? frontEnd.translation().text() : null;
    }

    public List<Diagnostic> diagnostics() {
        return frontEnd.translation().diagnostics();
    }

    public Model model() {
        return frontEnd.model();
    }

    public TraceModel trace() {
        return frontEnd.translation().trace();
    }

    /** The ERROR diagnostics: elements the translation dropped. Empty if it is complete. */
    public List<Diagnostic> dropped() {
        return frontEnd.errors();
    }

    /** Why no Modelica was produced, or null if it was. */
    public String reason() {
        if (ok()) {
            return null;
        }
        if (frontEnd.hasSyntaxErrors()) {
            return "syntax errors: " + frontEnd.syntaxErrors();
        }
        if (!frontEnd.diagnostics().isEmpty()) {
            return "errors: " + frontEnd.diagnosticText();
        }
        if (frontEnd.translationError() != null) {
            return "threw " + frontEnd.translationError();
        }
        if (frontEnd.model() == null) {
            return "the definition is not a model";
        }
        return "ERROR diagnostics: " + frontEnd.errors();
    }
}
