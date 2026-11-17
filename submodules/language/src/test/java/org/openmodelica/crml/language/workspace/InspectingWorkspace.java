package org.openmodelica.crml.language.workspace;

import java.nio.file.Path;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import org.openmodelica.crml.language.opcall.MixfixParser;
import org.openmodelica.crml.language.workspace.StageResult.CallResult;
import org.openmodelica.crml.language.workspace.StageResult.LiftResult;
import org.openmodelica.crml.language.workspace.StageResult.LinkResult;

/**
 * A {@link CrmlWorkspace} for tests: records what each stage of
 * {@link #resolve()} did per resource, can stop after a given stage, and can
 * run a callback between stages.
 *
 * <pre>{@code
 * InspectingWorkspace ws = InspectingWorkspace.upTo(Stage.LINK);
 * CrmlResource r = ws.load(file);
 * ws.resolve();
 * ws.trace(r).link.unresolved();   // still a Sequence: operator calls were not resolved
 * }</pre>
 *
 * <p>Stages can only be cut off at the end: the stages depend on the ones
 * before them. A resource whose resolution was cut off stays
 * {@link CrmlResource.State#BUILT built}.
 *
 * <p>The trace refers to the live DOM: what a stage returned may be changed
 * by the next one. Look at the DOM between stages with the callback, or copy
 * it there.
 */
public class InspectingWorkspace extends CrmlWorkspace {

    /** The stages, in the order they run. Building happens while loading. */
    public enum Stage {
        BUILD, LINK, OPERATOR_CALLS, LIFT
    }

    /** What the stages did to one resource; a field stays null if its stage did not run. */
    public static final class Trace {
        public LinkResult link;
        public MixfixParser parser;
        public CallResult calls;
        public LiftResult lift;
    }

    private final Stage last;
    private final BiConsumer<Stage, Trace> afterStage;
    private final Map<CrmlResource, Trace> traces = new IdentityHashMap<>();

    /**
     * @param libraryPath see {@link CrmlWorkspace#CrmlWorkspace(List)}
     * @param last the last stage to run
     * @param afterStage called after each stage that ran, with the stage and the trace so far
     */
    public InspectingWorkspace(List<Path> libraryPath, Stage last, BiConsumer<Stage, Trace> afterStage) {
        super(libraryPath);
        this.last = last;
        this.afterStage = afterStage;
    }

    /** Runs all stages, recording them. */
    public InspectingWorkspace() {
        this(Collections.<Path>emptyList(), Stage.LIFT, (stage, trace) -> { });
    }

    /** Runs the stages up to and including {@code last}, recording them. */
    public static InspectingWorkspace upTo(Stage last) {
        return new InspectingWorkspace(Collections.<Path>emptyList(), last, (stage, trace) -> { });
    }

    /** The trace of {@code resource}; empty if it was never resolved. */
    public Trace trace(CrmlResource resource) {
        return traces.computeIfAbsent(resource, r -> new Trace());
    }

    @Override
    LinkResult link(CrmlResource resource) {
        if (!runs(Stage.LINK)) {
            return null;
        }
        LinkResult result = super.link(resource);
        trace(resource).link = result;
        afterStage.accept(Stage.LINK, trace(resource));
        return result;
    }

    @Override
    MixfixParser operatorParser(CrmlResource resource) {
        if (!runs(Stage.OPERATOR_CALLS)) {
            return null;
        }
        MixfixParser parser = super.operatorParser(resource);
        trace(resource).parser = parser;
        return parser;
    }

    @Override
    CallResult resolveOperatorCalls(CrmlResource resource, MixfixParser parser) {
        if (!runs(Stage.OPERATOR_CALLS)) {
            return null;
        }
        CallResult result = super.resolveOperatorCalls(resource, parser);
        trace(resource).calls = result;
        afterStage.accept(Stage.OPERATOR_CALLS, trace(resource));
        return result;
    }

    @Override
    LiftResult lift(CrmlResource resource) {
        if (!runs(Stage.LIFT)) {
            return null;
        }
        LiftResult result = super.lift(resource);
        trace(resource).lift = result;
        afterStage.accept(Stage.LIFT, trace(resource));
        return result;
    }

    private boolean runs(Stage stage) {
        return stage.compareTo(last) <= 0;
    }
}
