package crml.language.dom.builders;

import org.eclipse.emf.ecore.EClass;

import crml.language.dom.BuildContext;
import crml.language.grammar.crmlParser.TypeContext;
import crml.language.grammar.crmlParser.Var_defContext;
import crml.model.language.LanguageFactory;
import crml.model.language.LanguagePackage;
import crml.model.language.Variable;

public class VariableBuilder {
    private final BuildContext builder;
    private final LanguageFactory factory;
    private final LanguagePackage metamodel;
    private final EClass vtype;
    
    public VariableBuilder(BuildContext builder) { 
        this.builder = builder; 
        this.metamodel = builder.metamodel(); 
        this.factory = builder.factory();
        this.vtype = metamodel.getVariable();
    }

    public Variable variable(Var_defContext context){
        //TODO: what is fixed qualifier?
        Variable var = factory.createVariable();
        builder.link(
                var, 
                vtype.getEStructuralFeature("domain"), 
                resolveType(context.type())
        );
        var.setName(context.id().getText());
        //TODO: Arglist
        //TODO: Value
        
        return var;

    }

    private String resolveType(TypeContext context){
        if(context.builtin_type() != null){
            return context.builtin_type().getText();
        }
        if(context.id() != null) {
            return context.id().getText();
        }
        throw new IllegalStateException("Unable to get type id.");
    }
}
