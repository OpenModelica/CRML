package org.openmodelica.crml.compiler.crmlcv2;

import org.eclipse.emf.ecore.EObject;

import org.openmodelica.crml.model.metamodel.BuiltinBinaryOperatorKind;
import org.openmodelica.crml.model.metamodel.BuiltinType;
import org.openmodelica.crml.model.metamodel.BuiltinUnaryOperatorKind;

/**
 * Factories for the recurring {@link Diagnostic} shapes. These replace the
 * exception factories the string generator used in {@code ValueGenUtil}.
 */
public final class Diagnostics {

    private Diagnostics() {
    }

    /** An operand type combination the operator is not defined for: an ill-typed model. */
    public static Diagnostic incompatibleTypes(BuiltinBinaryOperatorKind opType, BuiltinType lt, BuiltinType rt, EObject source) {
        return new Diagnostic(Diagnostic.Severity.ERROR, Diagnostic.Category.INCOMPATIBLE_TYPES, "BinaryOperator." + opType,
            "Operator " + opType + " is not defined for operand types (" + lt + ", " + rt + ")", source);
    }

    public static Diagnostic incompatibleTypes(BuiltinUnaryOperatorKind opType, BuiltinType t, EObject source) {
        return new Diagnostic(Diagnostic.Severity.ERROR, Diagnostic.Category.INCOMPATIBLE_TYPES, "UnaryOperator." + opType,
            "Operator " + opType + " is not defined for operand type " + t, source);
    }

    /** A legal CRML combination with no (correct) Modelica target. */
    public static Diagnostic unsupported(BuiltinBinaryOperatorKind opType, String reason, EObject source) {
        return new Diagnostic(Diagnostic.Severity.WARNING, Diagnostic.Category.UNSUPPORTED, "BinaryOperator." + opType,
            "Cannot generate Modelica for operator " + opType + ": " + reason, source);
    }

    public static Diagnostic unsupported(BuiltinUnaryOperatorKind opType, String reason, EObject source) {
        return new Diagnostic(Diagnostic.Severity.WARNING, Diagnostic.Category.UNSUPPORTED, "UnaryOperator." + opType,
            "Cannot generate Modelica for operator " + opType + ": " + reason, source);
    }

    /** A construct CRML defines but CRMLtoModelica.mo has no (correct) implementation for. */
    public static Diagnostic libraryGap(String constructKind, String reason, EObject source) {
        return new Diagnostic(Diagnostic.Severity.WARNING, Diagnostic.Category.LIBRARY_GAP, constructKind, reason, source);
    }

    /** A construct that is planned but not yet built in this milestone. */
    public static Diagnostic notYetImplemented(String constructKind, String reason, EObject source) {
        return new Diagnostic(Diagnostic.Severity.WARNING, Diagnostic.Category.NOT_YET_IMPLEMENTED, constructKind, reason,
            source);
    }

    public static Diagnostic info(String constructKind, String message, EObject source) {
        return new Diagnostic(Diagnostic.Severity.INFO, Diagnostic.Category.NOTE, constructKind, message, source);
    }

    /** Input that cannot be translated as written, or a defect of the compiler. */
    public static Diagnostic error(String constructKind, String message, EObject source) {
        return new Diagnostic(Diagnostic.Severity.ERROR, Diagnostic.Category.INVALID, constructKind, message, source);
    }
}
