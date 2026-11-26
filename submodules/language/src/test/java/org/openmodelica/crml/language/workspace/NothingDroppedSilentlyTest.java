package org.openmodelica.crml.language.workspace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.emf.ecore.resource.Resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openmodelica.crml.language.workspace.CrmlDiagnostic.Kind;
import org.openmodelica.crml.model.metamodel.Model;
import org.openmodelica.crml.model.metamodel.Variable;

/**
 * Every way building the DOM can leave out a part of a model that parses
 * reports an error on the resource: a model built without a part is never
 * taken for a complete one. Each case also has a variable {@code ok}, which
 * must be built whatever happens to the rest.
 */
public class NothingDroppedSilentlyTest {

    static List<Arguments> drops() {
        return Arrays.asList(
            Arguments.of("the definition of a variable",
                "model M is {\n\tInteger big is 12345678901234;\n\tBoolean ok is true;\n};"),
            Arguments.of("the definition of a class member",
                "model M is {\n\tclass K is { Integer big is 12345678901234; };\n\tBoolean ok is true;\n};"),
            Arguments.of("the body of an operator",
                "model M is {\n\tOperator [Integer] Integer x 'big' = 12345678901234;\n\tBoolean ok is true;\n};"),
            Arguments.of("a type declaration",
                "model M is {\n\ttype Speed extends Real\n\tBoolean ok is true;\n};"),
            Arguments.of("a parameter declaration",
                "model M is {\n\tparameter Real p, q;\n\tBoolean ok is true;\n};"),
            Arguments.of("a Category",
                "model M is {\n\tCategory c = { (+, -) };\n\tBoolean ok is true;\n};"),
            Arguments.of("an alias in a class",
                "model M is {\n\tclass K is { Boolean a is true; alias a; };\n\tBoolean ok is true;\n};"),
            Arguments.of("a forbid in a class",
                "model M is {\n\tclass K is { Boolean a is true; forbid +; };\n\tBoolean ok is true;\n};"),
            Arguments.of("a parameter declaration in a class",
                "model M is {\n\tclass K is { Boolean a is true; parameter Real p; };\n\tBoolean ok is true;\n};"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("drops")
    void aPartLeftOutIsAnError(String part, String source) {
        CrmlResource resource = new CrmlWorkspace().load(source);

        assertEquals(CrmlResource.State.BUILD_FAILED, resource.getState(), part + ": " + resource.getErrors());
        assertFalse(resource.isComplete());
        assertTrue(kinds(resource).contains(Kind.DOM_BUILD), part + ": " + resource.getErrors());
        assertTrue(hasVariable((Model) resource.getRoot(), "ok"), "the rest of the model is built");
    }

    /** A comment is not a part of the model: leaving it out is no error. */
    @Test
    void aCommentIsNoPart() {
        CrmlResource resource = new CrmlWorkspace().load(
            "model M is {\n\t// a comment\n\tclass K is { // another\n\t\tBoolean a is true; };\n\tBoolean ok is true;\n};");

        assertTrue(resource.getErrors().isEmpty(), resource.getErrors().toString());
        assertTrue(resource.isComplete());
        assertTrue(hasVariable((Model) resource.getRoot(), "ok"));
    }

    private static List<Kind> kinds(CrmlResource resource) {
        List<Kind> kinds = new ArrayList<Kind>();
        for (Resource.Diagnostic error : resource.getErrors()) {
            if (error instanceof CrmlDiagnostic) {
                kinds.add(((CrmlDiagnostic) error).getKind());
            }
        }
        return kinds;
    }

    private static boolean hasVariable(Model model, String name) {
        for (Variable variable : model.getVariables()) {
            if (name.equals(variable.getName())) {
                return true;
            }
        }
        return false;
    }
}
