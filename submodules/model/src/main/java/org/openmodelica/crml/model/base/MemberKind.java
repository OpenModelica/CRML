package org.openmodelica.crml.model.base;

import org.openmodelica.crml.model.metamodel.Class;
import org.openmodelica.crml.model.metamodel.Element;
import org.openmodelica.crml.model.metamodel.NamedEntity;
import org.openmodelica.crml.model.metamodel.Operator;

/**
 * The kinds of members a {@link Namespace} has. A name hides only members of
 * its own kind: a variable {@code X} hides an inherited variable {@code X},
 * not an inherited class {@code X}.
 */
public enum MemberKind {
    /** Variables, objects and sets: the {@code Element}s, which have values. */
    VALUE,
    /** Classes. */
    TYPE,
    /** Operators and templates. */
    OPERATOR;

    /**
     * The kind of {@code member}.
     *
     * @return the kind, or null for an entity that is none of these
     */
    public static MemberKind of(NamedEntity member) {
        if (member instanceof Element) {
            return VALUE;
        }
        if (member instanceof Class) {
            return TYPE;
        }
        if (member instanceof Operator) {
            return OPERATOR;
        }
        return null;
    }
}
