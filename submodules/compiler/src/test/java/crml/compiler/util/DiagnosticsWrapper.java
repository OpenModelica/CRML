package crml.compiler.util;

import static j2html.TagCreator.table;
import static j2html.TagCreator.tbody;
import static j2html.TagCreator.td;
import static j2html.TagCreator.th;
import static j2html.TagCreator.thead;
import static j2html.TagCreator.tr;

import java.util.ArrayList;
import java.util.List;

import crml.compiler.crmlcv2.Diagnostic;
import crml.test.CustomHtmlReporter;
import j2html.tags.DomContent;

/**
 * Renders everything the transformation could not map. A construct that is
 * reached but unmapped shows up here rather than aborting the translation, so
 * this table plus the trace table together answer "was every construct
 * reached".
 */
public class DiagnosticsWrapper implements CustomHtmlReporter {

    private final List<Diagnostic> diagnostics;

    public DiagnosticsWrapper(List<Diagnostic> diagnostics) {
        this.diagnostics = diagnostics;
    }

    public static DiagnosticsWrapper of(List<Diagnostic> diagnostics) {
        return new DiagnosticsWrapper(diagnostics);
    }

    @Override
    public Object report() {
        List<DomContent> rows = new ArrayList<>();
        for (Diagnostic diagnostic : diagnostics) {
            rows.add(tr(
                td(String.valueOf(diagnostic.severity())),
                td(diagnostic.constructKind()),
                td(diagnostic.message())));
        }
        return table(
            thead(tr(th("Severity"), th("Construct"), th("Message"))),
            tbody(rows.toArray(new DomContent[0])));
    }
}
