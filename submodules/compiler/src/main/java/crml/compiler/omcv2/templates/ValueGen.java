package crml.compiler.omcv2.templates;


import crml.compiler.omcv2.scopes.ModelScope;
import crml.compiler.omcv2.scopes.Scope;
import crml.compiler.omcv2.templates.value.BinaryOperatorGen;
import crml.compiler.omcv2.templates.value.UnaryOperatorGen;
import crml.model.language.BinaryOperator;
import crml.model.language.UnaryOperator;
import crml.model.language.Value;

public class ValueGen {

    public static Scope generate(ModelScope host, Value definition) {
        if (definition instanceof BinaryOperator) {
            return BinaryOperatorGen.generate(host, (BinaryOperator) definition);
        } else if (definition instanceof UnaryOperator) {
            return UnaryOperatorGen.generate(host, (UnaryOperator) definition);
        } else {
            throw new UnsupportedOperationException("Unimplemented value kind: " + definition.getClass().getSimpleName());
        }
    }
}