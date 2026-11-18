package org.openmodelica.crml.language.dom;

import org.antlr.v4.runtime.tree.ParseTree;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.openmodelica.crml.language.dom.util.BuildResult;
import org.openmodelica.crml.language.dom.util.ScopeResolutionOptions;
import org.openmodelica.crml.language.workspace.CrmlDiagnostic;
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
    
    /**
     * Records that the construct at {@code at} is valid CRML that is not
     * supported yet. Building goes on; the file then has no DOM.
     */
    void reportImplementationError(ParseTree at, String message);

    /**
     * Records an error in the model at {@code at}, as a
     * {@link CrmlDiagnostic.Kind#DOM_BUILD} diagnostic at its position.
     * Building goes on, so that one run reports all such errors; the file
     * then has no DOM.
     */
    void reportError(ParseTree at, String message);

    /**
     * Records an error found after building, at the position {@code at} was
     * built from (or that of its closest container that has one).
     */
    void reportError(EObject at, CrmlDiagnostic.Kind kind, String message);

    /**
     * Records that {@code object} was built from {@code source}, for an
     * object a builder creates without {@link #build(ParseTree)}: errors
     * found at it later are reported at that position.
     */
    void position(EObject object, ParseTree source);
}