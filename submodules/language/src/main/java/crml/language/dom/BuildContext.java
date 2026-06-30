package crml.language.dom;

import org.antlr.v4.runtime.tree.ParseTree;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;

import crml.language.dom.util.BuildResult;
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

    void link(EObject host, EStructuralFeature reference, String id, EClass targetType);

    default void link(EObject host, EStructuralFeature reference, String id) {
        link(host, reference, id, null);
    };
    // shared services live here too: factory, symbol table, ParseTreeProperty, error reporter

    public static class Link {
        private final EObject host;
        private final EStructuralFeature reference;
        private final String id;
        private final EClass targetType;

        public Link(EObject host, EReference reference, String id, EClass targetType){
            this.host = host;
            this.reference = reference;
            this.id = id;
            this.targetType = targetType;
        }

        public Link(EObject host, EReference reference, String id){
            this.host = host;
            this.reference = reference;
            this.id = id;
            this.targetType = null;
        }
    }
}