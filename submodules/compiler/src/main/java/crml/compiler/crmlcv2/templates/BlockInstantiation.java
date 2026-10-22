package crml.compiler.crmlcv2.templates;

import java.util.Map;

import org.eclipse.emf.ecore.EObject;

import crml.compiler.crmlcv2.TransformationContext;
import crml.model.modelica.ClassDefinition;
import crml.model.modelica.ComponentReference;
import crml.model.modelica.Expression;
import crml.model.modelica.Reference;
import crml.modelica.build.Modelica;

/**
 * Instantiating a Modelica block and wiring its inputs.
 *
 * <p>A block cannot be embedded in an expression the way a function call can: it
 * has to be declared as a named component and its inputs bound by equations on
 * the enclosing class before anything can read its output. This formalises what
 * the string generator's {@code BlockScope} did as a constructor side effect -
 * the same hoist, but explicit, named and traced.
 *
 * <p>For the blocks in {@code CRMLtoModelica.mo} the port names are fixed: every
 * block takes {@code r1} (and {@code r2} where it is binary) and produces
 * {@code out}. Verified for EventFilter, Integrate, ClockTick, CardClock,
 * BoolTick, unaryBoolAnd, ClockAdd and setAnd. Blocks this compiler generates
 * itself use their CRML parameter names instead.
 */
public final class BlockInstantiation implements Modelica {

    /**
     * Declares {@code blockType <allocated>;}, emits one equation per input, and
     * returns a reference to the block's output port.
     *
     * @param inputs port name to the expression driving it; iteration order
     *               decides equation order, so pass an ordered map
     */
    public ComponentReference instantiate(TransformationContext ctx,
            Reference<ClassDefinition> blockType, String namePrefix, Map<String, Expression> inputs,
            String outputPort, EObject crmlSource) {
        String name = ctx.allocateName(namePrefix);
        ctx.declare(component(blockType, name), crmlSource);

        for (Map.Entry<String, Expression> input : inputs.entrySet()) {
            ctx.equate(eq(
                ref(name + "." + input.getKey()),
                input.getValue()), crmlSource);
        }

        return ref(name + "." + outputPort);
    }
}
