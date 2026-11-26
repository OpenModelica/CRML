package org.openmodelica.crml.testutil.constraints;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A parsed constraint file. Blank and comment-only lines are dropped; every other
 * line is kept with its number and source text, and either a constraint or the
 * syntax error that stopped it, so one bad line does not hide the others.
 */
public final class ConstraintFile {

    /** One non-blank line of a constraint file. */
    public static final class Line {
        /** 1-based line number in the file. */
        public final int number;
        /** The line as written, trimmed. */
        public final String text;
        /** The parsed constraint; null if the line has a syntax error. */
        public final Ast.Constraint constraint;
        /** The syntax error, e.g. "column 12: expected ']'"; null if the line parsed. */
        public final String syntaxError;

        Line(int number, String text, Ast.Constraint constraint, String syntaxError) {
            this.number = number;
            this.text = text;
            this.constraint = constraint;
            this.syntaxError = syntaxError;
        }
    }

    private final List<Line> lines;

    private ConstraintFile(List<Line> lines) {
        this.lines = Collections.unmodifiableList(lines);
    }

    public List<Line> lines() {
        return lines;
    }

    public static ConstraintFile parse(Path path) throws IOException {
        return parse(new String(Files.readAllBytes(path), StandardCharsets.UTF_8));
    }

    public static ConstraintFile parse(String text) {
        List<Line> lines = new ArrayList<>();
        String[] raw = text.split("\\r?\\n", -1);
        for (int i = 0; i < raw.length; i++) {
            String source = raw[i];
            try {
                Ast.Constraint constraint = Parser.parse(source);
                if (constraint != null) {
                    lines.add(new Line(i + 1, source.trim(), constraint, null));
                }
            } catch (ParseException e) {
                lines.add(new Line(i + 1, source.trim(), null, e.toString()));
            }
        }
        return new ConstraintFile(lines);
    }
}
