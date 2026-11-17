package org.openmodelica.crml.language.workspace;

import org.eclipse.emf.ecore.resource.Resource;

/**
 * A problem found while loading or linking a {@link CrmlResource}: a syntax
 * error, a DOM building failure, an unresolved reference or an exception
 * thrown by a stage. Kept in {@link Resource#getErrors()} /
 * {@link Resource#getWarnings()}.
 */
public class CrmlDiagnostic implements Resource.Diagnostic {

    /** What kind of problem it is, so tests can match on it. */
    public enum Kind {
        /** Rejected by the parser. */
        SYNTAX,
        /** Building the DOM failed. */
        DOM_BUILD,
        /** The linker found nothing in scope for a name. */
        UNRESOLVED_REFERENCE,
        /** The linker failed with an exception. */
        LINKING,
        AMBIGUOUS_REFERENCE,
        DUPLICATE_DECLARATION,
        /** Operator-call resolution failed, e.g. an ambiguous call. */
        OPERATOR_CALL,
        /** Linking the lifted values failed. */
        LIFTING,
        /**
         * A dependency is declared by a file that has syntax or build errors.
         * Reported by validation, on the dependent; the dependency's own
         * errors are not repeated.
         */
        BROKEN_DEPENDENCY
    }

    private final Kind kind;
    private final String message;
    private final String location;
    private final int line;
    private final int column;
    private final Throwable cause;

    public CrmlDiagnostic(Kind kind, String message, String location, int line, int column) {
        this(kind, message, location, line, column, null);
    }

    /**
     * @param cause the exception behind this diagnostic, kept for reports
     *        and {@code --stacktrace}; may be null
     */
    public CrmlDiagnostic(Kind kind, String message, String location, int line, int column, Throwable cause) {
        this.kind = kind;
        this.message = message;
        this.location = location;
        this.line = line;
        this.column = column;
        this.cause = cause;
    }

    /** A diagnostic for an exception, without a source position. */
    public static CrmlDiagnostic of(Kind kind, Throwable cause, String location) {
        String message = cause.getMessage() != null ? cause.getMessage() : cause.toString();
        return new CrmlDiagnostic(kind, message, location, 0, 0, cause);
    }

    public Kind getKind() { return kind; }

    /** The exception behind this diagnostic, or null if there is none. */
    public Throwable getCause() { return cause; }

    @Override public String getMessage() { return message; }
    @Override public String getLocation() { return location; }
    @Override public int getLine() { return line; }
    @Override public int getColumn() { return column; }

    @Override
    public String toString() {
        return location + ":" + line + ":" + column + ": " + kind + ": " + message;
    }
}
