package crml.compiler.crmlcv2.verification.engine;

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

import crml.compiler.crmlcv2.util.TypeResolver;
import crml.model.language.BuiltinType;
import crml.model.language.Domain;
import crml.model.language.IndirectTypeReference;
import crml.model.language.Model;
import crml.model.language.TypeReference;
import crml.model.language.UserTypereference;
import crml.model.language.Variable;
import crml.model.trace.TraceLink;
import crml.model.trace.TraceLinkKind;
import crml.model.trace.TraceModel;

/**
 * The names under which a model's CRML variables appear in a simulation result,
 * down to the scalars that can be compared.
 *
 * <p>A variable of a scalar type is one name. A class instance is the names of
 * its members, inherited ones included, under {@code instance.member}, and a
 * member that is itself a class instance is walked the same way. A set is an
 * array, so its elements are {@code name[1]} to {@code name[n]}. These are the
 * names the generated Modelica gives them: a CRML class becomes a nested model,
 * a class-typed variable a component of it, and a set an array.
 *
 * <p>Whether a variable was dropped is read from the trace: a variable, or a
 * class member, that the translation could not map has no component linked to
 * it, and neither has anything under it.
 */
final class ComparedVariables {

    /** One name in the result file, and whether it can be compared. */
    static final class Entry {
        final String name;
        final boolean comparable;
        final boolean dropped;

        Entry(String name, boolean comparable, boolean dropped) {
            this.name = name;
            this.comparable = comparable;
            this.dropped = dropped;
        }

        @Override
        public String toString() {
            return name + (comparable ? "" : " (not comparable)") + (dropped ? " (dropped)" : "");
        }
    }

    /** The types whose values both sides represent as one scalar of the same name. */
    private static final Set<BuiltinType> SCALAR = EnumSet.of(
        BuiltinType.BOOLEAN, BuiltinType.REQUIREMENT, BuiltinType.REAL, BuiltinType.INTEGER, BuiltinType.STRING);

    private final Set<Variable> declared = new HashSet<>();
    private final List<Entry> entries = new ArrayList<>();

    private ComparedVariables(TraceModel trace) {
        for (TraceLink link : trace.getLinks()) {
            if (link.getKind() == TraceLinkKind.VARIABLE_TO_COMPONENT && link.getCrml() instanceof Variable) {
                declared.add((Variable) link.getCrml());
            }
        }
    }

    /** Every name of {@code model}'s top-level variables, in declaration order. */
    static List<Entry> of(Model model, TraceModel trace) {
        ComparedVariables walk = new ComparedVariables(trace);
        for (Variable variable : model.getVariables()) {
            walk.variable("", variable, false, new ArrayDeque<>());
        }
        return Collections.unmodifiableList(walk.entries);
    }

    private void variable(String prefix, Variable variable, boolean parentDropped,
            Deque<crml.model.language.Class> enclosing) {
        String name = prefix + variable.getName();
        boolean dropped = parentDropped || !declared.contains(variable);
        if (variable.getDefinition() instanceof crml.model.language.Set<?>) {
            crml.model.language.Set<?> set = (crml.model.language.Set<?>) variable.getDefinition();
            for (int i = 1; i <= set.getElements().size(); i++) {
                type(name + "[" + i + "]", set.getDomain(), dropped, enclosing);
            }
        } else {
            type(name, variable.getDomain(), dropped, enclosing);
        }
    }

    private void type(String name, TypeReference type, boolean dropped,
            Deque<crml.model.language.Class> enclosing) {
        if (SCALAR.contains(TypeResolver.resolveBuiltin(type))) {
            entries.add(new Entry(name, true, dropped));
            return;
        }
        crml.model.language.Class clazz = classOf(type);
        // A class that contains itself, directly or not, cannot be instantiated;
        // stop rather than recurse forever.
        if (clazz == null || enclosing.contains(clazz)) {
            entries.add(new Entry(name, false, dropped));
            return;
        }
        enclosing.push(clazz);
        for (Variable member : members(clazz)) {
            variable(name + ".", member, dropped, enclosing);
        }
        enclosing.pop();
    }

    /**
     * The members of {@code clazz}, inherited first. A member redeclared further
     * down replaces the inherited one, as an extends clause does in Modelica.
     */
    private static Iterable<Variable> members(crml.model.language.Class clazz) {
        Map<String, Variable> members = new LinkedHashMap<>();
        collect(clazz, members, new HashSet<>());
        return members.values();
    }

    private static void collect(crml.model.language.Class clazz, Map<String, Variable> members,
            Set<crml.model.language.Class> visited) {
        if (!visited.add(clazz)) {
            return;
        }
        for (crml.model.language.Class superClass : clazz.getSuperClasses()) {
            collect(superClass, members, visited);
        }
        for (Variable member : clazz.getVariables()) {
            members.put(member.getName(), member);
        }
    }

    private static crml.model.language.Class classOf(TypeReference type) {
        if (type instanceof IndirectTypeReference) {
            return classOf(((IndirectTypeReference) type).getReferredType());
        }
        if (type instanceof UserTypereference) {
            Domain domain = ((UserTypereference) type).getDomain();
            if (domain instanceof crml.model.language.Class) {
                return (crml.model.language.Class) domain;
            }
        }
        return null;
    }
}
