package crml.compiler.util;

import static j2html.TagCreator.table;
import static j2html.TagCreator.tbody;
import static j2html.TagCreator.td;
import static j2html.TagCreator.th;
import static j2html.TagCreator.thead;
import static j2html.TagCreator.tr;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import crml.compiler.crmlcv2.verification.engine.Verification;
import crml.test.CustomHtmlReporter;
import j2html.tags.DomContent;

/**
 * Every variable a verification case compares, with what happened to it - also
 * for a case that stopped before the comparison, so it is always visible which
 * variables would have been checked.
 */
public final class VariablesWrapper implements CustomHtmlReporter {

    private final Map<String, Verification.VariableStatus> variables;

    private VariablesWrapper(Map<String, Verification.VariableStatus> variables) {
        this.variables = variables;
    }

    public static VariablesWrapper of(Map<String, Verification.VariableStatus> variables) {
        return new VariablesWrapper(variables);
    }

    @Override
    public Object report() {
        List<DomContent> rows = new ArrayList<>();
        for (Map.Entry<String, Verification.VariableStatus> entry : variables.entrySet()) {
            rows.add(tr(td(entry.getKey()), td(String.valueOf(entry.getValue()))));
        }
        return table(
            thead(tr(th("Variable"), th("Status"))),
            tbody(rows.toArray(new DomContent[0])));
    }
}
