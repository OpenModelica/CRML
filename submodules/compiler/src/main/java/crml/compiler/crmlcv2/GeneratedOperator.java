package crml.compiler.crmlcv2;

import java.util.Collections;
import java.util.List;

import crml.model.language.Variable;
import crml.model.modelica.ClassDefinition;
import crml.model.modelica.ClassKind;

/**
 * The Modelica class generated for one CRML template or user operator, plus
 * what a call site needs in order to use it.
 *
 * <p>An operator whose body is a pure expression over its parameters becomes a
 * Modelica {@code function} and its calls become function calls. An operator
 * whose body hoists a component or an equation cannot be a function - a
 * Modelica function body is an algorithm and holds no components with equations
 * - so it becomes a {@code block}, and its calls become component
 * instantiations wired with equations.
 */
public final class GeneratedOperator {

    /** The name of the single output of a generated operator class. */
    public static final String OUTPUT_PORT = "out";

    private final ClassDefinition definition;
    private final List<Variable> parameters;

    public GeneratedOperator(ClassDefinition definition, List<Variable> parameters) {
        this.definition = definition;
        this.parameters = Collections.unmodifiableList(parameters);
    }

    public ClassDefinition definition() {
        return definition;
    }

    public String name() {
        return definition.getName();
    }

    /** True when calls must instantiate this class rather than call it. */
    public boolean isBlock() {
        return definition.getKind() != ClassKind.FUNCTION;
    }

    /** The operator's header variables, in declaration order. */
    public List<Variable> parameters() {
        return parameters;
    }
}
