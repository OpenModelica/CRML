package org.openmodelica.crml.compiler.crmlcv2.util;

import org.openmodelica.crml.compiler.crmlcv2.Diagnostics;
import org.openmodelica.crml.compiler.crmlcv2.TransformationContext;
import org.openmodelica.crml.model.metamodel.BuiltinType;
import org.openmodelica.crml.model.metamodel.BuiltinTypeReference;
import org.openmodelica.crml.model.metamodel.Domain;
import org.openmodelica.crml.model.metamodel.IndirectTypeReference;
import org.openmodelica.crml.model.metamodel.TypeReference;
import org.openmodelica.crml.model.metamodel.UserTypereference;
import org.openmodelica.crml.modelica.build.Modelica;
import org.openmodelica.crml.modelica.metamodel.ClassDefinition;
import org.openmodelica.crml.modelica.metamodel.RawReason;
import org.openmodelica.crml.modelica.metamodel.Reference;

/**
 * How a CRML type becomes a Modelica type.
 *
 * <p>Only the mapping lives here. What type a CRML value has is the model's
 * business: {@code Value#inferType()}, {@code TypeReference#toType()} and
 * {@code Variable#declaredType()}.
 */
public final class ModelicaTypes implements Modelica {

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
    public Reference<ClassDefinition> resolve(TransformationContext ctx, TypeReference type) {
        if (type instanceof IndirectTypeReference) {
            return resolve(ctx, ((IndirectTypeReference) type).getReferredType());
        }
        if (type instanceof BuiltinTypeReference) {
            return builtinReference(((BuiltinTypeReference) type).getBuiltinType());
        }
        if (type instanceof UserTypereference) {
            Domain domain = ((UserTypereference) type).getDomain();
            if (domain instanceof org.openmodelica.crml.model.metamodel.Class) {
                org.openmodelica.crml.model.metamodel.Class clazz = (org.openmodelica.crml.model.metamodel.Class) domain;
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

    /** The Modelica type of a CRML builtin type. */
    public Reference<ClassDefinition> builtinReference(BuiltinType builtin) {
        String name = name(builtin);
        return isModelicaPrimitive(builtin) ? this.<ClassDefinition>builtinRef(name)
                                            : this.<ClassDefinition>libraryRef(name);
    }

    /**
     * The name of the Modelica type of a CRML builtin type: a type of the
     * language, or one of CRMLtoModelica.mo. Boolean and Requirement are both
     * a Boolean4.
     */
    public static String name(BuiltinType type) {
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
                throw new IllegalArgumentException("no Modelica type for CRML type " + type);
        }
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
}
