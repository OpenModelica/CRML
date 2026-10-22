package crml.compiler.crmlcv2;

import org.eclipse.emf.ecore.EObject;

import crml.model.language.BuiltinBinaryOperatorKind;
import crml.model.language.BuiltinType;
import crml.model.language.BuiltinUnaryOperatorKind;

/**
 * Factories for the recurring {@link Diagnostic} shapes. These replace the
 * exception factories the string generator used in {@code ValueGenUtil}.
 */
public final class Diagnostics {

    private Diagnostics() {
    }

    /** An operand type combination the operator is not defined for: an ill-typed model. */
    public static Diagnostic incompatibleTypes(BuiltinBinaryOperatorKind opType, BuiltinType lt, BuiltinType rt, EObject source) {
        return new Diagnostic(Diagnostic.Severity.ERROR, "BinaryOperator." + opType,
            "Operator " + opType + " is not defined for operand types (" + lt + ", " + rt + ")", source);
    }

    public static Diagnostic incompatibleTypes(BuiltinUnaryOperatorKind opType, BuiltinType t, EObject source) {
        return new Diagnostic(Diagnostic.Severity.ERROR, "UnaryOperator." + opType,
            "Operator " + opType + " is not defined for operand type " + t, source);
    }

    /** A legal CRML combination with no (correct) Modelica target. */
    public static Diagnostic unsupported(BuiltinBinaryOperatorKind opType, String reason, EObject source) {
        return new Diagnostic(Diagnostic.Severity.WARNING, "BinaryOperator." + opType,
            "Cannot generate Modelica for operator " + opType + ": " + reason, source);
    }

    public static Diagnostic unsupported(BuiltinUnaryOperatorKind opType, String reason, EObject source) {
        return new Diagnostic(Diagnostic.Severity.WARNING, "UnaryOperator." + opType,
            "Cannot generate Modelica for operator " + opType + ": " + reason, source);
    }

    /** A construct CRML defines but CRMLtoModelica.mo has no (correct) implementation for. */
    public static Diagnostic libraryGap(String constructKind, String reason, EObject source) {
        return new Diagnostic(Diagnostic.Severity.WARNING, constructKind, reason, source);
    }

    /** A construct that is planned but not yet built in this milestone. */
    public static Diagnostic notYetImplemented(String constructKind, String reason, EObject source) {
        return new Diagnostic(Diagnostic.Severity.WARNING, constructKind, reason, source);
    }

    public static Diagnostic info(String constructKind, String message, EObject source) {
        return new Diagnostic(Diagnostic.Severity.INFO, constructKind, message, source);
    }

    public static Diagnostic error(String constructKind, String message, EObject source) {
        return new Diagnostic(Diagnostic.Severity.ERROR, constructKind, message, source);
    }
}
