package crml.compiler.omcv2.templates;

import java.util.ArrayList;
import java.util.List;

import crml.compiler.omcv2.scopes.ModelScope;
import crml.compiler.omcv2.scopes.RawstringScope;
import crml.compiler.omcv2.scopes.Scope;
import crml.compiler.omcv2.util.TypeResolver;
import crml.model.language.OperatorHeaderElement;
import crml.model.language.Template;
import crml.model.language.Variable;

public interface TemplateGen {
    public default Scope generate(Template template) {
        ModelScope scope = new ModelScope(template.getName());

        for (OperatorHeaderElement element : template.getHeader()) {
            if(element instanceof Variable){
                Variable variable = (Variable) element;
                RawstringScope varscope = new RawstringScope(TypeResolver.resolve(variable.getDomain())+" "+variable.getName()+";");
                scope.addVariable(varscope);
            }
            
        }
        scope.addVariable(new RawstringScope(TypeResolver.resolve(template.getDomain())+" out;"));
        scope.addEquation(new RawstringScope("out = "+ValueGen.generate(scope, template.getDefinition())));

        return scope;
    }
}
