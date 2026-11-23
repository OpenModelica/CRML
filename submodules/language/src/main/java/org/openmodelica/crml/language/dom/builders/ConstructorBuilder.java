package org.openmodelica.crml.language.dom.builders;

import java.util.List;

import org.eclipse.emf.ecore.EClass;

import crml.language.grammar.crmlParser.ConstructorContext;
import crml.language.grammar.crmlParser.Constructor_headContext;
import crml.language.grammar.crmlParser.ExpContext;
import crml.language.grammar.crmlParser.IdContext;
import crml.language.grammar.crmlParser.TypeContext;

import org.openmodelica.crml.language.dom.BuildContext;
import org.openmodelica.crml.language.dom.util.ScopeResolutionOptions;
import org.openmodelica.crml.language.dom.util.BuildResult.SingleBuildResult;
import org.openmodelica.crml.language.dom.util.ScopeResolutionOptions.ResolutionStrategy;
import org.openmodelica.crml.model.metamodel.Binding;
import org.openmodelica.crml.model.metamodel.ConstructorValue;
import org.openmodelica.crml.model.metamodel.CRMLFactory;
import org.openmodelica.crml.model.metamodel.TypeReference;
import org.openmodelica.crml.model.metamodel.Value;

public class ConstructorBuilder {
    private final BuildContext builder;
    private final CRMLFactory factory;
    private final EClass bc;

    public ConstructorBuilder(BuildContext builder) {
        this.builder = builder;
        this.factory = builder.factory();
        this.bc = builder.metamodel().getBinding();
    }

    /**
     * A constructor without a value: {@code new T}, the default constructor, or
     * {@code new T (x = e, ...)}, which binds the given elements.
     */
    public ConstructorValue get(ConstructorContext context){
        ConstructorValue constval = create(context.type());

        if(context.arg_list() != null){
            List<IdContext> ids = context.arg_list().id();
            List<ExpContext> exps = context.arg_list().exp();

            for(int i = 0; i < ids.size(); i++){
                Binding binding = factory.createBinding();
                builder.link(
                    binding, 
                    bc.getEStructuralFeature("element"), 
                    ids.get(i).getText(),
                    new ScopeResolutionOptions(ResolutionStrategy.CONSTRUCTOR_BINDING)
                );

                Value value = (Value) builder.build(exps.get(i), SingleBuildResult.class).<Value>result();
                binding.setValue(value);

                constval.getBindings().add(binding);
            }
        }
        // Without an argument list this is the default constructor: nothing to bind.
        return constval;
    }

    /**
     * A constructor with a value, {@code new T e}: the prefix alternative
     * {@code head=constructor_head rhs=exp} of {@code exp}.
     *
     * @param head  {@code new T}
     * @param value {@code e}; null if the parser recovered from a missing value
     */
    public ConstructorValue get(Constructor_headContext head, ExpContext value){
        ConstructorValue constval = create(head.type());
        if (value != null) {
            constval.setValue((Value) builder.build(value, SingleBuildResult.class).<Value>result());
        }
        return constval;
    }

    private ConstructorValue create(TypeContext type){
        ConstructorValue constval = factory.createConstructorValue();
        TypeReference typeref = (TypeReference) builder.build(type, SingleBuildResult.class).<TypeReference>result();
        constval.setDomain(typeref);
        return constval;
    }
}
