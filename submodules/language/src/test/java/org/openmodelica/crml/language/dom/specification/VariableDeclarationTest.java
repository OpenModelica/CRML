package org.openmodelica.crml.language.dom.specification;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.openmodelica.crml.language.pretty.PrettyPrint;
import org.openmodelica.crml.language.util.DomRun;
import org.openmodelica.crml.language.workspace.CrmlDiagnostic;
import org.openmodelica.crml.language.workspace.InspectingWorkspace.Stage;

public class VariableDeclarationTest {

    private static DomRun link(String content) {
        return DomRun.of("model Foo is {\n" + content + "\n};", Stage.LINK);
    }

    @Test
    void testCorrect_multiVariableDeclaration() {
        DomRun run = link("Integer x1, x2;");
        assertFalse(run.parsed().syntax().hasErrors(), "Parse should succeed");

        if (run.dom() != null) {
            System.out.println("DOM:\n" + PrettyPrint.prettyPrint(run.dom()));
        }
        run.errors().forEach(error -> {
            System.out.println("Error: " + error);
            if (error instanceof CrmlDiagnostic && ((CrmlDiagnostic) error).getCause() != null) {
                ((CrmlDiagnostic) error).getCause().printStackTrace(System.out);
            }
        });

        assertTrue(run.errors().isEmpty(), "DOM translation reported errors:" + run.errorText());
    }
}
