package crml.language.dom.builders;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;

import crml.language.dom.BuildContext;
import crml.language.dom.util.BuildResult.MultiBuildResult;
import crml.language.dom.util.BuildResult.SingleBuildResult;
import crml.language.grammar.crmlParser.DefinitionContext;
import crml.language.grammar.crmlParser.DependencyContext;
import crml.language.grammar.crmlParser.DependencySetContext;
import crml.language.grammar.crmlParser.Element_defContext;
import crml.language.grammar.crmlParser.IdContext;
import crml.language.grammar.crmlParser.SingleDependencyContext;
import crml.model.language.Dependency;
import crml.model.language.LanguageFactory;
import crml.model.language.Model;
import crml.model.language.ProxyDependency;
import crml.model.language.Variable;

public class RootBuilder {
    private final BuildContext builder;
    private final LanguageFactory factory;
    public RootBuilder(BuildContext builder) { 
        this.builder = builder; 
        this.factory = builder.factory();
    }
    
    public EObject root(DefinitionContext context){
        String type = context.definition_type().getText();
        switch (type) {
            case "model":
                return processModel(context);
                //break;
            case "library":
                throw new UnsupportedOperationException("Loading a library is not implemented yet");
                //break;
            case "package":
                throw new UnsupportedOperationException("Loading a package is not implemented yet");
                //break;
            default:
                throw new RuntimeException("Unknown definition type: "+type);
        }
    }

    private Model processModel(DefinitionContext context){
        Model model = factory.createModel();
        
        for(DependencyContext depcontext : context.dependency()){
            model.getSuperlibs().addAll(parse(depcontext));
        }

        for(Element_defContext elemets : context.element_def()){
            EObject res = builder.build(elemets, SingleBuildResult.class).result();
            if(res instanceof Variable){
                model.getVaraibles().add((Variable) res);
            } else if (res instanceof crml.model.language.Object) {
                model.getObjects().add((crml.model.language.Object)res);
            } 
            throw new IllegalStateException("Element type was not recognized: "+ elemets.getClass().getSimpleName());
        }
        return model;
    }

    public List<Dependency> parse(DependencyContext context){
        List<Dependency> dependencies = new ArrayList<>();

        if(context instanceof SingleDependencyContext){
            SingleDependencyContext sdpc = (SingleDependencyContext) context;
            ProxyDependency dependecy = factory.createProxyDependency();
            dependecy.setName(sdpc.id().getText());

            dependencies.add(dependecy);
            return dependencies;
        }
        if(context instanceof DependencySetContext){
            DependencySetContext dsc =  (DependencySetContext) context;
            for(IdContext id : dsc.id()){
                ProxyDependency dependency = factory.createProxyDependency();
                dependency.setName(id.getText());
                dependencies.add(dependency);
            }
            return dependencies;
        }
        throw new IllegalStateException("Unknown dependency type: "+context.getClass().getSimpleName());
    }
}
