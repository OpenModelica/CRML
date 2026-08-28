package crml.compiler.omcv2.templates;



import javax.management.RuntimeErrorException;

import crml.compiler.omcv2.scopes.FunctionScope;
import crml.compiler.omcv2.scopes.ModelScope;
import crml.compiler.omcv2.scopes.Scope;
import crml.compiler.omcv2.templates.value.BinaryOperatorGen;
import crml.compiler.omcv2.templates.value.ConstructorGen;
import crml.compiler.omcv2.templates.value.UnaryOperatorGen;
import crml.model.language.BinaryOperator;
import crml.model.language.BooleanConstant;
import crml.model.language.ConstructorValue;
import crml.model.language.IfValue;
import crml.model.language.IntegerConstant;
import crml.model.language.RealConstant;
import crml.model.language.TimeValue;
import crml.model.language.UnaryOperator;
import crml.model.language.Value;
import crml.model.language.VariableReference;

public class ValueGen {

    public static Scope generate(ModelScope host, Value definition) {
        if (definition instanceof VariableReference) {
            VariableReference vref = (VariableReference) definition;
            return new FunctionScope(vref.getVariable().getName());
        } else if (definition instanceof IfValue) {
            IfValue ival = (IfValue) definition;
            Scope cond = generate(host, ival.getCondition());
            Scope tb = generate(host, ival.getThen());
            Scope ob = generate(host, ival.getOthervise());
            //TODO: this does not check if the condition expression returns a Boolean4 or if it is a valid Boolean2 expression.
            return new FunctionScope("if ("+ cond.reference()+" == CRMLtoModelica.Types.Boolean4.true4) then "+tb.reference()+" else "+ob.reference());
            //return new FunctionScope("if "+ cond.reference()+" then "+tb.reference()+" else "+ob.reference());
        } else if (definition instanceof BooleanConstant) {
            BooleanConstant boolconst = (BooleanConstant) definition;
            switch(boolconst.getValue()){
                case TRUE: return new FunctionScope("CRMLtoModelica.Types.Boolean4.true4");
                case FALSE: return new FunctionScope("CRMLtoModelica.Types.Boolean4.false4");
                case UNDECIDED: return new FunctionScope("CRMLtoModelica.Types.Boolean4.undecided");
                case UNDEFINED: return new FunctionScope("CRMLtoModelica.Types.Boolean4.undefined");
                default: throw new RuntimeException("Unknown boolean literal: "+boolconst.getValue());
            }
        } else if (definition instanceof RealConstant) {
            RealConstant realconst = (RealConstant) definition;
            if(realconst.getLiteral()!=null){
                return new FunctionScope(realconst.getLiteral());
            } else {
                return new FunctionScope(realconst.getValue().toString());
            }
        } else if (definition instanceof IntegerConstant) {
            IntegerConstant intconst = (IntegerConstant) definition;
            return new FunctionScope(intconst.getValue().toString());
        } else if (definition instanceof TimeValue) {
            return new FunctionScope("time");
        } else if (definition instanceof BinaryOperator) {
            return BinaryOperatorGen.generate(host, (BinaryOperator) definition);
        } else if (definition instanceof UnaryOperator) {
            return UnaryOperatorGen.generate(host, (UnaryOperator) definition);
        } else if (definition instanceof ConstructorValue) {
            return ConstructorGen.generate(host, (ConstructorValue) definition);
        } else {
            throw new UnsupportedOperationException("Unimplemented value kind: " + definition.getClass().getSimpleName());
        }
    }
}