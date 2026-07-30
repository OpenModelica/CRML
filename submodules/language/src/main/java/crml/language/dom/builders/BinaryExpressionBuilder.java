package crml.language.dom.builders;

import crml.language.dom.BuildContext;
import crml.language.dom.util.BuildResult.SingleBuildResult;
import crml.language.grammar.crmlParser.ExpContext;
import crml.model.language.BinaryOperator;
import crml.model.language.BuiltinBinaryOperatorKind;
import crml.model.language.LanguageFactory;
import crml.model.language.Value;

public class BinaryExpressionBuilder {
    private final BuildContext builder;
    private final LanguageFactory factory;

    public BinaryExpressionBuilder(BuildContext builder) { 
        this.builder = builder; 
        this.factory = builder.factory();
    }

    public boolean test(ExpContext context){
        return context.lhs!=null && context.rhs!=null && (
            context.bop0!=null ^ 
            context.bop1!=null ^ 
            context.bop2!=null ^ 
            context.bop3!=null ^ 
            context.bop4!=null ^ 
            context.bop5!=null ^ 
            context.bop6!=null
        );
    }

    public BinaryOperator get(ExpContext context){
        BinaryOperator binop = factory.createBinaryOperator();
        binop.setOptype(resolveBinaryOpCode(opcode(context)));

        SingleBuildResult<?> buildres1 = builder.build(context.lhs, SingleBuildResult.class);
        binop.setLhs(buildres1.<Value>result());

        SingleBuildResult<?> buildres2 = builder.build(context.rhs, SingleBuildResult.class);
        binop.setRhs(buildres2.<Value>result());

        return binop;
    }

    private static String opcode(ExpContext context){
        if(context.bop0!=null) return context.bop0.getText();
        if(context.bop1!=null) return context.bop1.getText();
        if(context.bop2!=null) return context.bop2.getText();
        if(context.bop3!=null) return context.bop3.getText();
        if(context.bop4!=null) return context.bop4.getText();
        if(context.bop5!=null) return context.bop5.getText();
        if(context.bop6!=null) return context.bop6.getText();
        throw new IllegalStateException("No binary operator found in context: "+context.getText());
    }
    private static BuiltinBinaryOperatorKind resolveBinaryOpCode(String text){
        switch (text) {
            case "<": return BuiltinBinaryOperatorKind.LT;
            case "<=": return BuiltinBinaryOperatorKind.LE;
            case ">": return BuiltinBinaryOperatorKind.GT;
            case ">=": return BuiltinBinaryOperatorKind.GE;
            case "at": return BuiltinBinaryOperatorKind.AT;
            case "==": return BuiltinBinaryOperatorKind.EQ;
            case "<>": return BuiltinBinaryOperatorKind.NEQ;
            case "and": return BuiltinBinaryOperatorKind.AND;
            case "*": return BuiltinBinaryOperatorKind.MUL;
            case "/": return BuiltinBinaryOperatorKind.DIV;
            case "+": return BuiltinBinaryOperatorKind.ADD;
            case "-": return BuiltinBinaryOperatorKind.SUB;
            case "or": return BuiltinBinaryOperatorKind.OR;
            case "mod": return BuiltinBinaryOperatorKind.MOD;
            case "^": return BuiltinBinaryOperatorKind.POW; //TODO: check if this is correct
        
            default:
                throw new IllegalStateException("Binary operator is not recognized: "+text);
        }
    }
}
