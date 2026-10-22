package crml.compiler.util;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.ecore.EObject;

import crml.compiler.crmlcv2.Diagnostic;
import crml.compiler.crmlcv2.OMCv2;
import crml.compiler.crmlcv2.TranslationResult;
import crml.language.dom.DOMVisitor;
import crml.language.dom.util.BuildResult;
import crml.language.util.Parser;
import crml.model.language.Model;

/**
 * CRML source to generated Modelica: parse, build the object model, link,
 * resolve operator calls, translate with {@link OMCv2}.
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
        Parser.ParserResult parsed = new Parser().parse(source);
        List<String> syntaxErrors = new ArrayList<>();
        if (parsed.syntax().hasErrors()) {
            for (Object syntaxError : parsed.syntax().errors()) {
                syntaxErrors.add(String.valueOf(syntaxError));
            }
            return new CrmlFrontEnd(source, syntaxErrors, null, null, null);
        }

        EObject dom = null;
        try {
            DOMVisitor visitor = new DOMVisitor();
            BuildResult result = visitor.build(parsed.ast());
            if (result instanceof BuildResult.SingleBuildResult) {
                dom = ((BuildResult.SingleBuildResult<?>) result).result();
            }
            visitor.linker();
            // Natural-language operator calls are parsed as raw Sequences; this
            // turns the ones that name an operator in scope into calls.
            visitor.resolveOperatorCalls();
        } catch (Throwable e) {
            return new CrmlFrontEnd(source, syntaxErrors, dom, null, e);
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
