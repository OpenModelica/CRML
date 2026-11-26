package org.openmodelica.crml.language.workspace;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;

/**
 * Creates a {@link CrmlResource} for {@code .crml} files.
 *
 * <p>Registered only on the resource set of a {@link CrmlWorkspace}, never in
 * the global {@link Resource.Factory.Registry#INSTANCE}, so that tests and
 * the language server do not share hidden state.
 */
public class CrmlResourceFactory implements Resource.Factory {

    /** The file extension this factory is registered for. */
    public static final String EXTENSION = "crml";

    @Override
    public Resource createResource(URI uri) {
        return new CrmlResource(uri);
    }
}
