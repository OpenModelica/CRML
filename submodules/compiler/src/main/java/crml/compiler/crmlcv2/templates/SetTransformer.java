package crml.compiler.crmlcv2.templates;

import java.util.ArrayList;
import java.util.List;

import crml.compiler.crmlcv2.Diagnostic;
import crml.compiler.crmlcv2.Diagnostics;
import crml.compiler.crmlcv2.TransformationContext;
import crml.compiler.crmlcv2.UnsupportedConstruct;
import crml.compiler.crmlcv2.util.TypeResolver;
import crml.model.language.BuiltinType;
import crml.model.language.Set;
import crml.model.language.Value;
import crml.model.modelica.ArrayConstructor;
import crml.model.modelica.ClassDefinition;
import crml.model.modelica.ComponentDeclaration;
import crml.model.modelica.Expression;
import crml.model.modelica.Reference;
import crml.modelica.build.Modelica;

/**
 * A CRML set becomes a Modelica array.
 *
 * <p>{@code CRMLtoModelica.Blocks.setAnd} is the library's one set consumer and
 * takes {@code Types.Boolean4[:]}, which is the shape produced here - but its
 * equation section is empty, so a model that relies on it computes nothing.
 * That is recorded as an INFO rather than silently emitted.
 */
public final class SetTransformer implements Modelica {

    private final Transformer transformer;

    public SetTransformer(Transformer transformer) {
        this.transformer = transformer;
    }

    /** The {@code {a, b, c}} literal for a set used as a value. */
    public ArrayConstructor literal(TransformationContext ctx, Set<?> set) {
        List<Expression> elements = new ArrayList<Expression>();
        for (Object element : set.getElements()) {
            if (!(element instanceof Value)) {
                throw new UnsupportedConstruct(Diagnostics.error("Set",
                    "a set element is not a value: " + element, set));
            }
            elements.add(transformer.values().transform(ctx, (Value) element));
        }
        ctx.report(Diagnostics.info("Set",
            "emitted as an array; its intended consumer CRMLtoModelica.Blocks.setAnd has an "
            + "empty equation section, so anything reading this set computes nothing", set));
        return array(elements);
    }

    /**
     * The Modelica type of a set's elements, or null when it cannot be worked
     * out.
     *
     * <p>A Modelica array is typed by its element, but a CRML set carries no
     * element type: {@code Set.domain} is never populated by the DOM builders,
     * and the declaring variable's domain names the set's own CRML type, which
     * is not the same thing - "Periods P3 is { P1, P2, Pn }" declares a set of
     * Periods, and it is Period that the array elements are. So the element type
     * comes from the elements themselves.
     */
    public Reference<ClassDefinition> elementType(Set<?> set) {
        for (Object element : set.getElements()) {
            if (element instanceof Value) {
                BuiltinType elementType = TypeResolver.inferBuiltin((Value) element);
                if (elementType != null) {
                    String name = TypeResolver.resolve(elementType);
                    return elementType == BuiltinType.REAL || elementType == BuiltinType.INTEGER
                        || elementType == BuiltinType.STRING
                        ? this.<ClassDefinition>builtinRef(name)
                        : this.<ClassDefinition>libraryRef(name);
                }
            }
        }
        return null;
    }

    /** A set declared on the model itself, rather than used as a value. */
    public void declare(TransformationContext ctx, Set<?> set) {
        Reference<ClassDefinition> elementType = elementType(set);
        if (elementType == null) {
            elementType = transformer.types().resolve(ctx, set.getDomain());
        }

        ArrayConstructor elements;
        try {
            elements = literal(ctx, set);
        } catch (UnsupportedConstruct e) {
            Diagnostic cause = e.diagnostic();
            ctx.reportWithPlaceholder(new Diagnostic(cause.severity(), cause.constructKind(),
                "set '" + set.getName() + "' was dropped: " + cause.message(),
                cause.crmlSource() == null ? set : cause.crmlSource()));
            return;
        }

        ComponentDeclaration component = component(elementType, set.getName());
        component.getArrayDimensions().add(dimension(integer(elements.getElements().size())));
        component.setBinding(elements);
        ctx.declare(component, set);
    }

    /** The array dimension a variable needs when its definition is a set. */
    public static int sizeOf(Set<?> set) {
        return set.getElements().size();
    }
}
