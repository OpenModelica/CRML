package crml.language.dom.builders;

import org.eclipse.emf.ecore.EClass;

import crml.language.dom.BuildContext;
import crml.language.grammar.crmlParser.TypeContext;
import crml.model.language.BuiltinType;
import crml.model.language.BuiltinTypeReference;
import crml.model.language.LanguageFactory;
import crml.model.language.LanguagePackage;
import crml.model.language.TypeReference;
import crml.model.language.UserTypereference;

public class TypeReferenceBuilder {
    private final BuildContext builder;
    private final LanguageFactory factory;
    private final LanguagePackage metamodel;
    private final EClass refclass;

    public TypeReferenceBuilder(BuildContext builder) {
        this.builder = builder;
        this.metamodel = builder.metamodel();
        this.factory = builder.factory();
        this.refclass = metamodel.getUserTypereference();
    }

    public TypeReference reference(TypeContext context) {
        if (context.builtin_type() != null) {
            String typename = context.builtin_type().getText();
            BuiltinTypeReference ref = factory.createBuiltinTypeReference();
            switch (typename) {
                case "String": {
                    ref.setBuiltinType(BuiltinType.STRING);
                    return ref;
                }
                case "Boolean": {
                    ref.setBuiltinType(BuiltinType.BOOLEAN);
                    return ref;
                }
                case "Integer": {
                    ref.setBuiltinType(BuiltinType.INTEGER);
                    return ref;
                }
                case "Real": {
                    ref.setBuiltinType(BuiltinType.REAL);
                    return ref;
                }
                case "Event": {
                    ref.setBuiltinType(BuiltinType.EVENT);
                    return ref;
                }
                case "Clock": {
                    ref.setBuiltinType(BuiltinType.CLOCK);
                    return ref;
                }
                case "Period": {
                    ref.setBuiltinType(BuiltinType.PERIOD);
                    return ref;
                }
                case "Periods": {
                    ref.setBuiltinType(BuiltinType.PERIODS);
                    return ref;
                }
                case "Requirement": {
                    ref.setBuiltinType(BuiltinType.REQUIREMENT);
                    return ref;
                }
                default: {
                    builder.reportError("Unable to resolve built-in type: " + typename);
                    return null;
                }
            }
        } else {
            UserTypereference ref =  factory.createUserTypereference();
            builder.link(
                ref, 
                refclass.getEStructuralFeature("domain"), 
                context.id().getText()
            );
            return ref;
        }

    }
}
