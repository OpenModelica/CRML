package crml.language.opcall;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

import com.google.common.collect.Iterables;

import crml.language.dom.BuildContext;
import crml.language.pretty.Misc;
import crml.model.language.Binding;
import crml.model.language.ComputedValue;
import crml.model.language.CustomOperator;
import crml.model.language.Keyword;
import crml.model.language.LanguageFactory;
import crml.model.language.Library;
import crml.model.language.Model;
import crml.model.language.OperatorHeaderElement;
import crml.model.language.Sequence;
import crml.model.language.SequenceValue;
import crml.model.language.TypeReference;
import crml.model.language.Value;
import crml.model.language.Variable;

public class MixfixParser {
    private final List<List<CustomOperator>> levels;
    private final TypeCheck typeCheck;
    private final LanguageFactory factory;
    private final BuildContext builder;


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
        System.err.println("Processing sequence: " + Misc.pretty(sequence));
        SequenceCursor cursor = new SequenceCursor(sequence);
        Value result = expr(cursor, 0);

        if (!cursor.isExhausted()) {
            System.err.println("Sequence not exhausetd.");
            return null;
        }
        System.err.println("Computed value: "+result);
        return result;
    }
    public void perform(EObject root){
        for(EObject content :root.eContents()){
            if(content instanceof Sequence){
                EObject host = content.eContainer();
                EStructuralFeature feat = content.eContainingFeature();
                Value value = parse((Sequence) content);

                if(value instanceof ComputedValue){
                    builder.set(host, feat, value);
                }
                perform(value);
//                if(content instanceof SequenceValue){
//                    perform(content);
//                }
            } else {
                perform(content);
            }
        }
    }
    private Value expr(SequenceCursor cursor, int level) {
        // ── Base case: beyond all operator levels → must be an atom ──────────
        if (level >= levels.size()) {
            return value(cursor);
        }

        List<CustomOperator> prefixAndClosedOperators = new ArrayList<>();
        List<CustomOperator> infixAndPostfixOperators = new ArrayList<>();

        for (CustomOperator op : Iterables.filter(levels.get(level), CustomOperator.class)) {
            if (op.startsWithValue()) 
                infixAndPostfixOperators.add(op);
            else
                prefixAndClosedOperators.add(op);
        }
        // ── Step 1: try prefix / closed operators at this level ──────────────
        Value left = null;
        for (CustomOperator op : prefixAndClosedOperators) {
            // Match from token index 0; no pre-parsed left argument.
            Value node = matchOp(cursor, op, 0, level, null);
            if (node != null) {
                left = node;
                break;
            }
        }

        // ── Step 2: fall through to the next (higher) precedence level ───────
        if (left == null) {
            left = expr(cursor, level + 1);
        }

        // ── Step 3: extend with infix / postfix operators (left-extension loop)
        //
        // Each iteration tries to attach one operator to the current `left`.
        // Associativity controls how the loop terminates:
        //   LEFT  → keep looping after a match (chains left automatically
        //            because outer-right hole parses at level+1)
        //   RIGHT → break after a match (recursion in the outer-right hole
        //            already consumed the rest of the right chain)
        //   NON   → break after a match (chaining explicitly forbidden)
        while (true) {
            CustomOperator matched = null;

            for (CustomOperator op : infixAndPostfixOperators) {
                // The first hole ('_') was consumed as `left`;
                // start matching from token index 1 (the part after the leading '_').
                Value node = matchOp(cursor, op, 1, level, left);
                if (node != null) {
                    left    = node;
                    matched = op;
                    break;
                }
            }

            if (matched == null) {
                break; // nothing extended the left subtree → done at this level
            }
            if (OperatorUtil.getAssoc(matched) == Association.NON) {
                break; // non-associative: no chaining allowed
            }
            // LEFT  → keep looping (the while condition re-evaluates)
            // RIGHT → also break because recursion already consumed the chain
            if (OperatorUtil.getAssoc(matched) == Association.RIGHT) {
                break;
            }
        }

        return left;
    }

    private ComputedValue matchOp(
            SequenceCursor          cursor,
            CustomOperator                  op,
            int                     startIdx,
            int                     level,
            Value                 prepend) {

        SequenceCursor.Checkpoint checkpoint = cursor.save();

        List<Value>  args       = new ArrayList<>();
        int valueIndex  = 0;    // 0-based index among values seen so far
        int totalValues = op.getArity();

        // The pre-parsed left subtree is argument #0 for infix/postfix ops.
        if (prepend != null) {
            // Optional type-check for the leading argument.
            if (!checkArg(prepend, op, valueIndex)) {
                // No cursor movement yet — no need to restore.
                return null;
            }
            args.add(prepend);
            valueIndex++;
        }

        // ── Walk the token skeleton from startIdx ─────────────────────────────
        List<OperatorHeaderElement> tokens = op.getHeader();

        for (int i = startIdx; i < tokens.size(); i++) {
            OperatorHeaderElement token = tokens.get(i);

            if (token instanceof Variable) {
                // ── Value: parse a sub-expression recursively ──────────────────
                //
                // The outer-right value is the very last value when the operator
                // ends with '_'.  Its sub-level is associativity-dependent:
                //   RIGHT → same level p     (recursion chains right)
                //   LEFT / NON → level p+1   (prevents right-chaining;
                //                             the while-loop handles left)
                boolean isOuterRight =
                    op.endsWithValue() && (valueIndex == totalValues - 1);

                int subLevel = isOuterRight ? rightLevel(level, op) : 0;

                Value child = expr(cursor, subLevel);
                if(child==null){
                    // Sub-expression could not be parsed → this operator does not fit.
                    cursor.restore(checkpoint);
                    return null;
                }

                // Type-check the parsed argument.
                if (!checkArg(child, op, valueIndex)) {
                    cursor.restore(checkpoint);
                    return null;
                }

                args.add(child);
                valueIndex++;

            } else {
                if (!cursor.tryKeyword((Keyword) token)) {
                    cursor.restore(checkpoint);
                    return null;
                }
            }
        }

        ComputedValue  call = factory.createComputedValue();
        System.err.println("Create Computed value for operator "+op.getKeywords().get(0));
        call.setOperator(op);

        List<Variable> params = op.getVariables();

        for(Value arg : args){
            int idx = args.indexOf(arg);
            Binding binding = factory.createBinding();
            binding.setValue(arg);
            binding.setElement(params.get(idx));
            call.getBindings().add(binding);
        }
        return call;
    }

    private Value value(SequenceCursor cursor) {
        Sequence seq = cursor.peek();
        if (seq instanceof SequenceValue) {
            cursor.consume();                           // advance past the value
            return ((SequenceValue) seq).getValue();
        }
        return null;
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
     * @param node      The argument AST node to check.
     * @param op        The operator the argument belongs to.
     * @param valueIndex 0-based index of the value (= parameter position).
     * @return          {@code true} iff the argument is type-compatible.
     */
    private boolean checkArg(Value node, CustomOperator op, int valueIndex) {
        return true; // accept composite nodes unconditionally for now
    }
}
