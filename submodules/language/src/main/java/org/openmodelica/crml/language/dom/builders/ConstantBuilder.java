package org.openmodelica.crml.language.dom.builders;

import org.apache.commons.lang3.math.NumberUtils;

import crml.language.grammar.crmlParser.Boolean_valueContext;
import crml.language.grammar.crmlParser.ConstantContext;
import crml.language.grammar.crmlParser.NumberContext;
import org.openmodelica.crml.model.metamodel.ConstantValue;
import org.openmodelica.crml.model.metamodel.IntegerConstant;
import org.openmodelica.crml.model.metamodel.CRMLFactory;
import org.openmodelica.crml.model.metamodel.RealConstant;
import org.openmodelica.crml.model.metamodel.StringConstant;
import org.openmodelica.crml.language.dom.BuildContext;
import org.openmodelica.crml.model.metamodel.BooleanConstant;
import org.openmodelica.crml.model.metamodel.BooleanLiteral;

public class ConstantBuilder {
    private final BuildContext builder;
    private final CRMLFactory factory;

    public ConstantBuilder(BuildContext builder) { 
        this.builder = builder; 
        this.factory = builder.factory();
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
        } else if (context.string() != null) {
            String text = context.string().STRING().getText().replaceFirst("\"$", "").replaceFirst("^\"", "");
            StringConstant str = factory.createStringConstant();
            str.setRawString(text);
            return str;
        } else if (context.time() != null) {
            return factory.createTimeValue();
        } else if (context.number() != null) {
            //TODO: decide if it is an integer.
            NumberContext numc = context.number();

            Number value = NumberUtils.createNumber(numc.getText());
            if(value instanceof Float || value instanceof Double) {
                RealConstant rv = factory.createRealConstant();
                rv.setValue(value.doubleValue());
                rv.setLiteral(numc.getText());
                return rv;
            } else if (value instanceof Integer) {
                IntegerConstant iv = factory.createIntegerConstant();
                iv.setValue(value.intValue());
                return iv;
            } else {
                throw new RuntimeException("Unimplemented number parse case: "+value.getClass().getSimpleName());
            }
            //Double value = Double.parseDouble(numc.getText());
        } else {
            builder.reportError(context, "Unimplemented constant: "+context.getText());
            return null;
        }
    }
}
