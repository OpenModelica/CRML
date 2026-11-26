package org.openmodelica.crml.compiler.crmlcv2.verification.engine.verification.signal;

/**
 * A signal in a result file, how it encodes its value, and why it was chosen.
 */
public final class Binding {

    private final String signal;
    private final Encoding encoding;
    private final String origin;
    private final boolean optional;

    private Binding(String signal, Encoding encoding, String origin, boolean optional) {
        this.signal = signal;
        this.encoding = encoding;
        this.origin = origin;
        this.optional = optional;
    }

    /** A signal of the compiled model, as the compiler generates it. */
    public static Binding generated(String signal, Encoding encoding) {
        return new Binding(signal, encoding, "generated", false);
    }

    /** A signal of the reference; {@code origin} says how it was found, e.g. "alias" or "rule: block output". */
    public static Binding reference(String signal, Encoding encoding, String origin) {
        return new Binding(signal, encoding, origin, false);
    }

    /** This binding, compared only if the reference has a counterpart for it. */
    public Binding optional() {
        return new Binding(signal, encoding, origin, true);
    }

    /** The name of the signal in the result file. */
    public String signal() {
        return signal;
    }

    public Encoding encoding() {
        return encoding;
    }

    /** How the signal was found: "generated", "same name", "alias", or "rule: ...". */
    public String origin() {
        return origin;
    }

    /** True if the output is still compared without this signal when the reference has no counterpart. */
    public boolean isOptional() {
        return optional;
    }

    @Override
    public String toString() {
        return signal + " (" + encoding + ", " + origin + ")";
    }
}
