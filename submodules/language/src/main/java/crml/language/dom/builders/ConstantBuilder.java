package crml.language.dom.builders;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

import crml.language.dom.BuildContext;
import crml.language.dom.util.BuildResult.SingleBuildResult;
import crml.language.grammar.crmlParser.Boolean_valueContext;
import crml.language.grammar.crmlParser.Class_defContext;
import crml.language.grammar.crmlParser.Class_var_defContext;
import crml.language.grammar.crmlParser.ConstantContext;
import crml.language.grammar.crmlParser.NumberContext;
import crml.model.language.Class;
import crml.model.language.ConstantValue;
import crml.model.language.LanguageFactory;
import crml.model.language.RealConstant;
import crml.model.language.Variable;
import crml.model.language.BooleanConstant;
import crml.model.language.BooleanLiteral;

import static crml.language.dom.builders.util.DomUtils.text;

public class ConstantBuilder {
    private final BuildContext builder;
    private final LanguageFactory factory;
    private final EClass crmlclass;

    public ConstantBuilder(BuildContext builder) { 
        this.builder = builder; 
        this.factory = builder.factory();
        this.crmlclass = builder.metamodel().getClass_();
    }

    public ConstantValue constant(ConstantContext context){
        if(context.boolean_value()!=null){
            Boolean_valueContext ctx = context.boolean_value();
            BooleanConstant constant = factory.createBooleanConstant();
            switch (ctx.getText()) {
                case "true":
                    constant.setValue(BooleanLiteral.TRUE);
                    return constant;
                case "false":
                    constant.setValue(BooleanLiteral.FALSE);
                    return constant;
                case "undecided":
                    constant.setValue(BooleanLiteral.UNDECIDED);
                    return constant;
                case "undefined":
                    constant.setValue(BooleanLiteral.UNDEFINED);
                    return constant;
                default:
                    throw new IllegalStateException("Unable to process boolean constant value: "+ctx.getText());
            }
        } else if (context.time() != null) {
            return factory.createTimeValue();
        } else if (context.number() != null) {
            //TODO: decide if it is an integer.
            NumberContext numc = context.number();
            Double value = Double.parseDouble(numc.getText());
            RealConstant rv = factory.createRealConstant();
            rv.setValue(value);
            return rv;
        } else {
            builder.reportError("Unimplemented constant: "+context.getText());
            return null;
        }
    }
}
