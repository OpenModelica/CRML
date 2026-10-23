package crml.language.dom;

import org.antlr.v4.runtime.tree.ParseTree;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

import crml.language.dom.util.BuildResult;
import crml.language.dom.util.ScopeResolutionOptions;
import crml.model.language.LanguageFactory;
import crml.model.language.LanguagePackage;

public interface BuildContext {
    BuildResult build(ParseTree node);
    <T extends BuildResult> T build(ParseTree node, Class<T> type);

    default LanguageFactory factory(){
        return LanguageFactory.eINSTANCE;
    }

    default LanguagePackage metamodel(){
        return LanguagePackage.eINSTANCE;
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