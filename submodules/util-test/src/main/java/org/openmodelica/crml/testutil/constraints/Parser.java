package org.openmodelica.crml.testutil.constraints;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openmodelica.crml.testutil.constraints.Lexer.Kind;
import org.openmodelica.crml.testutil.constraints.Lexer.Token;

/**
 * Recursive-descent parser for one line, one method per grammar rule (see
 * CONSTRAINT_LANGUAGE.md, "Syntax"). Words are keywords only where the grammar
 * expects one; everywhere else they are names.
 */
final class Parser {

    private static final List<String> COMPARE = Arrays.asList("==", "!=", "<", "<=", ">", ">=");

    private final List<Token> tokens;
    private int index;

    private Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    /** The constraint on {@code line}, or null for a blank or comment-only line. */
    static Ast.Constraint parse(String line) throws ParseException {
        List<Token> tokens = Lexer.tokenize(line);
        if (tokens.size() == 1) {
            return null;
        }
        Parser parser = new Parser(tokens);
        Ast.Constraint constraint = parser.constraint();
        parser.expectEnd();
        return constraint;
    }

    // constraint ::= quantifier? chain condition | "count" chain compare INT
    private Ast.Constraint constraint() throws ParseException {
        // A chain always starts with "[", so a leading word is the quantifier or "count".
        if (peek().kind == Kind.WORD && peek(1).is(Kind.OP, "[")) {
            Token word = next();
            switch (word.text) {
                case "all":
                    return new Ast.Constraint(Ast.Quantifier.ALL, chain(), condition());
                case "some":
                    return new Ast.Constraint(Ast.Quantifier.SOME, chain(), condition());
                case "count":
                    return count();
                default:
                    throw new ParseException(word.column,
                        "expected 'all', 'some', 'count' or '[', found " + word);
            }
        }
        return new Ast.Constraint(Ast.Quantifier.PLAIN, chain(), condition());
    }

    private Ast.Constraint count() throws ParseException {
        Ast.Chain chain = chain();
        Token op = next();
        if (op.kind != Kind.OP || !COMPARE.contains(op.text)) {
            throw new ParseException(op.column, "expected a comparison (==, !=, <, <=, >, >=), found " + op);
        }
        Token n = next();
        if (n.kind != Kind.INT) {
            throw new ParseException(n.column, "expected an integer, found " + n);
        }
        return new Ast.Constraint(Ast.Quantifier.PLAIN, chain, new Ast.Count(op.text, Long.parseLong(n.text)));
    }

    // chain ::= selector ( "/" selector )* step*
    private Ast.Chain chain() throws ParseException {
        List<Ast.Selector> selectors = new ArrayList<>();
        selectors.add(selector());
        while (acceptOp("/")) {
            selectors.add(selector());
        }
        List<Ast.Step> steps = new ArrayList<>();
        while (acceptOp(".")) {
            steps.add(step());
        }
        return new Ast.Chain(selectors, steps);
    }

    // step ::= "." NAME | "." "@" PSEUDO   (the "." is already consumed)
    private Ast.Step step() throws ParseException {
        if (acceptOp("@")) {
            Token pseudo = next();
            if (pseudo.kind == Kind.WORD) {
                switch (pseudo.text) {
                    case "root":
                        return new Ast.PseudoStep(Ast.PseudoStep.Kind.ROOT);
                    case "container":
                        return new Ast.PseudoStep(Ast.PseudoStep.Kind.CONTAINER);
                    case "file":
                        return new Ast.PseudoStep(Ast.PseudoStep.Kind.FILE);
                    default:
                        break;
                }
            }
            throw new ParseException(pseudo.column, "expected root, container or file after '@', found " + pseudo);
        }
        return new Ast.FeatureStep(name());
    }

    // selector ::= "[" TYPE ( "::" filter ( "," filter )* )? "]"
    private Ast.Selector selector() throws ParseException {
        expectOp("[");
        String type = name();
        List<Ast.Filter> filters = new ArrayList<>();
        if (acceptOp("::")) {
            filters.add(filter());
            while (acceptOp(",")) {
                filters.add(filter());
            }
        }
        expectOp("]");
        return new Ast.Selector(type, filters);
    }

    // filter ::= path ( "==" | "!=" | "contains" ) literal ;  path ::= NAME ( "." NAME )*
    private Ast.Filter filter() throws ParseException {
        List<String> path = new ArrayList<>();
        path.add(name());
        while (acceptOp(".")) {
            path.add(name());
        }
        Token op = next();
        String operator;
        if (op.is(Kind.OP, "==") || op.is(Kind.OP, "!=")) {
            operator = op.text;
        } else if (op.is(Kind.WORD, "contains")) {
            operator = "contains";
        } else {
            throw new ParseException(op.column, "expected ==, != or contains, found " + op);
        }
        return new Ast.Filter(path, operator, literal());
    }

    // condition ::= ( "==" | "!=" | "contains" | "not" "contains" ) operand
    //             | "is" "empty" | "is" "not" "empty" | "is" "a" TYPE
    private Ast.Condition condition() throws ParseException {
        Token token = next();
        if (token.is(Kind.OP, "==") || token.is(Kind.OP, "!=")) {
            return new Ast.Compare(token.text, operand());
        }
        if (token.is(Kind.WORD, "contains")) {
            return new Ast.Compare("contains", operand());
        }
        if (token.is(Kind.WORD, "not")) {
            expectWord("contains");
            return new Ast.Compare("not contains", operand());
        }
        if (token.is(Kind.WORD, "is")) {
            Token what = next();
            if (what.is(Kind.WORD, "empty")) {
                return new Ast.IsEmpty(false);
            }
            if (what.is(Kind.WORD, "not")) {
                expectWord("empty");
                return new Ast.IsEmpty(true);
            }
            if (what.is(Kind.WORD, "a")) {
                return new Ast.IsA(name());
            }
            throw new ParseException(what.column, "expected empty, not empty or a after 'is', found " + what);
        }
        throw new ParseException(token.column,
            "expected a condition (==, !=, contains, not contains, is ...), found " + token);
    }

    // operand ::= literal | chain
    private Ast.Operand operand() throws ParseException {
        if (peek().is(Kind.OP, "[")) {
            return new Ast.ChainOperand(chain());
        }
        return literal();
    }

    // literal ::= STRING | INT | "true" | "false"
    private Ast.Literal literal() throws ParseException {
        Token token = next();
        switch (token.kind) {
            case STRING:
                return new Ast.Literal(token.text);
            case INT:
                try {
                    return new Ast.Literal(Long.valueOf(token.text));
                } catch (NumberFormatException e) {
                    throw new ParseException(token.column, "integer out of range: " + token.text);
                }
            case WORD:
                if (token.text.equals("true") || token.text.equals("false")) {
                    return new Ast.Literal(Boolean.valueOf(token.text));
                }
                break;
            default:
                break;
        }
        throw new ParseException(token.column, "expected a string, an integer, true or false, found " + token);
    }

    private String name() throws ParseException {
        Token token = next();
        if (token.kind != Kind.WORD) {
            throw new ParseException(token.column, "expected a name, found " + token);
        }
        return token.text;
    }

    private void expectEnd() throws ParseException {
        Token token = peek();
        if (token.kind != Kind.END) {
            throw new ParseException(token.column, "expected end of line, found " + token);
        }
    }

    private void expectOp(String op) throws ParseException {
        Token token = next();
        if (!token.is(Kind.OP, op)) {
            throw new ParseException(token.column, "expected '" + op + "', found " + token);
        }
    }

    private void expectWord(String word) throws ParseException {
        Token token = next();
        if (!token.is(Kind.WORD, word)) {
            throw new ParseException(token.column, "expected '" + word + "', found " + token);
        }
    }

    private boolean acceptOp(String op) {
        if (peek().is(Kind.OP, op)) {
            index++;
            return true;
        }
        return false;
    }

    private Token peek() {
        return peek(0);
    }

    private Token peek(int ahead) {
        return tokens.get(Math.min(index + ahead, tokens.size() - 1));
    }

    private Token next() {
        Token token = peek();
        if (token.kind != Kind.END) {
            index++;
        }
        return token;
    }
}
