package org.openmodelica.crml.model.base;

import org.eclipse.emf.common.util.EList;
import org.openmodelica.crml.model.metamodel.NamedEntity;

/**
 * A CRML element that declares names: a {@code Class}, a {@code Library}
 * (and so a {@code Model}) or a {@code CustomOperator}. Mixed into the
 * metamodel with {@code interface Namespace wraps ...} in {@code crml.xcore},
 * so it is also an EClass.
 *
 * <p>The names follow UML: {@link #getOwnedMembers() owned members} are
 * declared here, {@link #getInheritedMembers() inherited members} come from
 * the {@link #getBaseNamespaces() base namespaces}, and
 * {@link #getMembers() members} are both. The metamodel only says what a
 * namespace has; which declaration a reference denotes is decided by the
 * scope resolver of the language module.
 */
public interface Namespace extends CrmlObject {

    /**
     * The members declared directly in this namespace, in declaration order.
     * Implemented per type in {@code crml.xcore}.
     */
    EList<NamedEntity> getOwnedMembers();

    /**
     * The namespaces this one inherits members from, in declaration order:
     * the superclasses of a class, the libraries and models a root depends
     * on. A dependency that is not bound is left out. Implemented per type
     * in {@code crml.xcore}.
     */
    EList<Namespace> getBaseNamespaces();

    /**
     * The members inherited from the {@link #getBaseNamespaces() base
     * namespaces}, transitively, that no owned member of the same name and
     * {@link MemberKind kind} hides. A base namespace passes on its own
     * members and the inherited ones it does not redeclare. A member reached
     * along several paths is listed once; two different members with the
     * same name and kind are both listed (for the resolver, an ambiguity).
     * Cycles are cut.
     *
     * @return an unmodifiable list, nearest bases first
     */
    default EList<NamedEntity> getInheritedMembers() {
        return NamespaceSupport.inheritedMembers(this);
    }

    /**
     * The {@link #getOwnedMembers() owned} followed by the
     * {@link #getInheritedMembers() inherited} members.
     *
     * @return an unmodifiable list
     */
    default EList<NamedEntity> getMembers() {
        return NamespaceSupport.members(this);
    }
}
