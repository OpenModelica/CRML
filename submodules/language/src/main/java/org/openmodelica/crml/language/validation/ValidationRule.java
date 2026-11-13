package org.openmodelica.crml.language.validation;

import java.util.Map;

import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.DiagnosticChain;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

/**
 * One validation rule over the CRML object model, run by {@link CRMLValidator}
 * on every object that is an instance of {@link #getTarget() its target type}.
 *
 * <p>A rule reports its findings with {@link #error} or {@link #warning}, which
 * follow the EMF conventions: the diagnostic's data is the offending object,
 * then the feature at fault (if any), so tools can locate the problem. When the
 * caller passes no {@link DiagnosticChain}, nothing is reported and only the
 * returned verdict counts.
 *
 * @param <T> type of the objects the rule checks
 */
public abstract class ValidationRule<T extends EObject> {

    private final Class<T> target;
    private final int code;

    /**
     * @param target type of the objects the rule checks; subtypes are checked too
     * @param code   diagnostic code of the rule, unique among the rules of a validator
     */
    protected ValidationRule(Class<T> target, int code) {
        this.target = target;
        this.code = code;
    }

    /** Type of the objects the rule checks. */
    public Class<T> getTarget() {
        return target;
    }

    /** Diagnostic code of the rule, reported in {@link Diagnostic#getCode()}. */
    public int getCode() {
        return code;
    }

    /**
     * True if {@code object} is an instance of {@link #getTarget() the target
     * type} or one of its subtypes. {@link CRMLValidator} calls
     * {@link #check} only on objects for which this returns true.
     *
     * <p>Override to narrow the rule further than its type allows, for example
     * to skip objects in a particular container. The override must still return
     * false for objects that are not instances of the target type, since
     * {@link #check} receives them cast to {@code T}.
     *
     * @param object any object of the model being validated
     * @return whether the rule checks {@code object}
     */
    public boolean appliesTo(EObject object) {
        return target.isInstance(object);
    }

    /**
     * Checks {@code object}, which {@link #appliesTo(EObject) the rule applies to}.
     *
     * @param object      the object to check
     * @param diagnostics where to report findings; may be {@code null}
     * @param context     the validation context shared by all validators of one run
     * @return false if the object violates the rule with severity error
     */
    public abstract boolean check(T object, DiagnosticChain diagnostics, Map<Object, Object> context);

    /** Type-checked entry point for {@link CRMLValidator}. */
    final boolean run(EObject object, DiagnosticChain diagnostics, Map<Object, Object> context) {
        return check(target.cast(object), diagnostics, context);
    }

    /** Reports an error on {@code object} (and {@code feature}, if not null); returns false. */
    protected boolean error(DiagnosticChain diagnostics, String message, EObject object, EStructuralFeature feature) {
        report(diagnostics, Diagnostic.ERROR, message, object, feature);
        return false;
    }

    /** Reports a warning on {@code object} (and {@code feature}, if not null); returns true. */
    protected boolean warning(DiagnosticChain diagnostics, String message, EObject object, EStructuralFeature feature) {
        report(diagnostics, Diagnostic.WARNING, message, object, feature);
        return true;
    }

    private void report(DiagnosticChain diagnostics, int severity, String message, EObject object, EStructuralFeature feature) {
        if (diagnostics == null) {
            return;
        }
        Object[] data = feature == null ? new Object[] { object } : new Object[] { object, feature };
        diagnostics.add(new BasicDiagnostic(severity, CRMLValidator.DIAGNOSTIC_SOURCE, code, message, data));
    }
}
