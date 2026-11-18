package org.openmodelica.crml.language.validation.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.DiagnosticChain;
import org.openmodelica.crml.language.opcall.OperatorUtil;
import org.openmodelica.crml.language.validation.ValidationRule;
import org.openmodelica.crml.language.workspace.SourceMap;
import org.openmodelica.crml.model.base.Namespace;
import org.openmodelica.crml.model.metamodel.CRMLPackage;
import org.openmodelica.crml.model.metamodel.Class;
import org.openmodelica.crml.model.metamodel.CustomOperator;
import org.openmodelica.crml.model.metamodel.Dependency;
import org.openmodelica.crml.model.metamodel.Library;
import org.openmodelica.crml.model.metamodel.LibraryDependency;
import org.openmodelica.crml.model.metamodel.NamedEntity;

/**
 * The names declared in a namespace must be unique (CRML specification,
 * set namespace and union of sets).
 *
 * <p>All kinds of members share one namespace: a variable, an object, a set,
 * a class and a named operator of the same name conflict. Operators are
 * values of the type {@code Operator} in CRML, so an operator {@code f} and
 * a variable {@code f} conflict too. Operators that can be called in natural
 * syntax also conflict when their keyword signatures are the same
 * ({@code _ 'and' _}); an unnamed operator conflicts only with other
 * operators, so a variable {@code inside} does not clash with the keyword of
 * an operator {@code _ 'inside' _}.
 *
 * <p>Checked, per namespace:
 * <ul>
 * <li>two of its own members (also two parameters of an operator);</li>
 * <li>for a root, one of its own members and a member of a {@code union}
 *     dependency (the merged set must have unique names);</li>
 * <li>for a root, members of two different dependencies. Reported on the
 *     later dependency, whether the name is used or not. One declaration
 *     reached through two dependencies is not a conflict;</li>
 * <li>for a class with several superclasses, members of two of them.</li>
 * </ul>
 * A member of a class with the name of an inherited one is a redeclaration,
 * which is not checked here. A parameter of an operator may have the name of
 * a declaration around the operator.
 *
 * <p>In a file with syntax errors, a member built from text in which the
 * parser recovered from an error ({@link SourceMap#recovered}) is not
 * checked: it may be made of fragments of the declarations around the error,
 * and would conflict with them.
 *
 * <p>Lookup does not depend on this rule: it still lets an own member hide
 * an inherited one, and reports a name provided by two dependencies as
 * ambiguous where it is used (see {@code NamespaceSupport.hides}). On a model
 * without name conflicts, both give the same result.
 */
// TODO: operator overloading. The specification identifies an operator by
// its typed signature, so operators with the same keywords and different
// parameter types are overloads. Without type inference to choose between
// them at a call, they are reported here as conflicts.
public class NameConflicts extends ValidationRule<Namespace> {

    /** Diagnostic code of this rule. */
    public static final int CODE = 2;

    public NameConflicts() {
        super(Namespace.class, CODE);
    }

    @Override
    public boolean check(Namespace namespace, DiagnosticChain diagnostics, Map<Object, Object> context) {
        boolean valid = checkOwned(namespace, diagnostics);
        if (namespace instanceof Library) {
            valid &= checkDependencies((Library) namespace, diagnostics);
        } else if (namespace instanceof Class) {
            valid &= checkSuperClasses((Class) namespace, diagnostics);
        }
        return valid;
    }

    /** Two own members; reported on the later one. */
    private boolean checkOwned(Namespace namespace, DiagnosticChain diagnostics) {
        boolean valid = true;
        List<NamedEntity> owned = namespace.getOwnedMembers();
        for (int j = 0; j < owned.size(); j++) {
            for (int i = 0; i < j; i++) {
                if (conflict(owned.get(i), owned.get(j))) {
                    valid = error(diagnostics, describe(owned.get(j)) + " conflicts with " + describe(owned.get(i))
                        + ", declared before it in " + describe(namespace), owned.get(j), null);
                    break;
                }
            }
        }
        return valid;
    }

    /**
     * An own member and a member of a dependency, reported on the own member;
     * members of two dependencies, reported on the later dependency.
     */
    private boolean checkDependencies(Library root, DiagnosticChain diagnostics) {
        boolean valid = true;
        List<LibraryDependency> dependencies = new ArrayList<>();
        List<Library> bases = new ArrayList<>();
        for (Dependency dependency : root.getSuperlibs()) {
            if (!(dependency instanceof LibraryDependency)) {
                continue;
            }
            Library base = ((LibraryDependency) dependency).getLibrary();
            if (base == null || base == root || bases.contains(base)) {
                continue; // unbound or a cycle (reported by the workspace), or listed twice (harmless)
            }
            dependencies.add((LibraryDependency) dependency);
            bases.add(base);
        }
        for (NamedEntity own : root.getOwnedMembers()) {
            for (Library base : bases) {
                NamedEntity other = firstConflict(own, base.getMembers());
                if (other != null) {
                    valid = error(diagnostics, describe(own) + " of " + describe(root) + " conflicts with "
                        + describe(other) + " of " + declaring(other)
                        + ": the names of a root and of the libraries it is a union with must be unique",
                        own, null);
                }
            }
        }
        for (int j = 1; j < bases.size(); j++) {
            for (NamedEntity member : bases.get(j).getMembers()) {
                for (int i = 0; i < j; i++) {
                    NamedEntity other = firstConflict(member, bases.get(i).getMembers());
                    if (other != null) {
                        valid = error(diagnostics, "Dependency " + bases.get(j).getName() + " of "
                            + describe(root) + " provides " + describe(member) + " of " + declaring(member)
                            + ", which conflicts with " + describe(other) + " of " + declaring(other)
                            + ", provided by " + bases.get(i).getName(),
                            dependencies.get(j), null);
                        break;
                    }
                }
            }
        }
        return valid;
    }

    /** Members of two superclasses; reported on the class. */
    private boolean checkSuperClasses(Class cls, DiagnosticChain diagnostics) {
        boolean valid = true;
        List<Class> bases = new ArrayList<>();
        for (Class base : cls.getSuperClasses()) {
            if (base != null && base != cls && !bases.contains(base)) {
                bases.add(base);
            }
        }
        for (int j = 1; j < bases.size(); j++) {
            for (NamedEntity member : bases.get(j).getMembers()) {
                for (int i = 0; i < j; i++) {
                    NamedEntity other = firstConflict(member, bases.get(i).getMembers());
                    if (other != null) {
                        valid = error(diagnostics, describe(cls) + " inherits " + describe(member) + " from "
                            + bases.get(j).getName() + " and " + describe(other) + " from " + bases.get(i).getName(),
                            cls, CRMLPackage.Literals.CLASS__SUPER_CLASSES);
                        break;
                    }
                }
            }
        }
        return valid;
    }

    private static NamedEntity firstConflict(NamedEntity member, List<NamedEntity> others) {
        for (NamedEntity other : others) {
            if (conflict(member, other)) {
                return other;
            }
        }
        return null;
    }

    /**
     * True if {@code a} and {@code b} cannot both be members of one
     * namespace: different declarations with the same name, or two operators
     * callable in natural syntax with the same keyword signature.
     */
    static boolean conflict(NamedEntity a, NamedEntity b) {
        if (a == b || recovered(a) || recovered(b)) {
            return false;
        }
        if (a.getName() != null && a.getName().equals(b.getName())) {
            return true;
        }
        return a instanceof CustomOperator && b instanceof CustomOperator
            && OperatorUtil.isMixfix((CustomOperator) a) && OperatorUtil.isMixfix((CustomOperator) b)
            && ((CustomOperator) a).getSignature().equals(((CustomOperator) b).getSignature());
    }

    /** True if {@code member} was built from text in which the parser recovered from an error. */
    private static boolean recovered(NamedEntity member) {
        SourceMap map = SourceMap.of(member);
        return map != null && map.recovered(member);
    }

    /** E.g. "Variable x", "UserOperator _ 'and' _". */
    private static String describe(Object member) {
        if (!(member instanceof NamedEntity)) {
            return String.valueOf(member);
        }
        NamedEntity entity = (NamedEntity) member;
        String name = entity.getName();
        if (name == null && entity instanceof CustomOperator) {
            name = ((CustomOperator) entity).getSignature();
        }
        return entity.eClass().getName() + " " + name;
    }

    /** The namespace declaring {@code member}, e.g. "Library L". */
    private static String declaring(NamedEntity member) {
        Namespace namespace = member.getNamespace();
        return namespace == null ? "?" : describe(namespace);
    }
}
