package org.openmodelica.crml.language.validation.rules;

import java.util.Map;

import org.eclipse.emf.common.util.DiagnosticChain;
import org.openmodelica.crml.language.validation.ValidationRule;
import org.openmodelica.crml.model.metamodel.CRMLPackage;
import org.openmodelica.crml.model.metamodel.CustomOperator;

/**
 * An operator without a name must have at least one keyword in its header.
 *
 * <p>An unnamed operator can only be called in natural-language (mixfix)
 * syntax, and the mixfix parser needs a keyword to anchor a call on; an
 * operator whose header is only parameters is skipped by it
 * ({@link org.openmodelica.crml.language.opcall.OperatorUtil#isMixfix}).
 * Without a name to call it by either, such an operator can never be used:
 *
 * <pre>
 * Operator [ Boolean ] Boolean a Boolean b = a and b;   // error
 * Operator [ Boolean ] Boolean a 'with' Boolean b = a and b;
 * Operator conj is Operator [ Boolean ] (Boolean a, Boolean b) = a and b;
 * </pre>
 *
 * Applies to every {@link CustomOperator}, so to Templates as well, which are
 * always unnamed.
 */
public class UnnamedOperatorHasKeyword extends ValidationRule<CustomOperator> {

    /** Diagnostic code of this rule. */
    public static final int CODE = 1;

    public UnnamedOperatorHasKeyword() {
        super(CustomOperator.class, CODE);
    }

    @Override
    public boolean check(CustomOperator operator, DiagnosticChain diagnostics, Map<Object, Object> context) {
        if (operator.getName() != null || !operator.getKeywords().isEmpty()) {
            return true;
        }
        return error(diagnostics,
            "An operator must have either a name or at least one keyword in its header, otherwise it cannot be called.",
            operator, null);
    }
}
