package crml.language.dom.builders;

import org.eclipse.emf.ecore.EClass;

import crml.language.dom.BuildContext;
import crml.language.dom.util.BuildResult.SingleBuildResult;
import crml.language.grammar.crmlParser.DurationContext;
import crml.language.grammar.crmlParser.ExpContext;
import crml.language.grammar.crmlParser.If_expContext;
import crml.language.grammar.crmlParser.IntegrateContext;
import crml.language.grammar.crmlParser.Period_opContext;
import crml.language.grammar.crmlParser.Set_defContext;
import crml.model.language.BinaryOperator;
import crml.model.language.BuiltinBinaryOperatorKind;
import crml.model.language.BuiltinUnaryOperatorKind;
import crml.model.language.DurationValue;
import crml.model.language.IfValue;
import crml.model.language.IntegrateValue;
import crml.model.language.LanguageFactory;
import crml.model.language.PeriodsValue;
import crml.model.language.ProjectionValue;
import crml.model.language.Sequence;
import crml.model.language.SequenceKeyword;
import crml.model.language.SequenceValue;
import crml.model.language.Set;
import crml.model.language.UnaryOperator;
import crml.model.language.Value;
import crml.model.language.VaraibleReference;

public class ExpressionBuilder {
    private final BuildContext builder;
    private final LanguageFactory factory;
    private final EClass varref;
    private final BinaryExpressionBuilder binary;
    private final UnaryExpressionBuilder unary;
    
    
    public ExpressionBuilder(BuildContext builder) { 
        this.builder = builder; 
        this.factory = builder.factory();
        this.varref = builder.metamodel().getVaraibleReference();
        this.binary = new BinaryExpressionBuilder(builder);
        this.unary = new UnaryExpressionBuilder(builder);
    }

    public Value value(ExpContext context){
        if(context.sub_exp() != null) {
            UnaryOperator unop = factory.createUnaryOperator();
            unop.setOptype(BuiltinUnaryOperatorKind.SUBEXPRESSION);

            SingleBuildResult<?> buildres = builder.build(context.sub_exp().exp(), SingleBuildResult.class);
            unop.setValue(buildres.<Value>result());
            return unop;
        } else if(context.constant()!=null){
            SingleBuildResult<?> buildres = builder.build(context.constant(), SingleBuildResult.class);
            return buildres.<Value>result();
        } else if(context.constructor() != null) {
            return (Value) builder.build(context.constructor(), SingleBuildResult.class).<Value>result();
        }else if(context.p1!= null && context.p2!= null) {
            ProjectionValue proj = factory.createProjectionValue();

            SingleBuildResult<?> p1 = builder.build(context.p1, SingleBuildResult.class);
            proj.setP1(p1.<Value>result());
            SingleBuildResult<?> p2 = builder.build(context.p2, SingleBuildResult.class);
            proj.setP2(p2.<Value>result());
            if(context.opt!=null){
                SingleBuildResult<?> opt = builder.build(context.opt, SingleBuildResult.class);
                proj.setOpt(opt.<Value>result());
            }
            return proj;
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
        } else if (unary.test(context)) {
            return unary.get(context);
        } else if (binary.test(context)) {
            return binary.get(context);
        } else if (context.keyword!=null && (context.lhs!=null || context.rhs!=null)) {
            Sequence head = null;

            if(context.lhs!=null){
                SingleBuildResult<?> lhsr = builder.build(context.lhs, SingleBuildResult.class);
                Object obj = lhsr.result;
                if(obj instanceof Sequence){
                    head = (Sequence) obj;
                } else {
                    SequenceValue val = factory.createSequenceValue();
                    val.setValue((Value) obj);
                    head = val;
                }
            }

            SequenceKeyword kw = factory.createSequenceKeyword();
            kw.setKeyword(context.keyword.USER_KEYWORD().getText());
            
            if(head==null){
                head = kw;
            } else {
                head.tail().setNext2(kw);
            }

            if(context.rhs!=null){
                SingleBuildResult<?> rhsr = builder.build(context.rhs, SingleBuildResult.class);
                Object obj = rhsr.result;
                if(obj instanceof Sequence){
                    kw.setNext((Sequence) obj);
                } else {
                    SequenceValue val = factory.createSequenceValue();
                    val.setValue((Value) obj);
                    kw.setNext(val);
                }
            }
            return head;

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
        } else if(context.set_def() != null) {
            Set_defContext setcontext = context.set_def();
            Set<Value> set = factory.<Value>createSet();
            for(ExpContext exp : setcontext.exp()) {
                SingleBuildResult<?> val = builder.build(exp, SingleBuildResult.class);
                set.getElements().add(val.<Value>result());
            }
            
            return set;
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

    

    
}
