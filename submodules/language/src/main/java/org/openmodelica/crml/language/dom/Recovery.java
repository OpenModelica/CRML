package org.openmodelica.crml.language.dom;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;

/**
 * What the parser leaves in the tree where it recovered from a syntax error,
 * so that a DOM can still be built from the rest of the file: rules that
 * failed (their {@code exception} is set, they may have no children), tokens
 * it skipped or made up ({@link ErrorNode}s, e.g. {@code <missing IDENT>}).
 */
public final class Recovery {

    /**
     * Thrown by a builder that cannot build a node because a part the
     * grammar requires is missing, e.g. a name. Building the node is given
     * up; the syntax error that caused it is already reported.
     */
    public static final class MissingPart extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public MissingPart(String part) {
            super("Missing " + part);
        }
    }

    private Recovery() {
    }

    /** True if the parser recovered from a syntax error somewhere in {@code tree}. */
    public static boolean hasSyntaxError(ParseTree tree) {
        if (tree instanceof ErrorNode) {
            return true;
        }
        if (tree instanceof ParserRuleContext) {
            ParserRuleContext context = (ParserRuleContext) tree;
            if (context.exception != null) {
                return true;
            }
            for (int i = 0; i < context.getChildCount(); i++) {
                if (hasSyntaxError(context.getChild(i))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * True if {@code tree} is absent or stands for nothing: a rule that failed
     * before matching anything, or a token the parser made up.
     */
    public static boolean isMissing(ParseTree tree) {
        if (tree == null || tree instanceof ErrorNode) {
            return true;
        }
        if (tree instanceof ParserRuleContext) {
            ParserRuleContext context = (ParserRuleContext) tree;
            if (context.getChildCount() == 0) {
                return context.exception != null;
            }
            for (int i = 0; i < context.getChildCount(); i++) {
                if (!(context.getChild(i) instanceof ErrorNode)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * The text of a part the grammar requires, e.g. a name.
     *
     * @throws MissingPart if the parser recovered it from a syntax error
     */
    public static String required(ParseTree tree, String part) {
        if (isMissing(tree)) {
            throw new MissingPart(part);
        }
        return tree.getText();
    }
}
