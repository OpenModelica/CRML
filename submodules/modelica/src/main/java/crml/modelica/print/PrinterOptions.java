package crml.modelica.print;

/**
 * Formatting knobs for {@link ModelicaPrinter}. The defaults reproduce the
 * layout the string-based generator used: four spaces per level and the
 * platform line separator.
 */
public final class PrinterOptions {

    public static final PrinterOptions DEFAULT = new PrinterOptions("    ", System.lineSeparator());

    private final String indentString;
    private final String lineSeparator;

    public PrinterOptions(String indentString, String lineSeparator) {
        this.indentString = indentString;
        this.lineSeparator = lineSeparator;
    }

    public String indentString() {
        return indentString;
    }

    public String lineSeparator() {
        return lineSeparator;
    }

    public PrinterOptions withIndentString(String newIndentString) {
        return new PrinterOptions(newIndentString, lineSeparator);
    }

    public PrinterOptions withLineSeparator(String newLineSeparator) {
        return new PrinterOptions(indentString, newLineSeparator);
    }
}
