package org.openmodelica.crml.compiler.crmlcv2.specification;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openmodelica.crml.compiler.crmlcv2.Diagnostic;
import org.openmodelica.crml.compiler.crmlcv2.TranslationResult;
import org.openmodelica.crml.compiler.util.CodeWrapper;
import org.openmodelica.crml.compiler.util.CompilerRoot;
import org.openmodelica.crml.compiler.util.CrmlFrontEnd;
import org.openmodelica.crml.compiler.util.DiagnosticsWrapper;
import org.openmodelica.crml.compiler.util.ObjectModelWrapper;
import org.openmodelica.crml.compiler.util.ResourceDiagnosticsWrapper;
import org.openmodelica.crml.compiler.util.ThrowableWrapper;
import org.openmodelica.crml.compiler.util.TraceWrapper;
import org.openmodelica.crml.language.workspace.CrmlResource;
import org.openmodelica.crml.language.workspace.CrmlWorkspace;
import org.openmodelica.crml.language.workspace.RootHeader;
import org.openmodelica.crml.model.metamodel.CRMLPackage;
import org.openmodelica.crml.modelica.metamodel.ModelicaPackage;
import org.openmodelica.crml.modelica.trace.metamodel.TracePackage;
import org.openmodelica.crml.testresources.TestResourcesRoot;
import org.openmodelica.crml.testutil.ModelExpectation;
import org.openmodelica.crml.testutil.ReportedTest;
import org.openmodelica.crml.testutil.constraints.ConstraintEngine;
import org.openmodelica.crml.testutil.constraints.ConstraintFile;
import org.openmodelica.crml.testutil.constraints.ConstraintResult;
import org.openmodelica.crml.testutil.constraints.ConstraintTable;
import org.openmodelica.crml.util.NaturalCompare;

/**
 * Translation of models that depend on libraries in other files: the cases of
 * {@code testModels/library-loading}, one test per case.
 *
 * <p>Per case, every file of the case folder is loaded into one workspace (the
 * folder is the library path) and every valid model of it (no error marker,
 * declares a {@code model}) is translated there. A case with no valid model is
 * left out: libraries and error files cannot be translated on their own, and
 * what such a case tests is checked on the DOM by {@code LibraryLoadingTest}
 * in {@code :language}. For each model, the test asserts:
 * <ul>
 * <li>loading, linking and the dependencies report no error, and the
 *     translation does not throw;</li>
 * <li>the translation records no ERROR diagnostic - unless the model depends,
 *     directly or not, on a library with errors (e.g.
 *     {@code unresolved_name_in_library}): the library's contents are emitted
 *     into the model, so its errors must then be reported as ERRORs.</li>
 * </ul>
 *
 * <p>If the case has a {@value #TRANSLATION_CONSTRAINTS} file, each
 * translation is added to the resource set as {@code <Model>.mo} (the generated
 * class) and {@code <Model>.trace} (the trace), and the file's lines must hold,
 * evaluated over the CRML, Modelica and trace metamodels. That is where a case
 * states which of its elements reach the generated Modelica (see "Checking a
 * translation" in {@code CONSTRAINT_LANGUAGE.md} of {@code :util-test}).
 *
 * <p>The report has one entry per case: the constraint table, then each file's
 * CRML source and DOM, then each model's Modelica, trace and diagnostics.
 */
public class LibraryLoading extends ReportedTest {
    static final Path CASES = TestResourcesRoot.RESOURCES.resolve("testModels").resolve("library-loading");

    /** The optional constraint file of a case on the translation. */
    static final String TRANSLATION_CONSTRAINTS = "translation.constraints";

    private static final ConstraintEngine ENGINE =
        new ConstraintEngine(CRMLPackage.eINSTANCE, ModelicaPackage.eINSTANCE, TracePackage.eINSTANCE);

    /** {@code (case name, folder)} for every case with at least one valid model. */
    static List<Arguments> cases() throws IOException {
        List<Arguments> cases = new ArrayList<>();
        try (Stream<Path> list = Files.list(CASES)) {
            for (Path caseDir : list.filter(Files::isDirectory)
                    .sorted(new NaturalCompare.PathCompare()).collect(Collectors.toList())) {
                if (!validModels(caseDir).isEmpty()) {
                    cases.add(Arguments.of(caseDir.getFileName().toString(), caseDir));
                }
            }
        }
        return cases;
    }

    /** The valid models of one case folder, in natural order. */
    private static List<Path> validModels(Path caseDir) throws IOException {
        List<Path> models = new ArrayList<>();
        for (Path file : files(caseDir)) {
            if (isValidModel(file)) {
                models.add(file);
            }
        }
        return models;
    }

    private static List<Path> files(Path caseDir) {
        List<Path> files = CompilerRoot.fileNameSourceHelper(caseDir);
        files.sort(new NaturalCompare.PathCompare());
        return files;
    }

    /** A file without an error marker that declares a model, not a library. */
    private static boolean isValidModel(Path file) throws IOException {
        if (ModelExpectation.fromFileName(file.getFileName().toString()) != ModelExpectation.CORRECT) {
            return false;
        }
        RootHeader header = new CrmlWorkspace().load(file).getHeader();
        return header != null && header.isModel();
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("cases")
    void translateCase(final String caseName, final Path caseDir) throws IOException {
        CrmlWorkspace workspace = new CrmlWorkspace(Collections.singletonList(caseDir));
        workspace.loadLibraryPath();
        ResourceSet resourceSet = workspace.getResourceSet();
        List<String> failures = new ArrayList<>();

        Map<String, CrmlFrontEnd> runs = new LinkedHashMap<>();
        for (Path model : validModels(caseDir)) {
            String name = model.getFileName().toString().replaceAll("\\.crml$", "");
            CrmlFrontEnd run = CrmlFrontEnd.run(workspace, model);
            runs.put(name, run);
            check(name, run, failures);
            if (run.translation() != null) {
                add(resourceSet, name + ".mo", run.translation().modelica());
                add(resourceSet, name + ".trace", run.translation().trace());
            }
        }

        List<ConstraintResult> constraints = null;
        Path constraintFile = caseDir.resolve(TRANSLATION_CONSTRAINTS);
        if (Files.isRegularFile(constraintFile)) {
            constraints = ENGINE.evaluate(ConstraintFile.parse(constraintFile), resourceSet);
            for (ConstraintResult result : constraints) {
                if (result.failed()) {
                    failures.add(TRANSLATION_CONSTRAINTS + " " + result);
                }
            }
        }

        report(caseDir, workspace, runs, constraints);
        assertTrue(failures.isEmpty(), String.join("\n", failures));
    }

    /** The assertions on one model's run, as failure messages. */
    private static void check(String name, CrmlFrontEnd run, List<String> failures) {
        if (run.hasSyntaxErrors()) {
            failures.add(name + ": syntax errors: " + String.join("; ", run.syntaxErrors()));
            return;
        }
        if (!run.diagnostics().isEmpty()) {
            failures.add(name + ": building the object model reported errors: " + run.diagnosticText());
        }
        if (run.translationError() != null) {
            failures.add(name + ": the translation threw " + run.translationError());
        }
        if (run.translation() == null) {
            return;
        }
        List<Diagnostic> errors = run.errors();
        if (dependsOnFaultyLibrary(run)) {
            if (errors.isEmpty()) {
                failures.add(name + ": depends on a library with errors, but the translation recorded no ERROR");
            }
        } else if (!errors.isEmpty()) {
            failures.add(name + ": the translation recorded ERROR diagnostics: " + errors);
        }
    }

    /** True if a root the model depends on, directly or not, has errors. */
    private static boolean dependsOnFaultyLibrary(CrmlFrontEnd run) {
        Map<String, CrmlResource> byName = new HashMap<>();
        for (CrmlResource library : run.libraries()) {
            if (library.getHeader() != null) {
                byName.put(library.getHeader().name(), library);
            }
        }
        CrmlResource model = run.dom() == null ? null : (CrmlResource) run.dom().eResource();
        return model != null && reachesFaultyLibrary(model.getHeader(), byName, new HashSet<String>());
    }

    private static boolean reachesFaultyLibrary(RootHeader header, Map<String, CrmlResource> byName,
            Set<String> visited) {
        if (header == null) {
            return false;
        }
        for (RootHeader.DependencyName dependency : header.dependencies()) {
            CrmlResource library = byName.get(dependency.name());
            if (library == null || !visited.add(dependency.name())) {
                continue;
            }
            if (!library.getErrors().isEmpty() || reachesFaultyLibrary(library.getHeader(), byName, visited)) {
                return true;
            }
        }
        return false;
    }

    /** Puts {@code root} into a new resource named {@code name}, so constraints can reach it. */
    private static void add(ResourceSet resourceSet, String name, EObject root) {
        Resource resource = new ResourceImpl(URI.createURI(name));
        resourceSet.getResources().add(resource);
        resource.getContents().add(root);
    }

    /**
     * The report of a case, in a fixed order: the case folder and the
     * constraint table, then each file of the case (source, DOM, errors),
     * then each model's translation. Sections with nothing to show are left out.
     */
    private void report(Path caseDir, CrmlWorkspace workspace, Map<String, CrmlFrontEnd> runs,
            List<ConstraintResult> constraints) {
        emit(caseDir, "Case folder");
        if (constraints != null) {
            emit(ConstraintTable.of(constraints), "Translation constraints");
        }
        for (CrmlResource resource : workspace.resources()) {
            String file = resource.getURI().lastSegment();
            emit(caseDir.resolve(file), file + ": CRML source");
            if (resource.getRoot() != null) {
                emit(ObjectModelWrapper.of(resource.getRoot()), file + ": DOM");
            }
            if (!resource.getErrors().isEmpty()) {
                emit(ResourceDiagnosticsWrapper.of(resource.getErrors()), file + ": errors");
            }
        }
        for (Map.Entry<String, CrmlFrontEnd> entry : runs.entrySet()) {
            String model = entry.getKey();
            CrmlFrontEnd run = entry.getValue();
            if (!run.diagnostics().isEmpty()) {
                emit(ResourceDiagnosticsWrapper.of(run.diagnostics()), model + ": errors before translation");
            }
            TranslationResult result = run.translation();
            if (result != null) {
                emit(CodeWrapper.of(result.text()), model + ".mo");
                emit(TraceWrapper.of(result.trace()), model + ".trace");
                emit(DiagnosticsWrapper.of(result.diagnostics()), model + ": translation diagnostics");
            }
            if (run.translationError() != null) {
                emit(ThrowableWrapper.of(run.translationError()), model + ": translation error");
            }
        }
    }
}
