package org.openmodelica.crml.compiler.crmlcv2.templates;

import org.eclipse.emf.ecore.EObject;
import org.openmodelica.crml.compiler.crmlcv2.Diagnostics;
import org.openmodelica.crml.compiler.crmlcv2.TransformationContext;
import org.openmodelica.crml.compiler.crmlcv2.templates.value.ConstructorTransformer;
import org.openmodelica.crml.compiler.crmlcv2.util.CRMLCompilerException;
import org.openmodelica.crml.model.metamodel.BinaryOperator;
import org.openmodelica.crml.model.metamodel.BooleanConstant;
import org.openmodelica.crml.model.metamodel.ComputedValue;
import org.openmodelica.crml.model.metamodel.ConstructorValue;
import org.openmodelica.crml.model.metamodel.DurationValue;
import org.openmodelica.crml.model.metamodel.IfValue;
import org.openmodelica.crml.model.metamodel.IntegerConstant;
import org.openmodelica.crml.model.metamodel.IntegrateValue;
import org.openmodelica.crml.model.metamodel.Liftable;
import org.openmodelica.crml.model.metamodel.PeriodsValue;
import org.openmodelica.crml.model.metamodel.ProjectionValue;
import org.openmodelica.crml.model.metamodel.RealConstant;
import org.openmodelica.crml.model.metamodel.ScatteredValue;
import org.openmodelica.crml.model.metamodel.Sequence;
import org.openmodelica.crml.model.metamodel.Set;
import org.openmodelica.crml.model.metamodel.StringConstant;
import org.openmodelica.crml.model.metamodel.TimeValue;
import org.openmodelica.crml.model.metamodel.UnaryOperator;
import org.openmodelica.crml.model.metamodel.Value;
import org.openmodelica.crml.model.metamodel.VariableReference;
import org.openmodelica.crml.modelica.metamodel.BinaryOperatorKind;
import org.openmodelica.crml.modelica.metamodel.Expression;
import org.openmodelica.crml.modelica.build.Modelica;

/**
 * The CRML {@code Value} dispatcher.
 *
 * <p>Every case returns an {@link Expression} unconditionally: there is no
 * "cannot be embedded inline" result any more. A value that needs something
 * hoisted declares it on the {@link TransformationContext} and returns a
 * reference to what it declared; a value with no Modelica target throws
 * {@link UnsupportedConstruct}, which the enclosing declaration or equation
 * turns into a diagnostic and a placeholder.
 *
 * <p>This is the dispatch for positions where a single value is expected. A
 * collection - {@code x.element}, or an application lifted by one - is
 * translated by {@link ScatteredValueTransformer#collect} instead, and is an
 * error here.
 */
public final class ValueTransformer implements Modelica {

    private final Transformer transformer;

    public ValueTransformer(Transformer transformer) {
        this.transformer = transformer;
    }

    public Expression transform(TransformationContext ctx, Value value) {
        if (value == null) {
            throw CRMLCompilerException.unsupported(Diagnostics.error("Value",
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
        if (value instanceof ScatteredValue) {
            // Always an error here: a lift is consumed by its application.
            return transformer.scatteredValues().transform(ctx, (ScatteredValue) value);
        }
        if (value instanceof Liftable && !((Liftable) value).getLifts().isEmpty()) {
            // A lifted application is a collection. It is translated where a
            // collection is expected (ScatteredValueTransformer.collect); here a
            // single value is, and a set does not propagate into one.
            EObject consumer = value.eContainer();
            throw CRMLCompilerException.unsupported(Diagnostics.notYetImplemented("Lift.SetWhereValueExpected",
                "a " + value.eClass().getName() + " with an '.element' operand yields a set, which "
                + (consumer == null ? "nothing" : "a " + consumer.eClass().getName())
                + " cannot take as a single value; aggregate it (and, or) or lift the consumer with '.element'",
                value));
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
            // org.openmodelica.crml.language.opcall.MixfixParser - the resolver that would replace
            // these with ComputedValue - is not called from anywhere, so calls like
            // "b1 'or' b2" arrive here verbatim. Resolving them is a prerequisite
            // for operator support (M3).
            throw CRMLCompilerException.unsupported(Diagnostics.notYetImplemented("Sequence",
                "an unresolved mixfix operator call reached the generator: MixfixParser is "
                + "never invoked, so this Sequence was not replaced with a ComputedValue (M3)", value));
        }
        if (value instanceof DurationValue) {
            throw CRMLCompilerException.unsupported(Diagnostics.libraryGap("DurationValue",
                "CRMLtoModelica.mo has no duration implementation", value));
        }
        if (value instanceof ProjectionValue) {
            throw CRMLCompilerException.unsupported(Diagnostics.libraryGap("ProjectionValue",
                "CRMLtoModelica.mo has no projection implementation", value));
        }
        throw CRMLCompilerException.unsupported(Diagnostics.error(value.eClass().getName(),
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
                throw CRMLCompilerException.unsupported(Diagnostics.error("BooleanConstant",
                    "unknown boolean literal: " + constant.getValue(), constant));
        }
    }

    /** True when the value contributes nothing but a declaration (a bare {@code new}). */
    public static boolean isDeclarationOnly(Value value) {
        return value instanceof ConstructorValue
            && ConstructorTransformer.isDefaultConstructor((ConstructorValue) value);
    }
}
