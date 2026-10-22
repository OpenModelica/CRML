package crml.compiler.crmlcv2.templates.value;


import crml.compiler.crmlcv2.Diagnostics;
import crml.compiler.crmlcv2.TransformationContext;
import crml.compiler.crmlcv2.UnsupportedConstruct;
import crml.compiler.crmlcv2.templates.Transformer;
import crml.compiler.crmlcv2.util.TypeResolver;
import crml.model.language.Binding;
import crml.model.language.BuiltinType;
import crml.model.language.ConstructorValue;
import crml.model.language.Element;
import crml.model.language.PeriodsValue;
import crml.model.modelica.ComponentDeclaration;
import crml.model.modelica.ComponentReference;
import crml.model.modelica.Expression;
import crml.modelica.build.Modelica;

/**
 * CRML constructors ("new T e") to Modelica.
 *
 * <p>Clock and Event are records with a companion {@code _build} record that
 * initialises them, so they hoist two component declarations onto the target
 * class and the expression becomes a reference to the first. The remaining
 * domains are casts, which stay inline.
 */
public final class ConstructorTransformer implements Modelica {

    private final Transformer transformer;

    public ConstructorTransformer(Transformer transformer) {
        this.transformer = transformer;
    }

    /** True for a bare {@code new} - nothing to translate as an expression. */
    public static boolean isDefaultConstructor(ConstructorValue constructor) {
        return constructor.getValue() == null && constructor.getBindings().isEmpty();
    }

    public Expression transform(TransformationContext ctx, ConstructorValue constructor) {
        if (!constructor.getBindings().isEmpty()) {
            return instantiate(ctx, constructor);
        }
        if (constructor.getValue() == null) {
            throw new UnsupportedConstruct(Diagnostics.error("ConstructorValue",
                "default constructor ('new') has no expression to translate; a variable "
                + "declared with it is emitted as a plain declaration instead", constructor));
        }

        BuiltinType domain = TypeResolver.resolveBuiltin(constructor.getDomain());
        Expression operand = transformer.values().transform(ctx, constructor.getValue());

        if (domain == BuiltinType.CLOCK) {
            return transformer.records().clock(ctx, operand, constructor);
        } else if (domain == BuiltinType.EVENT) {
            return transformer.records().event(ctx, operand, constructor);
        } else if (domain == BuiltinType.STRING) {
            return stringCast(constructor, operand);
        } else if (domain == BuiltinType.INTEGER) {
            return integerCast(constructor, operand);
        } else if (domain == BuiltinType.REAL) {
            return realCast(constructor, operand);
        } else if (domain == BuiltinType.BOOLEAN) {
            return booleanCast(constructor, operand);
        } else if (domain == BuiltinType.PERIOD || domain == BuiltinType.PERIODS) {
            // "new Periods ] ev, ev + d ]" names the type of a period literal
            // that has already built its own component; the constructor adds
            // nothing of its own.
            if (constructor.getValue() instanceof PeriodsValue) {
                return operand;
            }
            throw new UnsupportedConstruct(Diagnostics.error("ConstructorValue." + domain,
                "a " + domain + " can only be constructed from a period literal", constructor));
        } else {
            throw new UnsupportedConstruct(Diagnostics.notYetImplemented("ConstructorValue." + domain,
                "no constructor mapping for domain " + domain, constructor));
        }
    }

    /**
     * A constructor with an argument list - "new Contract(ecs is ecs, consumer is
     * consumer)" - instantiates a class. It becomes a component of that class
     * whose modifications bind the named members, hoisted onto the target class
     * so the expression can be a reference to it.
     */
    private ComponentReference instantiate(TransformationContext ctx, ConstructorValue constructor) {
        ComponentDeclaration instance = component(
            transformer.types().resolve(ctx, constructor.getDomain()), ctx.allocateName("inst"));
        for (Binding binding : constructor.getBindings()) {
            Element member = binding.getElement();
            if (member == null || member.getName() == null) {
                throw new UnsupportedConstruct(Diagnostics.error("ConstructorValue.bindings",
                    "a constructor argument does not name the member it binds", constructor));
            }
            if (binding.getValue() == null) {
                throw new UnsupportedConstruct(Diagnostics.error("ConstructorValue.bindings",
                    "member '" + member.getName() + "' is bound to nothing", constructor));
            }
            instance.getModifications().add(
                mod(member.getName(), transformer.values().transform(ctx, binding.getValue())));
        }
        return ctx.declare(instance, constructor);
    }

    private Expression stringCast(ConstructorValue constructor, Expression operand) {
        BuiltinType sourceType = TypeResolver.resolveBuiltin(constructor.getValue().getReturnType());
        if (sourceType == BuiltinType.BOOLEAN) {
            // The string generator emitted CRMLtoModelica.Functions.Bool4toString
            // here, which does not exist: the library's complete function set is
            // cvBooleanToBoolean4, add4, mul4, or4, not4, and4, PStart, PEnd, gEV,
            // lEV, Event2Boolean.
            throw new UnsupportedConstruct(Diagnostics.libraryGap("ConstructorValue.STRING",
                "String(Boolean4) needs CRMLtoModelica.Functions.Bool4toString, which does not exist "
                + "in CRMLtoModelica.mo", constructor));
        }
        return call(builtinRef("String"), operand);
    }

    private Expression integerCast(ConstructorValue constructor, Expression operand) {
        BuiltinType sourceType = TypeResolver.resolveBuiltin(constructor.getValue().getReturnType());
        if (sourceType == BuiltinType.REAL) {
            return call(builtinRef("integer"), operand);
        }
        // Modelica's Integer() converts enumerations only, and Boolean4 is the one
        // enumeration in play. A String or Event operand has no conversion.
        if (sourceType == null || sourceType == BuiltinType.BOOLEAN
                || sourceType == BuiltinType.REQUIREMENT || sourceType == BuiltinType.INTEGER) {
            return call(builtinRef("Integer"), operand);
        }
        throw new UnsupportedConstruct(Diagnostics.error("ConstructorValue.INTEGER",
            "Modelica's Integer() converts enumerations and Real only; operand type is " + sourceType, constructor));
    }

    private Expression realCast(ConstructorValue constructor, Expression operand) {
        BuiltinType sourceType = TypeResolver.resolveBuiltin(constructor.getValue().getReturnType());
        // The string generator emitted real(x)/Real(x); neither exists as a
        // conversion function. Real -> Real is a no-op, and Integer -> Real is
        // Modelica's own implicit conversion, so the operand stands on its own.
        if (sourceType == null || sourceType == BuiltinType.REAL || sourceType == BuiltinType.INTEGER) {
            return operand;
        }
        throw new UnsupportedConstruct(Diagnostics.error("ConstructorValue.REAL",
            "Modelica has no conversion to Real from " + sourceType, constructor));
    }

    private Expression booleanCast(ConstructorValue constructor, Expression operand) {
        // Return-type inference only populates constants today, so a computed
        // operand such as "Boolean((P end))" arrives with a null type. The only
        // defined Boolean constructor is from an Event, so proceed optimistically
        // rather than rejecting every non-constant operand.
        BuiltinType sourceType = TypeResolver.resolveBuiltin(constructor.getValue().getReturnType());
        if (sourceType == null || sourceType == BuiltinType.EVENT) {
            return call(libraryRef("CRMLtoModelica.Functions.Event2Boolean"), operand);
        }
        throw new UnsupportedConstruct(Diagnostics.error("ConstructorValue.BOOLEAN",
            "the Boolean constructor is only defined for an Event operand (got " + sourceType + ")", constructor));
    }
}
