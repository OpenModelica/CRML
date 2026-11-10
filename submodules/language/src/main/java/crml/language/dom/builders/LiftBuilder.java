package crml.language.dom.builders;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;

import crml.language.dom.BuildContext;
import crml.language.dom.util.BuildResult.SingleBuildResult;
import crml.language.grammar.crmlParser.ExpContext;
import crml.model.language.BuiltinBinaryOperatorKind;
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

    public static Liftable link(ScatteredValue scatteredValue){
        EObject container = scatteredValue.eContainer();
        while(container!=null){
            if(container instanceof Liftable){
                Liftable liftable = (Liftable) container;
                scatteredValue.setMethod(liftable);
                return liftable;
            }
            container = container.eContainer();
        }
        return null;
    }
}
