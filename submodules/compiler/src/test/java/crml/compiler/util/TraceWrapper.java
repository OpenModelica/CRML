package crml.compiler.util;

import static j2html.TagCreator.table;
import static j2html.TagCreator.tbody;
import static j2html.TagCreator.td;
import static j2html.TagCreator.th;
import static j2html.TagCreator.thead;
import static j2html.TagCreator.tr;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;

import crml.model.trace.TraceLink;
import crml.model.trace.TraceModel;
import crml.test.CustomHtmlReporter;
import j2html.tags.DomContent;

/**
 * Renders the CRML-to-Modelica trace as a table in the specification report.
 * This is the evidence base for the per-construct read-through check: it is
 * where "was this construct reached, and does what it produced look right"
 * gets answered.
 */
public class TraceWrapper implements CustomHtmlReporter {

    private final TraceModel trace;

    public TraceWrapper(TraceModel trace) {
        this.trace = trace;
    }

    public static TraceWrapper of(TraceModel trace) {
        return new TraceWrapper(trace);
    }

    @Override
    public Object report() {
        List<DomContent> rows = new ArrayList<>();
        for (TraceLink link : trace.getLinks()) {
            rows.add(tr(
                td(String.valueOf(link.getKind())),
                td(describe(link.getCrml())),
                td(describe(link.getModelica())),
                td(link.getNote() == null ? "" : link.getNote())));
        }
        return table(
            thead(tr(th("Kind"), th("CRML"), th("Modelica"), th("Note"))),
            tbody(rows.toArray(new DomContent[0])));
    }

    /** The element's type, plus its name where it has one. */
    private static String describe(EObject element) {
        if (element == null) {
            return "-";
        }
        String type = element.eClass().getName();
        EAttribute nameAttribute = (EAttribute) element.eClass().getEStructuralFeature("name");
        if (nameAttribute != null) {
            Object name = element.eGet(nameAttribute);
            if (name != null && !name.toString().isEmpty()) {
                return type + " '" + name + "'";
            }
        }
        return type;
    }
}
