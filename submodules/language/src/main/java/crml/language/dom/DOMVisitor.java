package crml.language.dom;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure0;

import crml.language.dom.builders.ClassBuilder;
import crml.language.dom.builders.RootBuilder;
import crml.language.dom.builders.TypeReferenceBuilder;
import crml.language.dom.builders.VariableBuilder;
import crml.language.dom.util.BuildResult;
import crml.language.dom.util.ScopeResolver;
import crml.language.grammar.crmlBaseVisitor;
import crml.language.grammar.crmlParser.Class_defContext;
import crml.language.grammar.crmlParser.DefinitionContext;
import crml.language.grammar.crmlParser.TypeContext;
import crml.language.grammar.crmlParser.Var_defContext;

public class DOMVisitor extends crmlBaseVisitor<BuildResult> implements BuildContext {
    private final ScopeResolver resolver = new ScopeResolver();
    private final List<Procedure0> tasks = new ArrayList<>();

    private final RootBuilder root = new RootBuilder(this);
    private final ClassBuilder cls = new ClassBuilder(this);
    private final VariableBuilder vars = new VariableBuilder(this);
    private final TypeReferenceBuilder typeref = new TypeReferenceBuilder(this);

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
    @Override
    public void link(EObject host, EStructuralFeature reference, String id, EClass targetType) {
        tasks.add(() -> {
            if(!resolver.link(host, reference, id, targetType)){
                reportError("Unable to resolve refernce '"+reference.getName()+"' to '"+ id+ "' in "+host);
            }
        });
    }

    public void linker(){
        tasks.forEach(it -> it.apply());
        tasks.clear();
    }
}
