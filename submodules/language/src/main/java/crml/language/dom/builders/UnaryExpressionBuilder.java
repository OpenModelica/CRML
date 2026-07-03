package crml.language.dom.builders;

import org.eclipse.emf.ecore.EClass;

import crml.language.dom.BuildContext;
import crml.language.dom.util.BuildResult.SingleBuildResult;
import crml.language.grammar.crmlParser.ExpContext;
import crml.model.language.BinaryOperator;
import crml.model.language.BuiltinBinaryOperatorKind;
import crml.model.language.BuiltinUnaryOperatorKind;
import crml.model.language.LanguageFactory;
import crml.model.language.UnaryOperator;
import crml.model.language.Value;

public class UnaryExpressionBuilder {
    private final BuildContext builder;
    private final LanguageFactory factory;
    private final EClass varref;

    public UnaryExpressionBuilder(BuildContext builder) { 
        this.builder = builder; 
        this.factory = builder.factory();
        this.varref = builder.metamodel().getVaraibleReference();
    }

    public boolean test(ExpContext context){
        return (context.lhs!=null ^ context.rhs!=null) && (
            context.uop0!=null ^ 
            context.uop1!=null ^ 
            context.uop2!=null ^ 
            context.uop3!=null ^ 
            context.uop4!=null 
        );
    }

    public UnaryOperator get(ExpContext context){
        UnaryOperator unop = factory.createUnaryOperator();
        unop.setOptype(resolveUnaryOpCode(opcode(context)));

        if(context.rhs!=null && context.lhs==null){
            SingleBuildResult<?> buildres1 = builder.build(context.rhs, SingleBuildResult.class);
            unop.setValue(buildres1.<Value>result());
        } else if (context.lhs!=null && context.rhs==null){
            SingleBuildResult<?> buildres1 = builder.build(context.lhs, SingleBuildResult.class);
            unop.setValue(buildres1.<Value>result());
        } else {
            throw new IllegalStateException("lhs and rhs are mutually exclusive for a unary expression.");
        }
        return unop;
    }

    private static String opcode(ExpContext context){
        if(context.uop0!=null) return context.uop0.getText();
        if(context.uop1!=null) return context.uop1.getText();
        if(context.uop2!=null) return context.uop2.getText();
        if(context.uop3!=null) return context.uop3.getText();
        if(context.uop4!=null) return context.uop4.getText();
        throw new IllegalStateException("No binary operator found in context: "+context.getText());
    }
    private static BuiltinUnaryOperatorKind resolveUnaryOpCode(String text){
        switch (text) {
            case "sin": return BuiltinUnaryOperatorKind.SIN;
            case "asin": return BuiltinUnaryOperatorKind.ASIN;
            case "cos": return BuiltinUnaryOperatorKind.COS;
            case "acos": return BuiltinUnaryOperatorKind.ACOS;
            case "log10": return BuiltinUnaryOperatorKind.LOG10;
            case "log": return BuiltinUnaryOperatorKind.LOG;
            case "exp": return BuiltinUnaryOperatorKind.EXP_OP;
            case "not": return BuiltinUnaryOperatorKind.NOT;
            case "-": return BuiltinUnaryOperatorKind.SUB;
            case "+": return BuiltinUnaryOperatorKind.ADD;
            case "start": return BuiltinUnaryOperatorKind.START;
            case "end": return BuiltinUnaryOperatorKind.END;
            case "tick": return BuiltinUnaryOperatorKind.TICK;
            case "card": return BuiltinUnaryOperatorKind.CARD;
            default:
                throw new IllegalStateException("Unary operator is not recognized: "+text);
        }
    }
}
