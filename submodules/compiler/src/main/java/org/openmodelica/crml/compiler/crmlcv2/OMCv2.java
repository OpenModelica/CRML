package org.openmodelica.crml.compiler.crmlcv2;

import org.openmodelica.crml.compiler.crmlcv2.templates.Transformer;
import org.openmodelica.crml.model.metamodel.Model;

/**
 * Entry point of the CRML object model to Modelica translation.
 */
public class OMCv2 {

    private final Transformer transformer = new Transformer();

    public TranslationResult translateModel(Model model) {
        TransformationContext ctx = transformer.transform(model);
        return new TranslationResult(ctx.target(), ctx.trace(), ctx.diagnostics());
    }

    public String translate(Model model) {
        return translateModel(model).text();
    }
}
