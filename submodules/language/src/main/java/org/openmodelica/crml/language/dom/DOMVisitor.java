package org.openmodelica.crml.language.dom;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.IntStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.xtext.xbase.lib.Functions.Function0;

import crml.language.grammar.crmlBaseVisitor;
import crml.language.grammar.crmlParser.Class_defContext;
import crml.language.grammar.crmlParser.ConstantContext;
import crml.language.grammar.crmlParser.DefinitionContext;
import crml.language.grammar.crmlParser.ExpContext;
import crml.language.grammar.crmlParser.OperatorContext;
import crml.language.grammar.crmlParser.TemplateContext;
import crml.language.grammar.crmlParser.TypeContext;
import crml.language.grammar.crmlParser.Var_defContext;

import org.openmodelica.crml.language.dom.builders.ClassBuilder;
import org.openmodelica.crml.language.dom.builders.ConstantBuilder;
import org.openmodelica.crml.language.dom.builders.ExpressionBuilder;
import org.openmodelica.crml.language.dom.builders.LiftBuilder;
import org.openmodelica.crml.language.dom.builders.RootBuilder;
import org.openmodelica.crml.language.dom.builders.TemplateBuilder;
import org.openmodelica.crml.language.dom.builders.TypeReferenceBuilder;
import org.openmodelica.crml.language.dom.builders.UserOperatorBuilder;
import org.openmodelica.crml.language.dom.builders.VariableBuilder;
import org.openmodelica.crml.language.dom.util.BuildResult;
import org.openmodelica.crml.language.dom.util.ScopeResolutionOptions;
import org.openmodelica.crml.language.dom.util.ScopeResolver;
import org.openmodelica.crml.language.opcall.MixfixParser;
import org.openmodelica.crml.language.scope.Resolution;
import org.openmodelica.crml.language.workspace.CrmlDiagnostic;
import org.openmodelica.crml.language.workspace.CrmlDiagnostic.Kind;
import org.openmodelica.crml.language.workspace.SourceMap;
import org.openmodelica.crml.model.metamodel.Library;
import org.openmodelica.crml.model.metamodel.Sequence;

public class DOMVisitor extends crmlBaseVisitor<BuildResult> implements BuildContext {
    private final ScopeResolver resolver = new ScopeResolver();
    private EObject builtRoot;
    private final List<LinkerTask> crossrefTasks = new ArrayList<>();
    private final List<ModificationTask> modificationTasks = new ArrayList<>();
    /** Reported with {@link #reportError}, not yet taken by {@link #takeErrors()}. */
    private final List<CrmlDiagnostic> errors = new ArrayList<>();
    /** Where each built object is in the source: the innermost node it was built from. */
    private final SourceMap sources;

    private final RootBuilder root = new RootBuilder(this);
    private final ClassBuilder cls = new ClassBuilder(this);
    private final VariableBuilder vars = new VariableBuilder(this);
    private final TypeReferenceBuilder typeref = new TypeReferenceBuilder(this);
    private final ExpressionBuilder exp = new ExpressionBuilder(this);
    private final ConstantBuilder cnst = new ConstantBuilder(this);
    private final UserOperatorBuilder userop =  new UserOperatorBuilder(this);
    private final TemplateBuilder templates =  new TemplateBuilder(this);

    /** A visitor that traces what it builds in a source map of its own, without the tokens. */
    public DOMVisitor() {
        this(new SourceMap(null));
    }

    /** A visitor that traces what it builds in {@code sources}. */
    public DOMVisitor(SourceMap sources) {
        this.sources = sources;
    }

    /** Where each object built so far is in the source. */
    public SourceMap sourceMap() {
        return sources;
    }

    /**
     * Builds the DOM of a whole parse tree. Never throws: what goes wrong is
     * in {@link #takeErrors()}, each error at its position in the source; an
     * exception thrown while building is reported at the innermost node being
     * built, with the exception as its cause.
     *
     * <p>A file with syntax errors is built from the tree the parser
     * recovered; a part of it that cannot be built is left out (see
     * {@link #buildPart}). The root is then partial.
     *
     * @return the root, or null if none could be built
     */
    public EObject buildRoot(ParseTree tree) {
        EObject result = null;
        try {
            BuildResult built = build(tree);
            if (built instanceof BuildResult.SingleBuildResult) {
                result = ((BuildResult.SingleBuildResult<?>) built).result();
            }
        } catch (BuildFailure e) {
            errors.add(diagnostic(Kind.DOM_BUILD, e.start, message(e.getCause()), e.getCause()));
        } catch (RuntimeException e) {
            errors.add(diagnostic(Kind.DOM_BUILD, start(tree), message(e), e));
        }
        if (result == null && errors.isEmpty()) {
            errors.add(diagnostic(Kind.DOM_BUILD, start(tree), "The file does not define a model or a library", null));
        }
        return result;
    }

    // recursion funnels through here, so dispatch stays in one place
    @Override public BuildResult build(ParseTree n) {
        if (n instanceof ParserRuleContext && Recovery.isMissing(n)) {
            // The parser expected a rule here, but nothing of it was written.
            return BuildResult.wrap((EObject) null);
        }
        BuildResult result;
        try {
            result = visit(n);
        } catch (BuildFailure e) {
            throw e;
        } catch (RuntimeException e) {
            throw new BuildFailure(start(n), e);
        }
        remember(result, n);
        return result;
    }
    @Override public <T extends BuildResult> T build(ParseTree n, Class<T> type) {
        BuildResult result = build(n);
        try {
            return type.cast(result);
        } catch (ClassCastException e) {
            throw new BuildFailure(start(n), e);
        }
    }

    @Override public <T extends BuildResult> T buildPart(ParseTree n, Class<T> type) {
        if (n == null) {
            return null;
        }
        try {
            return build(n, type);
        } catch (BuildFailure e) {
            if (!Recovery.hasSyntaxError(n)) {
                errors.add(diagnostic(Kind.DOM_BUILD, e.start, message(e.getCause()), e.getCause()));
            }
            return null;
        }
    }

    @Override
    public void reportError(ParseTree at, String message) {
        errors.add(diagnostic(Kind.DOM_BUILD, start(at), message, null));
    }

    @Override
    public void reportImplementationError(ParseTree at, String message) {
        errors.add(diagnostic(Kind.DOM_BUILD, start(at), "Not supported yet: " + message, null));
    }

    @Override
    public void position(EObject object, ParseTree source) {
        sources.record(object, source);
    }

    @Override
    public void reportError(EObject at, Kind kind, String message) {
        errors.add(diagnosticAt(at, kind, message, null));
    }

    /**
     * The errors reported since the last call, in the order they were
     * reported; afterwards there are none.
     */
    public List<CrmlDiagnostic> takeErrors() {
        List<CrmlDiagnostic> taken = new ArrayList<>(errors);
        errors.clear();
        return taken;
    }

    /**
     * A diagnostic at the position {@code at} was built from, or that of its
     * closest container built from the source; without a position if there
     * is none (e.g. {@code at} was not built by this visitor).
     */
    public CrmlDiagnostic diagnosticAt(EObject at, Kind kind, String message, Throwable cause) {
        return diagnostic(kind, sources.position(at), message, cause);
    }

    private void remember(BuildResult result, ParseTree n) {
        // The innermost node is built first, so it wins over the nodes that
        // only pass its result on.
        if (result instanceof BuildResult.SingleBuildResult) {
            sources.record(((BuildResult.SingleBuildResult<?>) result).<EObject>result(), n);
        } else if (result instanceof BuildResult.MultiBuildResult) {
            for (EObject object : ((BuildResult.MultiBuildResult<?>) result).<EObject>results()) {
                sources.record(object, n);
            }
        }
    }

    private static Token start(ParseTree n) {
        if (n instanceof ParserRuleContext) {
            return ((ParserRuleContext) n).getStart();
        }
        if (n instanceof TerminalNode) {
            return ((TerminalNode) n).getSymbol();
        }
        return null;
    }

    /** A diagnostic at {@code start}, located at the source name of its stream. */
    private static CrmlDiagnostic diagnostic(Kind kind, Token start, String message, Throwable cause) {
        if (start == null) {
            return new CrmlDiagnostic(kind, message, null, 0, 0, cause);
        }
        String location = start.getInputStream() == null ? null : start.getInputStream().getSourceName();
        if (IntStream.UNKNOWN_SOURCE_NAME.equals(location)) {
            location = null;
        }
        return new CrmlDiagnostic(kind, message, location, start.getLine(), start.getCharPositionInLine(), cause);
    }

    private static String message(Throwable e) {
        return e.getMessage() != null ? e.getMessage() : e.toString();
    }

    /** An exception thrown while building, with the start of the innermost node being built. */
    private static final class BuildFailure extends RuntimeException {
        private static final long serialVersionUID = 1L;
        private final transient Token start;

        BuildFailure(Token start, RuntimeException cause) {
            super(cause);
            this.start = start;
        }
    }

    // routing — mechanical, no instanceof
    @Override public BuildResult visitDefinition(DefinitionContext c) {
        builtRoot = root.root(c);
        return BuildResult.wrap(builtRoot);
    }
    @Override public BuildResult visitVar_def(Var_defContext c) { return BuildResult.wrap(vars.variable(c)); }
    @Override public BuildResult visitType(TypeContext c) { return BuildResult.wrap(typeref.reference(c)); }
    @Override public BuildResult visitClass_def(Class_defContext c) { return BuildResult.wrap(cls.buildClass(c)); }
    @Override public BuildResult visitExp(ExpContext c) { return BuildResult.wrap(exp.value(c)); }
    @Override public BuildResult visitConstant(ConstantContext c) { return BuildResult.wrap(cnst.constant(c)); }
    @Override public BuildResult visitOperator(OperatorContext c) { return BuildResult.wrap(userop.get(c)); }
    @Override public BuildResult visitTemplate(TemplateContext c) { return BuildResult.wrap(templates.get(c)); }
    
    @Override
    public void link(EObject host, EStructuralFeature reference, String id, ScopeResolutionOptions options) {
        crossrefTasks.add(new LinkerTask(host, reference, id, options));
    }

    /**
     * Runs the queued link tasks until no more of them resolves and returns
     * what is left. The unresolved tasks stay queued, so a later call retries
     * them.
     *
     * @return the references that could not be resolved; empty if all were
     */
    public List<UnresolvedLink> link(){
        while(crossrefTasks.removeIf(LinkerTask::apply));

        List<UnresolvedLink> unresolved = new ArrayList<>();
        for (LinkerTask task : crossrefTasks) {
            unresolved.add(task.toUnresolvedLink());
        }
        return unresolved;
    }

    /** The root built by {@link #build}, or null if none was built yet. */
    public EObject root(){
        return builtRoot;
    }

    /**
     * The operator-call parser for the built root: the operators visible in
     * the root {@code Model} or {@code Library}, its own first, then those of
     * its dependencies (see {@code Scopes.operators}).
     *
     * @return the parser, or null if the root is neither a Model nor a Library
     */
    public MixfixParser operatorParser(){
        if (builtRoot instanceof Library) {
            return new MixfixParser((Library) builtRoot, this);
        }
        return null;
    }

    /**
     * Replaces the natural-language operator calls in the built root - parsed
     * as raw Sequences, since the grammar cannot know which operators are in
     * scope - with ComputedValue calls to the operators {@code parser} knows,
     * and applies the replacements.
     *
     * <p>Run this after {@link #link()}: matching a call against an operator
     * needs the operator declarations resolved. A Sequence that names no
     * operator in scope is left as it is, so a consumer can report it rather
     * than silently receiving a wrong tree. Lifted values ({@code x.element})
     * are linked afterwards, by {@link LiftBuilder#linkAll}.
     *
     * @return the applied replacements, in the order they were applied
     */
    public List<DomModification> resolveOperatorCalls(MixfixParser parser){
        if (parser == null || builtRoot == null) {
            return new ArrayList<>();
        }
        parser.perform(builtRoot);
        return modify();
    }

    @Override
    public void set(EObject host, EStructuralFeature reference, EObject value) {
        if(host instanceof Sequence){
            throw CRMLLanguageException.inconsistent("Why are we setting a Seauence? ");
        }
        modificationTasks.add(new ModificationTask(host, reference, value));
    }
    private List<DomModification> modify(){
        List<DomModification> applied = new ArrayList<>();
        while(modificationTasks.removeIf(task -> {
            if (!task.apply()) {
                return false;
            }
            applied.add(task.applied());
            if (task.oldValue instanceof EObject) {
                sources.replace((EObject) task.oldValue, task.value);
            }
            return true;
        }));

        if(!modificationTasks.isEmpty()){
            throw CRMLLanguageException.domFailure(modificationTasks);
        }
        return applied;
    }

    private class LinkerTask implements Function0<Boolean>{
        private final EObject source;
        private final EStructuralFeature feature;
        private final String id;
        private final ScopeResolutionOptions options;

        private LinkerTask(EObject host, EStructuralFeature feature, String id, ScopeResolutionOptions options){
            this.source = host;
            this.feature = feature;
            this.id = id;
            this.options = options;
        }

        private LinkerTask(EObject host, EStructuralFeature feature, String id){
            this(host, feature, id, null );
        }

        /** The result of the last attempt; null before the first. */
        private Resolution last;

        /** Tries to link; true if the reference is set. */
        @Override
        public Boolean apply() {
            last = resolver.link(source, feature, id, options);
            return last.found();
        }

        private UnresolvedLink toUnresolvedLink() {
            return new UnresolvedLink(source, feature, id, options, last);
        }

        @Override
        public String toString() {
            return "Linker task: '"+ feature.getName() + "' to '" + id + "' in " + source.toString();
        }
    }

    private class ModificationTask implements Function0<Boolean>{
        private final EObject host;
        private final EStructuralFeature feature;
        private final EObject value;
        private Object oldValue;

        private ModificationTask(EObject host, EStructuralFeature feature, EObject value){
            this.host = host;
            this.feature = feature;
            this.value = value;
        }

        @Override
        public Boolean apply() {
            oldValue = host.eGet(feature);
            host.eSet(feature, value);
            return true;
        }

        private DomModification applied() {
            return new DomModification(host, feature, oldValue, value);
        }

        @Override
        public String toString() {
            return "Modification task: '"+ feature.getName() + "' to '" + value + "' in " + host.toString();
        }
    }

    
}
