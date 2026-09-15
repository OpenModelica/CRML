package crml.test;

import static j2html.TagCreator.code;
import static j2html.TagCreator.span;
import static j2html.TagCreator.text;

import j2html.tags.DomContent;
import j2html.tags.specialized.CodeTag;

import java.util.Arrays;
import java.util.stream.IntStream;

public class FormatUtil {
    public static CodeTag numcode(String code, int... highlight) {
        String[] lines = code.split("\\R", -1);

        return code()
            .withClass("numbered-code")
            .with(
                IntStream.range(0, lines.length)
                    .mapToObj(i -> {
                        String cssClass = "code-line";

                        // Convert zero-based array index to one-based line number
                        if (Arrays.stream(highlight).anyMatch(line -> line == i + 1)) {
                            cssClass += " highlighted";
                        }

                        return span(text(lines[i]))
                            .withClass(cssClass);
                    })
                    .toArray(DomContent[]::new)
            );
    }
}
