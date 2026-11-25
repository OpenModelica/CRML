package org.openmodelica.crml.compiler.crmlcv2.verification.engine.verification;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.openmodelica.crml.compiler.crmlcv2.verification.engine.verification.signal.Binding;
import org.openmodelica.crml.compiler.crmlcv2.verification.engine.verification.signal.Encoding;

/**
 * The signals the compiler generates for an output, and how they encode it.
 *
 * <p>This is fixed by the compiler and by the records of {@code CRMLtoModelica.mo},
 * so the compiled model is never searched: a signal listed here that the result
 * does not record is a compiler or library error ({@code MISSING}), not something
 * to look for under another name. A change to the generated names or to the
 * library's records shows up here, as a regression of the verification suite.
 */
final class Contract {

    private Contract() {
    }

    /** The generated signals of {@code output}, of kind SCALAR, CLOCK, PERIOD or EVENT. */
    static List<Binding> of(Outputs.Output output) {
        String name = output.name;
        switch (output.kind) {
            case SCALAR:
                return single(name, output.real ? Encoding.REAL : output.boolean4 ? Encoding.BOOLEAN4 : Encoding.VALUE);
            case CLOCK:
                // CRMLClock.out is true4 exactly at the clock's ticks.
                return single(name + ".out", Encoding.PULSES);
            case EVENT:
                return single(name + ".t", Encoding.VALUE);
            case PERIOD:
                // The boundary flags are compared where the reference records them.
                return Arrays.asList(
                    Binding.generated(name + ".is_open", Encoding.VALUE),
                    Binding.generated(name + ".isLeftBoundaryIncluded", Encoding.VALUE).optional(),
                    Binding.generated(name + ".isRightBoundaryIncluded", Encoding.VALUE).optional());
            default:
                throw new IllegalArgumentException("not an observable output: " + output.kind);
        }
    }

    private static List<Binding> single(String signal, Encoding encoding) {
        return Collections.singletonList(Binding.generated(signal, encoding));
    }
}
