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
import org.openmodelica.crml.language.dom.CRMLLanguageException;
import org.openmodelica.crml.language.workspace.CrmlDiagnostic;
import org.openmodelica.crml.language.workspace.CrmlResource;
import org.openmodelica.crml.language.workspace.CrmlWorkspace;
import org.openmodelica.crml.model.metamodel.Model;

/**
 * CRML source to generated Modelica: load the file into a
 * {@link CrmlWorkspace} of its own (parse, build the object model, link,
 * resolve operator calls), then translate with {@link OMCv2}.
 *
 * <p>Shared by the specification report and the verification suite, so both
 * see exactly the same front end. Never throws for a problem in the model: each
 * stage records what it produced and the first failure, and the later stages
 * are skipped.
 */
public final class CrmlFrontEnd {

    private final Path source;
    private final List<String> syntaxErrors;
    private final EObject dom;
    private final TranslationResult translation;
    private final Throwable error;

    private CrmlFrontEnd(Path source, List<String> syntaxErrors, EObject dom,
            TranslationResult translation, Throwable error) {
        this.source = source;
        this.syntaxErrors = Collections.unmodifiableList(syntaxErrors);
        this.dom = dom;
        this.translation = translation;
        this.error = error;
    }

    public static CrmlFrontEnd run(Path source) throws IOException {
        CrmlWorkspace workspace = new CrmlWorkspace();
        CrmlResource resource = workspace.load(source);
        List<String> syntaxErrors = new ArrayList<>();
        if (resource.getState() == CrmlResource.State.SYNTAX_ERRORS) {
            for (Resource.Diagnostic syntaxError : resource.getErrors()) {
                syntaxErrors.add(String.valueOf(syntaxError));
            }
            return new CrmlFrontEnd(source, syntaxErrors, null, null, null);
        }

        // Natural-language operator calls are parsed as raw Sequences; resolving
        // turns the ones that name an operator in scope into calls.
        workspace.resolve();
        EObject dom = resource.getRoot();
        Throwable error = error(resource.getErrors());
        if (error != null) {
            return new CrmlFrontEnd(source, syntaxErrors, dom, null, error);
        }

        if (!(dom instanceof Model)) {
            return new CrmlFrontEnd(source, syntaxErrors, dom, null, null);
        }
        try {
            TranslationResult translation = new OMCv2().translateModel((Model) dom);
            return new CrmlFrontEnd(source, syntaxErrors, dom, translation, null);
        } catch (Throwable e) {
            return new CrmlFrontEnd(source, syntaxErrors, dom, null, e);
        }
    }

    /**
     * The errors of loading and resolving as one exception, as the front end
     * reported them before it used the workspace: the linker failure for
     * unresolved references, else the exception behind the first error.
     *
     * <p>TODO: replace {@link #error()} with the diagnostics themselves.
     */
    private static Throwable error(List<Resource.Diagnostic> errors) {
        List<Resource.Diagnostic> unresolved = new ArrayList<>();
        for (Resource.Diagnostic error : errors) {
            if (error instanceof CrmlDiagnostic && ((CrmlDiagnostic) error).getKind() == CrmlDiagnostic.Kind.UNRESOLVED_REFERENCE) {
                unresolved.add(error);
            }
        }
        if (!unresolved.isEmpty()) {
            return CRMLLanguageException.linkerFailure(unresolved);
        }
        for (Resource.Diagnostic error : errors) {
            if (error instanceof CrmlDiagnostic && ((CrmlDiagnostic) error).getCause() != null) {
                return ((CrmlDiagnostic) error).getCause();
            }
        }
        return errors.isEmpty() ? null : CRMLLanguageException.inconsistent(errors.get(0).toString());
    }

    public Path source() {
        return source;
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

    /** What the object model build or the translation threw, or null. */
    public Throwable error() {
        return error;
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
