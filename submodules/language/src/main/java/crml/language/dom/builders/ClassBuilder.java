package crml.language.dom.builders;

import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

import crml.language.dom.BuildContext;
import crml.language.dom.util.BuildResult.SingleBuildResult;
import crml.language.grammar.crmlParser.Class_defContext;
import crml.language.grammar.crmlParser.Class_var_defContext;
import crml.language.grammar.crmlParser.ExtensionContext;
import crml.model.language.Class;
import crml.model.language.LanguageFactory;
import crml.model.language.Variable;

import static crml.language.dom.builders.util.DomUtils.text;

public class ClassBuilder {
    private final BuildContext builder;
    private final LanguageFactory factory;
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
                cls.getVaraibles().add((Variable) res);
//            } else if(res instanceof crml.model.language.Class) {
//                model.getClasses().add((Class) res);
            } else {
                builder.reportError("Element type was not recognized:: "+ elemets.getClass().getSimpleName());
            }
        }

        return cls;
    }
}
