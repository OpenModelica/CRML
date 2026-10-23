package crml.compiler.crmlcv2;

import org.eclipse.emf.ecore.EObject;

/**
 * One thing the transformation could not do, recorded rather than thrown.
 *
 * <p>The governing rule is that every CRML construct is reached and produces
 * either a correct Modelica AST or a {@code Diagnostic}: nothing throws out of
 * the transformation and nothing is silently skipped.
 *
 * <p>No file/line/column: CRML object-model elements carry no source positions
 * (the DOM builders receive ANTLR contexts and discard them), so the only
 * anchor available is the object-model element itself.
 */
public final class Diagnostic {

    /**
     * How to read a diagnostic's severity.
     *
     * <ul>
     * <li>{@link #ERROR} - the construct does have a Modelica mapping and this
     *     compiler implements it, but this input could not be translated: an
     *     ill-typed model, or a defect in the compiler. An ERROR is a bug
     *     somewhere, which is what the specification tests assert against.
     * <li>{@link #WARNING} - the construct is legal CRML but has no (correct)
     *     target: either CRMLtoModelica.mo has no implementation, or this
     *     milestone has not built the mapping yet. The affected element is
     *     dropped and a Placeholder marks the gap in the generated .mo.
     * <li>{@link #INFO} - translated, but with a caveat worth surfacing.
     * </ul>
     */
    public enum Severity {
        ERROR,
        WARNING,
        INFO
    }

    private final Severity severity;
    private final String message;
    private final EObject crmlSource;
    private final String constructKind;

    public Diagnostic(Severity severity, String constructKind, String message, EObject crmlSource) {
        this.severity = severity;
        this.constructKind = constructKind;
        this.message = message;
        this.crmlSource = crmlSource;
    }

    public Severity severity() {
        return severity;
    }

    public String message() {
        return message;
    }

    /** The CRML object-model element the diagnostic is about; may be null. */
    public EObject crmlSource() {
        return crmlSource;
    }

    /** A stable label for the construct, e.g. "BinaryOperator.AT". */
    public String constructKind() {
        return constructKind;
    }

    @Override
    public String toString() {
        return severity + " " + constructKind + ": " + message;
    }
}
