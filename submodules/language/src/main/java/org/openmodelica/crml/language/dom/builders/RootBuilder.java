package org.openmodelica.crml.language.dom.builders;

import static org.openmodelica.crml.language.dom.builders.util.DomUtils.text;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EObject;

import crml.language.grammar.crmlParser.DefinitionContext;
import crml.language.grammar.crmlParser.DependencyContext;
import crml.language.grammar.crmlParser.Element_defContext;
import org.openmodelica.crml.model.metamodel.Class;
import org.openmodelica.crml.model.metamodel.Dependency;
import org.openmodelica.crml.language.dom.BuildContext;
import org.openmodelica.crml.language.dom.util.BuildResult.SingleBuildResult;
import org.openmodelica.crml.model.metamodel.CRMLFactory;
import org.openmodelica.crml.model.metamodel.Library;
import org.openmodelica.crml.model.metamodel.Model;
import org.openmodelica.crml.model.metamodel.Variable;

public class RootBuilder {
    private final BuildContext builder;
    private final CRMLFactory factory;
    public RootBuilder(BuildContext builder) { 
        this.builder = builder; 
        this.factory = builder.factory();
    }
    
    public EObject root(DefinitionContext context){
        String type = context.definition_type().getText();
        switch (type) {
            case "model":
                return processModel(context);
            case "library":
                return processLibrary(context);
            case "package":
                builder.reportImplementationError("Loading a package is not implemented yet");
                break;
            default:
                builder.reportError("Unknown definition type: "+type);
        }
        return null;
    }

    private Model processModel(DefinitionContext context){
        Model model = factory.createModel();
        
        model.setName(text(context.id()));

        for(DependencyContext depcontext : context.dependency()){
            model.getSuperlibs().addAll(parse(depcontext));
        }

        for(Element_defContext elemets : context.element_def()){
            SingleBuildResult<?> buildres = builder.build(elemets, SingleBuildResult.class);
            if(buildres == null) {
                continue;
            }
                

            EObject res = buildres.result();
            if(res instanceof Variable){
                model.getVariables().add((Variable) res);
            } else if(res instanceof org.openmodelica.crml.model.metamodel.Class) {
                model.getClasses().add((Class) res);
            } else if (res instanceof org.openmodelica.crml.model.metamodel.Object) {
                model.getObjects().add((org.openmodelica.crml.model.metamodel.Object)res);
            } else if (res instanceof org.openmodelica.crml.model.metamodel.Operator) {
                model.getOperators().add((org.openmodelica.crml.model.metamodel.Operator)res);
            } else {
                builder.reportError("Element type was not recognized:: "+ elemets.getClass().getSimpleName());
            }
        }
        return model;
    }

    private Library processLibrary(DefinitionContext context){
        Library library = factory.createLibrary();
        
        library.setName(text(context.id()));

        for(DependencyContext depcontext : context.dependency()){
            library.getSuperlibs().addAll(parse(depcontext));
        }

        for(Element_defContext elemets : context.element_def()){
            SingleBuildResult<?> buildres = builder.build(elemets, SingleBuildResult.class);
            if(buildres == null) {
                continue;
            }
                
            EObject res = buildres.result();
            if(res instanceof org.openmodelica.crml.model.metamodel.Class) {
                library.getClasses().add((Class) res);
            } else if (res instanceof org.openmodelica.crml.model.metamodel.Operator) {
                library.getOperators().add((org.openmodelica.crml.model.metamodel.Operator)res);
            } else {
                builder.reportError("Element type was not recognized:: "+ elemets.getClass().getSimpleName());
            }
        }
        return library;
    }

    public List<Dependency> parse(DependencyContext context){
        List<Dependency> dependencies = new ArrayList<>();
        return dependencies;
        /*
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
        builder.reportError("Unknown dependency type: "+context.getClass().getSimpleName());
        return null;*/
    }
}
