package crml.language.dom;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.xtext.xbase.lib.Functions.Function0;

import crml.language.dom.builders.ClassBuilder;
import crml.language.dom.builders.ConstantBuilder;
import crml.language.dom.builders.ExpressionBuilder;
import crml.language.dom.builders.RootBuilder;
import crml.language.dom.builders.TemplateBuilder;
import crml.language.dom.builders.TypeReferenceBuilder;
import crml.language.dom.builders.UserOperatorBuilder;
import crml.language.dom.builders.VariableBuilder;
import crml.language.dom.builders.ConstructorBuilder;
import crml.language.dom.util.BuildResult;
import crml.language.dom.util.ScopeResolutionOptions;
import crml.language.dom.util.ScopeResolver;
import crml.language.grammar.crmlBaseVisitor;
import crml.language.grammar.crmlParser.Class_defContext;
import crml.language.grammar.crmlParser.ConstantContext;
import crml.language.grammar.crmlParser.ConstructorContext;
import crml.language.grammar.crmlParser.DefinitionContext;
import crml.language.grammar.crmlParser.ExpContext;
import crml.language.grammar.crmlParser.OperatorContext;
import crml.language.grammar.crmlParser.TemplateContext;
import crml.language.grammar.crmlParser.TypeContext;
import crml.language.grammar.crmlParser.Var_defContext;

public class DOMVisitor extends crmlBaseVisitor<BuildResult> implements BuildContext {
    private final ScopeResolver resolver = new ScopeResolver();
    private final List<Function0<Boolean>> crossrefTasks = new ArrayList<>();
    private final List<Function0<Boolean>> modificationTasks = new ArrayList<>();

    private final RootBuilder root = new RootBuilder(this);
    private final ClassBuilder cls = new ClassBuilder(this);
    private final VariableBuilder vars = new VariableBuilder(this);
    private final TypeReferenceBuilder typeref = new TypeReferenceBuilder(this);
    private final ExpressionBuilder exp = new ExpressionBuilder(this);
    private final ConstantBuilder cnst = new ConstantBuilder(this);
    private final ConstructorBuilder consr = new ConstructorBuilder(this);
    private final UserOperatorBuilder userop =  new UserOperatorBuilder(this);
    private final TemplateBuilder templates =  new TemplateBuilder(this);

    // recursion funnels through here, so dispatch stays in one place
    @Override public BuildResult build(ParseTree n) { return visit(n); }
    @Override public <T extends BuildResult> T build(ParseTree n, Class<T> type) {
        return type.cast(visit(n));
    }

    // routing — mechanical, no instanceof
    @Override public BuildResult visitDefinition(DefinitionContext c) { return BuildResult.wrap(root.root(c)); }
    @Override public BuildResult visitVar_def(Var_defContext c) { return BuildResult.wrap(vars.variable(c)); }
    @Override public BuildResult visitType(TypeContext c) { return BuildResult.wrap(typeref.reference(c)); }
    @Override public BuildResult visitClass_def(Class_defContext c) { return BuildResult.wrap(cls.buildClass(c)); }
    @Override public BuildResult visitExp(ExpContext c) { return BuildResult.wrap(exp.value(c)); }
    @Override public BuildResult visitConstant(ConstantContext c) { return BuildResult.wrap(cnst.constant(c)); }
    @Override public BuildResult visitConstructor(ConstructorContext c) { return BuildResult.wrap(consr.get(c)); }
    @Override public BuildResult visitOperator(OperatorContext c) { return BuildResult.wrap(userop.get(c)); }
    @Override public BuildResult visitTemplate(TemplateContext c) { return BuildResult.wrap(templates.get(c)); }
    
    
    @Override
    public void link(EObject host, EStructuralFeature reference, String id, ScopeResolutionOptions options) {
        crossrefTasks.add(new LinkerTask(host, reference, id, options));
    }

    public void linker(){
        while(crossrefTasks.removeIf(task -> task.apply())){}

        if(crossrefTasks.size()>0){
            StringBuilder b = new StringBuilder();
            crossrefTasks.forEach(task -> {
                b.append(System.lineSeparator());
                b.append(task.toString());
                
            });
            throw new RuntimeException("Unable to resolve tasks:"+b.toString());
        }
    }
    
    @Override
    public void set(EObject host, EStructuralFeature reference, EObject value) {
        modificationTasks.add(new ModificationTask(host, reference, value));
    }
    public void modify(){
        while(modificationTasks.removeIf(task -> task.apply())){}

        if(modificationTasks.size()>0){
            StringBuilder b = new StringBuilder();
            modificationTasks.forEach(task -> {
                b.append(System.lineSeparator());
                b.append(task.toString());
                
            });
            throw new RuntimeException("Unable to modify tasks:"+b.toString());
        }
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

        @Override
        public Boolean apply() {
            return resolver.link(source, feature, id, options);
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

        private ModificationTask(EObject host, EStructuralFeature feature, EObject value){
            this.host = host;
            this.feature = feature;
            this.value = value;
        }

        @Override
        public Boolean apply() {
            host.eSet(feature, value);
            return true;
        }

        @Override
        public String toString() {
            return "Modification task: '"+ feature.getName() + "' to '" + value + "' in " + host.toString();
        }
    }

    
}
