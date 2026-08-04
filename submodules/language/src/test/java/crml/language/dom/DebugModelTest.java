package crml.language.dom;

import java.io.IOException;
import java.nio.file.Path;

import org.eclipse.emf.ecore.EObject;
import org.junit.jupiter.api.Test;

import crml.language.dom.util.BuildResult;
import crml.language.opcall.ai.MixfixParser;
import crml.language.pretty.PrettyPrint;
import crml.language.util.Parser;
import crml.model.language.Model;
import crml.util.SafeResource;

/**
 * Scratch harness for debugging a single CRML model end-to-end.
 * Not part of the specification suite (does not extend ReportedTest / emit
 * into the HTML report) — it just prints everything to stdout.
 *
 * Point it at any file by editing MODEL_RESOURCE, or drop your own model
 * into src/test/resources/debug/debug.crml.
 */
//  ./gradlew :language:clean :language:test --tests "crml.language.dom.DebugModelTest" -i 
public class DebugModelTest {

    private static final String MODEL_RESOURCE = "debug/debug.crml";

    @Test
    public void debugModel() throws IOException {
        Path fileName = SafeResource.get(MODEL_RESOURCE);
        System.out.println("=== Model: " + fileName + " ===");

        Parser.ParserResult parsed = new Parser().parse(fileName);

        System.out.println("--- Syntax Errors ---");
        if (parsed.syntax().hasErrors()) {
            parsed.syntax().errors().forEach(System.out::println);
        } else {
            System.out.println("(none)");
        }

        System.out.println("--- AST ---");
        System.out.println(parsed.toPrettyTree());

        Throwable error = null;
        EObject dom = null;
        try {
            DOMVisitor visitor = new DOMVisitor();
            BuildResult result = visitor.build(parsed.ast());
            if (result instanceof BuildResult.SingleBuildResult) {
                dom = ((BuildResult.SingleBuildResult<?>) result).result();
            }
            visitor.linker();
            Model model = (Model) dom;
            MixfixParser mixfix = new MixfixParser(model, visitor);
            mixfix.perform(model);
            visitor.modify();
        } catch (Throwable e) {
            error = e;
        }

        System.out.println("--- DOM ---");
        System.out.println(PrettyPrint.prettyPrint(dom));

        if (error != null) {
            System.out.println("--- Error ---");
            error.printStackTrace(System.out);
        }
    }
}
