package crml.compiler.crmlcv2.util;

import crml.compiler.crmlcv2.Diagnostics;
import crml.compiler.crmlcv2.TransformationContext;
import crml.model.language.BuiltinType;
import crml.model.language.BuiltinTypeReference;
import crml.model.language.ConstructorValue;
import crml.model.language.Domain;
import crml.model.language.IndirectTypeReference;
import crml.model.language.TypeReference;
import crml.model.language.UnaryOperator;
import crml.model.language.UserTypereference;
import crml.model.language.Value;
import crml.model.language.Variable;
import crml.model.language.VariableReference;
import crml.model.modelica.ClassDefinition;
import crml.model.modelica.RawReason;
import crml.model.modelica.Reference;
import crml.modelica.build.Modelica;

public class TypeResolver implements Modelica {

    /**
     * The Modelica type a CRML type reference names.
     *
     * <p>This is the one place the raw-against-resolved decision is taken for
     * types. CRML's builtin types are records and an enumeration inside
     * CRMLtoModelica.mo, which is never parsed; Real, Integer and String are
     * part of the Modelica language. Both are permanently raw, and only a CRML
     * class has an object in the generated tree to point at.
     *
     * <p>Never throws: an unresolvable type is reported and comes back as an
     * UNRESOLVED reference, so the caller drops one declaration rather than the
     * whole model.
     */
    public Reference<ClassDefinition> resolve(TransformationContext ctx, TypeReference type){
        if (type instanceof IndirectTypeReference) {
            return resolve(ctx, ((IndirectTypeReference) type).getReferredType());
        }
        if (type instanceof BuiltinTypeReference) {
            BuiltinType builtin = ((BuiltinTypeReference) type).getBuiltinType();
            String name = resolve(builtin);
            return isModelicaPrimitive(builtin) ? this.<ClassDefinition>builtinRef(name)
                                                : this.<ClassDefinition>libraryRef(name);
        }
        if (type instanceof UserTypereference) {
            Domain domain = ((UserTypereference) type).getDomain();
            if (domain instanceof crml.model.language.Class) {
                crml.model.language.Class clazz = (crml.model.language.Class) domain;
                ClassDefinition definition = ctx.generatedClass(clazz);
                if (definition != null) {
                    return resolvedRef(definition);
                }
                return unresolved(ctx, type, "no Modelica class was generated for CRML class '"
                    + clazz.getName() + "'");
            }
            return unresolved(ctx, type, "a user type reference whose domain is "
                + (domain == null ? "unset" : domain.eClass().getName()) + " has no Modelica mapping");
        }
        if (type == null) {
            return unresolved(ctx, null, "no type reference to resolve");
        }
        return unresolved(ctx, type, "unable to resolve type reference: " + type.eClass().getName());
    }

    /** Real, Integer and String are the language's own; the rest come from the library. */
    private static boolean isModelicaPrimitive(BuiltinType type) {
        return type == BuiltinType.REAL || type == BuiltinType.INTEGER || type == BuiltinType.STRING;
    }

    private Reference<ClassDefinition> unresolved(TransformationContext ctx, TypeReference type,
            String message) {
        ctx.report(Diagnostics.error("TypeReference", message, type));
        return this.<ClassDefinition>rawRef("", RawReason.UNRESOLVED);
    }

    /** The legacy name-only resolution, kept for the two callers that need a bare name. */
    public static String resolveName(TypeReference type){
        if(type instanceof IndirectTypeReference){
            return resolveName(((IndirectTypeReference) type).getReferredType());
        } else if(type instanceof BuiltinTypeReference) {
            BuiltinTypeReference btr = (BuiltinTypeReference) type;
            return resolve(btr.getBuiltinType());
        } else if(type instanceof UserTypereference) {
            UserTypereference utr = (UserTypereference) type;
            if(utr.getDomain() instanceof crml.model.language.Class) {
                crml.model.language.Class clazz =  (crml.model.language.Class) utr.getDomain();
                return clazz.getName();
            }
            throw new RuntimeException("Unimplemented.");
        } else if (type == null) {
            throw new RuntimeException("No type reference to resolve.");
        } else {
            throw new RuntimeException("Unable to resolve type reference: " + type.eClass().getName());
        }
    }

    // Resolves a TypeReference down to its BuiltinType, or null if it isn't
    // one (unresolved / not yet inferred, or a user-defined class type).
    public static BuiltinType resolveBuiltin(TypeReference type){
        if(type == null){
            return null;
        } else if(type instanceof IndirectTypeReference){
            return resolveBuiltin(((IndirectTypeReference) type).getReferredType());
        } else if(type instanceof BuiltinTypeReference){
            return ((BuiltinTypeReference) type).getBuiltinType();
        } else {
            return null;
        }
    }

    /**
     * The builtin type of a value, looking past the object model's own gaps.
     *
     * <p>{@code Value#getReturnType()} is only populated for constants today, so
     * a reference to a variable of a known domain still reports "unknown". Where
     * a decision genuinely depends on the type - not merely on it being
     * compatible - this looks through the reference to the variable's declared
     * domain, and through a constructor to the domain it constructs.
     *
     * <p>Deliberately not used by the operator transformers: their type dispatch
     * is written around "known-or-unknown" predicates and was verified against
     * typeinference.csv in that form. Making inference sharper there is a
     * separate change with its own consequences.
     */
    public static BuiltinType inferBuiltin(Value value){
        if (value == null) {
            return null;
        }
        BuiltinType declared = resolveBuiltin(value.getReturnType());
        if (declared != null) {
            return declared;
        }
        if (value instanceof VariableReference) {
            Variable variable = ((VariableReference) value).getVariable();
            return variable == null ? null : resolveBuiltin(variable.getDomain());
        }
        if (value instanceof ConstructorValue) {
            return resolveBuiltin(((ConstructorValue) value).getDomain());
        }
        if (value instanceof UnaryOperator) {
            // The operators whose result is an Event whatever their operand:
            // typeinference.csv has TICK,EVENT,1,CLOCK and START/END,EVENT,1,PERIOD.
            UnaryOperator unary = (UnaryOperator) value;
            switch (unary.getOptype()) {
                case TICK:
                case START:
                case END:
                    return BuiltinType.EVENT;
                case SUBEXPRESSION:
                    return inferBuiltin(unary.getValue());
                default:
                    return null;
            }
        }
        return null;
    }

    public static String resolve(BuiltinType type){
        switch (type) {
            case BOOLEAN: return "CRMLtoModelica.Types.Boolean4";
            case PERIOD: return "CRMLtoModelica.Types.CRMLPeriod";
            case PERIODS: return "CRMLtoModelica.Types.CRMLPeriods";
            case EVENT: return "CRMLtoModelica.Types.Event";
            case REQUIREMENT: return "CRMLtoModelica.Types.Boolean4";
            case CLOCK: return "CRMLtoModelica.Types.CRMLClock";
            case REAL: return "Real";
            case STRING: return "String";
            case INTEGER: return "Integer";
            default:
                throw new RuntimeException("Unable to resolve type: "+type);
        }
    }
}
