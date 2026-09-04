package crml.compiler.crmlcv2.templates.value;

import static crml.compiler.crmlcv2.templates.value.ValueGenUtil.call;

import crml.compiler.crmlcv2.scopes.FunctionScope;
import crml.compiler.crmlcv2.scopes.ModelScope;
import crml.compiler.crmlcv2.scopes.NameAllocator;
import crml.compiler.crmlcv2.scopes.RawstringScope;
import crml.compiler.crmlcv2.scopes.Scope;
import crml.compiler.crmlcv2.templates.ValueGen;
import crml.compiler.crmlcv2.util.TypeResolver;
import crml.model.language.BuiltinType;
import crml.model.language.ConstructorValue;

// Mirrors translation.crmlVisitorImpl#visitConstructor / OperatorMapping's
// "CONSTRUCTORS" table, adapted to the omcv2 Scope/BuiltinType idiom.
public class ConstructorGen {

    // Unique instance names for the record-style Clock/Event constructions
    // below; a single allocator shared across both prefixes mirrors the one
    // global counter the legacy visitor used for "c"/"e"/"P" names.
    private static final NameAllocator names = new NameAllocator();

    public static Scope generate(ModelScope host, ConstructorValue constructor){
        if (!constructor.getBindings().isEmpty()) {
            throw new UnsupportedOperationException(
                "Translation of constructor with argument list is not implemented.");
        }

        if (constructor.getValue() == null) {
            // Constructor with no expression ("new") - translates to nothing in
            // Modelica. Callers with an optional initializer (e.g. VariableGen,
            // which already special-cases Variable#getDefinition()==null) must
            // check for this before calling ConstructorGen; there is no
            // sensible inline expression to hand back here.
            throw new UnsupportedOperationException(
                "Default constructor ('new') has no expression to translate.");
        }

        BuiltinType domain = TypeResolver.resolveBuiltin(constructor.getDomain());
        Scope operand = ValueGen.generate(host, constructor.getValue());

        if (domain == BuiltinType.CLOCK) {
            return generateRecordBuild(host, "c", TypeResolver.resolve(BuiltinType.CLOCK),
                "CRMLtoModelica.Types.CRMLClock_build", "clock", operand);
        } else if (domain == BuiltinType.EVENT) {
            return generateRecordBuild(host, "e", TypeResolver.resolve(BuiltinType.EVENT),
                "CRMLtoModelica.Types.CRMLEvent_build", "E", operand);
        } else if (domain == BuiltinType.STRING) {
            return generateStringCast(constructor, operand);
        } else if (domain == BuiltinType.INTEGER) {
            return generateIntegerCast(constructor, operand);
        } else if (domain == BuiltinType.REAL) {
            return generateRealCast(constructor, operand);
        } else if (domain == BuiltinType.BOOLEAN) {
            return generateBooleanCast(constructor, operand);
        } else {
            throw new UnsupportedOperationException(
                "Constructor for domain " + domain + " is not implemented.");
        }
    }

    // Clock/Event are records with a companion "_build" record that
    // validates/initializes them; both are declared with a modifier (not
    // wired via equations like BlockScope's causal ports), so two plain
    // declarations on the host model are all that is needed.
    private static Scope generateRecordBuild(ModelScope host, String prefix, String recordType,
            String buildType, String buildParam, Scope operand){
        String varName = names.allocate(prefix);
        host.addVariable(new RawstringScope(
            recordType + " " + varName + "(b=" + operand.reference() + ");"));
        host.addVariable(new RawstringScope(
            buildType + " " + varName + "_init(" + buildParam + "=" + varName + ");"));
        return new FunctionScope(varName);
    }

    private static Scope generateStringCast(ConstructorValue constructor, Scope operand){
        BuiltinType sourceType = TypeResolver.resolveBuiltin(constructor.getValue().getReturnType());
        if (sourceType == BuiltinType.BOOLEAN) {
            return call("CRMLtoModelica.Functions.Bool4toString", operand);
        }
        return call("String", operand);
    }

    private static Scope generateIntegerCast(ConstructorValue constructor, Scope operand){
        BuiltinType sourceType = TypeResolver.resolveBuiltin(constructor.getValue().getReturnType());
        if (sourceType == BuiltinType.REAL) {
            return call("integer", operand);
        }
        return call("Integer", operand);
    }

    private static Scope generateRealCast(ConstructorValue constructor, Scope operand){
        BuiltinType sourceType = TypeResolver.resolveBuiltin(constructor.getValue().getReturnType());
        if (sourceType == BuiltinType.REAL) {
            return call("real", operand);
        }
        return call("Real", operand);
    }

    private static Scope generateBooleanCast(ConstructorValue constructor, Scope operand){
        // Return-type inference only populates Constant values today (see
        // ValueGenUtil's "OrUnknown" predicates), so a computed operand like
        // `Boolean((P end))` reaches here with a null type; the only defined
        // Boolean constructor is from an Event, so proceed optimistically
        // rather than rejecting every non-constant operand.
        BuiltinType sourceType = TypeResolver.resolveBuiltin(constructor.getValue().getReturnType());
        if (sourceType == null || sourceType == BuiltinType.EVENT) {
            return call("CRMLtoModelica.Functions.Event2Boolean", operand);
        }
        throw new UnsupportedOperationException(
            "Boolean constructor is only defined for an Event operand (got operand type " + sourceType + ")");
    }
}
