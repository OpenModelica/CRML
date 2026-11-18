package org.openmodelica.crml.language.scope;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.openmodelica.crml.model.base.CrmlObject;
import org.openmodelica.crml.model.base.MemberKind;
import org.openmodelica.crml.model.base.Namespace;
import org.openmodelica.crml.model.base.NamespaceSupport;
import org.openmodelica.crml.model.metamodel.CustomOperator;
import org.openmodelica.crml.model.metamodel.Element;
import org.openmodelica.crml.model.metamodel.NamedEntity;
import org.openmodelica.crml.model.metamodel.Variable;

/**
 * The default lookup: the members of the namespace the context is in, then
 * those of the namespaces around it, up to the root. A member is hidden by a
 * member of a nearer namespace with the same hiding key and kind
 * ({@link NamespaceSupport#hides(NamedEntity, NamedEntity)}); members of the
 * same namespace never hide each other, so two of the same name are both
 * listed. The root's members include those of its dependencies.
 *
 * <p>Visibility follows Modelica, since a class becomes a nested Modelica
 * model and an operator a nested function or block:
 * <ul>
 * <li>the starting namespace gives all its members, its own and its
 *     inherited ones (the variables of a class and of its superclasses, the
 *     parameters of an operator, everything a root and its dependencies
 *     declare);</li>
 * <li>a namespace around it gives its classes, its operators and its
 *     constants. Its other variables, objects and sets are returned
 *     {@linkplain ScopeEntry#restricted restricted}
 *     ({@link AccessRestriction#ENCLOSING_NON_CONSTANT}): they cannot be used,
 *     but still hide farther members of their name, so that a name resolves
 *     to "not accessible" rather than to something farther away.</li>
 * </ul>
 * Other strategies, such as access to the members of a component, can reuse
 * {@link #entriesFrom(Namespace, NamedEntity)} or the same split between
 * the starting namespace and the ones around it.
 */
public class UpwardScopeStrategy implements ScopeStrategy {

    @Override
    public List<ScopeEntry> entries(EObject context) {
        if (!(context instanceof CrmlObject)) {
            return Collections.emptyList();
        }
        return entriesFrom(((CrmlObject) context).getNamespace(), declaration(context));
    }

    /**
     * The nearest {@code Element} (variable, object, set) or
     * {@code CustomOperator} containing {@code context}, or {@code context}
     * itself if it is one: a name written in it must not refer to it.
     */
    @Override
    public NamedEntity declaration(EObject context) {
        for (EObject object = context; object != null; object = object.eContainer()) {
            if (object instanceof Element || object instanceof CustomOperator) {
                return (NamedEntity) object;
            }
        }
        return null;
    }

    /**
     * The members found from {@code start}, itself included, and the
     * namespaces around it, nearest first; those that cannot be used from
     * {@code start} are {@linkplain ScopeEntry#accessible() restricted}.
     *
     * @param self the declaration to leave out; it still hides farther
     *        members of its name, so a self-reference is not resolved to an
     *        outer declaration. May be null.
     */
    public List<ScopeEntry> entriesFrom(Namespace start, NamedEntity self) {
        List<ScopeEntry> result = new ArrayList<>();
        List<NamedEntity> nearer = new ArrayList<>();
        for (Namespace namespace = start; namespace != null; namespace = namespace.getNamespace()) {
            List<NamedEntity> level = new ArrayList<>();
            for (NamedEntity member : namespace.getMembers()) {
                if (!hidden(member, nearer)) {
                    level.add(member);
                }
            }
            for (NamedEntity member : level) {
                if (member == self) {
                    continue;
                }
                AccessRestriction restriction = namespace == start ? null : restriction(member);
                result.add(restriction == null
                    ? ScopeEntry.of(member, namespace)
                    : ScopeEntry.restricted(member, namespace, restriction));
            }
            nearer.addAll(level);
        }
        return result;
    }

    /**
     * Why {@code member} of a namespace around the starting one cannot be
     * used by name; null if it can. Classes, operators and constants can.
     */
    // TODO: constants of a library. The CRML specification lets a library
    // declare constants (constant Real pi is 3.141592;), which this rule
    // would make visible in the classes and operators of the library and of
    // its dependents. The metamodel has no variables in a Library yet and
    // RootBuilder rejects them, so only the constants of a model are seen.
    protected AccessRestriction restriction(NamedEntity member) {
        if (MemberKind.of(member) != MemberKind.VALUE) {
            return null;
        }
        if (member instanceof Variable && Boolean.TRUE.equals(((Variable) member).getConstant())) {
            return null;
        }
        return AccessRestriction.ENCLOSING_NON_CONSTANT;
    }

    private static boolean hidden(NamedEntity member, List<NamedEntity> nearer) {
        for (NamedEntity near : nearer) {
            if (NamespaceSupport.hides(near, member)) {
                return true;
            }
        }
        return false;
    }
}
