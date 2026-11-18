package org.openmodelica.crml.language.scope;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.openmodelica.crml.model.metamodel.Class;
import org.openmodelica.crml.model.metamodel.ConstructorValue;
import org.openmodelica.crml.model.metamodel.NamedEntity;
import org.openmodelica.crml.model.metamodel.UserTypereference;

/**
 * The lookup for a binding of a constructor ({@code Pump { id = 1 }}): the
 * members of the constructor's class, inherited ones included. There is no
 * upward walk: a binding names a member of the class. Empty while the class
 * is not linked yet, so the linker retries later.
 */
public class ConstructorBindingScopeStrategy implements ScopeStrategy {

    @Override
    public List<ScopeEntry> entries(EObject context) {
        Class cls = constructedClass(context);
        if (cls == null) {
            return Collections.emptyList();
        }
        List<ScopeEntry> result = new ArrayList<>();
        for (NamedEntity member : cls.getMembers()) {
            result.add(ScopeEntry.of(member, cls));
        }
        return result;
    }

    /** The class of the nearest constructor containing {@code context}, or null. */
    private static Class constructedClass(EObject context) {
        for (EObject object = context; object != null; object = object.eContainer()) {
            if (object instanceof ConstructorValue) {
                Object type = ((ConstructorValue) object).getDomain();
                if (type instanceof UserTypereference && ((UserTypereference) type).getDomain() instanceof Class) {
                    return (Class) ((UserTypereference) type).getDomain();
                }
                return null;
            }
        }
        return null;
    }
}
