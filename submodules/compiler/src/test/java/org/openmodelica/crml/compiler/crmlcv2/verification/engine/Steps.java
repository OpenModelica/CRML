package org.openmodelica.crml.compiler.crmlcv2.verification.engine;

import java.util.ArrayList;
import java.util.List;

/**
 * A discrete signal as the sequence of values it takes and the instants it takes
 * them - the form in which a period's open flag, an event's time or a clock's
 * ticks can be compared across two simulators.
 *
 * <p>Two results never sample at the same points, and an event instant may hold
 * several values in one result (before and after) and one in the other, so the
 * steps are normalised first: changes closer together than the tolerance count
 * as one, which keeps the last value, and a step back to the value already held
 * disappears. That also removes zero-width pulses, which a result may or may
 * not record depending on where its output points fall.
 */
final class Steps {

    private final List<double[]> steps = new ArrayList<>();

    /** From the (time, value) change points a script printed, in time order. */
    static Steps of(List<double[]> points, double tolerance) {
        Steps result = new Steps();
        for (double[] point : points) {
            if (!result.steps.isEmpty() && point[0] - result.last()[0] <= tolerance) {
                result.steps.remove(result.steps.size() - 1);
            }
            if (result.steps.isEmpty() || !same(result.last()[1], point[1])) {
                result.steps.add(point);
            }
        }
        return result;
    }

    /**
     * The instants at which the signal becomes {@code level}, from its change
     * points, as a count of them over time, normalised as {@link #of} would
     * normalise a tick counter: a step {@code (t0, 0)} at the first point, then
     * {@code (t, n)} at the n-th instant. Instants closer together than the
     * tolerance count as one, and one at the start replaces the initial step.
     */
    static Steps rises(List<double[]> points, double level, double tolerance) {
        Steps result = new Steps();
        double[] previous = null;
        int count = 0;
        for (double[] point : points) {
            if (previous == null) {
                result.steps.add(new double[] {point[0], 0});
            }
            boolean rise = same(point[1], level) && (previous == null || !same(previous[1], level));
            if (rise && (count == 0 || point[0] - result.last()[0] > tolerance)) {
                if (point[0] - result.last()[0] <= tolerance) {
                    result.steps.remove(result.steps.size() - 1);
                }
                result.steps.add(new double[] {point[0], ++count});
            }
            previous = point;
        }
        return result;
    }

    /** Null if equal within {@code tolerance}, otherwise what differs. */
    String difference(Steps expected, double tolerance) {
        int n = Math.min(steps.size(), expected.steps.size());
        for (int i = 0; i < n; i++) {
            double[] a = steps.get(i);
            double[] e = expected.steps.get(i);
            if (!same(a[1], e[1]) || Math.abs(a[0] - e[0]) > tolerance) {
                return "step " + (i + 1) + ": expected " + format(e) + ", got " + format(a)
                    + "; expected " + expected + ", got " + this;
            }
        }
        if (steps.size() != expected.steps.size()) {
            return "expected " + expected.steps.size() + " step(s) " + expected
                + ", got " + steps.size() + " " + this;
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder text = new StringBuilder("[");
        for (int i = 0; i < steps.size() && i < 12; i++) {
            text.append(i == 0 ? "" : ", ").append(format(steps.get(i)));
        }
        return text.append(steps.size() > 12 ? ", ...]" : "]").toString();
    }

    private static String format(double[] step) {
        return "t=" + trim(step[0]) + ": " + trim(step[1]);
    }

    private static String trim(double value) {
        return value == Math.rint(value) ? String.valueOf((long) value) : String.valueOf(value);
    }

    private static boolean same(double a, double b) {
        return Math.abs(a - b) <= 1e-9 * Math.max(1, Math.abs(b));
    }

    private double[] last() {
        return steps.get(steps.size() - 1);
    }
}
