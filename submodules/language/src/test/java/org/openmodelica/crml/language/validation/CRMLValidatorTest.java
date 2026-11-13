package org.openmodelica.crml.language.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.ecore.EObject;
import org.junit.jupiter.api.Test;
import org.openmodelica.crml.language.dom.DOMVisitor;
import org.openmodelica.crml.language.dom.util.BuildResult;
import org.openmodelica.crml.language.util.Parser;
import org.openmodelica.crml.language.validation.rules.UnnamedOperatorHasKeyword;
import org.openmodelica.crml.model.metamodel.CRMLPackage;
import org.openmodelica.crml.model.metamodel.CustomOperator;

/** {@link CRMLValidator} on models built from CRML text. */
public class CRMLValidatorTest {

    private static EObject build(String body) {
        Parser.ParserResult parsed = new Parser().parse("model M is {\n" + body + "\n};");
        assertFalse(parsed.syntax().hasErrors(), "syntax errors: " + parsed.syntax().errors());
        BuildResult result = new DOMVisitor().build(parsed.ast());
        return ((BuildResult.SingleBuildResult<?>) result).result();
    }

    /** The diagnostics reported by the CRML rules (not EMF's default constraints). */
    private static List<Diagnostic> crmlFindings(Diagnostic root) {
        List<Diagnostic> found = new ArrayList<>();
        for (Diagnostic d : root.getChildren()) {
            if (CRMLValidator.DIAGNOSTIC_SOURCE.equals(d.getSource())) {
                found.add(d);
            }
        }
        return found;
    }

    @Test
    void unnamedOperatorWithoutKeywordIsAnError() {
        EObject model = build("Operator [ Boolean ] Boolean a Boolean b = a and b;");

        List<Diagnostic> findings = crmlFindings(CRMLValidator.validate(model));

        assertEquals(1, findings.size(), findings.toString());
        Diagnostic d = findings.get(0);
        assertEquals(Diagnostic.ERROR, d.getSeverity());
        assertEquals(UnnamedOperatorHasKeyword.CODE, d.getCode());
        assertTrue(d.getData().get(0) instanceof CustomOperator);
        assertSame(CRMLPackage.Literals.CUSTOM_OPERATOR__HEADER, d.getData().get(1));
    }

    @Test
    void unnamedOperatorWithKeywordIsValid() {
        EObject model = build("Operator [ Boolean ] Boolean a 'with' Boolean b = a and b;");
        assertTrue(crmlFindings(CRMLValidator.validate(model)).isEmpty());
    }

    @Test
    void namedOperatorWithoutKeywordIsValid() {
        EObject model = build("Operator conj is Operator [ Boolean ] (Boolean a, Boolean b) = a and b;");
        assertTrue(crmlFindings(CRMLValidator.validate(model)).isEmpty());
    }
}
