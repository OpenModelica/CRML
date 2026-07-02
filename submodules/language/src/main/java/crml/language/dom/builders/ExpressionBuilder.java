package crml.language.dom.builders;

import org.eclipse.emf.ecore.EClass;

import crml.language.dom.BuildContext;
import crml.language.dom.util.BuildResult.SingleBuildResult;
import crml.language.grammar.crmlParser.DurationContext;
import crml.language.grammar.crmlParser.ExpContext;
import crml.language.grammar.crmlParser.If_expContext;
import crml.language.grammar.crmlParser.IntegrateContext;
import crml.language.grammar.crmlParser.Period_opContext;
import crml.model.language.BinaryOperator;
import crml.model.language.BuiltinBinaryOperatorKind;
import crml.model.language.BuiltinUnaryOperatorKind;
import crml.model.language.DurationValue;
import crml.model.language.IfValue;
import crml.model.language.IntegrateValue;
import crml.model.language.LanguageFactory;
import crml.model.language.PeriodsValue;
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
        } else if(context.period_op() != null) {
            Period_opContext percontext = context.period_op();
            PeriodsValue periods = factory.createPeriodsValue();

            periods.setIsStartInclusive(percontext.lb.getText().equals("["));

            SingleBuildResult<?> start = builder.build(percontext.exp(0), SingleBuildResult.class);
            periods.setStartValue(start.<Value>result());

            SingleBuildResult<?> end = builder.build(percontext.exp(1), SingleBuildResult.class);
            periods.setEndValue(end.<Value>result());

            periods.setIsEndInclusive(percontext.rb.getText().equals("]"));
            return periods;
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
        } else if(context.integrate() != null) {
            IntegrateContext intcontext = context.integrate();
            IntegrateValue integrate = factory.createIntegrateValue();

            SingleBuildResult<?> inedgrand = builder.build(intcontext.exp(0), SingleBuildResult.class);
            integrate.setIntegrand(inedgrand.<Value>result());

            SingleBuildResult<?> interval = builder.build(intcontext.exp(1), SingleBuildResult.class);
            integrate.setInterval(interval.<Value>result());

            return integrate;
        } else if(context.if_exp() != null) {
            If_expContext ifexp = context.if_exp();
            IfValue ifvalue = factory.createIfValue();

            SingleBuildResult<?> condexp = builder.build(ifexp.if_e, SingleBuildResult.class);
            ifvalue.setCondition(condexp.<Value>result());

            SingleBuildResult<?> thenexp = builder.build(ifexp.then_e, SingleBuildResult.class);
            ifvalue.setThen(thenexp.<Value>result());
            if(ifexp.else_e!=null){
                SingleBuildResult<?> elseexp = builder.build(ifexp.else_e, SingleBuildResult.class);
                ifvalue.setOthervise(elseexp.<Value>result());
            }
            return ifvalue;
        } else if(context.duration() != null) {
            DurationContext durcontext = context.duration();
            DurationValue duration = factory.createDurationValue();

            SingleBuildResult<?> exp1 = builder.build(durcontext.exp(0), SingleBuildResult.class);
            duration.setExp1(exp1.<Value>result());

            SingleBuildResult<?> exp2 = builder.build(durcontext.exp(1), SingleBuildResult.class);
            duration.setExp2(exp2.<Value>result());

            return duration;
        } else {
            builder.reportError("Unable to process value: "+context.getText());
            return null;
        }
    }

    private static BuiltinUnaryOperatorKind resolveUnaryOpCode(String text){
        switch (text) {
            case "sin": return BuiltinUnaryOperatorKind.SIN;
            case "not": return BuiltinUnaryOperatorKind.NOT;
            case "-": return BuiltinUnaryOperatorKind.SUB;
            case "+": return BuiltinUnaryOperatorKind.ADD;
            default:
                throw new IllegalStateException("Unary operator is not recognized: "+text);
        }
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
