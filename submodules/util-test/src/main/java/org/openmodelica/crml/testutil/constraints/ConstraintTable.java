package org.openmodelica.crml.testutil.constraints;

import static j2html.TagCreator.code;
import static j2html.TagCreator.table;
import static j2html.TagCreator.tbody;
import static j2html.TagCreator.td;
import static j2html.TagCreator.th;
import static j2html.TagCreator.thead;
import static j2html.TagCreator.tr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openmodelica.crml.testutil.CustomHtmlReporter;

import j2html.tags.DomContent;

/**
 * The results of a constraint file as one table, one row per line: line number,
 * the constraint as written, status and detail. Shown expanded, in the style of
 * the verification report's outputs table.
 */
public final class ConstraintTable implements CustomHtmlReporter {

    private final List<ConstraintResult> results;

    private ConstraintTable(List<ConstraintResult> results) {
        this.results = Collections.unmodifiableList(new ArrayList<>(results));
    }

    public static ConstraintTable of(List<ConstraintResult> results) {
        return new ConstraintTable(results);
    }

    @Override
    public Object report() {
        List<DomContent> rows = new ArrayList<>();
        for (ConstraintResult result : results) {
            rows.add(tr(
                td(String.valueOf(result.line)),
                td(code(result.text)),
                td(result.status.toString()),
                td(result.detail)));
        }
        return table(
            thead(tr(th("#"), th("Constraint"), th("Status"), th("Detail"))),
            tbody(rows.toArray(new DomContent[0])));
    }
}
