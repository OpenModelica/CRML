package org.openmodelica.crml.testutil.constraints;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Splits one line of a constraint file into tokens, with one regular expression
 * per token kind (see CONSTRAINT_LANGUAGE.md, "Syntax"). A {@code #} outside a
 * string ends the line. The token list always ends with an {@link Kind#END}
 * token, whose column is just past the last character.
 */
final class Lexer {

    enum Kind { STRING, INT, OP, WORD, END }

    static final class Token {
        final Kind kind;
        /** The source text of the token; for a string, without quotes and unescaped. */
        final String text;
        /** 1-based column of the first character. */
        final int column;

        Token(Kind kind, String text, int column) {
            this.kind = kind;
            this.text = text;
            this.column = column;
        }

        boolean is(Kind kind, String text) {
            return this.kind == kind && this.text.equals(text);
        }

        @Override
        public String toString() {
            switch (kind) {
                case END:
                    return "end of line";
                case STRING:
                    return "string \"" + text + "\"";
                default:
                    return "'" + text + "'";
            }
        }
    }

    private static final Pattern WHITESPACE = Pattern.compile("\\s+");
    private static final Pattern COMMENT = Pattern.compile("#.*");
    private static final Pattern STRING = Pattern.compile("\"(?:[^\"\\\\]|\\\\.)*\"");
    private static final Pattern INT = Pattern.compile("-?\\d+");
    private static final Pattern OP = Pattern.compile("==|!=|<=|>=|<|>|::|[\\[\\],./@]");
    private static final Pattern WORD = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");

    private Lexer() {
    }

    static List<Token> tokenize(String line) throws ParseException {
        List<Token> tokens = new ArrayList<>();
        Matcher m = WHITESPACE.matcher(line);
        int pos = 0;
        while (pos < line.length()) {
            if (lookingAt(m, WHITESPACE, line, pos)) {
                pos = m.end();
                continue;
            }
            if (lookingAt(m, COMMENT, line, pos)) {
                break;
            }
            int column = pos + 1;
            if (lookingAt(m, STRING, line, pos)) {
                String quoted = m.group();
                tokens.add(new Token(Kind.STRING, unescape(quoted.substring(1, quoted.length() - 1)), column));
            } else if (lookingAt(m, INT, line, pos)) {
                tokens.add(new Token(Kind.INT, m.group(), column));
            } else if (lookingAt(m, OP, line, pos)) {
                tokens.add(new Token(Kind.OP, m.group(), column));
            } else if (lookingAt(m, WORD, line, pos)) {
                tokens.add(new Token(Kind.WORD, m.group(), column));
            } else if (line.charAt(pos) == '"') {
                throw new ParseException(column, "unterminated string");
            } else {
                throw new ParseException(column, "unexpected character '" + line.charAt(pos) + "'");
            }
            pos = m.end();
        }
        tokens.add(new Token(Kind.END, "", line.length() + 1));
        return tokens;
    }

    private static boolean lookingAt(Matcher m, Pattern pattern, String line, int pos) {
        m.usePattern(pattern);
        m.region(pos, line.length());
        return m.lookingAt();
    }

    /** Resolves {@code \x} to {@code x}, so {@code \"} and {@code \\} work. */
    private static String unescape(String body) {
        StringBuilder out = new StringBuilder(body.length());
        for (int i = 0; i < body.length(); i++) {
            char c = body.charAt(i);
            if (c == '\\' && i + 1 < body.length()) {
                c = body.charAt(++i);
            }
            out.append(c);
        }
        return out.toString();
    }
}
