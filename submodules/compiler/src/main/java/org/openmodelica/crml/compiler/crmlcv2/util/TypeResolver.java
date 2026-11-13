package org.openmodelica.crml.compiler.crmlcv2.util;

import org.openmodelica.crml.compiler.crmlcv2.Diagnostics;
import org.openmodelica.crml.compiler.crmlcv2.TransformationContext;
import org.openmodelica.crml.model.metamodel.BinaryOperator;
import org.openmodelica.crml.model.metamodel.BuiltinType;
import org.openmodelica.crml.model.metamodel.BuiltinTypeReference;
import org.openmodelica.crml.model.metamodel.BuiltinUnaryOperatorKind;
import org.openmodelica.crml.model.metamodel.ComputedValue;
import org.openmodelica.crml.model.metamodel.ConstructorValue;
import org.openmodelica.crml.model.metamodel.CustomOperator;
import org.openmodelica.crml.model.metamodel.Domain;
import org.openmodelica.crml.model.metamodel.IndirectTypeReference;
import org.openmodelica.crml.model.metamodel.Liftable;
import org.openmodelica.crml.model.metamodel.ScatteredValue;
import org.openmodelica.crml.model.metamodel.Set;
import org.openmodelica.crml.model.metamodel.TypeReference;
import org.openmodelica.crml.model.metamodel.UnaryOperator;
import org.openmodelica.crml.model.metamodel.UserTypereference;
import org.openmodelica.crml.model.metamodel.Value;
import org.openmodelica.crml.model.metamodel.Variable;
import org.openmodelica.crml.model.metamodel.VariableReference;
import org.openmodelica.crml.modelica.metamodel.ClassDefinition;
import org.openmodelica.crml.modelica.metamodel.RawReason;
import org.openmodelica.crml.modelica.metamodel.Reference;
import org.openmodelica.crml.modelica.build.Modelica;

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
        String name = resolve(builtin);
        return isModelicaPrimitive(builtin) ? this.<ClassDefinition>builtinRef(name)
                                            : this.<ClassDefinition>libraryRef(name);
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
            if(utr.getDomain() instanceof org.openmodelica.crml.model.metamodel.Class) {
                org.openmodelica.crml.model.metamodel.Class clazz =  (org.openmodelica.crml.model.metamodel.Class) utr.getDomain();
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
     * domain, through a constructor to the domain it constructs, through a
     * user operator or template call to the operator's result type, and through
     * {@code x.element} to the type of an element of {@code x}.
     *
     * <p>Used sparingly by the operator transformers: their type dispatch is
     * written around "known-or-unknown" predicates and was verified against
     * typeinference.csv in that form. Only the dispatches where an unknown
     * operand picks the wrong branch (filter, Event comparisons, multiplication)
     * look through; sharpening the rest is a separate change.
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
        if (value instanceof ComputedValue) {
            // A call to a user operator or template has the operator's declared
            // result type; the parser gives templates a Boolean domain.
            CustomOperator operator = ((ComputedValue) value).getOperator();
            return operator == null ? null : resolveBuiltin(operator.getDomain());
        }
        if (value instanceof ScatteredValue) {
            // x.element stands for one element of x.
            return elementBuiltin(((ScatteredValue) value).getData());
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

    /**
     * The builtin type of an element of a collection, or null if it is unknown
     * or the value is not a collection: an element of a set literal or of a
     * variable defined by one, a Period of a Periods, a result of a lifted
     * application.
     */
    public static BuiltinType elementBuiltin(Value collection) {
        Value value = collection;
        while (value instanceof UnaryOperator
                && ((UnaryOperator) value).getOptype() == BuiltinUnaryOperatorKind.SUBEXPRESSION) {
            value = ((UnaryOperator) value).getValue();
        }
        if (value == null) {
            return null;
        }
        if (value instanceof ScatteredValue && ((ScatteredValue) value).getMethod() == null) {
            // x.element with no application is the set of elements of x.
            return elementBuiltin(((ScatteredValue) value).getData());
        }
        if (value instanceof Set<?>) {
            return elementBuiltin((Set<?>) value);
        }
        if (value instanceof VariableReference) {
            Variable variable = ((VariableReference) value).getVariable();
            if (variable != null && variable.getDefinition() instanceof Set<?>) {
                BuiltinType element = elementBuiltin((Set<?>) variable.getDefinition());
                return element != null ? element : resolveBuiltin(variable.getDomain());
            }
        }
        if (value instanceof Liftable && !((Liftable) value).getLifts().isEmpty()) {
            return inferBuiltin(value);
        }
        return inferBuiltin(value) == BuiltinType.PERIODS ? BuiltinType.PERIOD : null;
    }

    /** The type of the first element of a set literal whose type is known. */
    private static BuiltinType elementBuiltin(Set<?> set) {
        for (Object element : set.getElements()) {
            if (element instanceof Value) {
                BuiltinType type = inferBuiltin((Value) element);
                if (type != null) {
                    return type;
                }
            }
        }
        return null;
    }

    /**
     * The result type of a built-in unary operator applied to an operand of the
     * given type (null if unknown).
     *
     * <p>Follows the rows of {@code typeinference.csv}, written out as a switch:
     * that file is not loaded at runtime. Throws {@code Lift.UnknownResultType}
     * where the rows do not decide the type. Used to declare the result array of
     * a lifted application, which is the one place the generator has to name the
     * type of an operator's result itself.
     */
    public static BuiltinType resultBuiltin(UnaryOperator op, BuiltinType operandType) {
        switch (op.getOptype()) {
            case NOT:
                return BuiltinType.BOOLEAN;
            case CARD:
                return BuiltinType.INTEGER;
            case TICK:
            case START:
            case END:
                return BuiltinType.EVENT;
            case SIN:
            case ASIN:
            case COS:
            case ACOS:
            case LOG:
            case LOG10:
            case EXP_OP:
                return BuiltinType.REAL;
            case ADD:
            case SUB:
                if (operandType == BuiltinType.REAL || operandType == BuiltinType.INTEGER) {
                    return operandType;
                }
                break;
            default:
                break;
        }
        throw CRMLCompilerException.unsupported(Diagnostics.notYetImplemented("Lift.UnknownResultType",
            "the result type of operator " + op.getOptype() + " on an operand of type "
            + name(operandType) + " is not known, so its lifted result cannot be declared", op));
    }

    /**
     * The result type of a built-in binary operator applied to operands of the
     * given types (null if unknown); see {@link #resultBuiltin(UnaryOperator, BuiltinType)}.
     *
     * <p>One departure from {@code typeinference.csv}: {@code /} and {@code ^}
     * are Real whatever their operands, because that is what Modelica's
     * {@code /} and {@code ^} yield, and the type is used to declare a Modelica
     * array.
     */
    public static BuiltinType resultBuiltin(BinaryOperator op, BuiltinType lt, BuiltinType rt) {
        switch (op.getOptype()) {
            case AND:
            case OR:
            case LT:
            case LE:
            case GT:
            case GE:
            case EQ:
            case NEQ:
                return BuiltinType.BOOLEAN;
            case FILTER:
                return BuiltinType.CLOCK;
            case DIV:
            case POW:
                return BuiltinType.REAL;
            case ADD:
                if (lt == BuiltinType.STRING || rt == BuiltinType.STRING) {
                    return BuiltinType.STRING;
                }
                // fall through
            case SUB:
            case MUL:
            case MOD:
                if (lt == BuiltinType.BOOLEAN || rt == BuiltinType.BOOLEAN) {
                    return BuiltinType.BOOLEAN;
                }
                if (lt == BuiltinType.REAL || rt == BuiltinType.REAL) {
                    return BuiltinType.REAL;
                }
                if (lt == BuiltinType.INTEGER && rt == BuiltinType.INTEGER) {
                    return BuiltinType.INTEGER;
                }
                break;
            default:
                break;
        }
        throw CRMLCompilerException.unsupported(Diagnostics.notYetImplemented("Lift.UnknownResultType",
            "the result type of operator " + op.getOptype() + " on operands of types ("
            + name(lt) + ", " + name(rt) + ") is not known, so its lifted result cannot be declared", op));
    }

    private static String name(BuiltinType type) {
        return type == null ? "unknown" : type.toString();
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
