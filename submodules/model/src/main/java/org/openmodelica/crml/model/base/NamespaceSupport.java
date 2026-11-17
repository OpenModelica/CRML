package org.openmodelica.crml.model.base;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.ECollections;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.openmodelica.crml.model.metamodel.Class;
import org.openmodelica.crml.model.metamodel.CustomOperator;
import org.openmodelica.crml.model.metamodel.Dependency;
import org.openmodelica.crml.model.metamodel.Library;
import org.openmodelica.crml.model.metamodel.LibraryDependency;
import org.openmodelica.crml.model.metamodel.Model;
import org.openmodelica.crml.model.metamodel.NamedEntity;
import org.openmodelica.crml.model.metamodel.Operator;
import org.openmodelica.crml.model.metamodel.Variable;

/**
 * The implementation of {@link CrmlObject}, {@link Namespace} and the
 * namespace operations of {@code crml.xcore}, which call these methods.
 *
 * <p>Nothing is cached: the linker changes superclasses and dependencies
 * while it runs, so every call computes its result from the current DOM.
 */
public final class NamespaceSupport {

    private NamespaceSupport() {
    }

    // ---------------------------------------------------------------------
    // CrmlObject
    // ---------------------------------------------------------------------

    /** See {@link CrmlObject#getNamespace()}. */
    public static Namespace namespaceOf(EObject object) {
        for (EObject container = object.eContainer(); container != null; container = container.eContainer()) {
            if (container instanceof Namespace) {
                return (Namespace) container;
            }
        }
        return null;
    }

    // ---------------------------------------------------------------------
    // Owned members, per type
    // ---------------------------------------------------------------------

    /** The variables of a class. */
    public static EList<NamedEntity> ownedMembers(Class cls) {
        return unmodifiable(cls.getVariables());
    }

    /** The classes, then the operators of a library. */
    public static EList<NamedEntity> ownedMembers(Library library) {
        List<NamedEntity> members = new ArrayList<>();
        members.addAll(library.getClasses());
        members.addAll(library.getOperators());
        return unmodifiable(members);
    }

    /** The members of a library, then the variables, objects and sets of a model. */
    public static EList<NamedEntity> ownedMembers(Model model) {
        List<NamedEntity> members = new ArrayList<>(ownedMembers((Library) model));
        members.addAll(model.getVariables());
        members.addAll(model.getObjects());
        members.addAll(model.getSets());
        return unmodifiable(members);
    }

    /** The parameters of an operator: the variables of its header. */
    public static EList<NamedEntity> ownedMembers(CustomOperator operator) {
        return unmodifiable(operator.getVariables());
    }

    // ---------------------------------------------------------------------
    // Base namespaces, per type
    // ---------------------------------------------------------------------

    /** The superclasses of a class, unset ones left out. */
    public static EList<Namespace> baseNamespaces(Class cls) {
        List<Namespace> bases = new ArrayList<>();
        for (Class superClass : cls.getSuperClasses()) {
            if (superClass != null) {
                bases.add(superClass);
            }
        }
        return unmodifiable(bases);
    }

    /** The libraries and models a root depends on, unbound dependencies left out. */
    public static EList<Namespace> baseNamespaces(Library library) {
        List<Namespace> bases = new ArrayList<>();
        for (Dependency dependency : library.getSuperlibs()) {
            if (dependency instanceof LibraryDependency) {
                Library bound = ((LibraryDependency) dependency).getLibrary();
                if (bound != null) {
                    bases.add(bound);
                }
            }
        }
        return unmodifiable(bases);
    }

    /** None: an operator inherits nothing. */
    public static EList<Namespace> baseNamespaces(CustomOperator operator) {
        return ECollections.emptyEList();
    }

    // ---------------------------------------------------------------------
    // Inherited members, generic
    // ---------------------------------------------------------------------

    /** See {@link Namespace#getInheritedMembers()}. */
    public static EList<NamedEntity> inheritedMembers(Namespace namespace) {
        Set<Namespace> path = new HashSet<>();
        path.add(namespace);
        return unmodifiable(visible(namespace.getOwnedMembers(), inherited(namespace, path)));
    }

    /** See {@link Namespace#getMembers()}. */
    public static EList<NamedEntity> members(Namespace namespace) {
        List<NamedEntity> members = new ArrayList<>(namespace.getOwnedMembers());
        members.addAll(namespace.getInheritedMembers());
        return unmodifiable(members);
    }

    /**
     * What the bases of {@code namespace} pass on, before hiding by its own
     * members: per base, its owned members and the inherited ones it does not
     * hide. {@code path} holds the namespaces being visited, to cut cycles; a
     * member reached along several paths is kept once.
     */
    private static Set<NamedEntity> inherited(Namespace namespace, Set<Namespace> path) {
        Set<NamedEntity> result = new LinkedHashSet<>();
        for (Namespace base : namespace.getBaseNamespaces()) {
            if (!path.add(base)) {
                continue; // a cycle; reported by validation
            }
            List<NamedEntity> owned = base.getOwnedMembers();
            result.addAll(owned);
            result.addAll(visible(owned, inherited(base, path)));
            path.remove(base);
        }
        return result;
    }

    /** The members of {@code inherited} that no member of {@code owned} hides. */
    private static List<NamedEntity> visible(List<NamedEntity> owned, Iterable<NamedEntity> inherited) {
        List<NamedEntity> result = new ArrayList<>();
        for (NamedEntity member : inherited) {
            if (!hides(owned, member)) {
                result.add(member);
            }
        }
        return result;
    }

    /** True if a member of {@code owned} has the name and kind of {@code member}. */
    private static boolean hides(List<NamedEntity> owned, NamedEntity member) {
        MemberKind kind = MemberKind.of(member);
        for (NamedEntity own : owned) {
            if (own != member && Objects.equals(own.getName(), member.getName()) && MemberKind.of(own) == kind) {
                return true;
            }
        }
        return false;
    }

    // ---------------------------------------------------------------------
    // Typed accessors
    // ---------------------------------------------------------------------

    /** The variables of a class, then the inherited ones it does not redeclare. */
    public static EList<Variable> allVariables(Class cls) {
        return ofType(cls.getMembers(), Variable.class);
    }

    /** The variables of a model, then those of the models it depends on that it does not redeclare. */
    public static EList<Variable> allVariables(Model model) {
        return ofType(model.getMembers(), Variable.class);
    }

    /** The classes of a library, then those of its dependencies that it does not redeclare. */
    public static EList<Class> allClasses(Library library) {
        return ofType(library.getMembers(), Class.class);
    }

    /** The operators of a library, then those of its dependencies that it does not redeclare. */
    public static EList<Operator> allOperators(Library library) {
        return ofType(library.getMembers(), Operator.class);
    }

    /**
     * The variable {@code name} of a class: its own, else the first inherited
     * one.
     *
     * @return the variable, or null if the class has none of that name
     */
    public static Variable variable(Class cls, String name) {
        for (Variable variable : allVariables(cls)) {
            if (Objects.equals(name, variable.getName())) {
                return variable;
            }
        }
        return null;
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    private static <T> EList<T> ofType(List<?> members, java.lang.Class<T> type) {
        List<T> result = new ArrayList<>();
        for (Object member : members) {
            if (type.isInstance(member)) {
                result.add(type.cast(member));
            }
        }
        return unmodifiable(result);
    }

    private static <T> EList<T> unmodifiable(List<? extends T> list) {
        return ECollections.unmodifiableEList(new BasicEList<T>(list));
    }
}
