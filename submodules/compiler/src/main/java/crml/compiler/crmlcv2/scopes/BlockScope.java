package crml.compiler.crmlcv2.scopes;

import java.util.Map;

// A Modelica `block` instance, as opposed to a `function` call: a block can't
// be embedded inline in an expression the way FunctionScope's text can, it
// has to be declared as a named component and have its inputs wired with
// equations on the enclosing model before it can be referenced. This class
// performs that declaration/wiring as a side effect of construction (against
// the given host), and reference() then returns the component's output port.
//
// This only covers the plain causal input/output blocks seen so far
// (CardClock, ClockTick, ...) - no connectors, no parameters, single output.
public class BlockScope implements Scope {
    private final String instanceName;
    private final String outputPort;

    public BlockScope(ModelScope host, String typeName, String instanceName, Map<String, Scope> inputs, String outputPort){
        this.instanceName = instanceName;
        this.outputPort = outputPort;

        host.addVariable(new RawstringScope(typeName + " " + instanceName + ";"));
        for (Map.Entry<String, Scope> input : inputs.entrySet()) {
            host.addEquation(new RawstringScope(instanceName + "." + input.getKey() + " = " + input.getValue().reference() + ";"));
        }
    }

    @Override
    public String toModelica(int indent) {
        return indent(indent) + reference();
    }

    @Override
    public String reference() {
        return instanceName + "." + outputPort;
    }
}