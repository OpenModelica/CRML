package org.openmodelica.crml.compiler.crmlcv2;

import org.eclipse.emf.ecore.EObject;

/**
 * One thing the transformation could not do, recorded rather than thrown.
 *
 * <p>The governing rule is that every CRML construct is reached and produces
 * either a correct Modelica AST or a {@code Diagnostic}: nothing throws out of
 * the transformation and nothing is silently skipped.
 *
 * <p>It is anchored at the CRML object that triggered it, its
 * {@link #crmlSource()}. Where that object is in the CRML text is not stored
 * here: it is looked up when needed through the source map of the object's
 * resource ({@code SourcePosition.of}). An ERROR on an object loaded from a
 * file is also recorded on that file's resource, as a {@code TRANSLATION}
 * error (see {@link OMCv2#translateModel}).
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

    public Diagnostic(Severity severity, Category category, String constructKind, String message,
            EObject crmlSource) {
        this.severity = severity;
        this.category = category;
        this.constructKind = constructKind;
        this.message = message;
        this.crmlSource = crmlSource;
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
        return new Diagnostic(severity, category, constructKind, newMessage, newSource);
    }

    /** The same diagnostic with another severity. */
    public Diagnostic withSeverity(Severity newSeverity) {
        return new Diagnostic(newSeverity, category, constructKind, message, crmlSource);
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

    /** {@code SEVERITY kind (CATEGORY): message}. */
    @Override
    public String toString() {
        return severity + " " + constructKind + " (" + category + "): " + message;
    }
}
