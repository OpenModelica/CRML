package org.openmodelica.crml.language.scope;

import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.openmodelica.crml.model.metamodel.NamedEntity;

/**
 * Where a lookup starts and what it sees. Chosen per reference: the default
 * is {@link UpwardScopeStrategy}; a constructor binding uses
 * {@link ConstructorBindingScopeStrategy}; member access will be another one.
 */
public interface ScopeStrategy {

    /**
     * The declarations visible at {@code context}, nearest first, with hidden
     * ones and the {@link #declaration(EObject) enclosing declaration} left
     * out. Not filtered by kind, without built-ins.
     */
    List<ScopeEntry> entries(EObject context);

    /**
     * The declaration {@code context} is written in, which a name there must
     * not refer to; null if self-reference does not apply.
     */
    default NamedEntity declaration(EObject context) {
        return null;
    }
}
