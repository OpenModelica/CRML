package org.openmodelica.crml.language.workspace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.emf.ecore.resource.Resource;
import org.junit.jupiter.api.Test;
import org.openmodelica.crml.language.workspace.CrmlDiagnostic.Kind;
import org.openmodelica.crml.model.metamodel.Model;
import org.openmodelica.crml.testresources.TestResourcesRoot;

/**
 * Files with syntax or build errors get a partial DOM: what could be built.
 * The cases of {@code testModels/incomplete} state what must be in it
 * ({@code IncompleteModelTest}); this checks how the problems are reported.
 */
public class PartialDomTest {

    @Test
    void aPartialDomIsResolved() {
        CrmlWorkspace ws = new CrmlWorkspace();
        CrmlResource r = ws.load("model M is {\n  Boolean x is true\n  Boolean y is x;\n};");
        ws.resolve();

        assertEquals(CrmlResource.State.SYNTAX_ERRORS, r.getState(), "the state tells there were syntax errors");
        assertFalse(r.isComplete());
        assertFalse(r.needsResolving(), "resolved once");
        Model m = (Model) r.getRoot();
        assertEquals(2, m.getVariables().size());
        assertNotNull(m.getVariables().get(1).getDefinition());
        assertTrue(kinds(r).stream().allMatch(k -> k == Kind.SYNTAX), () -> "only the syntax error: " + kinds(r));
    }

    @Test
    void aBuildErrorKeepsTheRestOfTheDom() {
        CrmlWorkspace ws = new CrmlWorkspace();
        // A library cannot hold variables.
        CrmlResource r = ws.load("library L is {\n  Boolean y is true;\n  Template b1 'or' b2 = not (not b1 and not b2);\n};");
        ws.resolve();

        assertEquals(CrmlResource.State.BUILD_FAILED, r.getState());
        assertFalse(r.isComplete());
        assertEquals(1, ((org.openmodelica.crml.model.metamodel.Library) r.getRoot()).getOperators().size());
        assertEquals(Collections.singletonList(Kind.DOM_BUILD), kinds(r));
    }

    @Test
    void aDependencyWithErrorsIsBrokenButUsed() {
        CrmlWorkspace ws = new CrmlWorkspace();
        CrmlResource library = ws.load("library L is {\n  Template b1 'or' b2 = not (not b1 and not b2)\n};");
        CrmlResource model = ws.load("model M is L union {\n  Boolean x is external;\n  Boolean y is x 'or' x;\n};");
        ws.resolve();

        assertEquals(RootIndex.Lookup.Status.BROKEN, ws.getIndex().lookup("L").status());
        assertSame(library.getRoot(), ws.getIndex().lookup("L").root(), "the partial root is given out");
        assertTrue(((Model) model.getRoot()).getVariables().get(1).getDefinition()
            instanceof org.openmodelica.crml.model.metamodel.ComputedValue, "'or' is resolved");
        List<String> problems = ws.validate(model).getChildren().stream()
            .map(d -> d.getMessage()).collect(Collectors.toList());
        assertTrue(problems.stream().anyMatch(m -> m.contains(Kind.BROKEN_DEPENDENCY.name())), problems::toString);
        assertEquals(org.eclipse.emf.common.util.Diagnostic.ERROR, ws.validate(model).getSeverity());
    }

    @Test
    void anIncompleteFileFailsNoStage() throws IOException {
        // No exception while building, linking or resolving the incomplete
        // models: what an editor shows as a compiler failure.
        Path cases = TestResourcesRoot.RESOURCES.resolve("testModels").resolve("incomplete");
        List<Path> files;
        try (Stream<Path> walk = Files.walk(cases)) {
            files = walk.filter(p -> p.toString().endsWith(".crml")).sorted().collect(Collectors.toList());
        }
        assertFalse(files.isEmpty());
        List<String> failures = new ArrayList<>();
        for (Path file : files) {
            CrmlWorkspace ws = new CrmlWorkspace();
            CrmlResource r = ws.load(file);
            ws.resolve();
            if (r.getRoot() == null) {
                failures.add(file.getFileName() + ": no DOM");
            }
            for (Resource.Diagnostic d : r.getErrors()) {
                if (((CrmlDiagnostic) d).getCause() != null) {
                    failures.add(file.getFileName() + ": " + d);
                }
            }
        }
        assertEquals(Collections.<String>emptyList(), failures);
    }

    private static List<Kind> kinds(CrmlResource r) {
        return r.getErrors().stream().map(d -> ((CrmlDiagnostic) d).getKind()).collect(Collectors.toList());
    }
}
