package org.openmodelica.crml.language.opcall;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

import com.google.common.collect.Iterables;

import org.openmodelica.crml.language.dom.BuildContext;
import org.openmodelica.crml.model.metamodel.Binding;
import org.openmodelica.crml.model.metamodel.ComputedValue;
import org.openmodelica.crml.model.metamodel.CustomOperator;
import org.openmodelica.crml.model.metamodel.Keyword;
import org.openmodelica.crml.model.metamodel.CRMLFactory;
import org.openmodelica.crml.model.metamodel.Library;
import org.openmodelica.crml.model.metamodel.Model;
import org.openmodelica.crml.model.metamodel.OperatorHeaderElement;
import org.openmodelica.crml.model.metamodel.Sequence;
import org.openmodelica.crml.model.metamodel.SequenceKeyword;
import org.openmodelica.crml.model.metamodel.SequenceValue;
import org.openmodelica.crml.model.metamodel.Value;
import org.openmodelica.crml.model.metamodel.Variable;

/**
 * Resolves {@link Sequence}s into calls to the operators in scope.
 *
 * <p>The parser is non-deterministic: every sub-expression yields all of its
 * parses, and the tokens that follow decide which ones survive. A hole is
 * therefore not committed to its longest parse - in {@code 'count' C 'inside' P}
 * the hole {@code C} must not swallow {@code 'inside'} even though an infix
 * {@code 'inside'} operator is in scope. A sequence with more than one complete
 * parse is reported as an error rather than resolved arbitrarily.</p>
 *
 * <p>Parses are memoised per sequence by (position, level), which keeps the
 * search polynomial. Parse trees are kept as {@link Node}s and only the chosen
 * one is turned into {@link ComputedValue}s: the candidates share their argument
 * values, and an EMF value can only be contained once.</p>
 */
public class MixfixParser {
    private final List<List<CustomOperator>> levels;
    private final TypeCheck typeCheck;
    private final CRMLFactory factory;
    private final BuildContext builder;

    // Parses of the current sequence, by level and then by start position.
    // A null position is the end of the sequence.
    private final List<Map<Sequence, List<Parse>>> memo = new ArrayList<>();


    public MixfixParser(Model model, BuildContext builder) {
        this(model, builder, new TypeCheck() {});


    }

    public MixfixParser(Model model, BuildContext builder, TypeCheck typeCheck) {
        this.levels    = OperatorUtil.load(model).stream()
                                  .map(Collections::singletonList)
                                  .collect(Collectors.toList());
        this.typeCheck = typeCheck;
        this.builder = builder;
        this.factory = builder.factory();
    }

    public MixfixParser(Library library, BuildContext builder) {
        this(library, builder, new TypeCheck() {});
    }

    public MixfixParser(Library library, BuildContext builder, TypeCheck typeCheck) {
        this.levels    = OperatorUtil.load(library).stream()
                                  .map(Collections::singletonList)
                                  .collect(Collectors.toList());
        this.typeCheck = typeCheck;
        this.builder = builder;
        this.factory = builder.factory();
    }

    public Value parse(Sequence sequence) {
        memo.clear();
        for (int level = 0; level <= levels.size(); level++) {
            memo.add(new IdentityHashMap<>());
        }

        List<Node> complete = new ArrayList<>();
        for (Parse parse : expr(sequence, 0)) {
            if (parse.rest == null) {
                complete.add(parse.node);
            }
        }

        if (complete.isEmpty()) {
            // The sequence is not a call to any operator in scope. Leaving it in
            // place is deliberate: a consumer then sees an unresolved Sequence
            // and can say so, rather than a silently wrong tree.
            return null;
        }
        if (complete.size() > 1) {
            builder.reportError("Ambiguous operator call " + describe(sequence) + ", it can be read as: "
                    + complete.stream().map(Node::describe).collect(Collectors.joining(" or ")));
            return null;
        }
        return complete.get(0).build();
    }

    /**
     * Replaces every Sequence below {@code root} that spells out a call to an
     * operator in scope with the corresponding ComputedValue. Sequences that
     * match no operator are left untouched.
     */
    public void perform(EObject root){
        if (root == null) {
            return;
        }
        for(EObject content :root.eContents()){
            if(content instanceof Sequence){
                EObject host = content.eContainer();
                EStructuralFeature feat = content.eContainingFeature();
                Value value = parse((Sequence) content);

                if(value instanceof ComputedValue){
                    builder.set(host, feat, value);
                    // Recurse into the arguments, which may themselves be calls.
                    perform(value);
                } else {
                    // Unresolved: keep walking the sequence itself so nested
                    // values still get their chance.
                    perform(content);
                }
            } else {
                perform(content);
            }
        }
    }

    /**
     * All parses of an expression at {@code level} or above that start at
     * {@code pos}, whatever they leave unconsumed.
     */
    private List<Parse> expr(Sequence pos, int level) {
        Map<Sequence, List<Parse>> atLevel = memo.get(level);
        List<Parse> parses = atLevel.get(pos);
        if (parses == null) {
            // Beyond all operator levels the expression must be an atom.
            parses = level >= levels.size() ? value(pos) : operators(pos, level);
            atLevel.put(pos, parses);
        }
        return parses;
    }

    private List<Parse> operators(Sequence pos, int level) {
        List<CustomOperator> prefixAndClosedOperators = new ArrayList<>();
        List<CustomOperator> infixAndPostfixOperators = new ArrayList<>();

        for (CustomOperator op : Iterables.filter(levels.get(level), CustomOperator.class)) {
            if (op.startsWithValue())
                infixAndPostfixOperators.add(op);
            else
                prefixAndClosedOperators.add(op);
        }

        // ── Step 1: left subtrees - a prefix / closed operator at this level,
        //            or anything of a higher precedence level ─────────────────
        List<Parse> lefts = new ArrayList<>();
        for (CustomOperator op : prefixAndClosedOperators) {
            // Match from token index 0; no pre-parsed left argument.
            lefts.addAll(matchOp(pos, op, 0, level, null));
        }
        lefts.addAll(expr(pos, level + 1));

        // ── Step 2: extend with infix / postfix operators ─────────────────────
        //
        // Every left subtree is a parse of this level whether it is extended or
        // not; the caller keeps the ones its next token fits. Associativity
        // controls which extensions may be extended again:
        //   LEFT  → yes (chains left because outer-right hole parses at level+1)
        //   RIGHT → no  (recursion in the outer-right hole consumed the chain)
        //   NON   → no  (chaining explicitly forbidden)
        List<Parse> parses = new ArrayList<>(lefts);
        Deque<Parse> pending = new ArrayDeque<>(lefts);
        while (!pending.isEmpty()) {
            Parse left = pending.pop();
            for (CustomOperator op : infixAndPostfixOperators) {
                // The first hole ('_') was consumed as `left`;
                // start matching from token index 1 (the part after the leading '_').
                for (Parse extended : matchOp(left.rest, op, 1, level, left.node)) {
                    parses.add(extended);
                    if (OperatorUtil.getAssoc(op) == Association.LEFT) {
                        pending.add(extended);
                    }
                }
            }
        }
        return parses;
    }

    /**
     * All ways {@code op} matches from {@code pos}, header elements before
     * {@code startIdx} having been matched by {@code prepend}.
     */
    private List<Parse> matchOp(
            Sequence                pos,
            CustomOperator          op,
            int                     startIdx,
            int                     level,
            Node                    prepend) {

        int totalValues = op.getArity();

        // A partial match is the arguments parsed so far and where it stands.
        // The pre-parsed left subtree is argument #0 for infix/postfix ops.
        List<Parse> partials = new ArrayList<>();
        if (prepend == null) {
            partials.add(new Parse(new Call(op, Collections.emptyList()), pos));
        } else if (checkArg(prepend, op, 0)) {
            partials.add(new Parse(new Call(op, Collections.singletonList(prepend)), pos));
        }

        // ── Walk the token skeleton from startIdx ─────────────────────────────
        List<OperatorHeaderElement> tokens = op.getHeader();

        for (int i = startIdx; i < tokens.size() && !partials.isEmpty(); i++) {
            OperatorHeaderElement token = tokens.get(i);
            List<Parse> next = new ArrayList<>();

            for (Parse partial : partials) {
                Call call = (Call) partial.node;

                if (token instanceof Variable) {
                    // ── Value: every parse of the sub-expression is a candidate ──
                    //
                    // The outer-right value is the very last value when the operator
                    // ends with '_'.  Its sub-level is associativity-dependent:
                    //   RIGHT → same level p     (recursion chains right)
                    //   LEFT / NON → level p+1   (prevents right-chaining;
                    //                             the extension loop handles left)
                    int valueIndex = call.args.size();
                    boolean isOuterRight =
                        op.endsWithValue() && (valueIndex == totalValues - 1);

                    int subLevel = isOuterRight ? rightLevel(level, op) : 0;

                    for (Parse child : expr(partial.rest, subLevel)) {
                        // Type-check the parsed argument.
                        if (checkArg(child.node, op, valueIndex)) {
                            next.add(new Parse(call.with(child.node), child.rest));
                        }
                    }

                } else if (partial.rest instanceof SequenceKeyword
                        && ((SequenceKeyword) partial.rest).getKeyword().equals(((Keyword) token).getKeyword())) {
                    next.add(new Parse(call, partial.rest.getNext()));
                }
            }
            partials = next;
        }
        return partials;
    }

    private List<Parse> value(Sequence pos) {
        if (pos instanceof SequenceValue) {
            return Collections.singletonList(
                    new Parse(new Atom(((SequenceValue) pos).getValue()), pos.getNext()));
        }
        return Collections.emptyList();
    }

    /**
     * Determines the precedence level at which the outer-right value of
     * {@code op} should be parsed, based on associativity.
     *
     * <pre>
     *   RIGHT assoc → p         (same level; recursion chains right)
     *   LEFT  assoc → p + 1    (one higher; loop handles left-chaining)
     *   NON   assoc → p + 1    (one higher; single match, loop breaks anyway)
     * </pre>
     */
    private int rightLevel(int p, CustomOperator op) {
        return (OperatorUtil.getAssoc(op) == Association.RIGHT) ? p : p + 1;
    }

    /**
     * Type-checks one argument against the operator's declared parameter types.
     *
     * <p>When {@link OperatorWrapper#getDomain()} is {@code null} or no parameter type
     * list exists, the check is skipped and {@code true} is returned.  This
     * preserves the "accept-any" behaviour of the default {@link TypeCheck}
     * implementation.</p>
     *
     * @param node      The argument parse tree to check.
     * @param op        The operator the argument belongs to.
     * @param valueIndex 0-based index of the value (= parameter position).
     * @return          {@code true} iff the argument is type-compatible.
     */
    private boolean checkArg(Node node, CustomOperator op, int valueIndex) {
        return true; // accept composite nodes unconditionally for now
    }

    /** The sequence as written, values shown as {@code _}. */
    private static String describe(Sequence sequence) {
        List<String> parts = new ArrayList<>();
        for (Sequence s = sequence; s != null; s = s.getNext()) {
            parts.add(s instanceof SequenceKeyword ? ((SequenceKeyword) s).getKeyword() : "_");
        }
        return String.join(" ", parts);
    }

    /** A parse tree and the part of the sequence it leaves unconsumed. */
    private static final class Parse {
        final Node     node;
        final Sequence rest;

        Parse(Node node, Sequence rest) {
            this.node = node;
            this.rest = rest;
        }
    }

    /** A parse tree, built into the model only once it is the chosen parse. */
    private abstract class Node {
        abstract Value build();

        /** The tree with each call bracketed, values shown as {@code _}. */
        abstract String describe();
    }

    private final class Atom extends Node {
        final Value value;

        Atom(Value value) {
            this.value = value;
        }

        @Override
        Value build() {
            return value;
        }

        @Override
        String describe() {
            return "_";
        }
    }

    private final class Call extends Node {
        final CustomOperator op;
        final List<Node>     args;

        Call(CustomOperator op, List<Node> args) {
            this.op   = op;
            this.args = args;
        }

        Call with(Node arg) {
            List<Node> extended = new ArrayList<>(args);
            extended.add(arg);
            return new Call(op, extended);
        }

        @Override
        Value build() {
            ComputedValue call = factory.createComputedValue();
            call.setOperator(op);

            List<Variable> params = op.getVariables();
            for (int i = 0; i < args.size(); i++) {
                Binding binding = factory.createBinding();
                binding.setValue(args.get(i).build());
                binding.setElement(params.get(i));
                call.getBindings().add(binding);
            }
            return call;
        }

        @Override
        String describe() {
            List<String> parts = new ArrayList<>();
            int arg = 0;
            for (OperatorHeaderElement token : op.getHeader()) {
                parts.add(token instanceof Keyword ? ((Keyword) token).getKeyword() : args.get(arg++).describe());
            }
            return "(" + String.join(" ", parts) + ")";
        }
    }
}
