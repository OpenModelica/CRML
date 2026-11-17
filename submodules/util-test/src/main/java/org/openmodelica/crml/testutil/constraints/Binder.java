package org.openmodelica.crml.testutil.constraints;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;

/**
 * Checks a constraint against the metamodel before it is evaluated: every type
 * name must be a class of the packages, and every step or filter name a feature
 * or parameterless operation of <em>some</em> class. Which class actually has it
 * is only known at evaluation time. This catches typos and renamed features, so
 * that they become {@code error} results instead of silently failing chains.
 */
final class Binder {

    /** A name in a constraint that the metamodel does not know. */
    static final class BindException extends Exception {
        private static final long serialVersionUID = 1L;

        BindException(String message) {
            super(message);
        }
    }

    private final Map<String, List<EClass>> classes = new LinkedHashMap<>();
    private final Set<String> names = new HashSet<>();

    Binder(Collection<? extends EPackage> packages) {
        for (EPackage pkg : packages) {
            collect(pkg);
        }
    }

    private void collect(EPackage pkg) {
        for (EClassifier classifier : pkg.getEClassifiers()) {
            if (!(classifier instanceof EClass)) {
                continue;
            }
            EClass eClass = (EClass) classifier;
            List<EClass> sameName = classes.computeIfAbsent(eClass.getName(), k -> new ArrayList<>());
            if (!sameName.contains(eClass)) {
                sameName.add(eClass);
            }
            for (EStructuralFeature feature : eClass.getEAllStructuralFeatures()) {
                names.add(feature.getName());
            }
            for (EOperation operation : eClass.getEAllOperations()) {
                if (operation.getEParameters().isEmpty()) {
                    names.add(operation.getName());
                    String property = propertyName(operation.getName());
                    if (property != null) {
                        names.add(property);
                    }
                }
            }
        }
        for (EPackage sub : pkg.getESubpackages()) {
            collect(sub);
        }
    }

    /** {@code getKeywords} gives {@code keywords}; any other name gives null. */
    static String propertyName(String operationName) {
        if (operationName.length() > 3 && operationName.startsWith("get")
                && Character.isUpperCase(operationName.charAt(3))) {
            return Character.toLowerCase(operationName.charAt(3)) + operationName.substring(4);
        }
        return null;
    }

    /** The class named {@code name}. */
    EClass type(String name) throws BindException {
        List<EClass> found = classes.get(name);
        if (found == null) {
            throw new BindException("unknown type " + name);
        }
        if (found.size() > 1) {
            throw new BindException("ambiguous type " + name + ": " + found.size() + " packages declare it");
        }
        return found.get(0);
    }

    void check(Ast.Constraint constraint) throws BindException {
        check(constraint.left);
        if (constraint.condition instanceof Ast.IsA) {
            type(((Ast.IsA) constraint.condition).type);
        } else if (constraint.condition instanceof Ast.Compare) {
            Ast.Operand right = ((Ast.Compare) constraint.condition).right;
            if (right instanceof Ast.ChainOperand) {
                check(((Ast.ChainOperand) right).chain);
            }
        }
    }

    private void check(Ast.Chain chain) throws BindException {
        for (Ast.Selector selector : chain.selectors) {
            type(selector.type);
            for (Ast.Filter filter : selector.filters) {
                for (String name : filter.path) {
                    name(name);
                }
            }
        }
        for (Ast.Step step : chain.steps) {
            if (step instanceof Ast.FeatureStep) {
                name(((Ast.FeatureStep) step).name);
            }
        }
    }

    private void name(String name) throws BindException {
        if (!names.contains(name)) {
            throw new BindException("unknown feature " + name + ": no class of the metamodel has it");
        }
    }
}
