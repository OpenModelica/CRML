package org.openmodelica.crml.compiler.crmlcv2.templates.value;

import java.util.LinkedHashMap;
import java.util.Map;

import org.openmodelica.crml.compiler.crmlcv2.Diagnostics;
import org.openmodelica.crml.compiler.crmlcv2.TransformationContext;
import org.openmodelica.crml.compiler.crmlcv2.templates.Transformer;
import org.openmodelica.crml.compiler.crmlcv2.util.CRMLCompilerException;
import org.openmodelica.crml.model.metamodel.IntegrateValue;
import org.openmodelica.crml.modelica.metamodel.Expression;
import org.openmodelica.crml.modelica.build.Modelica;

/**
 * Integrating a Boolean4 over a period, backed by
 * {@code CRMLtoModelica.Blocks.Integrate}.
 *
 * <p>The block's signature is {@code (r1: Boolean4, r2: CRMLPeriod,
 * a: Boolean4 = Types.Boolean4.true4) -> out: Boolean4}. The third input has a
 * default and is marked FIXME upstream, so it is left unbound.
 */
public final class IntegrateTransformer implements Modelica {

    private final Transformer transformer;

    public IntegrateTransformer(Transformer transformer) {
        this.transformer = transformer;
    }

    public Expression transform(TransformationContext ctx, IntegrateValue integrate) {
        if (integrate.getIntegrand() == null || integrate.getInterval() == null) {
            throw CRMLCompilerException.unsupported(Diagnostics.error("IntegrateValue",
                "an integration is missing its integrand or its interval", integrate));
        }
        Map<String, Expression> inputs = new LinkedHashMap<String, Expression>();
        inputs.put("r1", transformer.values().transform(ctx, integrate.getIntegrand()));
        inputs.put("r2", transformer.values().transform(ctx, integrate.getInterval()));
        return transformer.blocks().instantiate(ctx, libraryRef("CRMLtoModelica.Blocks.Integrate"),
            "integrate", inputs, "out", integrate);
    }
}
