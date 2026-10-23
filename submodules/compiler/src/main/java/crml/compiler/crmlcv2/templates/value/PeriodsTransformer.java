package crml.compiler.crmlcv2.templates.value;

import crml.compiler.crmlcv2.Diagnostics;
import crml.compiler.crmlcv2.TransformationContext;
import crml.compiler.crmlcv2.UnsupportedConstruct;
import crml.compiler.crmlcv2.templates.Transformer;
import crml.compiler.crmlcv2.util.TypeResolver;
import crml.model.language.BuiltinType;
import crml.model.language.PeriodsValue;
import crml.model.modelica.Expression;
import crml.modelica.build.Modelica;

/**
 * A CRML period literal to a {@code CRMLtoModelica.Types.CRMLPeriod} component
 * and its {@code CRMLPeriod_build} companion.
 *
 * <p>One CRML class covers two Modelica targets. A literal whose boundaries are
 * clocks - "Periods P is [ c1, c2 ]" - is a {@code CRMLPeriods}, whose
 * {@code start_event} and {@code close_event} are {@code Types.CRMLClock}; a
 * literal whose boundaries are events - "Period P is [ e1, e2 ]" - is a
 * {@code CRMLPeriod}, whose two boundary fields are {@code Types.Event}. Which
 * one is decided from the boundary values' types.
 *
 * <p>The string generator's {@code PeriodsGen} was dead code and wrong in three
 * ways; none of them is reproduced here:
 *
 * <ul>
 * <li>it named the component from {@code periods.hashCode()}, which makes the
 *     output non-deterministic. The name now comes from the context's
 *     allocator, which is scoped per class and counts from one.
 * <li>it concatenated the type name and the variable name with no separator,
 *     producing {@code CRMLtoModelica.Types.CRMLPeriodp12345(}. The component is
 *     now an object-model node, so the printer decides the spacing.
 * <li>it named the modifier {@code start=}, but the field in
 *     {@code CRMLtoModelica.mo} is {@code start_event} - and it passed a Boolean
 *     expression where {@code start_event} and {@code close_event} are of type
 *     {@code Types.Event}, so each boundary condition is wrapped in an Event
 *     construction first.
 * </ul>
 */
public final class PeriodsTransformer implements Modelica {

    private final Transformer transformer;

    public PeriodsTransformer(Transformer transformer) {
        this.transformer = transformer;
    }

    /**
     * A CRMLPeriod boundary field is a {@code Types.Event}. A boundary that
     * already is one goes in as it is; a Boolean condition is wrapped in an
     * Event construction first, which is the third of PeriodsGen's defects.
     */
    private Expression asEvent(TransformationContext ctx, Expression boundary,
            BuiltinType boundaryType, PeriodsValue periods) {
        if (boundaryType == BuiltinType.BOOLEAN || boundaryType == BuiltinType.REQUIREMENT) {
            return transformer.records().event(ctx, boundary, periods);
        }
        return boundary;
    }

    public Expression transform(TransformationContext ctx, PeriodsValue periods) {
        if (periods.getStartValue() == null || periods.getEndValue() == null) {
            throw new UnsupportedConstruct(Diagnostics.error("PeriodsValue",
                "a period literal is missing one of its boundary conditions", periods));
        }

        boolean leftIncluded = Boolean.TRUE.equals(periods.getIsStartInclusive());
        boolean rightIncluded = Boolean.TRUE.equals(periods.getIsEndInclusive());

        BuiltinType startType = TypeResolver.inferBuiltin(periods.getStartValue());
        BuiltinType endType = TypeResolver.inferBuiltin(periods.getEndValue());

        Expression start = transformer.values().transform(ctx, periods.getStartValue());
        Expression end = transformer.values().transform(ctx, periods.getEndValue());

        // Either boundary being a clock settles it: type inference resolves a
        // plain variable reference but not a computed boundary such as
        // "ev + d", so requiring both would fall through to the wrong record.
        if (startType == BuiltinType.CLOCK || endType == BuiltinType.CLOCK) {
            return transformer.records().periods(ctx, leftIncluded, rightIncluded, start, end, periods);
        }
        return transformer.records().period(ctx, leftIncluded, rightIncluded,
            asEvent(ctx, start, startType, periods), asEvent(ctx, end, endType, periods), periods);
    }
}
