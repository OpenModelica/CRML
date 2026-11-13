package org.openmodelica.crml.language.dom.builders;

import crml.language.grammar.crmlParser.TemplateContext;
import crml.language.grammar.crmlParser.TemplateKeywordContext;
import crml.language.grammar.crmlParser.TemplateParameterContext;
import crml.language.grammar.crmlParser.Template_parameterContext;

import org.openmodelica.crml.language.dom.BuildContext;
import org.openmodelica.crml.language.dom.util.BuildResult.SingleBuildResult;
import org.openmodelica.crml.model.metamodel.BuiltinType;
import org.openmodelica.crml.model.metamodel.BuiltinTypeReference;
import org.openmodelica.crml.model.metamodel.Keyword;
import org.openmodelica.crml.model.metamodel.CRMLFactory;
import org.openmodelica.crml.model.metamodel.Template;
import org.openmodelica.crml.model.metamodel.Value;
import org.openmodelica.crml.model.metamodel.Variable;

public class TemplateBuilder {
    private final BuildContext builder;
    private final CRMLFactory factory;

    public TemplateBuilder(BuildContext builder) {
        this.builder = builder;
        this.factory = builder.factory();
    }

    public Template get(TemplateContext context){
        Template template = factory.createTemplate();

        BuiltinTypeReference ref = factory.createBuiltinTypeReference();
        ref.setBuiltinType(BuiltinType.BOOLEAN);
        template.setDomain(ref);

        for(Template_parameterContext p : context.args){
            if(p instanceof TemplateParameterContext){
                TemplateParameterContext param = (TemplateParameterContext) p;

                Variable v =  factory.createVariable();

                BuiltinTypeReference ref2 = factory.createBuiltinTypeReference();
                ref2.setBuiltinType(BuiltinType.BOOLEAN);
                v.setDomain(ref2);

                v.setName(param.name.getText());
                template.getHeader().add(v);
            } else if(p instanceof TemplateKeywordContext){
                TemplateKeywordContext kwc = (TemplateKeywordContext) p;

                Keyword kw = factory.createKeyword();
                kw.setKeyword(kwc.getText());
                template.getHeader().add(kw);
            }
        }
        

        if(context.value!=null){
            Value value = (Value) builder.build(context.value, SingleBuildResult.class).<Value>result();
            template.setDefinition(value);
        }

        return template;
    }
}

