package org.openmodelica.crml.language.dom;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;

import org.eclipse.emf.ecore.resource.Resource;
import org.junit.jupiter.api.Test;
import org.openmodelica.crml.util.SafeResource;
import org.openmodelica.crml.language.pretty.PrettyPrint;
import org.openmodelica.crml.language.workspace.CrmlDiagnostic;
import org.openmodelica.crml.language.workspace.CrmlResource;
import org.openmodelica.crml.language.workspace.InspectingWorkspace;
import org.openmodelica.crml.language.workspace.InspectingWorkspace.Stage;

/**
 * Scratch harness for debugging a single CRML model end-to-end.
 * Not part of the specification suite (does not extend ReportedTest / emit
 * into the HTML report) — it just prints everything to stdout.
 *
 * Point it at any file by editing MODEL_RESOURCE, or drop your own model
 * into src/test/resources/debug/debug.crml.
 */
//  ./gradlew :language:clean :language:test --tests "org.openmodelica.crml.language.dom.DebugModelTest" -i
public class DebugModelTest {

    private static final String MODEL_RESOURCE = "debug/debug.crml";

    @Test
    @java.lang.SuppressWarnings("squid:S2699") // This test case is only to debug tests from an IDE.
    void debugModel() throws IOException {
        Path fileName = SafeResource.get(MODEL_RESOURCE);
        System.out.println("=== Model: " + fileName + " ===");

        // The DOM is printed between the stages, while it is still linked only.
        CrmlResource[] model = new CrmlResource[1];
        InspectingWorkspace workspace = new InspectingWorkspace(Collections.<Path>emptyList(), Stage.LIFT,
            (stage, trace) -> {
                if (stage == Stage.LINK) {
                    System.out.println("--- DOM RAW---");
                    System.out.println(PrettyPrint.prettyPrint(model[0].getRoot()));
                }
            });
        model[0] = workspace.load(fileName);
        CrmlResource resource = model[0];

        System.out.println("--- Syntax Errors ---");
        if (resource.getParsed().syntax().hasErrors()) {
            resource.getParsed().syntax().errors().forEach(System.out::println);
        } else {
            System.out.println("(none)");
        }

        System.out.println("--- AST ---");
        System.out.println(resource.getParsed().toPrettyTree());

        workspace.resolve();

        System.out.println("--- DOM ---");
        System.out.println(PrettyPrint.prettyPrint(resource.getRoot()));

        if (!resource.getErrors().isEmpty()) {
            System.out.println("--- Errors ---");
            for (Resource.Diagnostic error : resource.getErrors()) {
                System.out.println(error);
                if (error instanceof CrmlDiagnostic && ((CrmlDiagnostic) error).getCause() != null) {
                    ((CrmlDiagnostic) error).getCause().printStackTrace(System.out);
                }
            }
        }
    }
}
