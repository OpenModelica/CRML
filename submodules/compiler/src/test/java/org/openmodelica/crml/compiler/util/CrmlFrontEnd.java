package org.openmodelica.crml.compiler.util;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.openmodelica.crml.compiler.crmlcv2.Diagnostic;
import org.openmodelica.crml.compiler.crmlcv2.OMCv2;
import org.openmodelica.crml.compiler.crmlcv2.TranslationResult;
import org.openmodelica.crml.language.workspace.CrmlDiagnostic;
import org.openmodelica.crml.language.workspace.CrmlDiagnostic.LocationStyle;
import org.openmodelica.crml.language.workspace.CrmlResource;
import org.openmodelica.crml.language.workspace.CrmlWorkspace;
import org.openmodelica.crml.model.metamodel.Model;

/**
 * CRML source to generated Modelica: load the file into a
 * {@link CrmlWorkspace} of its own (parse, build the object model, link,
 * resolve operator calls), then translate with {@link OMCv2}.
 *
 * <p>Shared by the specification report and the verification suite, so both
 * see exactly the same front end. Never throws for a problem in the model:
 * loading and resolving report {@link #diagnostics() diagnostics}, the
 * translation is skipped if there are any, and an exception thrown by the
 * translation is kept as {@link #translationError()}.
 */
public final class CrmlFrontEnd {

    private final Path source;
    private final List<String> syntaxErrors;
    private final List<Resource.Diagnostic> diagnostics;
    private final EObject dom;
    private final TranslationResult translation;
    private final Throwable translationError;
    private final List<CrmlResource> libraries;

    private CrmlFrontEnd(Path source, List<CrmlResource> libraries, List<String> syntaxErrors,
            List<Resource.Diagnostic> diagnostics, EObject dom, TranslationResult translation,
            Throwable translationError) {
        this.source = source;
        this.libraries = Collections.unmodifiableList(libraries);
        this.syntaxErrors = Collections.unmodifiableList(syntaxErrors);
        this.diagnostics = Collections.unmodifiableList(new ArrayList<>(diagnostics));
        this.dom = dom;
        this.translation = translation;
        this.translationError = translationError;
    }

    public static CrmlFrontEnd run(Path source) throws IOException {
        return run(new CrmlWorkspace(), source);
    }

    /**
     * As {@link #run(Path)}, in a workspace of the caller's, e.g. to translate
     * several models of one run against the same loaded libraries. Libraries
     * must already be loaded ({@link CrmlWorkspace#loadLibraryPath()});
     * resolving skips what an earlier run already resolved. With a library
     * path, the {@link #diagnostics()} also hold the problems of the
     * dependencies of {@code source} (missing, broken, ambiguous, cyclic, a
     * library depending on a model); the errors of the other files are not
     * included.
     */
    public static CrmlFrontEnd run(CrmlWorkspace workspace, Path source) throws IOException {
        CrmlResource resource = workspace.load(source);
        List<CrmlResource> libraries = workspace.resources();
        libraries.remove(resource);
        List<String> syntaxErrors = new ArrayList<>();
        if (resource.getState() == CrmlResource.State.SYNTAX_ERRORS) {
            for (Resource.Diagnostic syntaxError : resource.getErrors()) {
                syntaxErrors.add(CrmlDiagnostic.format(syntaxError, LocationStyle.NONE));
            }
            return new CrmlFrontEnd(source, libraries, syntaxErrors, Collections.<Resource.Diagnostic>emptyList(),
                null, null, null);
        }

        // Natural-language operator calls are parsed as raw Sequences; resolving
        // turns the ones that name an operator in scope into calls.
        workspace.resolve();
        EObject dom = resource.getRoot();
        List<Resource.Diagnostic> diagnostics = new ArrayList<>(resource.getErrors());
        if (!workspace.getLibraryPath().isEmpty()) {
            diagnostics.addAll(dependencyProblems(workspace, resource));
        }
        if (!diagnostics.isEmpty() || !(dom instanceof Model)) {
            return new CrmlFrontEnd(source, libraries, syntaxErrors, diagnostics, dom, null, null);
        }
        try {
            TranslationResult translation = new OMCv2().translateModel((Model) dom);
            return new CrmlFrontEnd(source, libraries, syntaxErrors, diagnostics, dom, translation, null);
        } catch (Throwable e) {
            return new CrmlFrontEnd(source, libraries, syntaxErrors, diagnostics, dom, null, e);
        }
    }

    /**
     * The dependency problems of {@code resource}. The workspace reports them
     * only as part of its validation, wrapped next to the resource's own
     * errors, which are already in {@link #diagnostics()}.
     */
    private static List<Resource.Diagnostic> dependencyProblems(CrmlWorkspace workspace, CrmlResource resource) {
        List<Resource.Diagnostic> problems = new ArrayList<>();
        for (org.eclipse.emf.common.util.Diagnostic child : workspace.validate(resource).getChildren()) {
            List<?> data = child.getData();
            if (child.getSeverity() == org.eclipse.emf.common.util.Diagnostic.ERROR && !data.isEmpty()
                    && data.get(0) instanceof Resource.Diagnostic && !resource.getErrors().contains(data.get(0))) {
                problems.add((Resource.Diagnostic) data.get(0));
            }
        }
        return problems;
    }

    public Path source() {
        return source;
    }

    /**
     * The files loaded from the library path, other than {@link #source()},
     * in load order; empty without a library path. Their DOMs are resolved
     * as far as the run got.
     */
    public List<CrmlResource> libraries() {
        return libraries;
    }

    public boolean hasSyntaxErrors() {
        return !syntaxErrors.isEmpty();
    }

    public List<String> syntaxErrors() {
        return syntaxErrors;
    }

    /** The object model, or null if it could not be built. */
    public EObject dom() {
        return dom;
    }

    /** The object model as a {@link Model}, or null if it is something else. */
    public Model model() {
        return dom instanceof Model ? (Model) dom : null;
    }

    /** The translation, or null if no stage got that far. */
    public TranslationResult translation() {
        return translation;
    }

    /**
     * The errors of loading and resolving the file (building the object
     * model, linking, operator calls, lifting); empty if there was none.
     * Syntax errors are in {@link #syntaxErrors()} instead.
     */
    public List<Resource.Diagnostic> diagnostics() {
        return diagnostics;
    }

    /** What the translation threw, or null. */
    public Throwable translationError() {
        return translationError;
    }

    /** The {@link #diagnostics()} as text, without the file name: one file per run. */
    public String diagnosticText() {
        StringBuilder text = new StringBuilder();
        for (Resource.Diagnostic diagnostic : diagnostics) {
            text.append(text.length() == 0 ? "" : System.lineSeparator())
                .append(CrmlDiagnostic.format(diagnostic, LocationStyle.NONE));
        }
        return text.toString();
    }

    /** True if loading or resolving reported errors, or the translation threw. */
    public boolean failed() {
        return !diagnostics.isEmpty() || translationError != null;
    }

    /** The ERROR diagnostics of the translation; empty if there was none. */
    public List<Diagnostic> errors() {
        List<Diagnostic> errors = new ArrayList<>();
        if (translation != null) {
            for (Diagnostic diagnostic : translation.diagnostics()) {
                if (diagnostic.severity() == Diagnostic.Severity.ERROR) {
                    errors.add(diagnostic);
                }
            }
        }
        return errors;
    }
}
