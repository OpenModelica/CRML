package org.openmodelica.crml.language.util;

import static j2html.TagCreator.code;
import static j2html.TagCreator.each;
import static j2html.TagCreator.div;
import static j2html.TagCreator.p;
import static j2html.TagCreator.pre;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.resource.Resource;
import org.openmodelica.crml.language.workspace.CrmlDiagnostic;
import org.openmodelica.crml.testutil.CustomHtmlReporter;

/**
 * The diagnostics of a {@link org.openmodelica.crml.language.workspace.CrmlResource}
 * in a report: one line each, followed by the stack trace of the exception
 * behind it, if any.
 */
public class DiagnosticsWrapper implements CustomHtmlReporter {
    private final List<Resource.Diagnostic> diagnostics;

    public DiagnosticsWrapper(List<? extends Resource.Diagnostic> diagnostics) {
        this.diagnostics = new ArrayList<>(diagnostics);
    }

    public static DiagnosticsWrapper of(List<? extends Resource.Diagnostic> diagnostics) {
        return new DiagnosticsWrapper(diagnostics);
    }

    @Override
    public Object report() {
        return div(each(diagnostics, d -> {
            Throwable cause = d instanceof CrmlDiagnostic ? ((CrmlDiagnostic) d).getCause() : null;
            if (cause == null) {
                return p(d.toString());
            }
            StringWriter trace = new StringWriter();
            cause.printStackTrace(new PrintWriter(trace));
            return div(p(d.toString()), pre(code(trace.toString())));
        }));
    }

    @Override
    public boolean collapsible() {
        return true;
    }

    /** The first diagnostic, so what went wrong is visible without expanding. */
    @Override
    public String summary() {
        if (diagnostics.isEmpty()) {
            return "no diagnostics";
        }
        String first = diagnostics.get(0).toString();
        return diagnostics.size() == 1 ? first : first + " (+" + (diagnostics.size() - 1) + " more)";
    }
}
