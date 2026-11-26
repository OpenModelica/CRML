package org.openmodelica.crml.model.base;

import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

/**
 * Root class of every generated CRML implementation class. Extends EMF's
 * default root, so CRML objects behave as before.
 */
public class CrmlObjectImpl extends MinimalEObjectImpl.Container implements CrmlObject {

    protected CrmlObjectImpl() {
        super();
    }

    @Override
    public Namespace getNamespace() {
        return NamespaceSupport.namespaceOf(this);
    }
}
