package org.openmodelica.crml.compiler.crmlcv2.verification.engine.verification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openmodelica.crml.compiler.crmlcv2.verification.engine.verification.reference.ReferenceResolver;
import org.openmodelica.crml.compiler.crmlcv2.verification.engine.verification.signal.Binding;

/**
 * Which signals carry an output's meaning in each result: the generated ones
 * ({@link Contract}), each paired with its counterpart in the reference
 * ({@link ReferenceResolver}). The two may encode the value differently - a
 * clock's ticks as pulses in one, as a counter in the other - so each pair is
 * compared after decoding both into the same canonical form.
 *
 * <p>Everything else in the records that implement a type - a clock's counter
 * and tick buffer, the start and close events of a period - is implementation,
 * and is not compared.
 */
final class Observation {

    /** One generated signal and its counterpart in the reference. */
    static final class Check {
        final Binding actual;
        final Binding expected;

        Check(Binding actual, Binding expected) {
            this.actual = actual;
            this.expected = expected;
        }

        /** The signals compared, compiled model first, and how the reference's was found if not by name. */
        String describe() {
            if (actual.signal().equals(expected.signal()) && actual.encoding() == expected.encoding()) {
                return actual.signal();
            }
            return actual.signal() + " ~ " + expected.signal() + " (" + expected.encoding() + ", "
                + expected.origin() + ")";
        }
    }

    final List<Check> checks = new ArrayList<>();
    /** Why the output cannot be compared, or null if it can. */
    final String unobservable;
    /** A signal the compiled model should have but does not, or null. */
    final String missing;

    private Observation(String unobservable, String missing) {
        this.unobservable = unobservable;
        this.missing = missing;
    }

    /**
     * How {@code output} is observed, given the signals the simulation result
     * records. Only called for outputs of kind SCALAR, CLOCK, PERIOD and EVENT.
     */
    static Observation of(Outputs.Output output, Collection<String> actual, ReferenceResolver reference) {
        Observation observation = new Observation(null, null);
        for (Binding generated : Contract.of(output)) {
            Binding expected = reference.locate(output.name, generated);
            if (expected == null) {
                if (generated.isOptional()) {
                    continue;
                }
                return new Observation(reference.notFound(output.name, generated), null);
            }
            if (!actual.contains(generated.signal())) {
                return new Observation(null, generated.signal());
            }
            observation.checks.add(new Check(generated, expected));
        }
        return observation;
    }
}
