package org.openmodelica.crml.testutil.constraints;

/** The outcome of one line of a constraint file. */
public final class ConstraintResult {

    public enum Status {
        /** The constraint is true. */
        PASS,
        /** The constraint is false or unresolved; the detail says which. */
        FAIL,
        /** The line is malformed: syntax, an unknown type or feature, or a literal compared with an object. */
        ERROR;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    public final int line;
    public final String text;
    public final Status status;
    public final String detail;

    ConstraintResult(int line, String text, Status status, String detail) {
        this.line = line;
        this.text = text;
        this.status = status;
        this.detail = detail == null ? "" : detail;
    }

    /** True unless the status is {@link Status#PASS}. */
    public boolean failed() {
        return status != Status.PASS;
    }

    @Override
    public String toString() {
        return "line " + line + " [" + status + "] " + text + (detail.isEmpty() ? "" : ": " + detail);
    }
}
