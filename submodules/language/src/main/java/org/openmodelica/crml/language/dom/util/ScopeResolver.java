package org.openmodelica.crml.language.dom.util;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.openmodelica.crml.language.dom.util.ScopeResolutionOptions.ResolutionStrategy;
import org.openmodelica.crml.language.scope.ConstructorBindingScopeStrategy;
import org.openmodelica.crml.language.scope.Resolution;
import org.openmodelica.crml.language.scope.ScopeStrategy;
import org.openmodelica.crml.language.scope.Scopes;

/**
 * Links a reference of the DOM: resolves the name with {@link Scopes} and,
 * if it denotes exactly one declaration, sets the reference to it.
 */
public class ScopeResolver {
    private final Scopes scopes;
    private final ScopeStrategy constructorBinding = new ConstructorBindingScopeStrategy();

    public ScopeResolver() {
        this(new Scopes());
    }

    public ScopeResolver(Scopes scopes) {
        this.scopes = scopes;
    }

    /**
     * Resolves {@code id} for {@code host.feature} and sets the reference if
     * it was {@link Resolution#found() found}; a many-valued reference gets
     * the declaration added.
     *
     * @return the resolution, whether found or not
     */
    @SuppressWarnings("unchecked")
    public Resolution link(EObject host, EStructuralFeature feature, String id, ScopeResolutionOptions options) {
        Resolution resolution = scopes.resolve(host, (EReference) feature, id, strategy(options));
        if (resolution.found()) {
            if (feature.isMany()) {
                ((EList<EObject>) host.eGet(feature)).add(resolution.target());
            } else {
                host.eSet(feature, resolution.target());
            }
        }
        return resolution;
    }

    private ScopeStrategy strategy(ScopeResolutionOptions options) {
        if (options != null && options.strategy == ResolutionStrategy.CONSTRUCTOR_BINDING) {
            return constructorBinding;
        }
        return scopes.upward();
    }
}
