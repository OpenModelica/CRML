package crml.language.dom.builders;

import org.apache.commons.lang3.math.NumberUtils;

import crml.language.dom.BuildContext;
import crml.language.grammar.crmlParser.Boolean_valueContext;
import crml.language.grammar.crmlParser.ConstantContext;
import crml.language.grammar.crmlParser.NumberContext;
import crml.model.language.ConstantValue;
import crml.model.language.IntegerConstant;
import crml.model.language.LanguageFactory;
import crml.model.language.RealConstant;
import crml.model.language.StringConstant;
import crml.model.language.TimeValue;
import crml.model.language.BooleanConstant;
import crml.model.language.BooleanLiteral;
import crml.model.language.BuiltinType;
import crml.model.language.BuiltinTypeReference;

public class ConstantBuilder {
    private final BuildContext builder;
    private final LanguageFactory factory;

    public ConstantBuilder(BuildContext builder) { 
        this.builder = builder; 
        this.factory = builder.factory();
    }

    public ConstantValue constant(ConstantContext context){
        if(context.boolean_value()!=null){
            Boolean_valueContext ctx = context.boolean_value();
            BooleanConstant constant = factory.createBooleanConstant();
            
            BuiltinTypeReference typeref = factory.createBuiltinTypeReference();
            typeref.setBuiltinType(BuiltinType.BOOLEAN);
            constant.setReturnType(typeref);

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
            BuiltinTypeReference typeref = factory.createBuiltinTypeReference();
            typeref.setBuiltinType(BuiltinType.STRING);
            str.setReturnType(typeref);
            return str;
        } else if (context.time() != null) {
            TimeValue time = factory.createTimeValue();
            BuiltinTypeReference typeref = factory.createBuiltinTypeReference();
            typeref.setBuiltinType(BuiltinType.REAL);
            time.setReturnType(typeref);
            return time;
        } else if (context.number() != null) {
            //TODO: decide if it is an integer.
            NumberContext numc = context.number();

            Number value = NumberUtils.createNumber(numc.getText());
            if(value instanceof Float || value instanceof Double) {
                RealConstant rv = factory.createRealConstant();
                rv.setValue(value.doubleValue());
                rv.setLiteral(numc.getText());
                BuiltinTypeReference typeref = factory.createBuiltinTypeReference();
                typeref.setBuiltinType(BuiltinType.REAL);
                rv.setReturnType(typeref);
                return rv;
            } else if (value instanceof Integer) {
                IntegerConstant iv = factory.createIntegerConstant();
                iv.setValue(value.intValue());
                BuiltinTypeReference typeref = factory.createBuiltinTypeReference();
                typeref.setBuiltinType(BuiltinType.INTEGER);
                iv.setReturnType(typeref);
                return iv;
            } else {
                throw new RuntimeException("Unimplemented number parse case: "+value.getClass().getSimpleName());
            }
            //Double value = Double.parseDouble(numc.getText());
        } else {
            builder.reportError("Unimplemented constant: "+context.getText());
            return null;
        }
    }
}
