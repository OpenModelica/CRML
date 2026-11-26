package org.openmodelica.crml.language.dom.builders;

import org.openmodelica.crml.language.dom.Recovery;

import crml.language.grammar.crmlParser.Var_defContext;

import org.openmodelica.crml.language.dom.BuildContext;
import org.openmodelica.crml.language.dom.util.BuildResult.SingleBuildResult;
import org.openmodelica.crml.model.metamodel.BuiltinType;
import org.openmodelica.crml.model.metamodel.CRMLFactory;
import org.openmodelica.crml.model.metamodel.Set;
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

        var.setCollection(isCollection(context, var));
        
        return var;

    }

    /**
     * A variable is a set of its domain when its type says so, "Real {} S", or
     * when it is defined by a set literal, "Boolean S is { a, b }". Except
     * "Periods P is { P1, P2 }", which builds one Periods from Period elements.
     */
    private static boolean isCollection(Var_defContext context, Variable var) {
        if (context.type() != null && context.type().isset != null) {
            return true;
        }
        boolean periods = var.getDomain() != null && var.getDomain().toType().isSingle(BuiltinType.PERIODS);
        return var.getDefinition() instanceof Set<?> && !periods;
    }
}
