package crml.compiler.crmlcv2.templates;

import java.util.ArrayList;
import java.util.List;

import crml.compiler.crmlcv2.Diagnostics;
import crml.compiler.crmlcv2.GeneratedOperator;
import crml.compiler.crmlcv2.Lifter;
import crml.compiler.crmlcv2.SetHandle;
import crml.compiler.crmlcv2.TransformationContext;
import crml.compiler.crmlcv2.util.CRMLCompilerException;
import crml.compiler.crmlcv2.util.TypeResolver;
import crml.model.language.BinaryOperator;
import crml.model.language.Binding;
import crml.model.language.BuiltinType;
import crml.model.language.BuiltinUnaryOperatorKind;
import crml.model.language.ComputedValue;
import crml.model.language.Liftable;
import crml.model.language.ScatteredValue;
import crml.model.language.UnaryOperator;
import crml.model.language.Value;
import crml.model.language.Variable;
import crml.modelica.build.Modelica;

/**
 * Lifted applications: a call to a user operator, or a built-in unary or binary
 * operator, with at least one {@code x.element} operand.
 *
 * <p>{@link #region} translates one to the collection of its results. The
 * operator's own transformer does the per-element part through its
 * {@code transformLifted}; the {@link Lifter} that {@link #open} hands it does
 * everything that is the same for every operator.
 *
 * <p>A lifted application is a collection, so it is only valid in a set
 * position: as the {@code x} of another {@code x.element}, or as the operand of
 * an aggregator. Both reach it through {@link ScatteredValueTransformer#collect}.
 */
public final class LiftableTransformer implements Modelica {

    private final Transformer transformer;

    public LiftableTransformer(Transformer transformer) {
        this.transformer = transformer;
    }

    /** The collection of the results of a lifted application. Hoists its region into {@code ctx}. */
    public SetHandle region(TransformationContext ctx, Liftable application) {
        if (application instanceof ComputedValue) {
            return transformer.operators().transformLifted(open(ctx, application), (ComputedValue) application);
        }
        if (application instanceof UnaryOperator) {
            return transformer.unaryOperators().transformLifted(open(ctx, application), (UnaryOperator) application);
        }
        if (application instanceof BinaryOperator) {
            return transformer.binaryOperators().transformLifted(open(ctx, application), (BinaryOperator) application);
        }
        throw CRMLCompilerException.unsupported(Diagnostics.error(application.eClass().getName(),
            "no lifting for application kind " + application.eClass().getName(), application));
    }

    /**
     * Plans the region of a lifted application: checks its lifts, translates
     * the collection of each one in {@code ctx} - which may hoist, and may
     * throw - and allocates the region's names. Nothing of the region itself is
     * added to the class before {@link Lifter#close}.
     */
    public Lifter open(TransformationContext ctx, Liftable application) {
        List<Value> operands = operands(application);
        List<SetHandle> axes = new ArrayList<SetHandle>();
        for (ScatteredValue lift : application.getLifts()) {
            boolean direct = false;
            for (Value operand : operands) {
                direct = direct || unparen(operand) == lift;
            }
            if (!direct) {
                throw CRMLCompilerException.unsupported(Diagnostics.error("Lift.NotDirectArgument",
                    "an '.element' lifts a " + application.eClass().getName() + " without being one of its "
                    + operands.size() + " operand(s); it is contained in "
                    + (lift.eContainer() == null ? "nothing" : "a " + lift.eContainer().eClass().getName()), lift));
            }
            if (lift.getMethod() != application) {
                throw CRMLCompilerException.unsupported(Diagnostics.error("Lift.WrongMethod",
                    "an '.element' is listed as a lift of a " + application.eClass().getName()
                    + " but refers to " + (lift.getMethod() == null ? "no application"
                        : "a " + lift.getMethod().eClass().getName()) + " as its own", lift));
            }
            axes.add(transformer.scatteredValues().collect(ctx, lift.getData()));
        }

        String base;
        if (application instanceof ComputedValue) {
            ComputedValue call = (ComputedValue) application;
            if (call.getOperator() == null) {
                throw CRMLCompilerException.unsupported(Diagnostics.error("ComputedValue",
                    "the call does not refer to any operator", call));
            }
            checkElementTypes(call, axes);
            GeneratedOperator generated = transformer.operators().declare(ctx, call.getOperator());
            base = generated.name();
        } else if (application instanceof UnaryOperator) {
            base = lowerCase(((UnaryOperator) application).getOptype().getLiteral());
        } else {
            base = lowerCase(((BinaryOperator) application).getOptype().getLiteral());
        }
        return new Lifter(transformer, ctx, application, axes, base);
    }

    /**
     * The element type of each lift of a call must be the type of the formal it
     * is bound to. Checked only where both are known builtin types.
     */
    private static void checkElementTypes(ComputedValue call, List<SetHandle> axes) {
        for (int j = 0; j < axes.size(); j++) {
            ScatteredValue lift = call.getLifts().get(j);
            for (Binding binding : call.getBindings()) {
                if (unparen(binding.getValue()) != lift || !(binding.getElement() instanceof Variable)) {
                    continue;
                }
                Variable formal = (Variable) binding.getElement();
                BuiltinType expected = TypeResolver.resolveBuiltin(formal.getDomain());
                BuiltinType actual = axes.get(j).elementBuiltin();
                if (expected != null && actual != null && !assignable(actual, expected)) {
                    throw CRMLCompilerException.unsupported(Diagnostics.error("Lift.ElementType",
                        "parameter '" + formal.getName() + "' of operator '"
                        + OperatorTransformer.describe(call.getOperator()) + "' is of type " + expected
                        + ", but the elements lifted into it are of type " + actual, binding));
                }
            }
        }
    }

    /** Same Modelica type (Boolean and Requirement are both Boolean4), or an Integer where a Real is expected. */
    private static boolean assignable(BuiltinType actual, BuiltinType expected) {
        return TypeResolver.resolve(actual).equals(TypeResolver.resolve(expected))
            || (actual == BuiltinType.INTEGER && expected == BuiltinType.REAL);
    }

    private static List<Value> operands(Liftable application) {
        List<Value> operands = new ArrayList<Value>();
        if (application instanceof ComputedValue) {
            for (Binding binding : ((ComputedValue) application).getBindings()) {
                operands.add(binding.getValue());
            }
        } else if (application instanceof UnaryOperator) {
            operands.add(((UnaryOperator) application).getValue());
        } else if (application instanceof BinaryOperator) {
            operands.add(((BinaryOperator) application).getLhs());
            operands.add(((BinaryOperator) application).getRhs());
        }
        return operands;
    }

    /**
     * The value without the parentheses around it, if any. An {@code x.element}
     * in parentheses is still an operand of the application around them, as it
     * is for the linker. <b>Experimental, subject to change.</b>
     */
    public static Value unparen(Value value) {
        while (value instanceof UnaryOperator
                && ((UnaryOperator) value).getOptype() == BuiltinUnaryOperatorKind.SUBEXPRESSION) {
            value = ((UnaryOperator) value).getValue();
        }
        return value;
    }

    /** ASCII lower case, without depending on the default locale. */
    private static String lowerCase(String text) {
        StringBuilder builder = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            builder.append(c >= 'A' && c <= 'Z' ? (char) (c + ('a' - 'A')) : c);
        }
        return builder.toString();
    }
}
