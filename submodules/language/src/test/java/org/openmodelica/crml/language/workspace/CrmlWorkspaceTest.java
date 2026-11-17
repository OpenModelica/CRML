package org.openmodelica.crml.language.workspace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.URI;
import org.junit.jupiter.api.Test;
import org.openmodelica.crml.language.workspace.CrmlDiagnostic.Kind;
import org.openmodelica.crml.language.workspace.InspectingWorkspace.Stage;
import org.openmodelica.crml.model.metamodel.Model;
import org.openmodelica.crml.testresources.TestResourcesRoot;

/**
 * The single-resource behaviour of {@link CrmlWorkspace}: loading from files
 * and text, the stages of {@link CrmlWorkspace#resolve()}, diagnostics instead
 * of exceptions, and validation of resources with and without a DOM.
 */
public class CrmlWorkspaceTest {

    /** A model with one operator and one call to it. */
    private static final String CALL =
          "model M is {\n"
        + "Operator [ Boolean ] 'neg' Boolean a = not a;\n"
        + "Boolean x is external;\n"
        + "Boolean y is 'neg' x;\n"
        + "};";

    @Test
    void resolvesAllStages() {
        InspectingWorkspace ws = new InspectingWorkspace();
        CrmlResource r = ws.load(CALL);
        assertEquals(CrmlResource.State.BUILT, r.getState());
        assertTrue(r.getRoot() instanceof Model);

        ws.resolve();

        assertEquals(CrmlResource.State.RESOLVED, r.getState());
        assertTrue(r.getErrors().isEmpty(), () -> "errors: " + r.getErrors());
        InspectingWorkspace.Trace trace = ws.trace(r);
        assertTrue(trace.link.unresolved().isEmpty());
        assertNotNull(trace.parser);
        assertEquals(1, trace.calls.replaced().size());
        assertTrue(trace.calls.unresolved().isEmpty());
        assertNotNull(trace.lift);
        assertEquals(Diagnostic.OK, ws.validate(r).getSeverity());
    }

    @Test
    void stopsAfterLinking() {
        InspectingWorkspace ws = InspectingWorkspace.upTo(Stage.LINK);
        CrmlResource r = ws.load(CALL);

        ws.resolve();

        assertEquals(CrmlResource.State.BUILT, r.getState(), "a cut-off resource stays built");
        assertNotNull(ws.trace(r).link);
        assertNull(ws.trace(r).parser);
        assertNull(ws.trace(r).calls);
    }

    @Test
    void stopsAfterBuilding() {
        InspectingWorkspace ws = InspectingWorkspace.upTo(Stage.BUILD);
        CrmlResource r = ws.load(CALL);

        ws.resolve();

        assertEquals(CrmlResource.State.BUILT, r.getState());
        assertNull(ws.trace(r).link);
    }

    @Test
    void callbackSeesTheDomBetweenStages() {
        List<String> seen = new ArrayList<>();
        InspectingWorkspace ws = new InspectingWorkspace(new ArrayList<>(), Stage.LIFT, (stage, trace) -> {
            if (stage == Stage.LINK) {
                // Linked, but the call is still a Sequence.
                seen.add(stage + ":" + trace.link.unresolved().size());
            } else if (stage == Stage.OPERATOR_CALLS) {
                seen.add(stage + ":" + trace.calls.replaced().size());
            }
        });
        ws.load(CALL);

        ws.resolve();

        assertEquals("[LINK:0, OPERATOR_CALLS:1]", seen.toString());
    }

    @Test
    void unresolvedReferenceIsADiagnostic() {
        InspectingWorkspace ws = new InspectingWorkspace();
        CrmlResource r = ws.load("model M is {\nBoolean y is undefinedName;\n};");

        ws.resolve();

        assertEquals(1, ws.trace(r).link.unresolved().size());
        assertEquals("undefinedName", ws.trace(r).link.unresolved().get(0).id());
        assertEquals("[UNRESOLVED_REFERENCE]", kinds(r));
        assertEquals(ws.trace(r).link.diagnostics(), r.getErrors());
        assertEquals(Diagnostic.ERROR, ws.validate(r).getSeverity());
    }

    @Test
    void stageExceptionKeepsItsCause() {
        // Two operators with the same header: the call is ambiguous, and the
        // operator-call parser reports that by throwing.
        InspectingWorkspace ws = new InspectingWorkspace();
        CrmlResource r = ws.load("model M is {\n"
            + "Operator [ Boolean ] 'neg' Boolean a = not a;\n"
            + "Operator [ Boolean ] 'neg' Boolean b = not b;\n"
            + "Boolean x is external;\n"
            + "Boolean y is 'neg' x;\n"
            + "};");

        ws.resolve();

        assertEquals("[OPERATOR_CALL]", kinds(r));
        CrmlDiagnostic diagnostic = (CrmlDiagnostic) r.getErrors().get(0);
        assertNotNull(diagnostic.getCause());
        assertSame(diagnostic, ws.trace(r).calls.diagnostics().get(0));
        // The later stages still ran.
        assertNotNull(ws.trace(r).lift);
        Diagnostic validation = ws.validate(r);
        assertEquals(Diagnostic.ERROR, validation.getSeverity());
        assertSame(diagnostic.getCause(), validation.getChildren().get(0).getException());
    }

    @Test
    void syntaxErrorsCanBeValidatedWithoutADom() {
        CrmlWorkspace ws = new CrmlWorkspace();
        CrmlResource r = ws.load("model M is { Boolean };");

        ws.resolve();

        assertEquals(CrmlResource.State.SYNTAX_ERRORS, r.getState());
        assertNull(r.getRoot());
        assertNotNull(r.getHeader(), "the header is read even with syntax errors");
        assertEquals("M", r.getHeader().name());
        assertTrue(kinds(r).startsWith("[SYNTAX"));
        assertEquals(Diagnostic.ERROR, ws.validate(r).getSeverity());
    }

    @Test
    void reloadingTextReplacesTheResource() {
        CrmlWorkspace ws = new CrmlWorkspace();
        CrmlResource first = ws.load(URI.createURI("inline:/M.crml"),
            "model M is { Boolean };");
        CrmlResource second = ws.load(URI.createURI("inline:/M.crml"), CALL);

        assertSame(first, second);
        assertEquals(CrmlResource.State.BUILT, second.getState());
        assertTrue(second.getErrors().isEmpty());
        assertEquals(1, ws.resources().size());
        assertEquals(RootIndex.Lookup.Status.FOUND, ws.getIndex().lookup("M").status());
    }

    @Test
    void loadsFilesOnce() throws IOException {
        Path file = TestResourcesRoot.RESOURCES.resolve("testModels").resolve("library-loading")
            .resolve("operator").resolve("Negation.crml");
        CrmlWorkspace ws = new CrmlWorkspace();

        CrmlResource first = ws.load(file);
        CrmlResource second = ws.load(file);

        assertSame(first, second);
        assertEquals(1, ws.resources().size());
        assertFalse(first.getContents().isEmpty());
        assertEquals(RootIndex.Lookup.Status.FOUND, ws.getIndex().lookup(first.getHeader().name()).status());
    }

    private static String kinds(CrmlResource r) {
        return r.getErrors().stream()
            .map(d -> ((CrmlDiagnostic) d).getKind())
            .map(Kind::name)
            .collect(Collectors.toList())
            .toString();
    }
}
