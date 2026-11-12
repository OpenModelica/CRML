package org.openmodelica.crml.language.dom.builders;

import static org.openmodelica.crml.language.dom.builders.util.DomUtils.text;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

import crml.language.grammar.crmlParser.Class_defContext;
import crml.language.grammar.crmlParser.Class_var_defContext;
import org.openmodelica.crml.model.metamodel.Class;
import org.openmodelica.crml.language.dom.BuildContext;
import org.openmodelica.crml.language.dom.util.BuildResult.SingleBuildResult;
import org.openmodelica.crml.model.metamodel.CRMLFactory;
import org.openmodelica.crml.model.metamodel.Variable;

public class ClassBuilder {
    private final BuildContext builder;
    private final CRMLFactory factory;
    private final EClass crmlclass;

    public ClassBuilder(BuildContext builder) { 
        this.builder = builder; 
        this.factory = builder.factory();
        this.crmlclass = builder.metamodel().getClass_();
    }

    public Class buildClass(Class_defContext context){
        Class cls = factory.createClass();
        cls.setName(text(context.id()));

        cls.setPartial(context.partial!=null);

        //TODO: Do classes only have one superclass?
        if(context.extension()!=null){
            builder.link(
                cls, 
                crmlclass.getEStructuralFeature("superClasses"), 
                context.extension().type().getText()
            );
            //TODO: extension properties are missing
        }
        
        for(Class_var_defContext elemets : context.class_var_def()){
            SingleBuildResult<?> buildres = builder.build(elemets, SingleBuildResult.class);
            if(buildres == null) {
                continue;
            }
                

            EObject res = buildres.result();
            if(res instanceof Variable){
                cls.getVariables().add((Variable) res);
//            } else if(res instanceof org.openmodelica.crml.model.metamodel.Class) {
//                model.getClasses().add((Class) res);
            } else {
                builder.reportError("Element type was not recognized:: "+ elemets.getClass().getSimpleName());
            }
        }

        return cls;
    }
}
