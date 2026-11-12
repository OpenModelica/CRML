package org.openmodelica.crml.compiler.crmlcv2.verification.engine;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openmodelica.crml.compiler.crmlcv2.util.TypeResolver;
import org.openmodelica.crml.model.metamodel.BuiltinType;
import org.openmodelica.crml.model.metamodel.Domain;
import org.openmodelica.crml.model.metamodel.IndirectTypeReference;
import org.openmodelica.crml.model.metamodel.Model;
import org.openmodelica.crml.model.metamodel.TypeReference;
import org.openmodelica.crml.model.metamodel.UserTypereference;
import org.openmodelica.crml.model.metamodel.Variable;
import org.openmodelica.crml.modelica.trace.metamodel.TraceLink;
import org.openmodelica.crml.modelica.trace.metamodel.TraceLinkKind;
import org.openmodelica.crml.modelica.trace.metamodel.TraceModel;

/**
 * A model's outputs: its CRML variables as they appear in a simulation result,
 * each with the kind of value it holds.
 *
 * <p>A variable of a scalar type is one name. A class instance is the names of
 * its members, inherited ones included, under {@code instance.member}, and a
 * member that is itself a class instance is walked the same way. A set is an
 * array, so its elements are {@code name[1]} to {@code name[n]}. These are the
 * names the generated Modelica gives them.
 *
 * <p>A Clock, Period or Event is one output, not the fields of the record that
 * implements it: what it means is compared (see {@link Observation}), not how
 * the compiler's library or the reference model happens to store it.
 *
 * <p>Externals are inputs - the harness binds them - so they are listed but
 * not compared. Whether a variable was dropped is read from the trace: a
 * variable, or a class member, that the translation could not map has no
 * component linked to it, and neither has anything under it.
 */
final class Outputs {

    /** What kind of value an output holds, which decides how it is compared. */
    enum Kind {
        SCALAR,
        CLOCK,
        PERIOD,
        EVENT,
        /** An external: an input of the model, bound by the harness. */
        INPUT,
        /** A type with no comparison defined, such as Periods. */
        NO_RULE
    }

    /** One output. */
    static final class Output {
        final String name;
        final Kind kind;
        final boolean dropped;
        /** The CRML type, for an output of kind {@link Kind#NO_RULE}. */
        final String type;

        Output(String name, Kind kind, boolean dropped, String type) {
            this.name = name;
            this.kind = kind;
            this.dropped = dropped;
            this.type = type;
        }
    }

    private static final Set<BuiltinType> SCALAR = EnumSet.of(
        BuiltinType.BOOLEAN, BuiltinType.REQUIREMENT, BuiltinType.REAL, BuiltinType.INTEGER, BuiltinType.STRING);

    private final Set<Variable> declared = new HashSet<>();
    private final List<Output> outputs = new ArrayList<>();

    private Outputs(TraceModel trace) {
        for (TraceLink link : trace.getLinks()) {
            if (link.getKind() == TraceLinkKind.VARIABLE_TO_COMPONENT && link.getCrml() instanceof Variable) {
                declared.add((Variable) link.getCrml());
            }
        }
    }

    /** Every output of {@code model}, in declaration order. */
    static List<Output> of(Model model, TraceModel trace) {
        Outputs walk = new Outputs(trace);
        for (Variable variable : model.getVariables()) {
            walk.variable("", variable, false, new ArrayDeque<>());
        }
        return Collections.unmodifiableList(walk.outputs);
    }

    private void variable(String prefix, Variable variable, boolean parentDropped,
            Deque<org.openmodelica.crml.model.metamodel.Class> enclosing) {
        String name = prefix + variable.getName();
        boolean dropped = parentDropped || !declared.contains(variable);
        if (Boolean.TRUE.equals(variable.getExternal())) {
            outputs.add(new Output(name, Kind.INPUT, dropped, null));
        } else if (variable.getDefinition() instanceof org.openmodelica.crml.model.metamodel.Set<?>) {
            org.openmodelica.crml.model.metamodel.Set<?> set = (org.openmodelica.crml.model.metamodel.Set<?>) variable.getDefinition();
            for (int i = 1; i <= set.getElements().size(); i++) {
                type(name + "[" + i + "]", set.getDomain(), dropped, enclosing);
            }
        } else {
            type(name, variable.getDomain(), dropped, enclosing);
        }
    }

    private void type(String name, TypeReference type, boolean dropped,
            Deque<org.openmodelica.crml.model.metamodel.Class> enclosing) {
        BuiltinType builtin = TypeResolver.resolveBuiltin(type);
        if (SCALAR.contains(builtin)) {
            outputs.add(new Output(name, Kind.SCALAR, dropped, null));
        } else if (builtin == BuiltinType.CLOCK) {
            outputs.add(new Output(name, Kind.CLOCK, dropped, null));
        } else if (builtin == BuiltinType.PERIOD) {
            outputs.add(new Output(name, Kind.PERIOD, dropped, null));
        } else if (builtin == BuiltinType.EVENT) {
            outputs.add(new Output(name, Kind.EVENT, dropped, null));
        } else if (builtin != null) {
            outputs.add(new Output(name, Kind.NO_RULE, dropped, builtin.getLiteral()));
        } else {
            org.openmodelica.crml.model.metamodel.Class clazz = classOf(type);
            if (clazz == null) {
                outputs.add(new Output(name, Kind.NO_RULE, dropped, "unresolved type"));
            } else if (enclosing.contains(clazz)) {
                // A class that contains itself cannot be instantiated; stop
                // rather than recurse forever.
                outputs.add(new Output(name, Kind.NO_RULE, dropped, "recursive class " + clazz.getName()));
            } else {
                enclosing.push(clazz);
                for (Variable member : members(clazz)) {
                    variable(name + ".", member, dropped, enclosing);
                }
                enclosing.pop();
            }
        }
    }

    /**
     * The members of {@code clazz}, inherited first. A member redeclared further
     * down replaces the inherited one, as an extends clause does in Modelica.
     */
    private static Iterable<Variable> members(org.openmodelica.crml.model.metamodel.Class clazz) {
        Map<String, Variable> members = new LinkedHashMap<>();
        collect(clazz, members, new HashSet<>());
        return members.values();
    }

    private static void collect(org.openmodelica.crml.model.metamodel.Class clazz, Map<String, Variable> members,
            Set<org.openmodelica.crml.model.metamodel.Class> visited) {
        if (!visited.add(clazz)) {
            return;
        }
        for (org.openmodelica.crml.model.metamodel.Class superClass : clazz.getSuperClasses()) {
            collect(superClass, members, visited);
        }
        for (Variable member : clazz.getVariables()) {
            members.put(member.getName(), member);
        }
    }

    private static org.openmodelica.crml.model.metamodel.Class classOf(TypeReference type) {
        if (type instanceof IndirectTypeReference) {
            return classOf(((IndirectTypeReference) type).getReferredType());
        }
        if (type instanceof UserTypereference) {
            Domain domain = ((UserTypereference) type).getDomain();
            if (domain instanceof org.openmodelica.crml.model.metamodel.Class) {
                return (org.openmodelica.crml.model.metamodel.Class) domain;
            }
        }
        return null;
    }
}
