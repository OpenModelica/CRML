package crml.modelica.print;

/**
 * A line-oriented text sink that owns the current indent depth. Everything the
 * printer emits goes through here, so indentation and line separators are
 * decided in exactly one place.
 */
public final class IndentingWriter {

    private final StringBuilder builder = new StringBuilder();
    private final PrinterOptions options;
    private int depth;
    private boolean atLineStart = true;

    public IndentingWriter(PrinterOptions options) {
        this.options = options;
    }

    public PrinterOptions options() {
        return options;
    }

    public IndentingWriter indent() {
        depth++;
        return this;
    }

    public IndentingWriter outdent() {
        if (depth > 0) {
            depth--;
        }
        return this;
    }

    /** Appends text to the current line, indenting first if the line is empty. */
    public IndentingWriter append(String text) {
        if (text == null || text.isEmpty()) {
            return this;
        }
        if (atLineStart) {
            for (int i = 0; i < depth; i++) {
                builder.append(options.indentString());
            }
            atLineStart = false;
        }
        builder.append(text);
        return this;
    }

    /** Appends text and ends the line. */
    public IndentingWriter line(String text) {
        append(text);
        return newline();
    }

    public IndentingWriter newline() {
        builder.append(options.lineSeparator());
        atLineStart = true;
        return this;
    }

    /** True when nothing has been written since the last line break. */
    public boolean atLineStart() {
        return atLineStart;
    }

    @Override
    public String toString() {
        return builder.toString();
    }
}
