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
import org.eclipse.emf.ecore.EObject;
import org.junit.jupiter.api.Test;
import org.openmodelica.crml.language.opcall.MixfixParser;
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
        CrmlDiagnostic error = (CrmlDiagnostic) r.getErrors().get(0);
        assertEquals(2, error.getLine(), () -> "at the reference: " + error);
        assertEquals(13, error.getColumn(), () -> "at the reference: " + error);
        assertEquals(Diagnostic.ERROR, ws.validate(r).getSeverity());
    }

    @Test
    void ambiguousCallIsReportedAtItsPosition() {
        // Two operators with the same header: the call can be read as either.
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
        assertTrue(diagnostic.getMessage().startsWith("Ambiguous operator call"), diagnostic.getMessage());
        assertEquals(5, diagnostic.getLine());
        assertNull(diagnostic.getCause(), "an error in the model, not an exception");
        assertSame(diagnostic, ws.trace(r).calls.diagnostics().get(0));
        // The call stays a Sequence, and the later stages still ran.
        assertEquals(1, ws.trace(r).calls.unresolved().size());
        assertNotNull(ws.trace(r).lift);
    }

    @Test
    void unresolvedCallIsReportedAtItsFirstElement() {
        // No operator in scope has these keywords. The first sequence starts
        // with a value, the second with a keyword, the third with a constructor.
        InspectingWorkspace ws = new InspectingWorkspace();
        CrmlResource r = ws.load("model M is {\n"
            + "Boolean x is external;\n"
            + "Boolean y is x 'nope' x;\n"
            + "Boolean z is   'nope' x;\n"
            + "Boolean w is new Boolean x 'nope' x;\n"
            + "};");

        ws.resolve();

        assertEquals("[OPERATOR_CALL, OPERATOR_CALL, OPERATOR_CALL]", kinds(r));
        assertEquals(ws.trace(r).calls.diagnostics(), r.getErrors());
        int[][] positions = { { 3, 13 }, { 4, 15 }, { 5, 13 } };
        for (int i = 0; i < positions.length; i++) {
            CrmlDiagnostic diagnostic = (CrmlDiagnostic) r.getErrors().get(i);
            assertTrue(diagnostic.getMessage().startsWith("Unresolved operator call"), diagnostic.getMessage());
            assertEquals(positions[i][0], diagnostic.getLine(), () -> "at the first element: " + diagnostic);
            assertEquals(positions[i][1], diagnostic.getColumn(), () -> "at the first element: " + diagnostic);
            assertNull(diagnostic.getCause(), "an error in the model, not an exception");
        }
        assertEquals("Unresolved operator call _ 'nope' _: no operator in scope matches it",
            r.getErrors().get(0).getMessage());
        assertEquals("Unresolved operator call 'nope' _: no operator in scope matches it",
            r.getErrors().get(1).getMessage());
        // The calls stay Sequences, and the later stages still ran.
        assertEquals(3, ws.trace(r).calls.unresolved().size());
        assertNotNull(ws.trace(r).lift);
        assertEquals(Diagnostic.ERROR, ws.validate(r).getSeverity());
    }

    @Test
    void stageExceptionKeepsItsCause() {
        IllegalStateException thrown = new IllegalStateException("thrown by the stage");
        InspectingWorkspace ws = new InspectingWorkspace() {
            @Override
            MixfixParser operatorParser(CrmlResource resource) {
                return new MixfixParser((Model) resource.getRoot(), resource.getVisitor()) {
                    @Override
                    public void perform(EObject root) {
                        throw thrown;
                    }
                };
            }
        };
        CrmlResource r = ws.load(CALL);

        ws.resolve();

        assertEquals("[OPERATOR_CALL]", kinds(r));
        CrmlDiagnostic diagnostic = (CrmlDiagnostic) r.getErrors().get(0);
        assertSame(thrown, diagnostic.getCause());
        assertSame(diagnostic, ws.trace(r).calls.diagnostics().get(0));
        // The later stages still ran.
        assertNotNull(ws.trace(r).lift);
        Diagnostic validation = ws.validate(r);
        assertEquals(Diagnostic.ERROR, validation.getSeverity());
        assertSame(thrown, validation.getChildren().get(0).getException());
    }

    @Test
    void syntaxErrorsHaveTheirPosition() {
        CrmlWorkspace ws = new CrmlWorkspace();
        CrmlResource r = ws.load(URI.createURI("inline:/S.crml"), "model M is {\nBoolean ;\n};");

        CrmlDiagnostic error = (CrmlDiagnostic) r.getErrors().get(0);
        assertEquals(Kind.SYNTAX, error.getKind());
        assertEquals("inline:/S.crml", error.getLocation());
        assertEquals(2, error.getLine());
        assertEquals(8, error.getColumn());
        assertSame(error, r.getParsed().syntax().errors().get(0), "the parser's own diagnostic");
    }

    @Test
    void buildErrorsHaveTheirPosition() {
        CrmlWorkspace ws = new CrmlWorkspace();
        CrmlResource r = ws.load(URI.createURI("inline:/P.crml"), "\n  package P is {\n};");

        assertEquals(CrmlResource.State.BUILD_FAILED, r.getState(), () -> "errors: " + r.getErrors());
        assertNull(r.getRoot());
        assertEquals("[DOM_BUILD]", kinds(r));
        CrmlDiagnostic error = (CrmlDiagnostic) r.getErrors().get(0);
        assertEquals("inline:/P.crml", error.getLocation());
        assertEquals(2, error.getLine());
        assertEquals(2, error.getColumn());
        assertNull(error.getCause());
    }

    @Test
    void syntaxErrorsGiveAPartialDomThatCanBeValidated() {
        CrmlWorkspace ws = new CrmlWorkspace();
        CrmlResource r = ws.load("model M is { Boolean };");

        ws.resolve();

        assertEquals(CrmlResource.State.SYNTAX_ERRORS, r.getState());
        assertFalse(r.isComplete());
        Model m = (Model) r.getRoot();
        assertEquals("M", m.getName());
        assertTrue(m.getVariables().isEmpty(), "the variable without a name is left out");
        assertNotNull(r.getHeader(), "the header is read even with syntax errors");
        assertEquals("M", r.getHeader().name());
        assertTrue(kinds(r).matches("\\[SYNTAX(, SYNTAX)*\\]"), () -> "leaving out the variable is not reported again: " + kinds(r));
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

    @Test
    void validationReportsABrokenDependencyOnly() {
        CrmlWorkspace ws = new CrmlWorkspace();
        CrmlResource library = ws.load("library L is { Boolean };");
        CrmlResource model = ws.load("model M is L union {\nBoolean x is external;\n};");

        ws.resolve();

        assertEquals(CrmlResource.State.SYNTAX_ERRORS, library.getState());
        assertTrue(model.getErrors().stream().noneMatch(d -> ((CrmlDiagnostic) d).getKind() == Kind.BROKEN_DEPENDENCY),
            "a broken dependency is a validation error, not a load error");
        List<String> messages = new ArrayList<>();
        for (Diagnostic child : ws.validate(model).getChildren()) {
            messages.add(child.getMessage());
        }
        List<String> broken = messages.stream()
            .filter(m -> m.contains(Kind.BROKEN_DEPENDENCY.name()))
            .collect(Collectors.toList());
        assertEquals(1, broken.size(), () -> "validation: " + messages);
        assertTrue(broken.get(0).startsWith(model.getURI() + ":1:11: "), broken.get(0));
        assertTrue(messages.stream().noneMatch(m -> m.contains(Kind.SYNTAX.name())),
            () -> "the dependency's own errors are not repeated: " + messages);
    }

    // ---------------------------------------------------------------------
    // Multi-library: binding, order, dependency validation
    // ---------------------------------------------------------------------

    @Test
    void bindRecordsEachDependencyAndBindsTheFoundOnes() {
        InspectingWorkspace ws = InspectingWorkspace.upTo(Stage.BIND);
        CrmlResource logic = ws.load("library Logic is { };");
        CrmlResource model = ws.load("model M is Logic union Missing union { };");

        ws.resolve();

        StageResult.BindResult bind = ws.trace(model).bind;
        assertEquals(2, bind.dependencies().size());
        assertSame(logic.getRoot(), bind.dependencies().get(0).getLibrary());
        assertEquals(RootIndex.Lookup.Status.FOUND, bind.status(bind.dependencies().get(0)));
        assertNull(bind.dependencies().get(1).getLibrary());
        assertEquals(RootIndex.Lookup.Status.MISSING, bind.status(bind.dependencies().get(1)));
        assertNull(ws.trace(model).link, "cut after BIND");
        assertEquals(CrmlResource.State.BUILT, model.getState());
    }

    @Test
    void librariesAreLinkedBeforeTheirDependents() {
        List<StageResult> linked = new ArrayList<>();
        InspectingWorkspace ws = new InspectingWorkspace(new ArrayList<>(), Stage.LIFT, (stage, trace) -> {
            if (stage == Stage.LINK) {
                linked.add(trace.link);
            }
        });
        // Loaded in the opposite order.
        CrmlResource model = ws.load("model M is B union { };");
        CrmlResource b = ws.load("library B is A union { };");
        CrmlResource a = ws.load("library A is { };");

        ws.resolve();

        assertEquals(3, linked.size());
        assertSame(ws.trace(a).link, linked.get(0));
        assertSame(ws.trace(b).link, linked.get(1));
        assertSame(ws.trace(model).link, linked.get(2));
    }

    @Test
    void missingDependency() {
        CrmlWorkspace ws = new CrmlWorkspace();
        CrmlResource model = ws.load("model M is Missing union { };");
        ws.resolve();

        assertEquals("[MISSING_DEPENDENCY at 1:11]", dependencyProblems(ws, model));
    }

    @Test
    void ambiguousDependency() {
        CrmlWorkspace ws = new CrmlWorkspace();
        ws.load("library L is { };");
        ws.load("library L is { };");
        CrmlResource model = ws.load("model M is L union { };");
        ws.resolve();

        assertEquals("[AMBIGUOUS_DEPENDENCY at 1:11]", dependencyProblems(ws, model));
    }

    @Test
    void cycleIsReportedOnEveryRootOfIt() {
        CrmlWorkspace ws = new CrmlWorkspace();
        CrmlResource a = ws.load("library A is B union { };");
        CrmlResource b = ws.load("library B is A union { };");
        CrmlResource self = ws.load("library S is S union { };");
        ws.resolve();

        assertEquals("[DEPENDENCY_CYCLE at 1:13]", dependencyProblems(ws, a));
        assertEquals("[DEPENDENCY_CYCLE at 1:13]", dependencyProblems(ws, b));
        assertEquals("[DEPENDENCY_CYCLE at 1:13]", dependencyProblems(ws, self));
    }

    @Test
    void libraryDependingOnAModel() {
        CrmlWorkspace ws = new CrmlWorkspace();
        ws.load("model M is { };");
        CrmlResource library = ws.load("library L is M union { };");
        ws.resolve();

        assertEquals("[LIBRARY_DEPENDS_ON_MODEL at 1:13]", dependencyProblems(ws, library));
    }

    @Test
    void listingADependencyTwiceIsNotAProblem() {
        CrmlWorkspace ws = new CrmlWorkspace();
        ws.load("library L is { };");
        CrmlResource model = ws.load("model M is L union L union { };");
        ws.resolve();

        assertEquals("[]", dependencyProblems(ws, model));
        assertEquals(Diagnostic.OK, ws.validate(model).getSeverity());
    }

    /** The dependency problems {@code validate} reports for {@code resource}, as {@code KIND at line:column}. */
    private static String dependencyProblems(CrmlWorkspace ws, CrmlResource resource) {
        List<String> problems = new ArrayList<>();
        for (Diagnostic child : ws.validate(resource).getChildren()) {
            for (Kind kind : new Kind[] { Kind.MISSING_DEPENDENCY, Kind.BROKEN_DEPENDENCY, Kind.AMBIGUOUS_DEPENDENCY,
                    Kind.DEPENDENCY_CYCLE, Kind.LIBRARY_DEPENDS_ON_MODEL }) {
                String marker = ": " + kind.name() + ": ";
                if (child.getMessage().contains(marker)) {
                    String position = child.getMessage().substring(0, child.getMessage().indexOf(marker));
                    position = position.substring(resource.location().length() + 1);
                    problems.add(kind.name() + " at " + position);
                }
            }
        }
        return problems.toString();
    }

    private static String kinds(CrmlResource r) {
        return r.getErrors().stream()
            .map(d -> ((CrmlDiagnostic) d).getKind())
            .map(Kind::name)
            .collect(Collectors.toList())
            .toString();
    }
}
