package org.openmodelica.crml.language.scope;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.openmodelica.crml.model.base.CrmlObject;
import org.openmodelica.crml.model.base.Namespace;
import org.openmodelica.crml.model.base.NamespaceSupport;
import org.openmodelica.crml.model.metamodel.CustomOperator;
import org.openmodelica.crml.model.metamodel.Element;
import org.openmodelica.crml.model.metamodel.NamedEntity;

/**
 * The default lookup: the members of the namespace the context is in, then
 * those of the namespaces around it, up to the root. A member is hidden by a
 * member of a nearer namespace with the same hiding key and kind
 * ({@link NamespaceSupport#hides(NamedEntity, NamedEntity)}); members of the
 * same namespace never hide each other, so two of the same name are both
 * listed. The root's members include those of its dependencies.
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
     * The members visible from {@code start}, itself included, and the
     * namespaces around it.
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
                if (member != self) {
                    result.add(ScopeEntry.of(member, namespace));
                }
            }
            nearer.addAll(level);
        }
        return result;
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
