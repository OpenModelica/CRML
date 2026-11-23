package org.openmodelica.crml.model.base.typing;

import static j2html.TagCreator.table;
import static j2html.TagCreator.tbody;
import static j2html.TagCreator.td;
import static j2html.TagCreator.th;
import static j2html.TagCreator.thead;
import static j2html.TagCreator.tr;

import java.util.Objects;

import org.openmodelica.crml.testutil.CustomHtmlReporter;

/** One inference case in the test report: the expression, and the expected and inferred type and size. */
final class InferenceReport implements CustomHtmlReporter {

    private final String expression;
    private final ValueType expectedType;
    private final ValueType inferredType;
    private final Integer expectedSize;
    private final Integer inferredSize;

    InferenceReport(String expression, ValueType expectedType, ValueType inferredType,
            Integer expectedSize, Integer inferredSize) {
        this.expression = expression;
        this.expectedType = expectedType;
        this.inferredType = inferredType;
        this.expectedSize = expectedSize;
        this.inferredSize = inferredSize;
    }

    @Override
    public Object report() {
        return table(
            thead(tr(th(""), th("Expected"), th("Inferred"), th(""))),
            tbody(
                tr(td("Expression"), td(expression), td(""), td("")),
                tr(td("Type"), td(String.valueOf(expectedType)), td(String.valueOf(inferredType)),
                    td(mark(expectedType, inferredType))),
                tr(td("Size"), td(size(expectedSize)), td(size(inferredSize)),
                    td(mark(expectedSize, inferredSize)))));
    }

    private static String size(Integer size) {
        return size == null ? "not computable" : size.toString();
    }

    private static String mark(Object expected, Object inferred) {
        return Objects.equals(expected, inferred) ? "ok" : "MISMATCH";
    }
}
