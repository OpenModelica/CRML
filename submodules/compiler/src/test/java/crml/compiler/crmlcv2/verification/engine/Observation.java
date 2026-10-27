package crml.compiler.crmlcv2.verification.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Which signals carry an output's meaning in each result, and how to compare them.
 *
 * <p>The compiler's library (CRMLtoModelica.mo) and the reference models
 * (CRML.mo, simulated in Dymola) implement the same CRML types differently, so
 * the signals are matched by meaning, not by name:
 * <table>
 * <tr><th>Type</th><th>Compiled model</th><th>Reference</th><th>Compared as</th></tr>
 * <tr><td>scalar</td><td>{@code x}</td><td>{@code x}</td><td>trajectory</td></tr>
 * <tr><td>Period</td><td>{@code P.is_open}, {@code P.isLeft/RightBoundaryIncluded}</td>
 *     <td>{@code P.timePeriod} (a time locator record), or {@code P.is_open} where the
 *     reference uses the same record; same boundary flags</td>
 *     <td>open/closed changes, boundary flags</td></tr>
 * <tr><td>Clock</td><td>{@code c.counter}</td>
 *     <td>{@code c.counter} where the reference uses the same record (an external clock),
 *     otherwise the activation count of the synchronous clock that drives the harness's
 *     {@code show_c} or {@code showEvent_c} block</td><td>tick count over time</td></tr>
 * <tr><td>Event</td><td>{@code e.t}</td><td>{@code e.t}</td><td>event time over time</td></tr>
 * </table>
 * Everything else in those records - the tick buffer, the start and close
 * events of a period, a time locator's slots and index - is implementation, and
 * is not compared.
 */
final class Observation {

    /** How two signals are compared. */
    enum Method {
        /** {@code diffSimulationResults}: same name on both sides, trajectory within tolerance. */
        TRAJECTORY,
        /** Same sequence of value changes, at the same instants within tolerance. */
        STEPS
    }

    /** One pair of signals to compare. */
    static final class Check {
        final String actual;
        final String expected;
        final Method method;

        Check(String actual, String expected, Method method) {
            this.actual = actual;
            this.expected = expected;
            this.method = method;
        }

        String describe() {
            return actual.equals(expected) ? actual : actual + " ~ " + expected;
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
     * How {@code output} is observed, given the variables each result holds.
     * Only called for outputs of kind SCALAR, CLOCK, PERIOD and EVENT.
     */
    static Observation of(Outputs.Output output, Collection<String> actual, Collection<String> expected) {
        String name = output.name;
        switch (output.kind) {
            case SCALAR:
                return single(name, name, Method.TRAJECTORY, actual, expected);
            case EVENT:
                return single(name + ".t", name + ".t", Method.STEPS, actual, expected);
            case CLOCK: {
                String ticks = clockTicks(name, expected);
                if (ticks == null) {
                    return new Observation("the reference has no tick count for it: neither " + name
                        + ".counter nor a show_" + name + " or showEvent_" + name + " block", null);
                }
                return single(name + ".counter", ticks, Method.STEPS, actual, expected);
            }
            case PERIOD: {
                String open = expected.contains(name + ".timePeriod") ? name + ".timePeriod" : name + ".is_open";
                Observation observation = single(name + ".is_open", open, Method.STEPS, actual, expected);
                if (observation.unobservable == null && observation.missing == null) {
                    for (String flag : new String[] {".isLeftBoundaryIncluded", ".isRightBoundaryIncluded"}) {
                        if (expected.contains(name + flag) && actual.contains(name + flag)) {
                            observation.checks.add(new Check(name + flag, name + flag, Method.STEPS));
                        }
                    }
                }
                return observation;
            }
            default:
                throw new IllegalArgumentException("not an observable output: " + output.kind);
        }
    }

    private static Observation single(String actualSignal, String expectedSignal, Method method,
            Collection<String> actual, Collection<String> expected) {
        if (expectedSignal == null || !expected.contains(expectedSignal)) {
            return new Observation("the reference does not record "
                + (expectedSignal == null ? "it" : expectedSignal), null);
        }
        if (!actual.contains(actualSignal)) {
            return new Observation(null, actualSignal);
        }
        Observation observation = new Observation(null, null);
        observation.checks.add(new Check(actualSignal, expectedSignal, method));
        return observation;
    }

    /**
     * The reference's tick count for clock {@code name}: the clock's own counter
     * if the reference stores it as the compiler's record does, otherwise the
     * activation count of the synchronous clock whose partition holds the
     * harness's {@code show_<name>} or {@code showEvent_<name>} block. Null if
     * neither is there: the reference model then computes the clock in a block
     * that nothing ties to the CRML variable's name.
     */
    private static String clockTicks(String name, Collection<String> expected) {
        if (expected.contains(name + ".counter")) {
            return name + ".counter";
        }
        Pattern show = Pattern.compile("^(_Clocks\\.BaseClock_\\d+\\.SubClock_\\d+)\\.(?:show|showEvent)_"
            + Pattern.quote(name) + "\\.y$");
        for (String variable : expected) {
            Matcher matcher = show.matcher(variable);
            if (matcher.matches() && expected.contains(matcher.group(1) + ".activationCount")) {
                return matcher.group(1) + ".activationCount";
            }
        }
        return null;
    }
}
