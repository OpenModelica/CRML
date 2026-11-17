package org.openmodelica.crml.language.dom;

import static j2html.TagCreator.pre;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openmodelica.crml.language.dom.util.BuildResult;
import org.openmodelica.crml.language.util.AstWrapper;
import org.openmodelica.crml.language.util.CRMLSyntaxResultsWrapper;
import org.openmodelica.crml.language.util.DOMWrapper;
import org.openmodelica.crml.language.util.Parser;
import org.openmodelica.crml.language.util.ThrowableWrapper;
import org.openmodelica.crml.model.metamodel.CRMLPackage;
import org.openmodelica.crml.testresources.TestResourcesRoot;
import org.openmodelica.crml.testutil.CustomHtmlReporter;
import org.openmodelica.crml.testutil.ModelExpectation;
import org.openmodelica.crml.testutil.ReportedTest;
import org.openmodelica.crml.testutil.constraints.ConstraintEngine;
import org.openmodelica.crml.testutil.constraints.ConstraintFile;
import org.openmodelica.crml.testutil.constraints.ConstraintResult;
import org.openmodelica.crml.testutil.constraints.ConstraintTable;
import org.openmodelica.crml.util.NaturalCompare;

import crml.language.grammar.crmlParser.DefinitionContext;
import crml.language.grammar.crmlParser.DependencyContext;
import crml.language.grammar.crmlParser.DependencySetContext;
import crml.language.grammar.crmlParser.IdContext;
import crml.language.grammar.crmlParser.SingleDependencyContext;

/**
 * Parsing and DOM building of models spread over several files: the cases of
 * {@code testModels/library-loading} in {@code :test-resources}.
 *
 * <p>Each case is a folder; its files are the complete input of the test - the
 * model(s) and every library they depend on. Per case, this test checks:
 * <ol>
 * <li><b>Grammar:</b> every file parses exactly when its {@link ModelExpectation}
 *     marker says it should;</li>
 * <li><b>DOM:</b> every file that parses yields a root (Model or Library), each
 *     in its own {@link Resource}, all in one {@link ResourceSet};</li>
 * <li><b>Fixture completeness:</b> every dependency ({@code L union}) of a file
 *     expected to be correct is declared by a file of the case, so the case
 *     really provides all the models it needs;</li>
 * <li><b>Constraints:</b> if the case has an {@code expected.constraints} file,
 *     every line of it holds on the linked DOM (see {@code CONSTRAINT_LANGUAGE.md}
 *     in {@code :util-test}). This is where a case states what each reference
 *     must point to, e.g. that a call resolves to an operator of a library.</li>
 * </ol>
 *
 * <p>Linking and operator-call resolution are run as well and reported, but not
 * asserted: the linker does not look into other Resources yet, so a reference
 * into a library is expected to stay unresolved for now. Once it does, assert
 * here that correct files link with no unresolved operator call left.
 *
 * <p>Everything is reported at the end, in a fixed order (see {@link #report}),
 * because the report lists sections in the order they are first emitted.
 */
public class LibraryLoadingTest extends ReportedTest {

    static final Path CASES = TestResourcesRoot.RESOURCES.resolve("testModels").resolve("library-loading");

    /** The optional constraint file of a case. */
    static final String CONSTRAINTS = "expected.constraints";

    private static final ConstraintEngine ENGINE = new ConstraintEngine(CRMLPackage.eINSTANCE);

    /** One argument set per case folder: {@code (case name, folder)}. */
    static List<Arguments> cases() throws IOException {
        try (Stream<Path> list = Files.list(CASES)) {
            return list.filter(Files::isDirectory)
                .sorted(new NaturalCompare.PathCompare())
                .map(dir -> Arguments.of(dir.getFileName().toString(), dir))
                .collect(Collectors.toList());
        }
    }

    /** The input files of a case, in natural order. Disabled files are left out. */
    static List<Path> inputs(Path caseDir) throws IOException {
        try (Stream<Path> list = Files.list(caseDir)) {
            return list.filter(Files::isRegularFile)
                .filter(p -> p.getFileName().toString().endsWith(".crml"))
                .sorted(new NaturalCompare.PathCompare())
                .collect(Collectors.toList());
        }
    }

    /** What is known about one input file of a case. */
    private static final class Input {
        final Path file;
        final String label;
        final ModelExpectation expectation;
        Parser.ParserResult parsed;
        /** {@code model}/{@code library}, its name and dependencies; read from the AST. */
        String kind;
        String name;
        final List<String> dependencies = new ArrayList<>();
        DOMVisitor visitor;
        EObject root;
        /** The exception that stopped DOM building, linking or operator-call resolution. */
        Throwable domError;

        Input(Path file) {
            this.file = file;
            this.label = file.getFileName().toString();
            this.expectation = ModelExpectation.fromFileName(label);
        }

        boolean parsed() {
            return parsed != null && !parsed.syntax().hasErrors();
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("cases")
    void parseAndBuild(final String caseName, final Path caseDir) throws IOException {
        List<String> failures = new ArrayList<>();
        List<Input> inputs = new ArrayList<>();
        for (Path file : inputs(caseDir)) {
            inputs.add(new Input(file));
        }
        if (inputs.isEmpty()) {
            failures.add("the case has no .crml file");
        }
        ResourceSet resourceSet = new ResourceSetImpl();
        List<ConstraintResult> constraints = null;

        try {
            // 1. Grammar, and 2. DOM building: one Resource per file, one ResourceSet.
            for (Input input : inputs) {
                input.parsed = new Parser().parse(input.file);
                if (input.parsed() != input.expectation.parses()) {
                    failures.add(input.label + ": expected " + (input.expectation.parses() ? "to parse" : "a syntax error")
                        + ", but it " + (input.parsed() ? "parsed" : "has syntax errors"));
                }
                if (!input.parsed()) {
                    continue;
                }
                readHeader(input);

                try {
                    input.visitor = new DOMVisitor();
                    BuildResult result = input.visitor.build(input.parsed.ast());
                    if (result instanceof BuildResult.SingleBuildResult) {
                        input.root = ((BuildResult.SingleBuildResult<?>) result).result();
                    }
                } catch (Throwable e) {
                    input.domError = e;
                    failures.add(input.label + ": building the DOM threw " + e);
                }
                if (input.root == null) {
                    failures.add(input.label + ": no DOM root was built");
                    continue;
                }
                Resource resource = new ResourceImpl(URI.createFileURI(input.file.toAbsolutePath().toString()));
                resourceSet.getResources().add(resource);
                resource.getContents().add(input.root);
            }

            // 3. Every dependency of a correct file is provided by the case.
            Map<String, Input> declared = new LinkedHashMap<>();
            for (Input input : inputs) {
                if (input.name != null) {
                    declared.putIfAbsent(input.name, input);
                }
            }
            for (Input input : inputs) {
                if (input.expectation != ModelExpectation.CORRECT) {
                    continue;
                }
                for (String dependency : input.dependencies) {
                    if (!declared.containsKey(dependency)) {
                        failures.add(input.label + ": depends on '" + dependency
                            + "', which no file of the case declares");
                    }
                }
            }

            // Linking and operator-call resolution: reported only (see class comment).
            // Run after every Resource exists, as a cross-resource linker will need.
            for (Input input : inputs) {
                if (input.root == null) {
                    continue;
                }
                try {
                    input.visitor.linker();
                    input.visitor.resolveOperatorCalls();
                } catch (Throwable e) {
                    input.domError = e;
                }
            }

            // 4. The case's own constraints on the linked DOM.
            Path constraintFile = caseDir.resolve(CONSTRAINTS);
            if (Files.isRegularFile(constraintFile)) {
                constraints = ENGINE.evaluate(ConstraintFile.parse(constraintFile), resourceSet);
                for (ConstraintResult result : constraints) {
                    if (result.failed()) {
                        failures.add(CONSTRAINTS + " " + result);
                    }
                }
            }
        } finally {
            report(caseDir, inputs, resourceSet, constraints);
        }

        assertTrue(failures.isEmpty(), String.join("\n", failures));
    }

    /**
     * Emits the report of a case: the case folder, the resource set and the
     * constraint results first, then per file its CRML source, syntax errors,
     * AST, DOM and DOM errors. Sections with nothing to show are left out.
     */
    private void report(Path caseDir, List<Input> inputs, ResourceSet resourceSet,
            List<ConstraintResult> constraints) {
        emit(caseDir, "Case folder");
        emit(text(summary(inputs, resourceSet)), "Resource set");
        if (constraints != null) {
            emit(ConstraintTable.of(constraints), "Constraints");
        }
        for (Input input : inputs) {
            emit(input.file, input.label + ": CRML model");
            if (input.parsed == null) {
                continue;
            }
            if (input.parsed.syntax().hasErrors()) {
                emit(CRMLSyntaxResultsWrapper.of(input.parsed.syntax()), input.label + ": syntax errors");
            }
            emit(AstWrapper.of(input.parsed), input.label + ": AST");
            if (input.root != null) {
                emit(DOMWrapper.of(input.root), input.label + ": DOM");
            }
            if (input.domError != null) {
                emit(ThrowableWrapper.of(input.domError), input.label + ": DOM errors");
            }
        }
    }

    /** Fills in the kind, name and dependencies of a file that parsed. */
    private static void readHeader(Input input) {
        if (!(input.parsed.ast() instanceof DefinitionContext)) {
            return;
        }
        DefinitionContext definition = (DefinitionContext) input.parsed.ast();
        input.kind = definition.definition_type().getText();
        input.name = definition.id().getText();
        for (DependencyContext dependency : definition.dependency()) {
            if (dependency instanceof SingleDependencyContext) {
                input.dependencies.add(((SingleDependencyContext) dependency).id().getText());
            } else if (dependency instanceof DependencySetContext) {
                for (IdContext id : ((DependencySetContext) dependency).id()) {
                    input.dependencies.add(id.getText());
                }
            }
        }
    }

    /** One line per input file: what it declares, what it depends on, its Resource. */
    private static String summary(List<Input> inputs, ResourceSet resourceSet) {
        StringBuilder text = new StringBuilder();
        text.append(resourceSet.getResources().size()).append(" resource(s) in the resource set\n");
        for (Input input : inputs) {
            text.append(input.label).append(" [").append(input.expectation.name().toLowerCase()).append("]: ");
            if (!input.parsed()) {
                text.append("syntax errors, not loaded\n");
                continue;
            }
            text.append(input.kind).append(' ').append(input.name);
            if (!input.dependencies.isEmpty()) {
                text.append(" <- ").append(String.join(", ", input.dependencies));
            }
            text.append(input.root != null && input.root.eResource() != null ? "" : " (no resource)");
            text.append('\n');
        }
        return text.toString();
    }

    /** Multi-line text, shown expanded with its line breaks kept. */
    private static CustomHtmlReporter text(String text) {
        return () -> pre(text);
    }
}
