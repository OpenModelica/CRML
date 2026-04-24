package crml.language;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import crml.language.util.Parser;
import crml.language.util.Parser.ParserResult;

public class SimpleParseTest {

    private Path resource(String name) throws URISyntaxException {
        return Paths.get(getClass().getClassLoader().getResource("simple-parse-test/" + name).toURI());
    }

    @Test
    public void testModel() throws URISyntaxException, IOException {
        ParserResult parsed = new Parser().parse(resource("TestModel.crml"));
        parsed.syntax().errors().forEach(System.out::println);
        assertFalse(parsed.syntax().hasErrors());
    }

    @Test
    public void testExample() throws URISyntaxException, IOException {
        ParserResult parsed = new Parser().parse(resource("Example.crml"));
        parsed.syntax().errors().forEach(System.out::println);
        assertFalse(parsed.syntax().hasErrors());
    }
}
