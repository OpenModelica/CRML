package org.openmodelica.crml.language.workspace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.openmodelica.crml.model.metamodel.Sequence;

import crml.language.grammar.crmlLexer;

/**
 * The trace between the DOM of one file and its source: for each DOM object,
 * the parse tree node it was built from; the file's tokens, comments
 * included; and the places where the parser recovered from a syntax error
 * ({@link Hole holes}).
 *
 * <p>Attached to its {@link CrmlResource} as an adapter, so code that only
 * has an {@link EObject} finds it through {@link #of(EObject)}. It is built
 * with the DOM and kept after resolution; objects that resolution creates
 * (operator calls) are traced to the source they replace.
 *
 * <p>Positions are those of ANTLR: token indexes into the token stream, and
 * character indexes counting code points from the start of the file.
 */
public final class SourceMap extends AdapterImpl {

    /**
     * A place where the parser met a syntax error and recovered, e.g. a
     * missing value in {@code Boolean b is ;}. The DOM is built around it:
     * what is missing is simply not set.
     */
    public static final class Hole {
        private final SourceMap map;
        private final ParserRuleContext context;
        private final Token at;

        Hole(SourceMap map, ParserRuleContext context, Token at) {
            this.map = map;
            this.context = context;
            this.at = at;
        }

        /** The rule the parser was in when it met the error. */
        public ParserRuleContext context() { return context; }

        /** The token the parser could not use there, e.g. the {@code ;} of {@code Boolean b is ;}. */
        public Token at() { return at; }

        /** The innermost DOM object around the hole, or null if it is outside the DOM. */
        public EObject host() {
            return at == null ? null : map.objectAt(Math.max(0, at.getStartIndex() - 1));
        }

        @Override
        public String toString() {
            return "hole at " + (at == null ? "?" : at.getLine() + ":" + at.getCharPositionInLine())
                + " in " + context.getClass().getSimpleName();
        }
    }

    private final BufferedTokenStream tokens;
    private final Map<EObject, ParseTree> nodes = new IdentityHashMap<>();
    private final List<Hole> holes = new ArrayList<>();

    /** @param tokens the file's tokens, for comments; may be null */
    public SourceMap(BufferedTokenStream tokens) {
        this.tokens = tokens;
    }

    /** The map of the resource {@code object} is in, or null. */
    public static SourceMap of(EObject object) {
        return object == null ? null : of(object.eResource());
    }

    /** The map attached to {@code resource}, or null. */
    public static SourceMap of(Resource resource) {
        return resource == null ? null : (SourceMap) EcoreUtil.getExistingAdapter(resource, SourceMap.class);
    }

    @Override
    public boolean isAdapterForType(Object type) {
        return type == SourceMap.class;
    }

    @Override
    public void setTarget(Notifier newTarget) {
        if (newTarget != null && !(newTarget instanceof Resource)) {
            throw new IllegalArgumentException("A SourceMap is attached to a Resource, not to " + newTarget);
        }
        super.setTarget(newTarget);
    }

    // ---------------------------------------------------------------------
    // Recording
    // ---------------------------------------------------------------------

    /**
     * Records that {@code object} was built from {@code node}. The first
     * record wins: the innermost node is built first, the nodes that only
     * pass its result on come later.
     *
     * <p>Except for a {@link Sequence}: there the last record wins. A node
     * with a user keyword does not just pass on the Sequence its operand
     * built, it extends it, so {@code 'count' C 'inside' P}, parsed as
     * {@code ('count' C) 'inside' P}, is one Sequence of both nodes. The
     * outermost node is the one that covers all of it, and so the operator
     * call that replaces the Sequence (see {@link #replace}).
     */
    @java.lang.SuppressWarnings("squid:S6213") //record refers to a modelica constuct. No naming conflict on Java8.
    public void record(EObject object, ParseTree node) {
        if (object instanceof Sequence && node != null) {
            nodes.put(object, node);
        } else if (object != null && node != null) {
            nodes.putIfAbsent(object, node);
        }
    }

    /**
     * Records that {@code replacement} stands for {@code original} in the
     * source, e.g. an operator call for the Sequence it was parsed from.
     */
    public void replace(EObject original, EObject replacement) {
        ParseTree node = nodes.get(original);
        if (node != null && replacement != null) {
            nodes.put(replacement, node);
        }
    }

    /** Records a hole for every rule of {@code tree} in which the parser recovered from an error. */
    public void collectHoles(ParseTree tree) {
        if (tree instanceof ParserRuleContext) {
            ParserRuleContext context = (ParserRuleContext) tree;
            if (context.exception != null) {
                holes.add(new Hole(this, context, context.exception.getOffendingToken()));
            }
            for (int i = 0; i < context.getChildCount(); i++) {
                ParseTree child = context.getChild(i);
                if (child instanceof ErrorNode) {
                    holes.add(new Hole(this, context, ((ErrorNode) child).getSymbol()));
                } else {
                    collectHoles(child);
                }
            }
        }
    }

    // ---------------------------------------------------------------------
    // DOM to source
    // ---------------------------------------------------------------------

    /** The node {@code object} was built from, or null if it was not traced. */
    public ParseTree node(EObject object) {
        return nodes.get(object);
    }

    /**
     * The first token of {@code object}: that of its node; for an object
     * without one (e.g. a nested operator call), the first token of its
     * contents; null if none of them is traced.
     */
    public Token start(EObject object) {
        ParseTree node = nodes.get(object);
        if (node != null) {
            return first(node);
        }
        Token start = null;
        for (EObject content : object.eContents()) {
            Token token = start(content);
            if (token != null && (start == null || token.getTokenIndex() < start.getTokenIndex())) {
                start = token;
            }
        }
        return start;
    }

    /** The last token of {@code object}, found as for {@link #start}. */
    public Token stop(EObject object) {
        ParseTree node = nodes.get(object);
        if (node != null) {
            return last(node);
        }
        Token stop = null;
        for (EObject content : object.eContents()) {
            Token token = stop(content);
            if (token != null && (stop == null || token.getTokenIndex() > stop.getTokenIndex())) {
                stop = token;
            }
        }
        return stop;
    }

    /**
     * The first token of {@code object} or, if it has none, of its closest
     * container that has one: where to report a problem with it.
     */
    public Token position(EObject object) {
        for (EObject current = object; current != null; current = current.eContainer()) {
            Token start = start(current);
            if (start != null) {
                return start;
            }
        }
        return null;
    }

    // ---------------------------------------------------------------------
    // Source to DOM
    // ---------------------------------------------------------------------

    /**
     * The innermost traced object whose source contains the character at
     * {@code index}: the one with the shortest source, the deeper one if two
     * are equally long. Null if no object contains it, e.g. after the last
     * token of a file whose root is not closed.
     */
    @SuppressWarnings("java:S135") // guard clauses: skip objects with no known source, then those not containing index
    public EObject objectAt(int index) {
        EObject best = null;
        int bestLength = Integer.MAX_VALUE;
        int bestDepth = -1;
        for (EObject object : nodes.keySet()) {
            Token start = start(object);
            Token stop = stop(object);
            if (start == null || stop == null || start.getStartIndex() < 0 || stop.getStopIndex() < 0) {
                continue;
            }
            if (start.getStartIndex() > index || index > stop.getStopIndex()) {
                continue;
            }
            int length = stop.getStopIndex() - start.getStartIndex();
            int depth = depth(object);
            if (length < bestLength || (length == bestLength && depth > bestDepth)) {
                best = object;
                bestLength = length;
                bestDepth = depth;
            }
        }
        return best;
    }

    private static int depth(EObject object) {
        int depth = 0;
        for (EObject current = object.eContainer(); current != null; current = current.eContainer()) {
            depth++;
        }
        return depth;
    }

    /** The places where the parser recovered from a syntax error, in source order. */
    public List<Hole> holes() {
        return Collections.unmodifiableList(holes);
    }

    /**
     * True if the parser recovered from a syntax error within the source of
     * {@code object}, from its first to its last token, or on the token just
     * before it. Such an object may be made of fragments of the text around
     * the error: after a missing {@code ;}, {@code Real d is time from e;}
     * also gives a variable {@code e} of type {@code from}, and after an
     * unexpected {@code .}, {@code during env.on ensure b;} gives a variable
     * {@code ensure} of type {@code on}. Positions are compared by line and
     * column, since the token the parser conjures for a missing one has no
     * index. A declaration that follows an error directly is counted too,
     * even if it is well written.
     */
    public boolean recovered(EObject object) {
        Token start = start(object);
        Token stop = stop(object);
        if (start == null || stop == null) {
            return false;
        }
        int before = previousOnDefaultChannel(start);
        for (Hole hole : holes) {
            Token at = hole.at();
            if (at == null) {
                continue;
            }
            if (BY_POSITION.compare(start, at) <= 0 && BY_POSITION.compare(at, stop) <= 0) {
                return true;
            }
            if (before >= 0 && at.getTokenIndex() == before) {
                return true;
            }
        }
        return false;
    }

    /** The index of the token before {@code token}, comments and white space skipped; -1 if unknown. */
    private int previousOnDefaultChannel(Token token) {
        if (tokens == null || token.getTokenIndex() < 0) {
            return -1;
        }
        for (int i = token.getTokenIndex() - 1; i >= 0; i--) {
            if (tokens.get(i).getChannel() == Token.DEFAULT_CHANNEL) {
                return i;
            }
        }
        return -1;
    }

    /** Orders tokens by position in the source: line, then column. */
    private static final Comparator<Token> BY_POSITION =
        Comparator.comparingInt(Token::getLine).thenComparingInt(Token::getCharPositionInLine);

    // ---------------------------------------------------------------------
    // Comments
    // ---------------------------------------------------------------------

    /**
     * The comment documenting {@code object}: the comments directly above
     * its first token, with no blank line between them and it. A comment
     * that ends a line of code belongs to that code, not to what follows.
     *
     * @return the comment tokens in source order; empty if there are none or
     *         the tokens were not kept
     */
    public List<Token> comments(EObject object) {
        Token start = start(object);
        if (tokens == null || start == null || start.getTokenIndex() < 0) {
            return Collections.emptyList();
        }
        List<Token> hidden = tokens.getHiddenTokensToLeft(start.getTokenIndex());
        List<Token> comments = new ArrayList<>();
        if (hidden == null) {
            return comments;
        }
        // Walk up from the object: whitespace may separate it from the
        // comments, but not a blank line.
        for (int i = hidden.size() - 1; i >= 0 && !endsDocumentation(hidden.get(i)); i--) {
            Token token = hidden.get(i);
            if (token.getType() != crmlLexer.WS) {
                comments.add(0, token);
            }
        }
        return comments;
    }

    /**
     * True if the walk up from an object for its comments stops at the hidden
     * {@code token}: a blank line, anything other than a comment, or a comment
     * that ends a line of code.
     */
    private boolean endsDocumentation(Token token) {
        if (token.getType() == crmlLexer.WS) {
            return blankLineBetween(token);
        }
        boolean comment = token.getType() == crmlLexer.LINE_COMMENT || token.getType() == crmlLexer.ML_COMMENT;
        return !comment || endsCode(token);
    }

    /**
     * The text of the {@link #comments comment} documenting {@code object},
     * without the comment markers; null if there is none.
     */
    public String documentation(EObject object) {
        List<Token> comments = comments(object);
        if (comments.isEmpty()) {
            return null;
        }
        StringBuilder text = new StringBuilder();
        for (Token comment : comments) {
            String body = comment.getText();
            if (comment.getType() == crmlLexer.LINE_COMMENT) {
                body = body.substring(2);
            } else {
                body = body.substring(2, body.length() - 2);
            }
            for (String line : body.split("\\R")) {
                String trimmed = line.trim();
                if (trimmed.startsWith("*")) {
                    trimmed = trimmed.substring(1).trim();
                }
                if (!trimmed.isEmpty()) {
                    text.append(text.length() == 0 ? "" : "\n").append(trimmed);
                }
            }
        }
        return text.length() == 0 ? null : text.toString();
    }

    /**
     * True if the whitespace {@code ws} contains a blank line: a line
     * comment ends its own line, so one more line break makes a blank
     * line; after anything else it takes two.
     */
    private boolean blankLineBetween(Token ws) {
        int breaks = lineBreaks(ws.getText());
        Token before = ws.getTokenIndex() > 0 ? tokens.get(ws.getTokenIndex() - 1) : null;
        int needed = before != null && before.getType() == crmlLexer.LINE_COMMENT ? 1 : 2;
        return breaks >= needed;
    }

    /** True if code precedes {@code comment} on its line. */
    private boolean endsCode(Token comment) {
        for (int i = comment.getTokenIndex() - 1; i >= 0; i--) {
            Token token = tokens.get(i);
            if (token.getType() == crmlLexer.WS) {
                if (lineBreaks(token.getText()) > 0) {
                    return false;
                }
                continue;
            }
            if (token.getType() == crmlLexer.LINE_COMMENT) {
                return false; // it ends its line
            }
            return token.getChannel() == Token.DEFAULT_CHANNEL && token.getLine() == comment.getLine();
        }
        return false;
    }

    private static int lineBreaks(String text) {
        int breaks = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\n' || (c == '\r' && (i + 1 == text.length() || text.charAt(i + 1) != '\n'))) {
                breaks++;
            }
        }
        return breaks;
    }

    private static Token first(ParseTree node) {
        if (node instanceof ParserRuleContext) {
            return ((ParserRuleContext) node).getStart();
        }
        return node instanceof TerminalNode ? ((TerminalNode) node).getSymbol() : null;
    }

    private static Token last(ParseTree node) {
        if (node instanceof ParserRuleContext) {
            ParserRuleContext context = (ParserRuleContext) node;
            Token stop = context.getStop();
            // A rule that matched nothing ends before it starts.
            if (stop == null || stop.getTokenIndex() < context.getStart().getTokenIndex()) {
                return context.getStart();
            }
            return stop;
        }
        return node instanceof TerminalNode ? ((TerminalNode) node).getSymbol() : null;
    }
}
