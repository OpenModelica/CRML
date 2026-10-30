package crml.compiler.crmlcv2;

/**
 * Internal control flow for a construct that cannot be translated in an
 * expression position.
 *
 * <p>An expression hole cannot produce valid Modelica, so the enclosing
 * declaration or equation has to be dropped as a whole. Every value transformer
 * returns an {@link crml.model.modelica.Expression} unconditionally and throws
 * this instead of returning null; the declaration/equation boundary catches it,
 * records the carried {@link Diagnostic} and emits a Placeholder.
 *
 * <p>This never escapes {@link OMCv2}.
 */
public final class UnsupportedConstruct extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final transient Diagnostic diagnostic;

    public UnsupportedConstruct(Diagnostic diagnostic) {
        super(diagnostic.message(), null, false, false);
        this.diagnostic = diagnostic;
    }

    public Diagnostic diagnostic() {
        return diagnostic;
    }
}
