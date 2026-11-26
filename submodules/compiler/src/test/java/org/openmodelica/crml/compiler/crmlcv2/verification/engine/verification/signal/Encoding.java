package org.openmodelica.crml.compiler.crmlcv2.verification.engine.verification.signal;

import java.util.ArrayList;
import java.util.List;

/**
 * How a signal in a result file represents a value.
 *
 * <p>Two results may represent the same value differently: a clock as a pulse of
 * {@code true4} at each tick in one, as a counter that steps up at each tick in
 * the other. Each encoding decodes its signal into the canonical form of its
 * {@link Form}, and only canonical forms are compared, so a new representation
 * is one new encoding rather than one new comparison per pair of them.
 */
public enum Encoding {

    /** Any discrete value as it is: Integer, a period's open flag, an event's time. */
    VALUE(Form.STEPS),
    /** {@code Boolean4 = enumeration(undefined, undecided, false4, true4)}, as 1 to 4. */
    BOOLEAN4(Form.STEPS),
    /** A plain Boolean, as 0 and 1: false becomes {@code false4} and true {@code true4}. */
    BOOLEAN(Form.STEPS),
    /** A continuous signal. */
    REAL(Form.TRAJECTORY),
    /** {@code true4} for an instant at each tick, such as {@code CRMLClock.out}. */
    PULSES(Form.TICKS),
    /** The number of ticks so far, such as a synchronous clock's activation count. */
    COUNTER(Form.TICKS);

    /** The canonical form an encoding decodes into; two signals are comparable if they have the same form. */
    public enum Form {
        /** The sequence of values taken and the instants they are taken. */
        STEPS,
        /** The number of ticks over time. */
        TICKS,
        /** Values over time, compared at the reference's time points. */
        TRAJECTORY
    }

    private final Form form;

    Encoding(Form form) {
        this.form = form;
    }

    public Form form() {
        return form;
    }

    /**
     * The canonical form of {@code points}, (time, value) pairs in time order.
     * {@code tolerance} is how far apart two instants may be and still count as the same.
     */
    public Series decode(List<double[]> points, double tolerance) {
        switch (this) {
            case REAL:
                return Series.trajectory(points);
            case PULSES:
                return Series.rises(points, tolerance);
            case BOOLEAN:
                return Series.steps(toBoolean4(points), tolerance);
            default:
                // VALUE, BOOLEAN4 and COUNTER are already in canonical form.
                return Series.steps(points, tolerance);
        }
    }

    private static List<double[]> toBoolean4(List<double[]> points) {
        List<double[]> mapped = new ArrayList<>(points.size());
        for (double[] point : points) {
            mapped.add(new double[] {point[0], point[1] > 0.5 ? Series.TRUE4 : Series.FALSE4});
        }
        return mapped;
    }
}
