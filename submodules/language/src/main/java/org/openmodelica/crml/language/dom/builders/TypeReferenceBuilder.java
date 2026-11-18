package org.openmodelica.crml.language.dom.builders;

import org.eclipse.emf.ecore.EClass;

import crml.language.grammar.crmlParser.TypeContext;

import org.openmodelica.crml.language.dom.BuildContext;
import org.openmodelica.crml.language.scope.BuiltIns;
import org.openmodelica.crml.model.metamodel.BuiltinType;
import org.openmodelica.crml.model.metamodel.BuiltinTypeReference;
import org.openmodelica.crml.model.metamodel.CRMLFactory;
import org.openmodelica.crml.model.metamodel.CRMLPackage;
import org.openmodelica.crml.model.metamodel.TypeReference;
import org.openmodelica.crml.model.metamodel.UserTypereference;

public class TypeReferenceBuilder {
    private final BuildContext builder;
    private final CRMLFactory factory;
    private final CRMLPackage metamodel;
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
            BuiltinType type = BuiltIns.TYPES.get(typename);
            if (type == null) {
                builder.reportError(context.builtin_type(), "Unable to resolve built-in type: " + typename);
                return null;
            }
            BuiltinTypeReference ref = factory.createBuiltinTypeReference();
            ref.setBuiltinType(type);
            return ref;
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
