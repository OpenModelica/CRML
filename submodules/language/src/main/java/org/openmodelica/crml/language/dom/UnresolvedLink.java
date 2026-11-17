package org.openmodelica.crml.language.dom;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.openmodelica.crml.language.dom.util.ScopeResolutionOptions;

/**
 * A reference the linker could not resolve: {@code host.feature} should point
 * to whatever {@code id} names, but nothing in scope does.
 */
public final class UnresolvedLink {
    private final EObject host;
    private final EStructuralFeature feature;
    private final String id;
    private final ScopeResolutionOptions options;

    public UnresolvedLink(EObject host, EStructuralFeature feature, String id, ScopeResolutionOptions options) {
        this.host = host;
        this.feature = feature;
        this.id = id;
        this.options = options;
    }

    /** The object holding the reference. */
    public EObject host() { return host; }

    /** The reference that stayed unset. */
    public EStructuralFeature feature() { return feature; }

    /** The name that could not be resolved. */
    public String id() { return id; }

    /** How the name was looked up; null for the default lookup. */
    public ScopeResolutionOptions options() { return options; }

    @Override
    public String toString() {
        return "'" + id + "' (" + host.eClass().getName() + "." + feature.getName() + ")";
    }
}
