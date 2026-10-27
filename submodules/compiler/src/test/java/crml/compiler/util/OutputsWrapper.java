package crml.compiler.util;

import static j2html.TagCreator.table;
import static j2html.TagCreator.tbody;
import static j2html.TagCreator.td;
import static j2html.TagCreator.th;
import static j2html.TagCreator.thead;
import static j2html.TagCreator.tr;

import java.util.ArrayList;
import java.util.List;

import crml.compiler.crmlcv2.verification.engine.Verification;
import crml.test.CustomHtmlReporter;
import j2html.tags.DomContent;

/**
 * The outputs of a verification case with a reference, with what happened to
 * each - also for a case that stopped before the comparison, so it is always
 * visible what would have been checked, and which signals stand for each one.
 */
public final class OutputsWrapper implements CustomHtmlReporter {

    private final List<Verification.OutputResult> outputs;

    private OutputsWrapper(List<Verification.OutputResult> outputs) {
        this.outputs = outputs;
    }

    public static OutputsWrapper of(List<Verification.OutputResult> outputs) {
        return new OutputsWrapper(outputs);
    }

    @Override
    public Object report() {
        List<DomContent> rows = new ArrayList<>();
        for (Verification.OutputResult output : outputs) {
            rows.add(tr(
                td(output.name()),
                td(output.kind()),
                td(output.comparedAs()),
                td(String.valueOf(output.status())),
                td(output.detail())));
        }
        return table(
            thead(tr(th("Output"), th("Kind"), th("Compared as (compiled ~ reference)"), th("Status"), th("Detail"))),
            tbody(rows.toArray(new DomContent[0])));
    }
}
