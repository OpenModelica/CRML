package crml.compiler.omcv2.templates;

import crml.compiler.omcv2.scopes.ModelScope;
import crml.compiler.omcv2.scopes.RawstringScope;
import crml.compiler.omcv2.scopes.Scope;
import crml.compiler.omcv2.util.TypeResolver;
import crml.model.language.Variable;

public class VariableGen {
    public static Scope generate(ModelScope scope, Variable variable){
        RawstringScope decl = new RawstringScope(TypeResolver.resolve(variable.getDomain())+" "+variable.getName());
        scope.addVariable(decl);

        //TODO: configure value

        return scope;
    }
}
