package crml.modelica.print;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Modelica identifier rules. CRML names are not constrained to Modelica's
 * identifier syntax, so a generated name may need Modelica's quoted form
 * ({@code 'a name'}).
 *
 * <p>The whole printer routes every declared name through {@link #quote(String)},
 * which is what keeps a class header and its {@code end} in agreement.
 */
public final class Identifiers {

    /** Modelica 3.x reserved words; none of them may appear unquoted as a name. */
    private static final Set<String> KEYWORDS = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
            "algorithm", "and", "annotation", "assert", "block", "break", "class", "connect",
            "connector", "constant", "constrainedby", "der", "discrete", "each", "else",
            "elseif", "elsewhen", "encapsulated", "end", "enumeration", "equation", "expandable",
            "extends", "external", "false", "final", "flow", "for", "function", "if", "import",
            "impure", "in", "initial", "inner", "input", "loop", "model", "not", "operator",
            "or", "outer", "output", "package", "parameter", "partial", "protected", "public",
            "pure", "record", "redeclare", "replaceable", "return", "stream", "then", "true",
            "type", "when", "while", "within")));

    private Identifiers() {
    }

    /**
     * Returns {@code name} as it must be written in Modelica: unchanged when it
     * is a plain identifier, single-quoted otherwise.
     */
    public static String quote(String name) {
        if (name == null) {
            return null;
        }
        if (isPlainIdentifier(name) && !isKeyword(name)) {
            return name;
        }
        StringBuilder builder = new StringBuilder(name.length() + 2).append('\'');
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            switch (c) {
                case '\'': builder.append("\\'"); break;
                case '\\': builder.append("\\\\"); break;
                case '\n': builder.append("\\n"); break;
                case '\r': builder.append("\\r"); break;
                case '\t': builder.append("\\t"); break;
                default: builder.append(c);
            }
        }
        return builder.append('\'').toString();
    }

    /** Quotes every dot-separated segment of a path individually. */
    public static String quotePath(String dottedName) {
        if (dottedName == null) {
            return null;
        }
        String[] parts = dottedName.split("\\.", -1);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                builder.append('.');
            }
            builder.append(quote(parts[i]));
        }
        return builder.toString();
    }

    public static boolean isKeyword(String name) {
        return KEYWORDS.contains(name);
    }

    /** True when {@code name} matches Modelica's unquoted IDENT production. */
    public static boolean isPlainIdentifier(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        if (!isNonDigit(name.charAt(0))) {
            return false;
        }
        for (int i = 1; i < name.length(); i++) {
            char c = name.charAt(i);
            if (!isNonDigit(c) && (c < '0' || c > '9')) {
                return false;
            }
        }
        return true;
    }

    private static boolean isNonDigit(char c) {
        return c == '_' || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    /** Escapes a Modelica string literal body (without the surrounding quotes). */
    public static String escapeStringBody(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"': builder.append("\\\""); break;
                case '\\': builder.append("\\\\"); break;
                case '\n': builder.append("\\n"); break;
                case '\r': builder.append("\\r"); break;
                case '\t': builder.append("\\t"); break;
                default: builder.append(c);
            }
        }
        return builder.toString();
    }
}
