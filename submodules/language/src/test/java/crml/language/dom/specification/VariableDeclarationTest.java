package crml.language.dom.specification;

import static org.junit.jupiter.api.Assertions.*;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.eclipse.emf.ecore.EObject;
import org.junit.jupiter.api.Test;

import crml.language.dom.DOMVisitor;
import crml.language.dom.util.BuildResult;
import crml.language.pretty.PrettyPrint;
import crml.language.util.Parser;

public class VariableDeclarationTest {

    private final Parser parser = new Parser();

    private Parser.ParserResult parse(String content) {
        return parser.parse("model Foo is {\n" + content + "\n};");
    }

    @Test
    public void testCorrect_multiVariableDeclaration() {
        Parser.ParserResult parsed = parse("Integer x1, x2;");
        assertFalse(parsed.syntax().hasErrors(), "Parse should succeed");

        Throwable error = null;
        EObject dom = null;
        try {
            DOMVisitor visitor = new DOMVisitor();
            BuildResult result = visitor.build(parsed.ast());
            if (result instanceof BuildResult.SingleBuildResult) {
                dom = ((BuildResult.SingleBuildResult<?>) result).result();
            }
            visitor.linker();
        } catch (Throwable e) {
            error = e;
        }

        if (dom != null) {
            System.out.println("DOM:\n" + PrettyPrint.prettyPrint(dom));
        }
        if (error != null) {
            StringWriter sw = new StringWriter();
            error.printStackTrace(new PrintWriter(sw));
            System.out.println("Error:\n" + sw);
        }

        assertNull(error, "DOM translation threw an exception");
    }
}
