package org.openmodelica.crml.testutil.constraints;

/** A syntax error in one line of a constraint file, at a 1-based column. */
public final class ParseException extends Exception {
    private static final long serialVersionUID = 1L;

    private final int column;

    ParseException(int column, String message) {
        super(message);
        this.column = column;
    }

    /** 1-based column of the offending character or token. */
    public int column() {
        return column;
    }

    @Override
    public String toString() {
        return "column " + column + ": " + getMessage();
    }
}
