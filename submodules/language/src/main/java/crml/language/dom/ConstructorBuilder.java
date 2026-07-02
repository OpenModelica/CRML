package crml.language.dom;

import java.util.List;

import org.eclipse.emf.ecore.EClass;

import crml.language.dom.util.BuildResult.SingleBuildResult;
import crml.language.grammar.crmlParser.ConstructorContext;
import crml.language.grammar.crmlParser.ExpContext;
import crml.language.grammar.crmlParser.IdContext;
import crml.model.language.Binding;
import crml.model.language.ConstructorValue;
import crml.model.language.LanguageFactory;
import crml.model.language.TypeReference;
import crml.model.language.Value;

public class ConstructorBuilder {
    private final BuildContext builder;
    private final LanguageFactory factory;
    private final EClass cc;
    private final EClass bc;

    public ConstructorBuilder(BuildContext builder) {
        this.builder = builder;
        this.factory = builder.factory();
        this.cc = builder.metamodel().getConstructorValue();
        this.bc = builder.metamodel().getBinding();
    }

    public ConstructorValue get(ConstructorContext context){
        ConstructorValue constval = factory.createConstructorValue();

        TypeReference typeref = (TypeReference) builder.build(context.type(), SingleBuildResult.class).<TypeReference>result();
        constval.setDomain(typeref);

        if(context.arg_list() != null){
            List<IdContext> ids = context.arg_list().id();
            List<ExpContext> exps = context.arg_list().exp();

            for(int i = 0; i < ids.size(); i++){
                Binding binding = factory.createBinding();
                builder.link(binding, bc.getEStructuralFeature("element"), ids.get(i).getText());

                Value value = (Value) builder.build(exps.get(i), SingleBuildResult.class).<Value>result();
                binding.setValue(value);

                constval.getBindings().add(binding);
            }
        } else if (context.exp() != null){
            Value value = (Value) builder.build(context.exp(), SingleBuildResult.class).<Value>result();
            constval.setValue(value);
        } else {
            throw new IllegalStateException("Unreachable: constructor needs either a parameter list or a single value.");
        }
        return constval;
    }
}
