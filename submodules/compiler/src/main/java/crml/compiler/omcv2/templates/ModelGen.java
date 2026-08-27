package crml.compiler.omcv2.templates;

import crml.compiler.omcv2.scopes.ModelScope;
import crml.compiler.omcv2.scopes.Scope;
import crml.model.language.Model;
import crml.model.language.Operator;
import crml.model.language.Variable;

public class ModelGen {
    public Scope generate(Model model){
        ModelScope scope = new ModelScope(model.getName());

        for(Variable variable : model.getVariables()){
            VariableGen.generate(scope, variable);
        }
        
        for(Operator operator :model.getOperators()){

        }

        return scope;
    }
}
