package org.openmodelica.crml.compiler.crmlcv2;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.openmodelica.crml.compiler.crmlcv2.templates.Transformer;
import org.openmodelica.crml.language.workspace.CrmlDiagnostic.Kind;
import org.openmodelica.crml.language.workspace.CrmlResource;
import org.openmodelica.crml.model.metamodel.Model;

/**
 * Entry point of the CRML object model to Modelica translation.
 */
public class OMCv2 {

    private final Transformer transformer = new Transformer();

    /**
     * Translates {@code model}, also when it is incomplete: what cannot be
     * translated is dropped and diagnosed, and the rest is translated.
     *
     * <p>Every ERROR is marked on the CRML object that triggered it: if the
     * object was loaded from a file, its resource records a
     * {@link Kind#TRANSLATION} error at the object's position.
     */
    public TranslationResult translateModel(Model model) {
        TransformationContext ctx = transformer.transform(model);
        for (Diagnostic diagnostic : ctx.diagnostics()) {
            mark(diagnostic);
        }
        return new TranslationResult(ctx.target(), ctx.trace(), ctx.diagnostics());
    }

    /** Records an ERROR on the resource of its CRML source, if that is a CRML file. */
    private static void mark(Diagnostic diagnostic) {
        EObject source = diagnostic.crmlSource();
        Resource resource = source == null ? null : source.eResource();
        if (diagnostic.severity() == Diagnostic.Severity.ERROR && resource instanceof CrmlResource) {
            ((CrmlResource) resource).reportError(source, Kind.TRANSLATION,
                diagnostic.constructKind() + " (" + diagnostic.category() + "): " + diagnostic.message());
        }
    }

    public String translate(Model model) {
        return translateModel(model).text();
    }
}
