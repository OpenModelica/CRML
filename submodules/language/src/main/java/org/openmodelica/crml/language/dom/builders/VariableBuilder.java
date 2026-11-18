package org.openmodelica.crml.language.dom.builders;

import org.openmodelica.crml.language.dom.Recovery;

import crml.language.grammar.crmlParser.Var_defContext;

import org.openmodelica.crml.language.dom.BuildContext;
import org.openmodelica.crml.language.dom.util.BuildResult.SingleBuildResult;
import org.openmodelica.crml.model.metamodel.CRMLFactory;
import org.openmodelica.crml.model.metamodel.TypeReference;
import org.openmodelica.crml.model.metamodel.Value;
import org.openmodelica.crml.model.metamodel.Variable;

public class VariableBuilder {
    private final BuildContext builder;
    private final CRMLFactory factory;
    
    public VariableBuilder(BuildContext builder) { 
        this.builder = builder;  
        this.factory = builder.factory();
    }

    public Variable variable(Var_defContext context){
        //TODO: what is fixed qualifier?
        Variable var = factory.createVariable();

        var.setCollection(false);
        
        var.setName(Recovery.required(context.id(), "variable name"));

        var.setConstant(context.cnst!=null);

        var.setExternal(context.is_external!=null);

        TypeReference typeref = (TypeReference) builder.build(context.type(), SingleBuildResult.class).<TypeReference>result();
        var.setDomain(typeref);

        // Left out if it cannot be built: the variable is still declared.
        SingleBuildResult<?> definition = builder.buildPart(context.exp(), SingleBuildResult.class);
        if(definition != null){
            var.setDefinition(definition.<Value>result());
        }
        
        return var;

    }
}
