package crml.language.dom.builders;

import crml.language.dom.BuildContext;
import crml.language.dom.util.BuildResult.SingleBuildResult;
import crml.language.grammar.crmlParser.TemplateContext;
import crml.language.grammar.crmlParser.TemplateKeywordContext;
import crml.language.grammar.crmlParser.TemplateParameterContext;
import crml.language.grammar.crmlParser.Template_parameterContext;
import crml.model.language.BuiltinType;
import crml.model.language.BuiltinTypeReference;
import crml.model.language.Keyword;
import crml.model.language.LanguageFactory;
import crml.model.language.Parameter;
import crml.model.language.Template;
import crml.model.language.Value;
import crml.model.language.Variable;

public class TemplateBuilder {
    private final BuildContext builder;
    private final LanguageFactory factory;

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

                Parameter prm = factory.createParameter();
                Variable v =  factory.createVariable();
                prm.setVariable(v);

                BuiltinTypeReference ref2 = factory.createBuiltinTypeReference();
                ref2.setBuiltinType(BuiltinType.BOOLEAN);
                v.setDomain(ref2);

                v.setName(param.name.getText());
                template.getHeader().add(prm);
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

