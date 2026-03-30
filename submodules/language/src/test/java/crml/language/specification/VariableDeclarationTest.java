package crml.language.specification;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

import crml.language.util.Parser;

public class VariableDeclarationTest {
    private final Parser parser = new Parser();

    private Parser.ParserResult parse(String content) {
        return parser.parse("model Foo is {\n" + content + "\n};");
    }

    @Test
    public void testCorrect_multiVariableDeclaration() {
        assertFalse(parse("""
                Integer x1, x2;
            """).syntax().hasErrors());
    }
}
