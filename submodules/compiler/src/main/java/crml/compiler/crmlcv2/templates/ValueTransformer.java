package crml.compiler.crmlcv2.templates;


import crml.compiler.crmlcv2.Diagnostics;
import crml.compiler.crmlcv2.TransformationContext;
import crml.compiler.crmlcv2.UnsupportedConstruct;
import crml.compiler.crmlcv2.templates.value.BinaryOperatorTransformer;
import crml.compiler.crmlcv2.templates.value.ConstructorTransformer;
import crml.compiler.crmlcv2.templates.value.IntegrateTransformer;
import crml.compiler.crmlcv2.templates.value.PeriodsTransformer;
import crml.compiler.crmlcv2.templates.value.UnaryOperatorTransformer;
import crml.model.language.BinaryOperator;
import crml.model.language.BooleanConstant;
import crml.model.language.ComputedValue;
import crml.model.language.ConstructorValue;
import crml.model.language.DurationValue;
import crml.model.language.IfValue;
import crml.model.language.IntegerConstant;
import crml.model.language.IntegrateValue;
import crml.model.language.PeriodsValue;
import crml.model.language.ProjectionValue;
import crml.model.language.RealConstant;
import crml.model.language.Sequence;
import crml.model.language.Set;
import crml.model.language.StringConstant;
import crml.model.language.TimeValue;
import crml.model.language.UnaryOperator;
import crml.model.language.Value;
import crml.model.language.VariableReference;
import crml.model.modelica.BinaryOperatorKind;
import crml.model.modelica.Expression;
import crml.modelica.build.Modelica;

/**
 * The CRML {@code Value} dispatcher.
 *
 * <p>Every case returns an {@link Expression} unconditionally: there is no
 * "cannot be embedded inline" result any more. A value that needs something
 * hoisted declares it on the {@link TransformationContext} and returns a
 * reference to what it declared; a value with no Modelica target throws
 * {@link UnsupportedConstruct}, which the enclosing declaration or equation
 * turns into a diagnostic and a placeholder.
 */
public final class ValueTransformer implements Modelica {

    private final Transformer transformer;

    public ValueTransformer(Transformer transformer) {
        this.transformer = transformer;
    }

    public Expression transform(TransformationContext ctx, Value value) {
        if (value == null) {
            throw new UnsupportedConstruct(Diagnostics.error("Value",
                "a value is missing from the object model", null));
        }
        if (value instanceof VariableReference) {
            return ref(((VariableReference) value).getVariable().getName());
        }
        if (value instanceof IfValue) {
            return transformIf(ctx, (IfValue) value);
        }
        if (value instanceof BooleanConstant) {
            return transformBooleanConstant((BooleanConstant) value);
        }
        if (value instanceof RealConstant) {
            RealConstant constant = (RealConstant) value;
            return constant.getLiteral() != null ? real(constant.getLiteral()) : real(constant.getValue());
        }
        if (value instanceof IntegerConstant) {
            return integer(((IntegerConstant) value).getValue());
        }
        if (value instanceof StringConstant) {
            return string(((StringConstant) value).getRawString());
        }
        if (value instanceof TimeValue) {
            return ref("time");
        }
        if (value instanceof BinaryOperator) {
            return transformer.binaryOperators().transform(ctx, (BinaryOperator) value);
        }
        if (value instanceof UnaryOperator) {
            return transformer.unaryOperators().transform(ctx, (UnaryOperator) value);
        }
        if (value instanceof ConstructorValue) {
            return transformer.constructors().transform(ctx, (ConstructorValue) value);
        }
        if (value instanceof ComputedValue) {
            return transformer.operators().transformCall(ctx, (ComputedValue) value);
        }
        // --- planned, not yet built ----------------------------------------
        if (value instanceof PeriodsValue) {
            return transformer.periods().transform(ctx, (PeriodsValue) value);
        }
        if (value instanceof IntegrateValue) {
            return transformer.integrals().transform(ctx, (IntegrateValue) value);
        }
        if (value instanceof Set<?>) {
            return transformer.sets().literal(ctx, (Set<?>) value);
        }
        if (value instanceof Sequence) {
            // An unresolved mixfix operator call. crml.xcore states that an object
            // model reaching a generator contains no Sequence, but
            // crml.language.opcall.MixfixParser - the resolver that would replace
            // these with ComputedValue - is not called from anywhere, so calls like
            // "b1 'or' b2" arrive here verbatim. Resolving them is a prerequisite
            // for operator support (M3).
            throw new UnsupportedConstruct(Diagnostics.notYetImplemented("Sequence",
                "an unresolved mixfix operator call reached the generator: MixfixParser is "
                + "never invoked, so this Sequence was not replaced with a ComputedValue (M3)", value));
        }
        if (value instanceof DurationValue) {
            throw new UnsupportedConstruct(Diagnostics.libraryGap("DurationValue",
                "CRMLtoModelica.mo has no duration implementation", value));
        }
        if (value instanceof ProjectionValue) {
            throw new UnsupportedConstruct(Diagnostics.libraryGap("ProjectionValue",
                "CRMLtoModelica.mo has no projection implementation", value));
        }
        throw new UnsupportedConstruct(Diagnostics.error(value.eClass().getName(),
            "no Modelica mapping for value kind " + value.eClass().getName(), value));
    }

    private Expression transformIf(TransformationContext ctx, IfValue value) {
        Expression condition = transform(ctx, value.getCondition());
        Expression thenBranch = transform(ctx, value.getThen());
        Expression elseBranch = transform(ctx, value.getOthervise());
        // CRML conditions are Boolean4, which is an enumeration, so the Modelica
        // condition has to be a two-valued comparison against true4.
        return ifExpr(
            binary(BinaryOperatorKind.EQ, condition, ref("CRMLtoModelica.Types.Boolean4.true4")),
            thenBranch, elseBranch);
    }

    private Expression transformBooleanConstant(BooleanConstant constant) {
        switch (constant.getValue()) {
            case TRUE: return ref("CRMLtoModelica.Types.Boolean4.true4");
            case FALSE: return ref("CRMLtoModelica.Types.Boolean4.false4");
            case UNDECIDED: return ref("CRMLtoModelica.Types.Boolean4.undecided");
            case UNDEFINED: return ref("CRMLtoModelica.Types.Boolean4.undefined");
            default:
                throw new UnsupportedConstruct(Diagnostics.error("BooleanConstant",
                    "unknown boolean literal: " + constant.getValue(), constant));
        }
    }

    /** True when the value contributes nothing but a declaration (a bare {@code new}). */
    public static boolean isDeclarationOnly(Value value) {
        return value instanceof ConstructorValue
            && ConstructorTransformer.isDefaultConstructor((ConstructorValue) value);
    }
}
