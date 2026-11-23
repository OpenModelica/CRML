package org.openmodelica.crml.compiler.crmlcv2;

import org.eclipse.emf.ecore.EObject;

/**
 * One thing the transformation could not do, recorded rather than thrown.
 *
 * <p>The governing rule is that every CRML construct is reached and produces
 * either a correct Modelica AST or a {@code Diagnostic}: nothing throws out of
 * the transformation and nothing is silently skipped.
 *
 * <p>It is anchored at the CRML object that triggered it. An ERROR on an
 * object loaded from a file is also recorded on that file's resource, as a
 * {@code TRANSLATION} error at the object's position, which this diagnostic
 * then carries too (see {@link OMCv2#translateModel}).
 */
public final class Diagnostic {

    /**
     * How to read a diagnostic's severity: what it means for the translation.
     * Why the construct could not be translated is its {@link Category}.
     *
     * <ul>
     * <li>{@link #ERROR} - the translation is incomplete or wrong. Every
     *     element that is dropped is an ERROR, whatever its category: a
     *     Placeholder marks the gap in the generated .mo, and an UNSUPPORTED
     *     trace link leads from the CRML object that triggered it.
     * <li>{@link #WARNING} - a construct could not be translated as asked, but
     *     nothing was dropped. A WARNING raised for a construct becomes an
     *     ERROR when the element containing it is dropped.
     * <li>{@link #INFO} - translated, but with a caveat worth surfacing.
     * </ul>
     */
    public enum Severity {
        ERROR,
        WARNING,
        INFO
    }

    /** Why a construct could not be translated, whatever the severity says of the outcome. */
    public enum Category {
        /** An operand type combination the operator is not defined for: an ill-typed model. */
        INCOMPATIBLE_TYPES,
        /** A legal CRML combination with no (correct) Modelica target. */
        UNSUPPORTED,
        /** A construct CRML defines but CRMLtoModelica.mo has no (correct) implementation for. */
        LIBRARY_GAP,
        /** A construct that is planned but not built yet. */
        NOT_YET_IMPLEMENTED,
        /** Input that cannot be translated as written, or a defect of the compiler. */
        INVALID,
        /** Translated, with a caveat worth surfacing. */
        NOTE
    }

    private final Severity severity;
    private final Category category;
    private final String message;
    private final EObject crmlSource;
    private final String constructKind;
    private final String location;
    private final int line;
    private final int column;

    public Diagnostic(Severity severity, Category category, String constructKind, String message,
            EObject crmlSource) {
        this(severity, category, constructKind, message, crmlSource, null, 0, 0);
    }

    private Diagnostic(Severity severity, Category category, String constructKind, String message,
            EObject crmlSource, String location, int line, int column) {
        this.severity = severity;
        this.category = category;
        this.constructKind = constructKind;
        this.message = message;
        this.crmlSource = crmlSource;
        this.location = location;
        this.line = line;
        this.column = column;
    }

    public Severity severity() {
        return severity;
    }

    /** Why the construct could not be translated. */
    public Category category() {
        return category;
    }

    /** The same diagnostic with another message and CRML source, e.g. to say what was dropped because of it. */
    public Diagnostic withMessage(String newMessage, EObject newSource) {
        return new Diagnostic(severity, category, constructKind, newMessage, newSource, location, line, column);
    }

    /** The same diagnostic with another severity. */
    public Diagnostic withSeverity(Severity newSeverity) {
        return new Diagnostic(newSeverity, category, constructKind, message, crmlSource, location, line, column);
    }

    /** The same diagnostic at a source position: the file, and the line and column of the CRML source. */
    public Diagnostic withPosition(String newLocation, int newLine, int newColumn) {
        return new Diagnostic(severity, category, constructKind, message, crmlSource, newLocation, newLine,
            newColumn);
    }

    /** True if the position of the CRML source in its file is known. */
    public boolean hasPosition() {
        return line > 0;
    }

    /** The file of the CRML source, as it was loaded; null if unknown. */
    public String location() {
        return location;
    }

    /** The line of the CRML source, from 1; 0 if unknown. */
    public int line() {
        return line;
    }

    /** The column of the CRML source, from 0; 0 if unknown. */
    public int column() {
        return column;
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

    /** {@code [file:line:column: ]SEVERITY kind (CATEGORY): message}, the file by its name only. */
    @Override
    public String toString() {
        String position = hasPosition() ? fileName(location) + ":" + line + ":" + column + ": " : "";
        return position + severity + " " + constructKind + " (" + category + "): " + message;
    }

    private static String fileName(String path) {
        if (path == null) {
            return "";
        }
        int slash = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        return path.substring(slash + 1);
    }
}
