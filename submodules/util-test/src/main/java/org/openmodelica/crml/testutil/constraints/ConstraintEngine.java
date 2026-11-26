package org.openmodelica.crml.testutil.constraints;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.ResourceSet;

/**
 * Evaluates constraint files against EMF models. The engine knows nothing about
 * a particular metamodel: type and feature names are resolved in the packages
 * given to the constructor. See CONSTRAINT_LANGUAGE.md for the language.
 *
 * <pre>{@code
 * ConstraintFile file = ConstraintFile.parse(caseDir.resolve("expected.constraints"));
 * List<ConstraintResult> results = new ConstraintEngine(CRMLPackage.eINSTANCE).evaluate(file, resourceSet);
 * emit(ConstraintTable.of(results), "Constraints");
 * }</pre>
 */
public final class ConstraintEngine {

    private final Binder binder;

    public ConstraintEngine(EPackage... packages) {
        this(Arrays.asList(packages));
    }

    public ConstraintEngine(List<? extends EPackage> packages) {
        this.binder = new Binder(packages);
    }

    /** One result per non-blank line of {@code file}, in file order. */
    public List<ConstraintResult> evaluate(ConstraintFile file, ResourceSet resourceSet) {
        Evaluator evaluator = new Evaluator(binder, resourceSet);
        List<ConstraintResult> results = new ArrayList<>();
        for (ConstraintFile.Line line : file.lines()) {
            results.add(evaluator.evaluate(line));
        }
        return results;
    }
}
