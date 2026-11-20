package org.openmodelica.crml.testutil.constraints;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.openmodelica.crml.testutil.constraints.Lexer.Kind;
import org.openmodelica.crml.testutil.constraints.Lexer.Token;

class LexerParserTest {

    private static List<String> kinds(String line) throws ParseException {
        List<String> out = new ArrayList<>();
        for (Token t : Lexer.tokenize(line)) {
            out.add(t.kind + ":" + t.text);
        }
        return out;
    }

    // ---------------------------------------------------------------- lexer

    @Test
    void everyTokenKind() throws ParseException {
        assertEquals(java.util.Arrays.asList(
                "WORD:all", "OP:[", "WORD:A", "OP:::", "WORD:b", "OP:==", "STRING:x", "OP:]",
                "OP:.", "OP:@", "OP:/", "OP:,", "OP:!=", "OP:<=", "OP:>=", "OP:<", "OP:>",
                "INT:12", "INT:-3", "END:"),
            kinds("all [A::b == \"x\"] . @ / , != <= >= < > 12 -3"));
    }

    @Test
    void stringEscapes() throws ParseException {
        assertEquals("a\"b\\c", Lexer.tokenize("\"a\\\"b\\\\c\"").get(0).text);
    }

    @Test
    void commentEndsTheLineButNotInsideAString() throws ParseException {
        assertEquals(java.util.Arrays.asList("STRING:a#b", "END:"), kinds("\"a#b\" # comment"));
        assertEquals(java.util.Arrays.asList("END:"), kinds("   # only a comment"));
    }

    @Test
    void lexerErrorsHaveColumns() {
        ParseException unterminated = assertThrows(ParseException.class, () -> Lexer.tokenize("[A::b == \"x]"));
        assertEquals(10, unterminated.column());
        ParseException unexpected = assertThrows(ParseException.class, () -> Lexer.tokenize("[A] $"));
        assertEquals(5, unexpected.column());
    }

    @Test
    void endTokenColumnIsPastTheLine() throws ParseException {
        List<Token> tokens = Lexer.tokenize("[A]");
        assertEquals(Kind.END, tokens.get(3).kind);
        assertEquals(4, tokens.get(3).column);
    }

    // ---------------------------------------------------------------- parser

    @Test
    void blankAndCommentLinesHaveNoConstraint() throws ParseException {
        assertNull(Parser.parse(""));
        assertNull(Parser.parse("  # comment"));
    }

    @Test
    void fullChainRoundTrips() throws ParseException {
        Ast.Constraint c = Parser.parse(
            "[Model::name == \"M\", x.y contains 3] / [Variable::flag != true].a.@root.@container.@file == \"F\"");
        assertEquals(Ast.Quantifier.PLAIN, c.quantifier);
        assertEquals("[Model::name == \"M\", x.y contains 3] / [Variable::flag != true].a.@root.@container.@file",
            c.left.toString());
        assertEquals("== \"F\"", c.condition.toString());
        assertEquals(2, c.left.selectors.size());
        assertEquals(4, c.left.steps.size());
    }

    @Test
    void filterStepsAndPseudoSegmentsRoundTrip() throws ParseException {
        Ast.Constraint c = Parser.parse(
            "all [A::@container.name == \"L\"].@referrers[T::kind != \"X\"].m[B].@file contains \"F\"");
        assertEquals("[A::@container.name == \"L\"].@referrers[T::kind != \"X\"].m[B].@file", c.left.toString());
        assertEquals(5, c.left.steps.size());
        assertInstanceOf(Ast.FilterStep.class, c.left.steps.get(1));
        assertEquals(Ast.PseudoStep.Kind.REFERRERS, ((Ast.PseudoStep) c.left.steps.get(0)).kind);
        assertInstanceOf(Ast.PseudoStep.class, c.left.last().filters.get(0).path.get(0));
        // A selector right after the first one is a filter step, not a second selector.
        Ast.Constraint direct = Parser.parse("[A][B] is empty");
        assertEquals(1, direct.left.selectors.size());
        assertEquals(1, direct.left.steps.size());
    }

    @Test
    void quantifiersAndConditions() throws ParseException {
        assertEquals(Ast.Quantifier.ALL, Parser.parse("all [A].b is empty").quantifier);
        assertEquals(Ast.Quantifier.SOME, Parser.parse("some [A].b is not empty").quantifier);
        assertInstanceOf(Ast.IsEmpty.class, Parser.parse("[A] is empty").condition);
        assertEquals(true, ((Ast.IsEmpty) Parser.parse("[A] is not empty").condition).negated);
        assertEquals("B", ((Ast.IsA) Parser.parse("[A] is a B").condition).type);
        assertEquals("not contains", ((Ast.Compare) Parser.parse("[A].b not contains 1").condition).op);
        assertEquals("contains", ((Ast.Compare) Parser.parse("[A].b contains [B]").condition).op);
        assertInstanceOf(Ast.ChainOperand.class, ((Ast.Compare) Parser.parse("[A].b == [B::c == false].d").condition).right);
        Ast.Constraint count = Parser.parse("count [A] / [B].c >= 2");
        assertEquals(">= 2", count.condition.toString());
        assertEquals(Ast.Quantifier.PLAIN, count.quantifier);
    }

    @Test
    void keywordsAreNamesWhereANameIsExpected() throws ParseException {
        Ast.Constraint c = Parser.parse("[all::count == 1].is.a is a some");
        assertEquals("all", c.left.last().type);
        assertEquals("[all::count == 1].is.a", c.left.toString());
        assertEquals("some", ((Ast.IsA) c.condition).type);
    }

    @Test
    void syntaxErrorsHaveColumns() {
        assertEquals(17, column("[EClass::name ==]"));         // literal expected at ']'
        assertEquals(4, column("[A]"));                        // condition expected at end of line
        assertEquals(12, column("[A].b == 1 2"));              // end of line expected at '2'
        assertEquals(6, column("[A].@nope is empty"));         // unknown pseudo step
        assertEquals(6, column("[A::@nope == 1] is empty"));   // unknown pseudo segment in a filter
        assertEquals(7, column("[A].b[] is empty"));           // type expected in a filter step
        assertEquals(1, column("most [A] is empty"));          // unknown leading word
        assertEquals(11, column("count [A] = 1"));             // a single '=' is no token
        assertEquals(14, column("count [A] == x"));            // integer expected
    }

    private static int column(String line) {
        ConstraintFile.Line parsed = ConstraintFile.parse(line).lines().get(0);
        assertNull(parsed.constraint, line);
        return Integer.parseInt(parsed.syntaxError.replaceAll("^column (\\d+):.*$", "$1"));
    }

    @Test
    void fileKeepsLineNumbersAndSkipsBlankLines() {
        ConstraintFile file = ConstraintFile.parse("# header\n\n[A] is empty\r\n[A] oops\n");
        assertEquals(2, file.lines().size());
        assertEquals(3, file.lines().get(0).number);
        assertEquals(4, file.lines().get(1).number);
        assertEquals("[A] oops", file.lines().get(1).text);
    }
}
