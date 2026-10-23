package crml.compiler.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Translates every .crml file under a directory with {@link CrmlFrontEnd} and
 * writes, for models/a/B.crml, out/a/B.mo: the text the web build's
 * {@code translate(crml, "")} is expected to return, or a line starting with
 * "ERROR " when the translation fails.
 *
 * <p>The reference that submodules/web/test/compare.mjs checks the web build
 * against.
 */
public final class TranslateModels {

    private TranslateModels() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("usage: TranslateModels <models> <out>");
            System.exit(2);
        }
        Path models = Paths.get(args[0]);
        Path out = Paths.get(args[1]);
        List<Path> files;
        try (Stream<Path> walk = Files.walk(models)) {
            files = walk.filter(p -> p.toString().endsWith(".crml")).sorted().collect(Collectors.toList());
        }
        int translated = 0;
        for (Path file : files) {
            String text = translate(file);
            if (!text.startsWith("ERROR ")) {
                translated++;
            }
            String name = models.relativize(file).toString();
            Path mo = out.resolve(name.substring(0, name.length() - ".crml".length()) + ".mo");
            Files.createDirectories(mo.getParent());
            Files.write(mo, text.getBytes(StandardCharsets.UTF_8));
        }
        System.out.println(files.size() + " models, " + translated + " translated, written to " + out);
    }

    private static String translate(Path file) throws IOException {
        CrmlFrontEnd result = CrmlFrontEnd.run(file);
        if (result.hasSyntaxErrors()) {
            return "ERROR unable to parse\n";
        }
        if (result.error() != null) {
            return "ERROR " + result.error() + "\n";
        }
        if (result.translation() == null) {
            return "ERROR unable to build the object model\n";
        }
        return "within ;\n" + result.translation().text() + "\n";
    }
}
