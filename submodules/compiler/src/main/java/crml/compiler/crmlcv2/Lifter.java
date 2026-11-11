package crml.compiler.crmlcv2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;

import crml.compiler.crmlcv2.templates.LiftableTransformer;
import crml.compiler.crmlcv2.templates.Transformer;
import crml.compiler.crmlcv2.util.TypeResolver;
import crml.model.language.BuiltinType;
import crml.model.language.Liftable;
import crml.model.language.ScatteredValue;
import crml.model.language.Value;
import crml.model.modelica.BinaryOperatorKind;
import crml.model.modelica.ClassDefinition;
import crml.model.modelica.ComponentDeclaration;
import crml.model.modelica.ComponentReference;
import crml.model.modelica.Equation;
import crml.model.modelica.Expression;
import crml.model.modelica.Reference;
import crml.modelica.build.Modelica;

/**
 * Builds the <em>region</em> of one lifted application: an operator applied to
 * {@code x.element} operands is evaluated once for every combination of the
 * elements of those operands, and the results form a flat array with a mask.
 *
 * <p>One {@code Lifter} serves one application, in three steps:
 *
 * <ol>
 * <li><b>Plan</b> (the constructor, called from {@link LiftableTransformer#open}).
 *     Each lifted operand is an <em>axis</em>, in operand order. The names of
 *     the region are allocated; nothing is added to the class.
 * <li><b>Body</b>. The operator transformer asks for its operands with
 *     {@link #operand}: a lifted operand comes back as the element of the
 *     current slot, any other operand is translated once, in the
 *     {@linkplain #outer() enclosing context}, and is the same for every slot.
 *     What the operator needs per slot - a block instance, its input equations -
 *     it declares on {@link #body()}, the lifted mode of the context.
 * <li><b>Emit</b> ({@link #close}). Everything is added to the class at once:
 *     the sizes, the result and mask arrays, the per-slot components, and the
 *     nested {@code for} loops. If anything fails before that, the
 *     {@code Lifter} is simply dropped and nothing of the region reaches the
 *     class.
 * </ol>
 *
 * With axes of sizes {@code n1..nk} the region has {@code n1 * ... * nk} slots.
 * The slot of the combination {@code (i1, ..., ik)} is, 1-based and row-major,
 * {@code (...((i1 - 1) * n2 + i2 - 1) * n3 + ...) * nk + ik}. A slot is active
 * when every axis is active at its index; the mask records that, and for a
 * Boolean or Requirement result an inactive slot is {@code Boolean4.undefined},
 * the identity of the Boolean4 operators.
 */
public final class Lifter implements Modelica {

    /**
     * The names of one region and what the {@linkplain #body() lifted-mode
     * context} has buffered for it.
     */
    static final class LoopFrame implements Modelica {
        /** The loop variable of each axis. */
        final List<String> iterators = new ArrayList<String>();
        /** The size parameter of each axis. */
        final List<String> axisSizes = new ArrayList<String>();
        String sizeName;
        /** Components declared per slot, already carrying the slot dimension. */
        final List<ComponentDeclaration> components = new ArrayList<ComponentDeclaration>();
        /** Equations of the loop body. */
        final List<Equation> equations = new ArrayList<Equation>();

        /** The number of slots; a new tree on each call. */
        Expression size() {
            return ref(sizeName);
        }

        /** The index of the current slot; a new tree on each call. */
        Expression index() {
            Expression index = ref(iterators.get(0));
            for (int j = 1; j < iterators.size(); j++) {
                index = binary(BinaryOperatorKind.ADD,
                    binary(BinaryOperatorKind.MUL,
                        binary(BinaryOperatorKind.SUB, index, integer(1)),
                        ref(axisSizes.get(j))),
                    ref(iterators.get(j)));
            }
            return index;
        }
    }

    private static final String UNDEFINED = "CRMLtoModelica.Types.Boolean4.undefined";

    private final Transformer transformer;
    private final TransformationContext outer;
    private final Liftable application;
    private final List<SetHandle> axes;
    private final LoopFrame frame = new LoopFrame();
    private final TransformationContext body;
    private final String outName;
    private final String maskName;

    /**
     * Plans the region of {@code application}.
     *
     * @param axes the collection of each of the application's lifts, in the
     *             order of {@code application.getLifts()}
     * @param base the prefix of the region's names: the name of the operator
     */
    public Lifter(Transformer transformer, TransformationContext outer, Liftable application,
            List<SetHandle> axes, String base) {
        this.transformer = transformer;
        this.outer = outer;
        this.application = application;
        this.axes = Collections.unmodifiableList(new ArrayList<SetHandle>(axes));
        for (int j = 0; j < axes.size(); j++) {
            frame.iterators.add(outer.allocateName("i"));
            frame.axisSizes.add(outer.allocateName(base + "_n"));
        }
        frame.sizeName = outer.allocateName(base + "_size");
        this.outName = outer.allocateName(base + "_out");
        this.maskName = outer.allocateName(base + "_mask");
        this.body = outer.lifted(frame);
    }

    /** The enclosing context: where everything that is not per slot goes. */
    public TransformationContext outer() {
        return outer;
    }

    /** True if the operand is one of the application's {@code .element} operands. */
    public boolean isLifted(Value operand) {
        return axisOf(operand) >= 0;
    }

    private int axisOf(Value operand) {
        Value value = LiftableTransformer.unparen(operand);
        return value instanceof ScatteredValue ? application.getLifts().indexOf(value) : -1;
    }

    /**
     * The operand as the operator sees it for the current slot: the element of
     * the current slot for a lifted operand, otherwise the operand's translation
     * in the {@linkplain #outer() enclosing context}, which is evaluated once.
     * A new tree on each call.
     */
    public Expression operand(Value operand) {
        int axis = axisOf(operand);
        if (axis >= 0) {
            return axes.get(axis).element(ref(frame.iterators.get(axis)));
        }
        return transformer.values().transform(outer, operand);
    }

    /** The CRML type of {@link #operand}, or null if unknown. */
    public BuiltinType operandBuiltin(Value operand) {
        int axis = axisOf(operand);
        return axis >= 0 ? axes.get(axis).elementBuiltin() : TypeResolver.inferBuiltin(operand);
    }

    /**
     * The context for what the operator needs once per slot; see
     * {@link TransformationContext#lifted}.
     */
    public TransformationContext body() {
        return body;
    }

    /**
     * Emits the region into the enclosing context and returns its result as a
     * collection.
     *
     * @param value         the result for the current slot
     * @param resultType    the Modelica type of {@code value}; copied, not consumed
     * @param resultBuiltin the CRML type of {@code value}, or null if unknown
     * @param source        the CRML element the region is traced to
     */
    public SetHandle close(Expression value, Reference<ClassDefinition> resultType,
            BuiltinType resultBuiltin, EObject source) {
        int k = axes.size();

        // Sizes: one per axis, then their product.
        Expression product = null;
        for (int j = 0; j < k; j++) {
            String name = frame.axisSizes.get(j);
            outer.declare(parameter(this.<ClassDefinition>builtinRef("Integer"), name, axes.get(j).size()), source);
            product = product == null ? ref(name) : binary(BinaryOperatorKind.MUL, product, ref(name));
        }
        outer.declare(parameter(this.<ClassDefinition>builtinRef("Integer"), frame.sizeName, product), source);

        // Result and mask.
        ComponentDeclaration out = component(EcoreUtil.copy(resultType), outName);
        out.getArrayDimensions().add(dimension(frame.size()));
        outer.declare(out, source);
        ComponentDeclaration mask = component(this.<ClassDefinition>builtinRef("Boolean"), maskName);
        mask.getArrayDimensions().add(dimension(frame.size()));
        outer.declare(mask, source);

        // What the body declared per slot.
        for (ComponentDeclaration component : frame.components) {
            outer.declare(component, source);
        }

        // The loop body: the mask, what the body equated, the result.
        List<Equation> equations = new ArrayList<Equation>();
        Expression active = null;
        for (int j = 0; j < k; j++) {
            Expression axisActive = axes.get(j).active(ref(frame.iterators.get(j)));
            if (axisActive != null) {
                active = active == null ? axisActive : binary(BinaryOperatorKind.AND, active, axisActive);
            }
        }
        equations.add(eq(subscripted(ref(maskName), frame.index()), active == null ? bool(true) : active));
        equations.addAll(frame.equations);
        Expression result = value;
        if (resultBuiltin == BuiltinType.BOOLEAN || resultBuiltin == BuiltinType.REQUIREMENT) {
            result = ifExpr(subscripted(ref(maskName), frame.index()), value, ref(UNDEFINED));
        }
        equations.add(eq(subscripted(ref(outName), frame.index()), result));
        for (Equation equation : equations) {
            outer.linkNestedEquation(equation, source);
        }

        // One loop per axis, the first axis outermost.
        List<? extends Equation> nest = equations;
        for (int j = k - 1; j >= 0; j--) {
            nest = Collections.singletonList(
                forEq(frame.iterators.get(j), integer(1), ref(frame.axisSizes.get(j)), nest));
        }
        outer.equate(nest.get(0), source);

        if (k >= 2 && !frame.components.isEmpty()) {
            StringBuilder count = new StringBuilder();
            for (String name : frame.axisSizes) {
                count.append(count.length() == 0 ? "" : " * ").append(name);
            }
            outer.report(Diagnostics.info("Lift.InstanceCount",
                "an application lifted over " + k + " collections has one instance of each of its "
                + frame.components.size() + " per-element component(s) for every combination: "
                + count + " instances each", source));
        }

        return SetHandle.lifted(ref(outName), ref(maskName), ref(frame.sizeName), resultType, resultBuiltin);
    }
}
