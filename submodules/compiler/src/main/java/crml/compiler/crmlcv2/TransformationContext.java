package crml.compiler.crmlcv2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;

import crml.model.language.CustomOperator;
import crml.model.language.Model;
import crml.model.language.Value;
import crml.model.language.Variable;
import crml.model.modelica.ClassDefinition;
import crml.model.modelica.ClassKind;
import crml.model.modelica.ComponentDeclaration;
import crml.model.modelica.ComponentReference;
import crml.model.modelica.Equation;
import crml.model.modelica.ModelicaElement;
import crml.model.modelica.Placeholder;
import crml.model.trace.TraceFactory;
import crml.model.trace.TraceLink;
import crml.model.trace.TraceLinkKind;
import crml.model.trace.TraceModel;
import crml.modelica.build.Modelica;

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
        final Map<CustomOperator, UnsupportedConstruct> operatorFailures =
            new HashMap<CustomOperator, UnsupportedConstruct>();
        /** Class names already used, so generated names never collide. */
        final Set<String> classNames = new HashSet<String>();
        /**
         * The Modelica class generated for each CRML class. Keyed by the CRML
         * object, not by its name, so a lookup is a map hit rather than name
         * resolution - there is no scope, no path and no ambiguity.
         */
        final Map<crml.model.language.Class, ClassDefinition> generatedClasses =
            new LinkedHashMap<crml.model.language.Class, ClassDefinition>();
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
    private final Map<String, Integer> counters = new HashMap<String, Integer>();
    /**
     * Every name already spoken for in the target class. A generated name must
     * avoid the CRML variables' names too, including those not declared yet -
     * otherwise a model with its own variable "c1" gets "c1 = c1".
     */
    private final Set<String> usedNames = new HashSet<String>();

    private TransformationContext(Session session, ClassDefinition target) {
        this.session = session;
        this.target = target;
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
     */
    public ComponentReference declare(ComponentDeclaration declaration, EObject crmlSource) {
        reserveName(declaration.getName());
        target.getComponents().add(declaration);
        link(crmlSource, declaration, componentKind(crmlSource));
        return ref(declaration.getName());
    }

    /** Adds an equation to the target class. */
    public void equate(Equation equation, EObject crmlSource) {
        target.getEquations().add(equation);
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

    public void registerClass(crml.model.language.Class clazz, ClassDefinition definition) {
        session.generatedClasses.put(clazz, definition);
    }

    /** The class generated for {@code clazz}, or null if there is none. */
    public ClassDefinition generatedClass(crml.model.language.Class clazz) {
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

    /** A context targeting the top-level class of this translation. */
    public TransformationContext root() {
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
    public UnsupportedConstruct operatorFailure(CustomOperator operator) {
        return session.operatorFailures.get(operator);
    }

    public void recordOperatorFailure(CustomOperator operator, UnsupportedConstruct failure) {
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
        if (crmlSource instanceof crml.model.language.Set<?>) {
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
        if (crmlSource instanceof crml.model.language.Class) {
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
