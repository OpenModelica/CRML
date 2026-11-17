package org.openmodelica.crml.language.util;

import static j2html.TagCreator.code;
import static j2html.TagCreator.details;
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
import org.openmodelica.crml.language.workspace.CrmlDiagnostic.LocationStyle;
import org.openmodelica.crml.testutil.CustomHtmlReporter;

/**
 * The diagnostics of a {@link org.openmodelica.crml.language.workspace.CrmlResource}
 * in a report: one line each, followed by the stack trace of the exception
 * behind it, if any. Without a location by default: a single-file test has
 * one file, and a multi-file test names the file in the section title.
 * Pass {@link LocationStyle#FILE_NAME} for a list that mixes files.
 */
public class DiagnosticsWrapper implements CustomHtmlReporter {
    private final List<Resource.Diagnostic> diagnostics;
    private final LocationStyle style;

    public DiagnosticsWrapper(List<? extends Resource.Diagnostic> diagnostics, LocationStyle style) {
        this.diagnostics = new ArrayList<>(diagnostics);
        this.style = style;
    }

    public static DiagnosticsWrapper of(List<? extends Resource.Diagnostic> diagnostics) {
        return new DiagnosticsWrapper(diagnostics, LocationStyle.NONE);
    }

    public static DiagnosticsWrapper of(List<? extends Resource.Diagnostic> diagnostics, LocationStyle style) {
        return new DiagnosticsWrapper(diagnostics, style);
    }

    /**
     * One line per diagnostic. The stack trace behind one, if any, is in a
     * collapsed block of its own, so the lines are not repeated in a summary.
     */
    @Override
    public Object report() {
        return div(each(diagnostics, d -> {
            String text = CrmlDiagnostic.format(d, style);
            Throwable cause = d instanceof CrmlDiagnostic ? ((CrmlDiagnostic) d).getCause() : null;
            if (cause == null) {
                return p(text);
            }
            StringWriter trace = new StringWriter();
            cause.printStackTrace(new PrintWriter(trace));
            return div(p(text), details(j2html.TagCreator.summary("Stack trace"), pre(code(trace.toString()))));
        }));
    }
}
