package org.openmodelica.crml.language.dom;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

/**
 * One applied DOM change: {@code host.feature} was {@code oldValue} and is now
 * {@code newValue}. Operator-call resolution produces these when it replaces a
 * {@code Sequence} with the call it spells out.
 */
public final class DomModification {
    private final EObject host;
    private final EStructuralFeature feature;
    private final Object oldValue;
    private final EObject newValue;

    public DomModification(EObject host, EStructuralFeature feature, Object oldValue, EObject newValue) {
        this.host = host;
        this.feature = feature;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public EObject host() { return host; }
    public EStructuralFeature feature() { return feature; }

    /** The value before the change, e.g. the replaced {@code Sequence}. Detached now. */
    public Object oldValue() { return oldValue; }

    /** The value after the change, e.g. the {@code ComputedValue}. */
    public EObject newValue() { return newValue; }

    @Override
    public String toString() {
        return host.eClass().getName() + "." + feature.getName() + ": " + oldValue + " -> " + newValue;
    }
}
