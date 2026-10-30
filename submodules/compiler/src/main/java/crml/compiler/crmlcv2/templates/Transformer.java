package crml.compiler.crmlcv2.templates;

import crml.compiler.crmlcv2.TransformationContext;
import crml.compiler.crmlcv2.templates.value.BinaryOperatorTransformer;
import crml.compiler.crmlcv2.templates.value.ConstructorTransformer;
import crml.compiler.crmlcv2.templates.value.IntegrateTransformer;
import crml.compiler.crmlcv2.templates.value.PeriodsTransformer;
import crml.compiler.crmlcv2.templates.value.RecordBuild;
import crml.compiler.crmlcv2.templates.value.UnaryOperatorTransformer;
import crml.compiler.crmlcv2.util.TypeResolver;
import crml.model.language.Model;

/**
 * One instance of every template, wired to each other through this object.
 *
 * <p>The templates hold no translation state - that lives on the
 * {@link TransformationContext} - so one {@code Transformer} can translate any
 * number of models.
 */
public final class Transformer {

    private final TypeResolver types = new TypeResolver();
    private final BlockInstantiation blocks = new BlockInstantiation();
    private final RecordBuild records = new RecordBuild();
    private final ModelTransformer models = new ModelTransformer(this);
    private final ClassTransformer classes = new ClassTransformer(this);
    private final VariableTransformer variables = new VariableTransformer(this);
    private final OperatorTransformer operators = new OperatorTransformer(this);
    private final SetTransformer sets = new SetTransformer(this);
    private final ValueTransformer values = new ValueTransformer(this);
    private final BinaryOperatorTransformer binaryOperators = new BinaryOperatorTransformer(this);
    private final UnaryOperatorTransformer unaryOperators = new UnaryOperatorTransformer(this);
    private final ConstructorTransformer constructors = new ConstructorTransformer(this);
    private final IntegrateTransformer integrals = new IntegrateTransformer(this);
    private final PeriodsTransformer periods = new PeriodsTransformer(this);

    /** Translates one CRML model; see {@link ModelTransformer#transform}. */
    public TransformationContext transform(Model model) {
        return models.transform(model);
    }

    public TypeResolver types() {
        return types;
    }

    public BlockInstantiation blocks() {
        return blocks;
    }

    public RecordBuild records() {
        return records;
    }

    public ModelTransformer models() {
        return models;
    }

    public ClassTransformer classes() {
        return classes;
    }

    public VariableTransformer variables() {
        return variables;
    }

    public OperatorTransformer operators() {
        return operators;
    }

    public SetTransformer sets() {
        return sets;
    }

    public ValueTransformer values() {
        return values;
    }

    public BinaryOperatorTransformer binaryOperators() {
        return binaryOperators;
    }

    public UnaryOperatorTransformer unaryOperators() {
        return unaryOperators;
    }

    public ConstructorTransformer constructors() {
        return constructors;
    }

    public IntegrateTransformer integrals() {
        return integrals;
    }

    public PeriodsTransformer periods() {
        return periods;
    }
}
