package org.openmodelica.crml.language.workspace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openmodelica.crml.language.dom.DomModification;
import org.openmodelica.crml.language.dom.UnresolvedLink;
import org.openmodelica.crml.model.metamodel.Liftable;
import org.openmodelica.crml.model.metamodel.Sequence;

/**
 * What one stage of {@link CrmlWorkspace#resolve()} did to one resource.
 *
 * <p>The diagnostics of a stage are also added to the resource's errors; the
 * copy here tells which stage reported them. The results are read-only and are
 * only created by the workspace. They exist so that tests (through a subclass
 * of the workspace in this package) can look at each stage.
 */
public abstract class StageResult {
    private final List<CrmlDiagnostic> diagnostics;

    StageResult(List<CrmlDiagnostic> diagnostics) {
        this.diagnostics = Collections.unmodifiableList(new ArrayList<>(diagnostics));
    }

    /** The diagnostics this stage reported. */
    public List<CrmlDiagnostic> diagnostics() { return diagnostics; }

    /** Linking: the references that stayed unresolved. */
    public static final class LinkResult extends StageResult {
        private final List<UnresolvedLink> unresolved;

        LinkResult(List<UnresolvedLink> unresolved, List<CrmlDiagnostic> diagnostics) {
            super(diagnostics);
            this.unresolved = Collections.unmodifiableList(new ArrayList<>(unresolved));
        }

        /** The references the linker could not resolve; empty if all were. */
        public List<UnresolvedLink> unresolved() { return unresolved; }
    }

    /** Operator-call resolution: the sequences replaced and those left. */
    public static final class CallResult extends StageResult {
        private final List<DomModification> replaced;
        private final List<Sequence> unresolved;

        CallResult(List<DomModification> replaced, List<Sequence> unresolved, List<CrmlDiagnostic> diagnostics) {
            super(diagnostics);
            this.replaced = Collections.unmodifiableList(new ArrayList<>(replaced));
            this.unresolved = Collections.unmodifiableList(new ArrayList<>(unresolved));
        }

        /** Each Sequence that was replaced by the call it spells out. */
        public List<DomModification> replaced() { return replaced; }

        /** The Sequences left in the DOM (first element of each); they name no operator in scope. */
        public List<Sequence> unresolved() { return unresolved; }
    }

    /** Lifting: the applications that {@code x.element} values were linked to. */
    public static final class LiftResult extends StageResult {
        private final List<Liftable> linked;

        LiftResult(List<Liftable> linked, List<CrmlDiagnostic> diagnostics) {
            super(diagnostics);
            this.linked = Collections.unmodifiableList(new ArrayList<>(linked));
        }

        /** The Liftables a ScatteredValue was linked to. */
        public List<Liftable> linked() { return linked; }
    }
}
