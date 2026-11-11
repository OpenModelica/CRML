package crml.language.dom.builders;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;

import crml.language.dom.BuildContext;
import crml.language.dom.util.BuildResult.SingleBuildResult;
import crml.language.grammar.crmlParser.ExpContext;
import crml.model.language.BinaryOperator;
import crml.model.language.Binding;
import crml.model.language.BuiltinUnaryOperatorKind;
import crml.model.language.ComputedValue;
import crml.model.language.UnaryOperator;
import crml.model.language.LanguageFactory;
import crml.model.language.Value;
import crml.model.language.ScatteredValue;
import crml.model.language.Liftable;

public class LiftBuilder {
    private final BuildContext builder;
    private final LanguageFactory factory;

    public LiftBuilder(BuildContext builder) { 
        this.builder = builder; 
        this.factory = builder.factory();
    }

    public ScatteredValue get(ExpContext context){
        ScatteredValue scattered = factory.createScatteredValue();

        SingleBuildResult<?> buildres2 = builder.build(context.collection, SingleBuildResult.class);
        scattered.setData(buildres2.<Value>result());

        return scattered;
    }

    public static List<Liftable> linkAll(EObject root){
        List<Liftable> linked = new ArrayList<>();
        TreeIterator<EObject> it = EcoreUtil.getAllContents(root, true);
        while(it.hasNext()){
            EObject obj = it.next();
            if(obj instanceof ScatteredValue){
                Liftable liftable = link((ScatteredValue) obj);
                if(liftable!=null){
                    linked.add(liftable);
                }
            }
        }
        return linked;
    }

    /**
     * Links an {@code .element} to the application it is a direct operand of,
     * and returns that application, or null if there is none.
     *
     * <ul>
     * <li>An argument of an operator call links to the call, an operand of a
     * built-in unary or binary operator links to that operator.
     * <li>Parentheses around the {@code .element} are ignored. <b>Experimental,
     * subject to change.</b>
     * <li>The operand of an aggregator ({@code and (...)}, a binary operator
     * with no left hand side) is not linked: it denotes a set, which the
     * aggregator consumes.
     * <li>Under any other value ({@code if}, {@code integrate}, {@code new}, a
     * set literal, a variable definition, another {@code .element}, ...) it is
     * not linked, and the generator reports it. <b>Experimental, subject to
     * change.</b>
     * </ul>
     *
     * The lifts of an application are kept in operand order: the order of the
     * formals for a call, left hand side before right hand side for a binary
     * operator.
     */
    public static Liftable link(ScatteredValue scatteredValue){
        Liftable application = application(scatteredValue);
        if(application==null){
            return null;
        }
        if(scatteredValue.getMethod()==application){
            return application;
        }
        int position = position(scatteredValue);
        List<ScatteredValue> lifts = application.getLifts();
        int index = 0;
        while(index<lifts.size() && position(lifts.get(index))<=position){
            index++;
        }
        // The opposite sets scatteredValue.method.
        lifts.add(index, scatteredValue);
        return application;
    }

    /** The application the value is a direct operand of, or null. */
    private static Liftable application(ScatteredValue scatteredValue){
        EObject container = operandSlot(scatteredValue).eContainer();
        if(container instanceof Binding && container.eContainer() instanceof ComputedValue){
            return (ComputedValue) container.eContainer();
        }
        if(container instanceof UnaryOperator){
            return (UnaryOperator) container;
        }
        if(container instanceof BinaryOperator && ((BinaryOperator) container).getLhs()!=null){
            return (BinaryOperator) container;
        }
        return null;
    }

    /** The operand position of the value in its {@link #application}. */
    private static int position(ScatteredValue scatteredValue){
        EObject child = operandSlot(scatteredValue);
        EObject container = child.eContainer();
        if(container instanceof Binding && container.eContainer() instanceof ComputedValue){
            Binding binding = (Binding) container;
            ComputedValue call = (ComputedValue) container.eContainer();
            int formal = call.getOperator()==null ? -1
                : call.getOperator().getVariables().indexOf(binding.getElement());
            // The bindings of a call are created in formal order.
            return formal>=0 ? formal : call.getBindings().indexOf(binding);
        }
        if(container instanceof BinaryOperator){
            return child==((BinaryOperator) container).getLhs() ? 0 : 1;
        }
        return 0;
    }

    /** The value, or the outermost parentheses that hold nothing but the value. */
    private static EObject operandSlot(ScatteredValue scatteredValue){
        EObject child = scatteredValue;
        while(isParentheses(child.eContainer())){
            child = child.eContainer();
        }
        return child;
    }

    private static boolean isParentheses(EObject object){
        return object instanceof UnaryOperator
            && ((UnaryOperator) object).getOptype()==BuiltinUnaryOperatorKind.SUBEXPRESSION;
    }
}
