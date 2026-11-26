package org.openmodelica.crml.language.dom.builders;

import crml.language.grammar.crmlParser.ExpContext;

import org.openmodelica.crml.language.dom.BuildContext;
import org.openmodelica.crml.language.scope.BuiltIns;
import org.openmodelica.crml.language.dom.util.BuildResult.SingleBuildResult;
import org.openmodelica.crml.model.metamodel.BuiltinUnaryOperatorKind;
import org.openmodelica.crml.model.metamodel.CRMLFactory;
import org.openmodelica.crml.model.metamodel.UnaryOperator;
import org.openmodelica.crml.model.metamodel.Value;

public class UnaryExpressionBuilder {
    private final BuildContext builder;
    private final CRMLFactory factory;

    public UnaryExpressionBuilder(BuildContext builder) { 
        this.builder = builder; 
        this.factory = builder.factory();
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
        BuiltinUnaryOperatorKind kind = BuiltIns.UNARY_OPERATORS.get(text);
        if (kind == null) {
            throw new IllegalStateException("Unary operator is not recognized: "+text);
        }
        return kind;
    }
}
