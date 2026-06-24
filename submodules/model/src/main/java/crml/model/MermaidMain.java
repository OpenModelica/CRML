package crml.model;

import crml.model.language.LanguagePackage;
import io.github.folmate.ecore2mermaid.core.Ecore2Mermaid;
import io.github.folmate.ecore2mermaid.core.GeneratorOptions;
import io.github.folmate.ecore2mermaid.core.MermaidResult;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class MermaidMain {

    public static void main(String[] args) throws IOException {
        String outputPath = args.length > 0 ? args[0] : "crml-diagram.md";

        MermaidResult result = Ecore2Mermaid.fromPackages(
            Arrays.asList(LanguagePackage.eINSTANCE),
            GeneratorOptions.defaults()
        );

        Path output = Paths.get(outputPath);
        Files.createDirectories(output.getParent());
        Files.write(output, result.diagram().getBytes(StandardCharsets.UTF_8));
    }
}
