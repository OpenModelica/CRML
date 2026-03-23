package crml.language;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Test;

import crml.language.grammar.crmlLexer;
import crml.language.grammar.crmlParser;

public class SimpleParseTest {
    String model = """
model is ???
""";

    @Test
    public void test(){
        crmlLexer lexer = new crmlLexer(CharStreams.fromString(model));
        CommonTokenStream tokens = new CommonTokenStream( lexer );
        crmlParser parser = new crmlParser( tokens );
        //List<String> ruleNamesList = Arrays.asList(parser.getRuleNames());
        ParseTree tree = parser.definition();

        assertNotNull(tree);
    }    
}
