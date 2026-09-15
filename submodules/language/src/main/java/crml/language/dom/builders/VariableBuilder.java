package crml.language.dom.builders;

import crml.language.dom.BuildContext;
import crml.language.dom.util.BuildResult.SingleBuildResult;
import crml.language.grammar.crmlParser.Var_defContext;
import crml.model.language.LanguageFactory;
import crml.model.language.TypeReference;
import crml.model.language.Value;
import crml.model.language.Variable;

public class VariableBuilder {
    private final BuildContext builder;
    private final LanguageFactory factory;
    
    public VariableBuilder(BuildContext builder) { 
        this.builder = builder;  
        this.factory = builder.factory();
    }

    public Variable variable(Var_defContext context){
        //TODO: what is fixed qualifier?
        Variable var = factory.createVariable();
        
        var.setName(context.id().getText());

        var.setConstant(context.cnst!=null);

        var.setExternal(context.is_external!=null);

        TypeReference typeref = (TypeReference) builder.build(context.type(), SingleBuildResult.class).<TypeReference>result();
        var.setDomain(typeref);

        if(context.exp()!=null){
            Value value = (Value) builder.build(context.exp(), SingleBuildResult.class).<Value>result();
            var.setDefinition(value);
        }
        
        return var;

    }
}
