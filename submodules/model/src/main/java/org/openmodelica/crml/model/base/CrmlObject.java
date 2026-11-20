package org.openmodelica.crml.model.base;

import org.eclipse.emf.ecore.EObject;

/**
 * Root interface of every generated CRML interface.
 */
public interface CrmlObject extends EObject {

    /**
     * The namespace this element is in: the nearest {@link Namespace} that
     * strictly contains it, never the element itself. For a class this is
     * the library or model declaring it; for a value, the class, operator or
     * model it is written in.
     *
     * @return the namespace, or null for a root (a {@code Library} or
     *         {@code Model}) and for an element that is not contained in one
     */
    Namespace getNamespace();
}
