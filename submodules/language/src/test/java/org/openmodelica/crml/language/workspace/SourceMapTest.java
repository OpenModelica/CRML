package org.openmodelica.crml.language.workspace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.junit.jupiter.api.Test;
import org.openmodelica.crml.model.metamodel.ComputedValue;
import org.openmodelica.crml.model.metamodel.CustomOperator;
import org.openmodelica.crml.model.metamodel.Keyword;
import org.openmodelica.crml.model.metamodel.Library;
import org.openmodelica.crml.model.metamodel.Model;
import org.openmodelica.crml.model.metamodel.Variable;
import org.openmodelica.crml.model.metamodel.VariableReference;

/** The trace between a DOM and its source: {@link SourceMap}. */
public class SourceMapTest {

    private static final String CALL =
          "model M is {\n"
        + "  Operator [ Boolean ] 'neg' Boolean a = not a;\n"
        + "  Boolean x is external;\n"
        + "  Boolean y is 'neg' x;\n"
        + "};";

    @Test
    void isAttachedToTheResourceAndFoundFromItsObjects() {
        CrmlWorkspace ws = new CrmlWorkspace();
        CrmlResource r = ws.load(CALL);
        ws.resolve();

        SourceMap map = r.getSourceMap();
        assertNotNull(map);
        assertSame(map, SourceMap.of(r));
        assertSame(map, SourceMap.of(variable(r, "y")), "kept after resolution");
    }

    @Test
    void givesWhereEachObjectIs() {
        CrmlWorkspace ws = new CrmlWorkspace();
        CrmlResource r = ws.load(CALL);
        ws.resolve();
        SourceMap map = r.getSourceMap();

        Variable x = variable(r, "x");
        assertEquals("Boolean", map.start(x).getText());
        assertEquals(";", map.stop(x).getText());
        assertEquals(3, map.start(x).getLine());
        assertEquals("model", map.start(r.getRoot()).getText());
    }

    @Test
    void tracesAnOperatorCallToTheTextItWasParsedFrom() {
        CrmlWorkspace ws = new CrmlWorkspace();
        CrmlResource r = ws.load(CALL);
        ws.resolve();
        SourceMap map = r.getSourceMap();

        ComputedValue call = (ComputedValue) variable(r, "y").getDefinition();
        assertEquals("'neg'", map.start(call).getText());
        assertEquals("x", map.stop(call).getText());
    }

    @Test
    void findsTheInnermostObjectAtAnOffset() {
        CrmlWorkspace ws = new CrmlWorkspace();
        String text = "model M is {\n  Boolean x is external;\n  Boolean y is x;\n};";
        CrmlResource r = ws.load(text);
        ws.resolve();
        SourceMap map = r.getSourceMap();

        int reference = text.lastIndexOf("x;");
        EObject atReference = map.objectAt(reference);
        assertTrue(atReference instanceof VariableReference, String.valueOf(atReference));
        assertSame(variable(r, "y"), map.objectAt(text.indexOf("y is")));
        assertSame(r.getRoot(), map.objectAt(text.indexOf("\n  Boolean y")), "between two variables");
        assertNull(map.objectAt(text.length() + 10), "after the file");
    }

    @Test
    void positionsAnObjectWithoutANodeAtItsContainer() {
        CrmlWorkspace ws = new CrmlWorkspace();
        CrmlResource r = ws.load(CALL);
        ws.resolve();
        SourceMap map = r.getSourceMap();

        ComputedValue call = (ComputedValue) variable(r, "y").getDefinition();
        EObject binding = call.getBindings().get(0);
        assertEquals("x", map.position(binding).getText(), "a binding starts where its value does");
    }

    // ---------------------------------------------------------------------
    // Comments
    // ---------------------------------------------------------------------

    private static final String DOCUMENTED =
          "library L is {\n"
        + "  // Section heading\n"
        + "\n"
        + "  // Negation.\n"
        + "  // Of a Boolean.\n"
        + "  Operator [ Boolean ] 'neg' Boolean a = not a;\n"
        + "  Operator [ Boolean ] 'id' Boolean a = a; // trailing, about 'id'\n"
        + "  Operator [ Boolean ] 'same' Boolean a = a;\n"
        + "  /* Block\n"
        + "   * comment. */\n"
        + "  Operator [ Boolean ] 'yes' Boolean a = true;\n"
        + "};";

    @Test
    void documentsAnObjectWithTheCommentsDirectlyAboveIt() {
        CrmlResource r = loaded(DOCUMENTED);
        SourceMap map = r.getSourceMap();

        assertEquals("Negation.\nOf a Boolean.", map.documentation(operator(r, "'neg'")),
            "the heading is cut off by the blank line");
        assertEquals(2, map.comments(operator(r, "'neg'")).size());
        assertNull(map.documentation(operator(r, "'id'")));
        assertNull(map.documentation(operator(r, "'same'")), "the comment ending the line above is not about it");
        assertEquals("Block\ncomment.", map.documentation(operator(r, "'yes'")));
    }

    // ---------------------------------------------------------------------
    // Holes and partial DOMs
    // ---------------------------------------------------------------------

    @Test
    void recordsWhereTheParserRecovered() {
        String text = "model M is {\n  Boolean x is ;\n  Boolean y is true;\n};";
        CrmlResource r = loaded(text);
        SourceMap map = r.getSourceMap();

        assertFalse(map.holes().isEmpty());
        SourceMap.Hole hole = map.holes().get(0);
        assertEquals(";", hole.at().getText());
        assertEquals(2, hole.at().getLine());
        assertSame(variable(r, "x"), hole.host());
    }

    @Test
    void aCompleteFileHasNoHoles() {
        assertTrue(loaded(CALL).getSourceMap().holes().isEmpty());
    }

    @Test
    void reloadingReplacesTheMap() {
        CrmlWorkspace ws = new CrmlWorkspace();
        URI uri = URI.createURI("inline:/M.crml");
        CrmlResource r = ws.load(uri, "model M is { Boolean };");
        SourceMap first = r.getSourceMap();

        ws.load(uri, CALL);

        assertFalse(r.eAdapters().contains(first));
        assertEquals(1, r.eAdapters().stream().filter(SourceMap.class::isInstance).count());
        assertSame(r.getSourceMap(), SourceMap.of(r));
    }

    // ---------------------------------------------------------------------

    private static CrmlResource loaded(String text) {
        CrmlWorkspace ws = new CrmlWorkspace();
        CrmlResource r = ws.load(text);
        ws.resolve();
        return r;
    }

    private static Variable variable(CrmlResource r, String name) {
        return ((Model) r.getRoot()).getVariables().stream()
            .filter(v -> name.equals(v.getName())).findFirst().orElseThrow(AssertionError::new);
    }

    private static CustomOperator operator(CrmlResource r, String keyword) {
        for (EObject operator : ((Library) r.getRoot()).getOperators()) {
            List<String> keywords = ((CustomOperator) operator).getHeader().stream()
                .filter(Keyword.class::isInstance).map(Keyword.class::cast).map(Keyword::getKeyword)
                .collect(Collectors.toList());
            if (keywords.contains(keyword)) {
                return (CustomOperator) operator;
            }
        }
        throw new AssertionError("no operator " + keyword);
    }
}
