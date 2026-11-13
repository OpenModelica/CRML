package org.openmodelica.crml.compiler.crmlcv2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.openmodelica.crml.compiler.crmlcv2.util.CRMLCompilerException;
import org.openmodelica.crml.model.metamodel.CustomOperator;
import org.openmodelica.crml.model.metamodel.Model;
import org.openmodelica.crml.model.metamodel.Value;
import org.openmodelica.crml.model.metamodel.Variable;
import org.openmodelica.crml.modelica.metamodel.ClassDefinition;
import org.openmodelica.crml.modelica.metamodel.ClassKind;
import org.openmodelica.crml.modelica.metamodel.ComponentDeclaration;
import org.openmodelica.crml.modelica.metamodel.ComponentReference;
import org.openmodelica.crml.modelica.metamodel.Equation;
import org.openmodelica.crml.modelica.metamodel.Expression;
import org.openmodelica.crml.modelica.metamodel.ModelicaElement;
import org.openmodelica.crml.modelica.metamodel.Placeholder;
import org.openmodelica.crml.modelica.trace.metamodel.TraceFactory;
import org.openmodelica.crml.modelica.trace.metamodel.TraceLink;
import org.openmodelica.crml.modelica.trace.metamodel.TraceLinkKind;
import org.openmodelica.crml.modelica.trace.metamodel.TraceModel;
import org.openmodelica.crml.modelica.build.Modelica;

public final class TransformationContext implements Modelica {

    /** State shared by every context of one translation. */
    private static final class Session {
        final TraceModel trace = TraceFactory.eINSTANCE.createTraceModel();
        final List<Diagnostic> diagnostics = new ArrayList<Diagnostic>();
        ClassDefinition root;
        
        final Map<CustomOperator, GeneratedOperator> operators =
            new LinkedHashMap<CustomOperator, GeneratedOperator>();
        final Set<CustomOperator> visiting = new HashSet<CustomOperator>();
        /** Operators whose class could not be generated, so each is diagnosed once. */
        final Map<CustomOperator, CRMLCompilerException.UnsupportedConstruct> operatorFailures =
            new HashMap<CustomOperator, CRMLCompilerException.UnsupportedConstruct>();
        /** Class names already used, so generated names never collide. */
        final Set<String> classNames = new HashSet<String>();
        /**
         * The Modelica class generated for each CRML class. Keyed by the CRML
         * object, not by its name, so a lookup is a map hit rather than name
         * resolution - there is no scope, no path and no ambiguity.
         */
        final Map<org.openmodelica.crml.model.metamodel.Class, ClassDefinition> generatedClasses =
            new LinkedHashMap<org.openmodelica.crml.model.metamodel.Class, ClassDefinition>();
        /** Keys already reported through reportOnce. */
        final Set<String> reportedOnce = new HashSet<String>();
    }

    private final Session session;
    private final ClassDefinition target;
    /**
     * Name counters, scoped per target class and seeded deterministically. The
     * generator this replaces held a {@code static} allocator, so generated
     * names depended on how many models had been compiled earlier in the same
     * JVM.
     */
    private final Map<String, Integer> counters;
    /**
     * Every name already spoken for in the target class. A generated name must
     * avoid the CRML variables' names too, including those not declared yet -
     * otherwise a model with its own variable "c1" gets "c1 = c1".
     */
    private final Set<String> usedNames;
    /**
     * The context this one is the lifted mode of, or null in normal mode. See
     * {@link #lifted}.
     */
    private final TransformationContext outer;
    /** Where a lifted-mode context buffers what it declares; null in normal mode. */
    private final Lifter.LoopFrame frame;

    private TransformationContext(Session session, ClassDefinition target) {
        this.session = session;
        this.target = target;
        this.counters = new HashMap<String, Integer>();
        this.usedNames = new HashSet<String>();
        this.outer = null;
        this.frame = null;
    }

    /** The lifted mode of {@code outer}: same class, same names, buffered output. */
    private TransformationContext(TransformationContext outer, Lifter.LoopFrame frame) {
        this.session = outer.session;
        this.target = outer.target;
        this.counters = outer.counters;
        this.usedNames = outer.usedNames;
        this.outer = outer;
        this.frame = frame;
    }

    /**
     * The <em>lifted mode</em> of this context, in which the per-element part of
     * a lifted application ({@code x.element}) is generated. It targets the same
     * class and shares its names, but nothing it produces reaches the class
     * directly:
     *
     * <ul>
     * <li>{@link #declare} turns the component into an array with one entry per
     *     slot of the region, and returns a reference to the entry of the
     *     current slot;
     * <li>{@link #equate} puts the equation into the body of the region's loop.
     * </ul>
     *
     * Both are buffered in {@code frame}, and {@link Lifter#close} adds them to
     * the class at once. Everything else behaves as in normal mode, and
     * {@link #root()} and {@link #nested} always give normal-mode contexts: a
     * class is never generated per slot. Only {@link Lifter} creates one.
     */
    TransformationContext lifted(Lifter.LoopFrame frame) {
        return new TransformationContext(this, frame);
    }

    /** True in the {@link #lifted lifted mode}. */
    public boolean isLifted() {
        return frame != null;
    }

    /** Starts a fresh translation targeting {@code target}. */
    public static TransformationContext of(ClassDefinition target) {
        Session session = new Session();
        session.root = target;
        TransformationContext ctx = new TransformationContext(session, target);
        session.classNames.add(target.getName());
        return ctx;
    }

    public ClassDefinition target() {
        return target;
    }

    public TraceModel trace() {
        return session.trace;
    }

    public List<Diagnostic> diagnostics() {
        return session.diagnostics;
    }

    // --- the three mutators -------------------------------------------------

    /**
     * Adds a component to the target class and returns a reference to it, so an
     * expression that needed something hoisted can go on being an expression.
     *
     * <p>In {@link #lifted lifted mode} the component becomes an array over the
     * slots of the region and the reference is to the current slot's entry. A
     * binding is moved into the loop body, as an equation for that entry. A
     * component with modifications is not supported there
     * ({@code Lift.BodyModification}).
     */
    public ComponentReference declare(ComponentDeclaration declaration, EObject crmlSource) {
        if (frame != null) {
            return declareLifted(declaration, crmlSource);
        }
        reserveName(declaration.getName());
        target.getComponents().add(declaration);
        link(crmlSource, declaration, componentKind(crmlSource));
        return ref(declaration.getName());
    }

    private ComponentReference declareLifted(ComponentDeclaration declaration, EObject crmlSource) {
        if (!declaration.getModifications().isEmpty()) {
            throw CRMLCompilerException.unsupported(Diagnostics.notYetImplemented("Lift.BodyModification",
                "component '" + declaration.getName() + "' has modifications, which cannot be "
                + "written per element of a lifted application yet", crmlSource));
        }
        reserveName(declaration.getName());
        ComponentDeclaration copy = EcoreUtil.copy(declaration);
        // The slot dimension comes first, so name[p] selects one slot whatever
        // dimensions the component has of its own.
        copy.getArrayDimensions().add(0, dimension(frame.size()));
        Expression binding = copy.getBinding();
        copy.setBinding(null);
        frame.components.add(copy);
        if (binding != null) {
            frame.equations.add(eq(subscripted(ref(copy.getName()), frame.index()), binding));
        }
        return subscripted(ref(copy.getName()), frame.index());
    }

    /**
     * Adds an equation to the target class; in {@link #lifted lifted mode}, to
     * the body of the region's loop.
     */
    public void equate(Equation equation, EObject crmlSource) {
        if (frame != null) {
            frame.equations.add(equation);
            return;
        }
        target.getEquations().add(equation);
        link(crmlSource, equation, equationKind(crmlSource));
    }

    /**
     * Records the trace link of an equation that is not added to the class
     * itself: one in the body of a loop. Used by {@link Lifter#close}.
     */
    void linkNestedEquation(Equation equation, EObject crmlSource) {
        link(crmlSource, equation, equationKind(crmlSource));
    }

    /** Nests a class inside the target class. */
    public ClassDefinition defineClass(ClassDefinition definition, EObject crmlSource) {
        target.getNestedClasses().add(definition);
        link(crmlSource, definition, classKind(crmlSource, definition));
        return definition;
    }

    // --- supporting operations ----------------------------------------------

    /**
     * Reserves a name so no generated name can take it. Call this for every CRML
     * name that will end up in the target class, before transforming anything.
     */
    public void reserveName(String name) {
        if (name != null) {
            usedNames.add(name);
        }
    }

    /** A name unique within the target class: {@code prefix1}, {@code prefix2}, ... */
    public String allocateName(String prefix) {
        Integer previous = counters.get(prefix);
        int next = previous == null ? 1 : previous + 1;
        while (usedNames.contains(prefix + next)) {
            next++;
        }
        counters.put(prefix, next);
        usedNames.add(prefix + next);
        return prefix + next;
    }

    public void report(Diagnostic diagnostic) {
        session.diagnostics.add(diagnostic);
    }

    /** Reports at most one diagnostic per key per translation. */
    public void reportOnce(String key, Diagnostic diagnostic) {
        if (session.reportedOnce.add(key)) {
            report(diagnostic);
        }
    }

    // --- generated classes for CRML classes ----------------------------------

    public void registerClass(org.openmodelica.crml.model.metamodel.Class clazz, ClassDefinition definition) {
        session.generatedClasses.put(clazz, definition);
    }

    /** The class generated for {@code clazz}, or null if there is none. */
    public ClassDefinition generatedClass(org.openmodelica.crml.model.metamodel.Class clazz) {
        return session.generatedClasses.get(clazz);
    }

    /**
     * Records a diagnostic and leaves a matching comment in the generated .mo,
     * so a gap is visible both in the diagnostics list and in the output.
     */
    public void reportWithPlaceholder(Diagnostic diagnostic) {
        report(diagnostic);
        Placeholder placeholder = placeholder(
            diagnostic.constructKind() + ": " + diagnostic.message());
        target.getPlaceholders().add(placeholder);
        link(diagnostic.crmlSource(), placeholder, TraceLinkKind.UNSUPPORTED);
    }

    /** A context targeting another class, sharing this translation's trace and diagnostics. */
    public TransformationContext nested(ClassDefinition newTarget) {
        return new TransformationContext(session, newTarget);
    }

    /**
     * A context targeting the top-level class of this translation. Always in
     * normal mode, also when asked of a {@link #lifted lifted-mode} context, so
     * an operator class is never generated in lifted mode.
     */
    public TransformationContext root() {
        if (outer != null) {
            return outer.root();
        }
        return session.root == target ? this : new TransformationContext(session, session.root);
    }

    // --- generated operator classes ------------------------------------------

    /** The class generated for {@code operator}, or null if it has not been generated. */
    public GeneratedOperator generatedOperator(CustomOperator operator) {
        return session.operators.get(operator);
    }

    public void registerOperator(CustomOperator operator, GeneratedOperator generated) {
        session.operators.put(operator, generated);
    }

    /** The failure already recorded for {@code operator}, or null. */
    public CRMLCompilerException.UnsupportedConstruct operatorFailure(CustomOperator operator) {
        return session.operatorFailures.get(operator);
    }

    public void recordOperatorFailure(CustomOperator operator, CRMLCompilerException.UnsupportedConstruct failure) {
        session.operatorFailures.put(operator, failure);
    }

    /** False when {@code operator} is already being transformed - a cyclic definition. */
    public boolean beginVisiting(CustomOperator operator) {
        return session.visiting.add(operator);
    }

    public void endVisiting(CustomOperator operator) {
        session.visiting.remove(operator);
    }

    /**
     * Reserves a class name, so a generated name cannot take it. CRML class
     * names are used verbatim - a class-typed variable refers to its class by
     * name - so they are claimed before anything is generated.
     */
    public void reserveClassName(String name) {
        if (name != null) {
            session.classNames.add(name);
        }
    }

    /** A class name unique within this translation, derived from {@code preferred}. */
    public String allocateClassName(String preferred) {
        if (session.classNames.add(preferred)) {
            return preferred;
        }
        for (int i = 2; ; i++) {
            String candidate = preferred + "_" + i;
            if (session.classNames.add(candidate)) {
                return candidate;
            }
        }
    }

    // --- trace --------------------------------------------------------------

    private void link(EObject crmlSource, ModelicaElement modelicaTarget, TraceLinkKind kind) {
        if (crmlSource == null) {
            return;
        }
        TraceLink traceLink = TraceFactory.eINSTANCE.createTraceLink();
        traceLink.setCrml(crmlSource);
        traceLink.setModelica(modelicaTarget);
        traceLink.setKind(kind);
        session.trace.getLinks().add(traceLink);
    }

    /**
     * Records a link the three mutators cannot: the top-level class, which has
     * no enclosing target to be added to.
     */
    public void linkRoot(EObject crmlSource, ClassDefinition definition) {
        link(crmlSource, definition, classKind(crmlSource, definition));
    }

    // The link kind follows from what the CRML source element is, so call sites
    // never have to pass it and can never pass the wrong one.

    private static TraceLinkKind componentKind(EObject crmlSource) {
        if (crmlSource instanceof Variable) {
            return TraceLinkKind.VARIABLE_TO_COMPONENT;
        }
        if (crmlSource instanceof org.openmodelica.crml.model.metamodel.Set<?>) {
            return TraceLinkKind.SET_TO_COMPONENT;
        }
        if (crmlSource instanceof Value) {
            return TraceLinkKind.VALUE_TO_COMPONENT;
        }
        return TraceLinkKind.UNSUPPORTED;
    }

    private static TraceLinkKind equationKind(EObject crmlSource) {
        if (crmlSource instanceof Variable) {
            return TraceLinkKind.VARIABLE_TO_EQUATION;
        }
        if (crmlSource instanceof Value) {
            return TraceLinkKind.VALUE_TO_EQUATION;
        }
        return TraceLinkKind.UNSUPPORTED;
    }

    private static TraceLinkKind classKind(EObject crmlSource, ClassDefinition definition) {
        if (crmlSource instanceof Model) {
            return TraceLinkKind.MODEL_TO_CLASS;
        }
        if (crmlSource instanceof org.openmodelica.crml.model.metamodel.Class) {
            return TraceLinkKind.CLASS_TO_MODEL;
        }
        if (crmlSource instanceof CustomOperator) {
            return definition.getKind() == ClassKind.FUNCTION
                ? TraceLinkKind.OPERATOR_TO_FUNCTION
                : TraceLinkKind.OPERATOR_TO_BLOCK;
        }
        return TraceLinkKind.UNSUPPORTED;
    }
}
