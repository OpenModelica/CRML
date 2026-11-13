package org.openmodelica.crml.language.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.DiagnosticChain;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EValidator;
import org.eclipse.emf.ecore.impl.EValidatorRegistryImpl;
import org.eclipse.emf.ecore.util.Diagnostician;
import org.eclipse.emf.ecore.util.EObjectValidator;
import org.openmodelica.crml.language.validation.rules.UnnamedOperatorHasKeyword;
import org.openmodelica.crml.model.metamodel.CRMLPackage;

/**
 * {@link EValidator} of the CRML package: plugs CRML's own
 * {@link ValidationRule rules} into the EMF validation framework.
 *
 * <p>For every CRML object, the validator first runs EMF's default constraints
 * (multiplicities, unresolved proxies, ...), then every rule that applies to
 * the object. The {@link Diagnostician} walks the containment tree, so
 * validating the root of a model checks all of it:
 *
 * <pre>{@code
 * Diagnostic result = CRMLValidator.validate(model);
 * if (result.getSeverity() == Diagnostic.ERROR) { ... }
 * }</pre>
 *
 * <p>{@link #validate(EObject)} uses a private registry, so it leaves the
 * global {@link EValidator.Registry#INSTANCE} alone. Call {@link #register()}
 * to make the plain {@link Diagnostician#INSTANCE} use this validator as well.
 */
public class CRMLValidator extends EObjectValidator {

    /** Source of the diagnostics reported by the CRML rules. */
    public static final String DIAGNOSTIC_SOURCE = "org.openmodelica.crml.language";

    /** Shared instance with the {@link #defaultRules() default rules}. */
    public static final CRMLValidator INSTANCE = new CRMLValidator(defaultRules());

    private final List<ValidationRule<?>> rules;

    /** A validator that runs {@code rules} after EMF's default constraints. */
    public CRMLValidator(List<? extends ValidationRule<?>> rules) {
        this.rules = Collections.unmodifiableList(new ArrayList<>(rules));
    }

    /** The rules every CRML model is checked against. */
    public static List<ValidationRule<?>> defaultRules() {
        List<ValidationRule<?>> rules = new ArrayList<>();
        rules.add(new UnnamedOperatorHasKeyword());
        return rules;
    }

    /** The rules this validator runs. */
    public List<ValidationRule<?>> getRules() {
        return rules;
    }

    /** Registers {@link #INSTANCE} for the CRML package in the global registry. */
    public static void register() {
        register(EValidator.Registry.INSTANCE);
    }

    /** Registers {@link #INSTANCE} for the CRML package in {@code registry}. */
    public static void register(EValidator.Registry registry) {
        registry.put(CRMLPackage.eINSTANCE, INSTANCE);
    }

    /**
     * Validates {@code root} and everything it contains with {@link #INSTANCE},
     * without touching the global registry.
     */
    public static Diagnostic validate(EObject root) {
        EValidator.Registry registry = new EValidatorRegistryImpl();
        register(registry);
        return new Diagnostician(registry).validate(root);
    }

    @Override
    protected EPackage getEPackage() {
        return CRMLPackage.eINSTANCE;
    }

    @Override
    public boolean validate(EClass eClass, EObject eObject, DiagnosticChain diagnostics, Map<Object, Object> context) {
        boolean result = validate_EveryDefaultConstraint(eObject, diagnostics, context);
        // Same short cut as EMF's generated validators: without a chain to
        // report to, the first failure decides the verdict.
        for (ValidationRule<?> rule : rules) {
            if (!result && diagnostics == null) {
                break;
            }
            if (rule.appliesTo(eObject)) {
                result &= rule.run(eObject, diagnostics, context);
            }
        }
        return result;
    }

    @Override
    public boolean validate(EDataType eDataType, Object value, DiagnosticChain diagnostics, Map<Object, Object> context) {
        // CRML defines no constraints on its data types yet.
        return true;
    }
}
