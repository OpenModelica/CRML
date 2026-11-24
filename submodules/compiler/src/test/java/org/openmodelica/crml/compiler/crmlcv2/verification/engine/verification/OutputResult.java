package org.openmodelica.crml.compiler.crmlcv2.verification.engine.verification;

/** One output of the model, and what came of comparing it. */
public final class OutputResult {

    public enum Status {
        MATCH,
        DIFFERS,
        /** The reference records it, the simulation result does not. */
        MISSING,
        /** The translation produced no component for it. */
        DROPPED,
        /** The reference does not record it. */
        NOT_IN_REFERENCE,
        /** Its type has no comparison defined. */
        NO_RULE,
        /** An external, bound by the harness: not an output. */
        INPUT
    }

    private final String name;
    private final String kind;
    private final String comparedAs;
    private final Status status;
    private final String detail;

    private OutputResult(String name, String kind, String comparedAs, Status status, String detail) {
        this.name = name;
        this.kind = kind;
        this.comparedAs = comparedAs;
        this.status = status;
        this.detail = detail;
    }

    static OutputResult of(Outputs.Output output, Status status, String comparedAs, String detail) {
        String kind = output.kind == Outputs.Kind.NO_RULE ? output.type : output.kind.name().toLowerCase();
        return new OutputResult(output.name, kind, comparedAs, status, detail);
    }

    public String name() {
        return name;
    }

    /** The kind of value: scalar, clock, period, event, input, or a type with no rule. */
    public String kind() {
        return kind;
    }

    /** The signals compared, compiled model first, or empty if none was. */
    public String comparedAs() {
        return comparedAs;
    }

    public Status status() {
        return status;
    }

    /** Why it has its status, or empty. */
    public String detail() {
        return detail;
    }

    @Override
    public String toString() {
        return String.format("%-17s %-8s %s%s%s", status, kind, name,
            comparedAs.isEmpty() || comparedAs.equals(name) ? "" : "  [" + comparedAs + "]",
            detail.isEmpty() ? "" : "  -- " + detail);
    }
}
