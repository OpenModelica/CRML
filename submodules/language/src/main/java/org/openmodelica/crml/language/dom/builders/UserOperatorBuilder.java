package org.openmodelica.crml.language.dom.builders;

import crml.language.grammar.crmlParser.OperatorContext;
import crml.language.grammar.crmlParser.OperatorKeywordContext;
import crml.language.grammar.crmlParser.OperatorParameterContext;
import crml.language.grammar.crmlParser.Operator_parameterContext;
import org.openmodelica.crml.model.metamodel.Keyword;
import org.openmodelica.crml.language.dom.BuildContext;
import org.openmodelica.crml.language.dom.util.BuildResult.SingleBuildResult;
import org.openmodelica.crml.model.metamodel.CRMLFactory;
import org.openmodelica.crml.model.metamodel.TypeReference;
import org.openmodelica.crml.model.metamodel.UserOperator;
import org.openmodelica.crml.model.metamodel.Value;
import org.openmodelica.crml.model.metamodel.Variable;

public class UserOperatorBuilder {
    private final BuildContext builder;
    private final CRMLFactory factory;

    public UserOperatorBuilder(BuildContext builder) {
        this.builder = builder;
        this.factory = builder.factory();
    }

    public UserOperator get(OperatorContext context){
        UserOperator operator = factory.createUserOperator();

        TypeReference typeref = (TypeReference) builder.build(context.domain, SingleBuildResult.class).<TypeReference>result();
        operator.setDomain(typeref);

        for(Operator_parameterContext p : context.operator_def().args){
            if(p instanceof OperatorParameterContext){
                OperatorParameterContext param = (OperatorParameterContext) p;

                Variable v =  factory.createVariable();

                TypeReference paramtyperef = (TypeReference) builder.build(param.type(), SingleBuildResult.class).<TypeReference>result();
                v.setDomain(paramtyperef);
                v.setName(param.id().getText());
                operator.getHeader().add(v);
            } else if(p instanceof OperatorKeywordContext){
                OperatorKeywordContext kwc = (OperatorKeywordContext) p;

                Keyword kw = factory.createKeyword();
                kw.setKeyword(kwc.getText());
                operator.getHeader().add(kw);
            }
        }
        

        if(context.operator_def().value!=null){
            Value value = (Value) builder.build(context.operator_def().exp(), SingleBuildResult.class).<Value>result();
            operator.setDefinition(value);
        }

        return operator;
    }
}

