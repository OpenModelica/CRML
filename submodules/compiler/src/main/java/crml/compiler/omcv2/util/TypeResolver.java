package crml.compiler.omcv2.util;

import crml.model.language.BuiltinType;
import crml.model.language.BuiltinTypeReference;
import crml.model.language.IndirectTypeReference;
import crml.model.language.TypeReference;
import crml.model.language.UserTypereference;

public class TypeResolver {
    public static String resolve(TypeReference type){
        if(type instanceof IndirectTypeReference){
            return resolve(((IndirectTypeReference) type).getReferredType());
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
        } else {
            throw new RuntimeException("Unreachable.");
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

    public static String resolve(BuiltinType type){
        switch (type) {
            case BuiltinType.BOOLEAN: return "CRMLtoModelica.Types.Boolean4";
            case BuiltinType.PERIOD: return "CRMLtoModelica.Types.CRMLPeriod";
            case BuiltinType.PERIODS: return "CRMLtoModelica.Types.CRMLPeriods";
            case BuiltinType.EVENT: return "CRMLtoModelica.Types.Event";
            case BuiltinType.REQUIREMENT: return "CRMLtoModelica.Types.Boolean4";
            case BuiltinType.CLOCK: return "CRMLtoModelica.Types.CRMLClock";
            case BuiltinType.REAL: return "Real";
            case BuiltinType.STRING: return "String";
            case BuiltinType.INTEGER: return "Integer";
            default:
                throw new RuntimeException("Unable to resolve type: "+type);
        }
    }
}
