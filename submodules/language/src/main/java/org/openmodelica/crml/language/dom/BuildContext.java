package org.openmodelica.crml.language.dom;

import org.antlr.v4.runtime.tree.ParseTree;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.openmodelica.crml.language.dom.util.BuildResult;
import org.openmodelica.crml.language.dom.util.ScopeResolutionOptions;
import org.openmodelica.crml.model.metamodel.CRMLFactory;
import org.openmodelica.crml.model.metamodel.CRMLPackage;

public interface BuildContext {
    BuildResult build(ParseTree node);
    <T extends BuildResult> T build(ParseTree node, Class<T> type);

    default CRMLFactory factory(){
        return CRMLFactory.eINSTANCE;
    }

    default CRMLPackage metamodel(){
        return CRMLPackage.eINSTANCE;
    }

    void link(EObject host, EStructuralFeature reference, String id, ScopeResolutionOptions options);

    void set(EObject host, EStructuralFeature reference, EObject value);

    default void link(EObject host, EStructuralFeature reference, String id) {
        link(host, reference, id, null);
    };
    
    default void reportImplementationError(String text){
        throw new RuntimeException(text);
    }

    default void reportError(String text){
        throw new RuntimeException(text);
    }
}