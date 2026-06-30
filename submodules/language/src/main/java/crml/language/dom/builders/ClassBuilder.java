package crml.language.dom.builders;

import java.util.List;

import crml.language.dom.BuildContext;
import crml.language.grammar.crmlParser.Class_defContext;
import crml.language.grammar.crmlParser.ExtensionContext;
import crml.model.language.Class;
import crml.model.language.ClassDependency;
import crml.model.language.LanguageFactory;

public class ClassBuilder {
    private final BuildContext builder;
    private final LanguageFactory factory;
    public ClassBuilder(BuildContext builder) { 
        this.builder = builder; 
        this.factory = builder.factory();
    }

    public Class buildClass(Class_defContext context){
        Class cls = factory.createClass();
        cls.setName(context.id().getText());

        //TODO: Do classes only have one superclass?
        cls.getSuperClasses().add(processDependencies(context.extension()));
        return cls;
    }

    private ClassDependency processDependencies(ExtensionContext context){
        ClassDependency dep = factory.createClassDependency();
        dep.setName(context.type().getText()); //TODO: Absolutely incorrect but good for a placeholder
        return dep;
    }

}
