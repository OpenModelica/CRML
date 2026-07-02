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
import crml.model.language.VaraibleReference;

public class ExpressionBuilder {
    private final BuildContext builder;
    private final LanguageFactory factory;
    private final EClass varref;

    public ExpressionBuilder(BuildContext builder) { 
        this.builder = builder; 
        this.factory = builder.factory();
        this.varref = builder.metamodel().getVaraibleReference();
    }

    public Value value(ExpContext context){
        if(context.sub_exp() != null) {
            UnaryOperator unop = factory.createUnaryOperator();
            unop.setOptype(BuiltinUnaryOperatorKind.SUBEXPRESSION);

            SingleBuildResult<?> buildres = builder.build(context.sub_exp().exp(), SingleBuildResult.class);
            unop.setRhs(buildres.<Value>result());
            return unop;
        } else if(context.constant()!=null){
            SingleBuildResult<?> buildres = builder.build(context.constant(), SingleBuildResult.class);
            return buildres.<Value>result();
        } else if(context.constructor() != null) {
            return (Value) builder.build(context.constructor(), SingleBuildResult.class).<Value>result();
        } else if (context.lunary != null && context.left != null ) {
            UnaryOperator unop = factory.createUnaryOperator();
            unop.setOptype(resolveUnaryOpCode(context.lunary.getText()));

            SingleBuildResult<?> buildres = builder.build(context.left, SingleBuildResult.class);
            unop.setRhs(buildres.<Value>result());
            return unop;
        } else if (context.left!=null && context.binary!=null && context.right!=null) {
            BinaryOperator binop = factory.createBinaryOperator();
            binop.setOptype(resolveBinaryOpCode(context.binary.getText()));

            SingleBuildResult<?> buildres1 = builder.build(context.left, SingleBuildResult.class);
            binop.setLhs(buildres1.<Value>result());

            SingleBuildResult<?> buildres2 = builder.build(context.right, SingleBuildResult.class);
            binop.setRhs(buildres2.<Value>result());

            return binop;
        } else if(context.id() != null) {
            VaraibleReference ref = factory.createVaraibleReference();
            builder.link(ref, varref.getEStructuralFeature("variable"), context.id().getText());
            return ref;
        } else {
            builder.reportError("Unable to process value: "+context.getText());
            return null;
        }
    }

    private static BuiltinUnaryOperatorKind resolveUnaryOpCode(String text){
        switch (text) {
            case "sin": return BuiltinUnaryOperatorKind.SIN;
            case "not": return BuiltinUnaryOperatorKind.NOT;
            default:
                throw new IllegalStateException("Unary operator is not recognized: "+text);
        }
    }

    private static BuiltinBinaryOperatorKind resolveBinaryOpCode(String text){
        switch (text) {
            case "<": return BuiltinBinaryOperatorKind.LT;
            case ">": return BuiltinBinaryOperatorKind.GT;
            case "at": return BuiltinBinaryOperatorKind.AT;
            case "==": return BuiltinBinaryOperatorKind.EQ;
        
            default:
                throw new IllegalStateException("Binary operator is not recognized: "+text);
        }
    }
}
