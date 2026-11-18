package org.openmodelica.crml.language.scope;

import org.openmodelica.crml.model.base.MemberKind;
import org.openmodelica.crml.model.base.Namespace;
import org.openmodelica.crml.model.metamodel.NamedEntity;

/**
 * One declaration available at a point: a member of a namespace, or a
 * built-in type or operator.
 */
public final class ScopeEntry {
    private final String name;
    private final NamedEntity element;
    private final Namespace namespace;
    private final MemberKind kind;

    private ScopeEntry(String name, NamedEntity element, Namespace namespace, MemberKind kind) {
        this.name = name;
        this.element = element;
        this.namespace = namespace;
        this.kind = kind;
    }

    /** {@code member}, available as a member of {@code namespace}. */
    public static ScopeEntry of(NamedEntity member, Namespace namespace) {
        return new ScopeEntry(member.getName(), member, namespace, MemberKind.of(member));
    }

    /** A built-in type or operator. */
    public static ScopeEntry builtIn(String name, MemberKind kind) {
        return new ScopeEntry(name, null, null, kind);
    }

    /** The name, or null for an operator written in natural syntax, which has none. */
    public String name() { return name; }

    /** The declaration; null for a built-in. */
    public NamedEntity element() { return element; }

    /** The namespace it is a member of, owned or inherited; null for a built-in. */
    public Namespace namespace() { return namespace; }

    /** Whether it is a value, a type or an operator. */
    public MemberKind kind() { return kind; }

    public boolean builtIn() { return element == null; }

    @Override
    public String toString() {
        if (builtIn()) {
            return name + " (built-in " + kind + ")";
        }
        String in = namespace instanceof NamedEntity ? " in " + ((NamedEntity) namespace).getName() : "";
        return name + " (" + element.eClass().getName() + in + ")";
    }
}
