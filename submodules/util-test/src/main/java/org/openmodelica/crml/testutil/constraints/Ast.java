package org.openmodelica.crml.testutil.constraints;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Syntax tree of one constraint (see CONSTRAINT_LANGUAGE.md, "AST"). The classes
 * are immutable; {@code toString()} gives back source-like text for messages.
 */
public final class Ast {

    private Ast() {
    }

    /** The leading word of a constraint: none (the plain form), {@code all} or {@code some}. */
    public enum Quantifier { PLAIN, ALL, SOME }

    /** {@code quantifier? chain condition}; a {@link Count} condition has quantifier PLAIN. */
    public static final class Constraint {
        public final Quantifier quantifier;
        public final Chain left;
        public final Condition condition;

        Constraint(Quantifier quantifier, Chain left, Condition condition) {
            this.quantifier = quantifier;
            this.left = left;
            this.condition = condition;
        }
    }

    /** {@code selector ( "/" selector )* step*}. */
    public static final class Chain {
        public final List<Selector> selectors;
        public final List<Step> steps;

        Chain(List<Selector> selectors, List<Step> steps) {
            this.selectors = Collections.unmodifiableList(new ArrayList<>(selectors));
            this.steps = Collections.unmodifiableList(new ArrayList<>(steps));
        }

        /** The selectors before the last one: definite descriptions that narrow the universe. */
        public List<Selector> scope() {
            return selectors.subList(0, selectors.size() - 1);
        }

        /** The quantified selector, the one the steps start from. */
        public Selector last() {
            return selectors.get(selectors.size() - 1);
        }

        @Override
        public String toString() {
            StringBuilder text = new StringBuilder();
            for (int i = 0; i < selectors.size(); i++) {
                text.append(i == 0 ? "" : " / ").append(selectors.get(i));
            }
            for (Step step : steps) {
                text.append(step);
            }
            return text.toString();
        }
    }

    /** {@code "[" TYPE ( "::" filter ( "," filter )* )? "]"}. */
    public static final class Selector {
        public final String type;
        public final List<Filter> filters;

        Selector(String type, List<Filter> filters) {
            this.type = type;
            this.filters = Collections.unmodifiableList(new ArrayList<>(filters));
        }

        @Override
        public String toString() {
            StringBuilder text = new StringBuilder("[").append(type);
            for (int i = 0; i < filters.size(); i++) {
                text.append(i == 0 ? "::" : ", ").append(filters.get(i));
            }
            return text.append(']').toString();
        }
    }

    /** {@code path ( "==" | "!=" | "contains" ) literal}. */
    public static final class Filter {
        public final List<String> path;
        public final String op;
        public final Literal value;

        Filter(List<String> path, String op, Literal value) {
            this.path = Collections.unmodifiableList(new ArrayList<>(path));
            this.op = op;
            this.value = value;
        }

        @Override
        public String toString() {
            return String.join(".", path) + " " + op + " " + value;
        }
    }

    /** One step of a chain. */
    public abstract static class Step {
        Step() {
        }
    }

    /** {@code "." NAME}: a structural feature or a parameterless operation. */
    public static final class FeatureStep extends Step {
        public final String name;

        FeatureStep(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "." + name;
        }
    }

    /** {@code "." "@" PSEUDO}. */
    public static final class PseudoStep extends Step {
        public enum Kind { ROOT, CONTAINER, FILE }

        public final Kind kind;

        PseudoStep(Kind kind) {
            this.kind = kind;
        }

        @Override
        public String toString() {
            return ".@" + kind.name().toLowerCase();
        }
    }

    /** The condition of a constraint. */
    public abstract static class Condition {
        Condition() {
        }
    }

    /** {@code ==}, {@code !=}, {@code contains} or {@code not contains}, with an operand. */
    public static final class Compare extends Condition {
        public final String op;
        public final Operand right;

        Compare(String op, Operand right) {
            this.op = op;
            this.right = right;
        }

        @Override
        public String toString() {
            return op + " " + right;
        }
    }

    /** {@code is empty}, or {@code is not empty} when negated. */
    public static final class IsEmpty extends Condition {
        public final boolean negated;

        IsEmpty(boolean negated) {
            this.negated = negated;
        }

        @Override
        public String toString() {
            return negated ? "is not empty" : "is empty";
        }
    }

    /** {@code is a TYPE}. */
    public static final class IsA extends Condition {
        public final String type;

        IsA(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return "is a " + type;
        }
    }

    /** The {@code count chain compare INT} form; the chain is {@link Constraint#left}. */
    public static final class Count extends Condition {
        public final String op;
        public final long n;

        Count(String op, long n) {
            this.op = op;
            this.n = n;
        }

        @Override
        public String toString() {
            return op + " " + n;
        }
    }

    /** The right operand of a {@link Compare}. */
    public abstract static class Operand {
        Operand() {
        }
    }

    /** A string ({@code String}), integer ({@code Long}) or boolean ({@code Boolean}) literal. */
    public static final class Literal extends Operand {
        public final Object value;

        Literal(Object value) {
            this.value = value;
        }

        @Override
        public String toString() {
            if (value instanceof String) {
                return "\"" + ((String) value).replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
            }
            return String.valueOf(value);
        }
    }

    /** A chain used as a right operand: a definite description. */
    public static final class ChainOperand extends Operand {
        public final Chain chain;

        ChainOperand(Chain chain) {
            this.chain = chain;
        }

        @Override
        public String toString() {
            return chain.toString();
        }
    }
}
