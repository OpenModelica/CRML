package crml.compiler.crmlcv2.templates.value;

import org.eclipse.emf.ecore.EObject;

import crml.compiler.crmlcv2.TransformationContext;
import crml.compiler.crmlcv2.util.TypeResolver;
import crml.model.language.BuiltinType;
import crml.model.modelica.ComponentDeclaration;
import crml.model.modelica.ComponentReference;
import crml.model.modelica.Expression;
import crml.modelica.build.Modelica;

/**
 * The Clock, Event and Period records of {@code CRMLtoModelica.Types}, each of
 * which is declared together with a companion {@code _build} class that
 * initialises it.
 *
 * <p>Both parts are configured through modifiers rather than wired with
 * equations, so two declarations on the target class are all a construction
 * takes. The instance name comes from the context's allocator, which is scoped
 * per class.
 */
public final class RecordBuild implements Modelica {

    /** {@code CRMLClock cN(b = condition); CRMLClock_build cN_init(clock = cN);} */
    public ComponentReference clock(TransformationContext ctx, Expression condition, EObject crmlSource) {
        return build(ctx, "c", TypeResolver.resolve(BuiltinType.CLOCK),
            "CRMLtoModelica.Types.CRMLClock_build", "clock",
            mod("b", condition), crmlSource);
    }

    /** {@code Event eN(b = condition); CRMLEvent_build eN_init(E = eN);} */
    public ComponentReference event(TransformationContext ctx, Expression condition, EObject crmlSource) {
        return build(ctx, "e", TypeResolver.resolve(BuiltinType.EVENT),
            "CRMLtoModelica.Types.CRMLEvent_build", "E",
            mod("b", condition), crmlSource);
    }

    /**
     * {@code CRMLPeriod pN(isLeftBoundaryIncluded = ..., ..., start_event = ...,
     * close_event = ...); CRMLPeriod_build pN_init(P = pN);}
     *
     * <p>The boundary fields are Booleans and the two event fields are
     * {@code Types.Event} records, so a Boolean condition has to go through
     * {@link #event} first.
     */
    public ComponentReference period(TransformationContext ctx, boolean leftIncluded, boolean rightIncluded,
            Expression startEvent, Expression closeEvent, EObject crmlSource) {
        return build(ctx, "p", TypeResolver.resolve(BuiltinType.PERIOD),
            "CRMLtoModelica.Types.CRMLPeriod_build", "P",
            new crml.model.modelica.ModificationElement[] {
                mod("isLeftBoundaryIncluded", bool(leftIncluded)),
                mod("isRightBoundaryIncluded", bool(rightIncluded)),
                mod("start_event", startEvent),
                mod("close_event", closeEvent)
            }, crmlSource);
    }

    /**
     * {@code CRMLPeriods psN(isLeftBoundaryIncluded = ..., ..., start_event =
     * ..., close_event = ...); CRMLPeriods_build psN_init(ps = psN);}
     *
     * <p>Distinct from {@link #period}: CRMLPeriods' two boundary fields are
     * {@code Types.CRMLClock}, not {@code Types.Event}, which is what a CRML
     * "Periods" literal over clocks needs.
     */
    public ComponentReference periods(TransformationContext ctx, boolean leftIncluded, boolean rightIncluded,
            Expression startClock, Expression closeClock, EObject crmlSource) {
        return build(ctx, "ps", TypeResolver.resolve(BuiltinType.PERIODS),
            "CRMLtoModelica.Types.CRMLPeriods_build", "ps",
            new crml.model.modelica.ModificationElement[] {
                mod("isLeftBoundaryIncluded", bool(leftIncluded)),
                mod("isRightBoundaryIncluded", bool(rightIncluded)),
                mod("start_event", startClock),
                mod("close_event", closeClock)
            }, crmlSource);
    }

    private ComponentReference build(TransformationContext ctx, String prefix, String recordType,
            String buildType, String buildParam, crml.model.modelica.ModificationElement modification,
            EObject crmlSource) {
        return build(ctx, prefix, recordType, buildType, buildParam,
            new crml.model.modelica.ModificationElement[] { modification }, crmlSource);
    }

    private ComponentReference build(TransformationContext ctx, String prefix, String recordType,
            String buildType, String buildParam, crml.model.modelica.ModificationElement[] modifications,
            EObject crmlSource) {
        String name = ctx.allocateName(prefix);

        ComponentDeclaration record = component(libraryRef(recordType), name);
        for (crml.model.modelica.ModificationElement modification : modifications) {
            record.getModifications().add(modification);
        }
        ctx.declare(record, crmlSource);

        ComponentDeclaration companion = component(libraryRef(buildType), name + "_init");
        // A fresh reference per use: these are contained objects, so handing the
        // same node to two parents would silently move it out of the first.
        companion.getModifications().add(mod(buildParam, ref(name)));
        ctx.declare(companion, crmlSource);

        return ref(name);
    }
}
