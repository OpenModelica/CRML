package org.openmodelica.crml.testutil.constraints;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.emf.common.util.ECollections;
import org.eclipse.emf.common.util.Enumerator;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.openmodelica.crml.testutil.constraints.ConstraintResult.Status;

/**
 * Evaluates bound constraints on one {@link ResourceSet} (see
 * CONSTRAINT_LANGUAGE.md, "Semantics").
 *
 * <p>Following a chain either yields a value set or throws {@link Unresolved},
 * naming the step or definite description that failed. Each object of the
 * quantified set gets a {@link Truth}; the quantifier combines them with the
 * strong Kleene rules. Every object is evaluated, without a short cut, so the
 * report can list all counterexamples.
 */
final class Evaluator {

    /** The value of a predicate that may be undefined. */
    enum Truth { TRUE, FALSE, UNRESOLVED }

    /** A chain that cannot be followed. Internal: always turned into a result. */
    static final class Unresolved extends Exception {
        private static final long serialVersionUID = 1L;

        Unresolved(String reason) {
            super(reason, null, false, false);
        }
    }

    /** A malformed comparison found during evaluation, e.g. a literal against an object. */
    static final class CheckError extends RuntimeException {
        private static final long serialVersionUID = 1L;

        CheckError(String message) {
            super(message, null, false, false);
        }
    }

    private final Binder binder;
    /** Every object of every resource, in resource order and then tree order. */
    private final List<EObject> universe = new ArrayList<>();
    /** For {@code .@referrers}: object to the objects referring to it; built on first use. */
    private Map<EObject, Set<EObject>> referrers;

    Evaluator(Binder binder, ResourceSet resourceSet) {
        this.binder = binder;
        for (Resource resource : new ArrayList<>(resourceSet.getResources())) {
            for (EObject root : resource.getContents()) {
                universe.add(root);
                TreeIterator<EObject> it = root.eAllContents();
                while (it.hasNext()) {
                    universe.add(it.next());
                }
            }
        }
    }

    ConstraintResult evaluate(ConstraintFile.Line line) {
        if (line.constraint == null) {
            return new ConstraintResult(line.number, line.text, Status.ERROR, line.syntaxError);
        }
        try {
            binder.check(line.constraint);
            Outcome outcome = constraint(line.constraint);
            Status status = outcome.truth == Truth.TRUE ? Status.PASS : Status.FAIL;
            String detail = outcome.detail;
            if (outcome.truth == Truth.FALSE) {
                detail = "false: " + detail;
            } else if (outcome.truth == Truth.UNRESOLVED) {
                detail = "unresolved: " + detail;
            }
            return new ConstraintResult(line.number, line.text, status, detail);
        } catch (Binder.BindException | CheckError e) {
            return new ConstraintResult(line.number, line.text, Status.ERROR, e.getMessage());
        } catch (RuntimeException e) {
            // Each stage turns its problems into results: an unexpected exception
            // in the model's own code must not stop the other lines.
            return new ConstraintResult(line.number, line.text, Status.ERROR, "evaluation threw " + e);
        }
    }

    private static final class Outcome {
        final Truth truth;
        final String detail;

        Outcome(Truth truth, String detail) {
            this.truth = truth;
            this.detail = detail;
        }
    }

    // ---------------------------------------------------------------- constraints

    private Outcome constraint(Ast.Constraint c) throws Binder.BindException {
        List<EObject> matched;
        try {
            matched = quantifiedSet(c.left);
        } catch (Unresolved u) {
            return new Outcome(Truth.UNRESOLVED, u.getMessage());
        }
        if (c.condition instanceof Ast.Count) {
            return count(c.left, matched, (Ast.Count) c.condition);
        }
        if (c.quantifier == Ast.Quantifier.PLAIN && matched.size() != 1) {
            return new Outcome(Truth.UNRESOLVED,
                c.left.last() + " matched " + matched.size() + " objects, expected exactly one");
        }
        Tally tally = tally(c, matched);
        switch (c.quantifier) {
            case PLAIN:
                return plain(tally);
            case SOME:
                return some(tally);
            case ALL:
            default:
                return all(tally);
        }
    }

    /** The truth value of P(o) for every o in M, with what the detail needs. */
    private static final class Tally {
        final int size;
        int trues;
        final List<EObject> falses = new ArrayList<>();
        final List<Set<Object>> falseValues = new ArrayList<>();
        final List<String> unresolved = new ArrayList<>();

        Tally(int size) {
            this.size = size;
        }
    }

    /** Evaluates P(o) for every o in {@code matched}, without a short cut. */
    private Tally tally(Ast.Constraint c, List<EObject> matched) throws Binder.BindException {
        Tally tally = new Tally(matched.size());
        // The right operand is the same for every object: evaluate it once.
        Object right = null;
        if (c.condition instanceof Ast.Compare) {
            try {
                right = right(((Ast.Compare) c.condition).right);
            } catch (Unresolved u) {
                // P(o) is unresolved for every o; over an empty M nothing is evaluated.
                for (int i = 0; i < matched.size(); i++) {
                    tally.unresolved.add("right side: " + u.getMessage());
                }
                return tally;
            }
        }
        for (EObject o : matched) {
            Set<Object> values = null;
            try {
                values = steps(o, c.left.steps);
            } catch (Unresolved u) {
                tally.unresolved.add(u.getMessage());
            }
            if (values == null) {
                continue;
            }
            if (condition(c.condition, values, right)) {
                tally.trues++;
            } else {
                tally.falses.add(o);
                tally.falseValues.add(values);
            }
        }
        return tally;
    }

    /** A literal operand as it is; a chain operand as the one value it denotes. */
    private Object right(Ast.Operand operand) throws Unresolved, Binder.BindException {
        if (operand instanceof Ast.Literal) {
            return operand;
        }
        return definite(((Ast.ChainOperand) operand).chain);
    }

    /** The plain form: M has exactly one element, and this is its value. */
    private static Outcome plain(Tally t) {
        if (!t.unresolved.isEmpty()) {
            return new Outcome(Truth.UNRESOLVED, t.unresolved.get(0));
        }
        return t.trues == 1
            ? new Outcome(Truth.TRUE, "")
            : new Outcome(Truth.FALSE, "found " + Describer.describeAll(t.falseValues.get(0)));
    }

    /** ∃, strong Kleene: any true, else any unresolved, else false. */
    private static Outcome some(Tally t) {
        if (t.trues > 0) {
            return new Outcome(Truth.TRUE, "");
        }
        if (!t.unresolved.isEmpty()) {
            return new Outcome(Truth.UNRESOLVED,
                "no witness among " + t.size + " objects; " + unresolvedSummary(t.unresolved));
        }
        return new Outcome(Truth.FALSE,
            t.size == 0 ? "no witness: 0 objects matched" : "no witness among " + t.size + " objects");
    }

    /** ∀, strong Kleene: any false, else any unresolved, else true (vacuously over an empty M). */
    private static Outcome all(Tally t) {
        if (!t.falses.isEmpty()) {
            return new Outcome(Truth.FALSE, counterexamples(t.falses, t.falseValues));
        }
        if (!t.unresolved.isEmpty()) {
            return new Outcome(Truth.UNRESOLVED, unresolvedSummary(t.unresolved));
        }
        return new Outcome(Truth.TRUE, t.size == 0 ? "vacuously true: 0 objects matched" : "");
    }

    private Outcome count(Ast.Chain chain, List<EObject> matched, Ast.Count count) throws Binder.BindException {
        Set<Object> union = new LinkedHashSet<>();
        List<String> unresolved = new ArrayList<>();
        for (EObject o : matched) {
            try {
                union.addAll(steps(o, chain.steps));
            } catch (Unresolved u) {
                unresolved.add(u.getMessage());
            }
        }
        if (!unresolved.isEmpty()) {
            return new Outcome(Truth.UNRESOLVED, unresolvedSummary(unresolved));
        }
        boolean holds = compare(union.size(), count.op, count.n);
        return holds
            ? new Outcome(Truth.TRUE, "")
            : new Outcome(Truth.FALSE, "found " + union.size() + ": " + Describer.describeAll(union));
    }

    private static String counterexamples(List<EObject> objects, List<Set<Object>> values) {
        StringBuilder text = new StringBuilder("counterexamples: ");
        for (int i = 0; i < objects.size() && i < Describer.LIMIT; i++) {
            text.append(i == 0 ? "" : "; ")
                .append(Describer.describe(objects.get(i)))
                .append(" gives ")
                .append(Describer.describeAll(values.get(i)));
        }
        if (objects.size() > Describer.LIMIT) {
            text.append("; … and ").append(objects.size() - Describer.LIMIT).append(" more");
        }
        return text.toString();
    }

    private static String unresolvedSummary(List<String> reasons) {
        return reasons.get(0) + (reasons.size() > 1 ? " (and " + (reasons.size() - 1) + " more unresolved)" : "");
    }

    // ---------------------------------------------------------------- chains

    /** M: the set the last selector denotes, within the scope of the selectors before it. */
    private List<EObject> quantifiedSet(Ast.Chain chain) throws Unresolved, Binder.BindException {
        List<EObject> scope = universe;
        for (Ast.Selector selector : chain.scope()) {
            List<EObject> matched = select(selector, scope);
            if (matched.size() != 1) {
                throw new Unresolved(selector + " matched " + matched.size() + " objects, expected exactly one");
            }
            scope = contents(matched.get(0));
        }
        return select(chain.last(), scope);
    }

    /** A chain as a definite description: it must reach exactly one value. */
    private Object definite(Ast.Chain chain) throws Unresolved, Binder.BindException {
        List<EObject> matched = quantifiedSet(chain);
        if (matched.size() != 1) {
            throw new Unresolved(chain.last() + " matched " + matched.size() + " objects, expected exactly one");
        }
        Set<Object> values = steps(matched.get(0), chain.steps);
        if (values.size() != 1) {
            throw new Unresolved(chain + " gives " + values.size() + " values, expected exactly one");
        }
        return values.iterator().next();
    }

    private static List<EObject> contents(EObject object) {
        List<EObject> contents = new ArrayList<>();
        TreeIterator<EObject> it = object.eAllContents();
        while (it.hasNext()) {
            contents.add(it.next());
        }
        return contents;
    }

    private List<EObject> select(Ast.Selector selector, List<EObject> universe) throws Binder.BindException {
        EClass type = binder.type(selector.type);
        List<EObject> selected = new ArrayList<>();
        for (EObject candidate : universe) {
            if (type.isInstance(candidate) && filters(selector, candidate)) {
                selected.add(candidate);
            }
        }
        return selected;
    }

    /** True only if every filter is true; a path that cannot be followed is not true. */
    private boolean filters(Ast.Selector selector, EObject candidate) throws Binder.BindException {
        for (Ast.Filter filter : selector.filters) {
            Set<Object> values;
            try {
                values = steps(candidate, filter.path);
            } catch (Unresolved u) {
                return false;
            }
            boolean holds;
            switch (filter.op) {
                case "==":
                    holds = values.size() == 1 && same(values.iterator().next(), filter.value);
                    break;
                case "!=":
                    holds = !(values.size() == 1 && same(values.iterator().next(), filter.value));
                    break;
                default:
                    holds = containsSame(values, filter.value);
                    break;
            }
            if (!holds) {
                return false;
            }
        }
        return true;
    }

    /** Applies the steps to {@code {start}}, mapping every value and flattening. */
    private Set<Object> steps(EObject start, List<Ast.Step> steps) throws Unresolved, Binder.BindException {
        Set<Object> current = new LinkedHashSet<>(Collections.singleton(start));
        for (Ast.Step step : steps) {
            Set<Object> next = new LinkedHashSet<>();
            if (step instanceof Ast.FilterStep) {
                filter(((Ast.FilterStep) step).selector, current, next);
            } else {
                for (Object value : current) {
                    apply(step, value, next);
                }
            }
            current = next;
        }
        return current;
    }

    /** A filter step: the values that are objects of the selector's type and pass its filters. */
    private void filter(Ast.Selector selector, Set<Object> values, Set<Object> into) throws Binder.BindException {
        EClass type = binder.type(selector.type);
        for (Object value : values) {
            if (type.isInstance(value) && filters(selector, (EObject) value)) {
                into.add(value);
            }
        }
    }

    private void apply(Ast.Step step, Object value, Set<Object> into) throws Unresolved {
        if (!(value instanceof EObject)) {
            throw new Unresolved("step " + step + " on " + Describer.describe(value) + ": not an object");
        }
        EObject object = (EObject) value;
        if (step instanceof Ast.PseudoStep) {
            pseudo(((Ast.PseudoStep) step).kind, object, into);
            return;
        }
        String name = ((Ast.FeatureStep) step).name;
        EStructuralFeature feature = object.eClass().getEStructuralFeature(name);
        if (feature != null) {
            Object result = object.eGet(feature);
            add(step, object, feature.isMany(), result, into);
            return;
        }
        EOperation operation = operation(object.eClass(), name);
        if (operation == null) {
            throw new Unresolved("step " + step + " on " + Describer.describe(object) + ": no such feature");
        }
        Object result;
        try {
            result = object.eInvoke(operation, ECollections.emptyEList());
        } catch (InvocationTargetException e) {
            throw new Unresolved("step " + step + " on " + Describer.describe(object) + ": operation threw "
                + e.getCause());
        } catch (UnsupportedOperationException e) {
            throw new Unresolved("step " + step + " on " + Describer.describe(object)
                + ": operation cannot be invoked");
        }
        add(step, object, operation.isMany() || result instanceof Collection, result, into);
    }

    private static void add(Ast.Step step, EObject object, boolean many, Object result, Set<Object> into)
            throws Unresolved {
        if (many) {
            into.addAll((Collection<?>) result);
        } else if (result == null) {
            throw new Unresolved("step " + step + " on " + Describer.describe(object) + ": unset");
        } else {
            into.add(result);
        }
    }

    /** The parameterless operation called {@code name}, or {@code getName} for a step {@code name}. */
    private static EOperation operation(EClass eClass, String name) {
        for (EOperation operation : eClass.getEAllOperations()) {
            if (operation.getEParameters().isEmpty()
                    && (operation.getName().equals(name) || name.equals(Binder.propertyName(operation.getName())))) {
                return operation;
            }
        }
        return null;
    }

    private void pseudo(Ast.PseudoStep.Kind kind, EObject object, Set<Object> into) throws Unresolved {
        switch (kind) {
            case REFERRERS:
                into.addAll(referrers().getOrDefault(object, Collections.<EObject>emptySet()));
                return;
            case ROOT:
                if (object.eResource() == null) {
                    throw new Unresolved("step .@root on " + Describer.describe(object) + ": not in a resource");
                }
                into.add(EcoreUtil.getRootContainer(object));
                return;
            case CONTAINER:
                if (object.eContainer() == null) {
                    throw new Unresolved("step .@container on " + Describer.describe(object) + ": no container");
                }
                into.add(object.eContainer());
                return;
            case FILE:
            default:
                String file = Describer.file(object);
                if (file == null) {
                    throw new Unresolved("step .@file on " + Describer.describe(object) + ": not in a resource");
                }
                into.add(file);
                return;
        }
    }

    /**
     * The inverse of every non-containment reference between objects of the
     * universe. Containment and container references are left out, as are
     * derived ones, which only repeat what other features hold. Referrers are
     * in universe order.
     */
    private Map<EObject, Set<EObject>> referrers() {
        if (referrers == null) {
            referrers = new IdentityHashMap<>();
            for (EObject referrer : universe) {
                for (EReference reference : referrer.eClass().getEAllReferences()) {
                    if (reference.isContainment() || reference.isContainer() || reference.isDerived()) {
                        continue;
                    }
                    Object value = referrer.eGet(reference);
                    Collection<?> targets = reference.isMany() ? (Collection<?>) value
                        : value == null ? Collections.emptySet() : Collections.singleton(value);
                    for (Object target : targets) {
                        if (target instanceof EObject && !((EObject) target).eIsProxy()) {
                            referrers.computeIfAbsent((EObject) target, k -> new LinkedHashSet<>()).add(referrer);
                        }
                    }
                }
            }
        }
        return referrers;
    }

    // ---------------------------------------------------------------- conditions

    /** The condition on a resolved value set: two-valued. */
    private boolean condition(Ast.Condition condition, Set<Object> values, Object right)
            throws Binder.BindException {
        if (condition instanceof Ast.IsEmpty) {
            return values.isEmpty() != ((Ast.IsEmpty) condition).negated;
        }
        if (condition instanceof Ast.IsA) {
            EClass type = binder.type(((Ast.IsA) condition).type);
            for (Object value : values) {
                if (!type.isInstance(value)) {
                    return false;
                }
            }
            return true;
        }
        Ast.Compare compare = (Ast.Compare) condition;
        switch (compare.op) {
            case "==":
                return values.size() == 1 && same(values.iterator().next(), right);
            case "!=":
                return !(values.size() == 1 && same(values.iterator().next(), right));
            case "contains":
                return containsSame(values, right);
            case "not contains":
            default:
                return !containsSame(values, right);
        }
    }

    private static boolean containsSame(Collection<Object> values, Object right) {
        for (Object value : values) {
            if (same(value, right)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Whether a value equals the right operand: a literal (converted to the value's
     * type) or the value a definite description denotes (objects by identity).
     */
    private static boolean same(Object value, Object right) {
        if (right instanceof Ast.Literal) {
            return matchesLiteral(value, (Ast.Literal) right);
        }
        if (value instanceof EObject || right instanceof EObject) {
            return value == right;
        }
        if (value instanceof Number && right instanceof Number) {
            return toBigDecimal((Number) value).compareTo(toBigDecimal((Number) right)) == 0;
        }
        return Objects.equals(value, right);
    }

    private static boolean matchesLiteral(Object value, Ast.Literal literal) {
        Object lit = literal.value;
        // Before the object check: in a dynamic model an enum attribute's value is an
        // EEnumLiteral, which is an EObject as well as an Enumerator.
        if (lit instanceof String && value instanceof Enumerator) {
            Enumerator e = (Enumerator) value;
            return lit.equals(e.getName()) || lit.equals(e.getLiteral());
        }
        if (value instanceof EObject) {
            throw new CheckError("cannot compare the literal " + literal + " with the object "
                + Describer.describe(value) + "; compare one of its attributes, e.g. .name");
        }
        if (lit instanceof String) {
            if (value instanceof String) {
                return value.equals(lit);
            }
            if (value instanceof Character) {
                return lit.equals(value.toString());
            }
        } else if (lit instanceof Long) {
            if (value instanceof Number) {
                return toBigDecimal((Number) value).compareTo(BigDecimal.valueOf((Long) lit)) == 0;
            }
        } else if (lit instanceof Boolean) {
            if (value instanceof Boolean) {
                return value.equals(lit);
            }
        }
        throw new CheckError("cannot compare the literal " + literal + " with the "
            + value.getClass().getSimpleName() + " value " + Describer.describe(value));
    }

    private static BigDecimal toBigDecimal(Number n) {
        if (n instanceof BigDecimal) {
            return (BigDecimal) n;
        }
        if (n instanceof BigInteger) {
            return new BigDecimal((BigInteger) n);
        }
        if (n instanceof Double || n instanceof Float) {
            return BigDecimal.valueOf(n.doubleValue());
        }
        return BigDecimal.valueOf(n.longValue());
    }

    private static boolean compare(long actual, String op, long n) {
        switch (op) {
            case "==":
                return actual == n;
            case "!=":
                return actual != n;
            case "<":
                return actual < n;
            case "<=":
                return actual <= n;
            case ">":
                return actual > n;
            case ">=":
            default:
                return actual >= n;
        }
    }
}
