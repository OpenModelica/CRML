package org.openmodelica.crml.language.workspace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;

import crml.language.grammar.crmlParser.DefinitionContext;
import crml.language.grammar.crmlParser.DependencyContext;
import crml.language.grammar.crmlParser.DependencySetContext;
import crml.language.grammar.crmlParser.IdContext;
import crml.language.grammar.crmlParser.SingleDependencyContext;

/**
 * What a file declares, read from the parse tree: its kind ({@code model},
 * {@code library}, ...), its name and the names it depends on.
 *
 * <p>Read from the parse tree, not from the DOM, so that it is also available
 * for a file with syntax errors whose header still parsed. The workspace uses
 * it to tell a <em>broken</em> dependency from a <em>missing</em> one.
 */
public final class RootHeader {

    /** One dependency as written in the header. */
    public static final class DependencyName {
        private final String name;
        private final boolean flattened;
        private final int line;
        private final int column;

        public DependencyName(String name, boolean flattened) {
            this(name, flattened, 0, 0);
        }

        /** @param line 1-based, as in ANTLR; 0 if unknown */
        public DependencyName(String name, boolean flattened, int line, int column) {
            this.name = name;
            this.flattened = flattened;
            this.line = line;
            this.column = column;
        }

        /** The declared name of the library or model depended on. */
        public String name() { return name; }

        /** True if it came from {@code flatten { A, B } union}. */
        public boolean flattened() { return flattened; }

        /** Where the name is written: its line, 0 if unknown. */
        public int line() { return line; }

        /** Where the name is written: its column in {@link #line()}. */
        public int column() { return column; }

        @Override
        public String toString() {
            return flattened ? "flatten " + name : name;
        }
    }

    private final String kind;
    private final String name;
    private final List<DependencyName> dependencies;

    public RootHeader(String kind, String name, List<DependencyName> dependencies) {
        this.kind = kind;
        this.name = name;
        this.dependencies = Collections.unmodifiableList(new ArrayList<>(dependencies));
    }

    /**
     * Reads the header of a parse tree.
     *
     * @return the header, or null if the tree has no readable header
     *         (not a definition, or the kind or name are missing)
     */
    public static RootHeader read(ParseTree tree) {
        if (!(tree instanceof DefinitionContext)) {
            return null;
        }
        DefinitionContext definition = (DefinitionContext) tree;
        if (definition.definition_type() == null || definition.id() == null) {
            return null;
        }
        List<DependencyName> dependencies = new ArrayList<>();
        for (DependencyContext dependency : definition.dependency()) {
            if (dependency instanceof SingleDependencyContext) {
                IdContext id = ((SingleDependencyContext) dependency).id();
                if (id != null) {
                    dependencies.add(dependency(id, false));
                }
            } else if (dependency instanceof DependencySetContext) {
                for (IdContext id : ((DependencySetContext) dependency).id()) {
                    dependencies.add(dependency(id, true));
                }
            }
        }
        return new RootHeader(definition.definition_type().getText(), definition.id().getText(), dependencies);
    }

    private static DependencyName dependency(IdContext id, boolean flattened) {
        return new DependencyName(id.getText(), flattened,
            id.getStart().getLine(), id.getStart().getCharPositionInLine());
    }

    /** {@code model}, {@code library} or {@code package}, as written. */
    public String kind() { return kind; }

    /** The declared name; dependencies are looked up by it, not by file name. */
    public String name() { return name; }

    /** The dependencies in the order they are written. */
    public List<DependencyName> dependencies() { return dependencies; }

    public boolean isModel() { return "model".equals(kind); }

    @Override
    public String toString() {
        return kind + " " + name + (dependencies.isEmpty() ? "" : " <- " + dependencies);
    }
}
