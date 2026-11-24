package org.openmodelica.crml.compiler.crmlcv2.verification.engine.verification;

import java.util.ArrayList;
import java.util.List;

/**
 * Comparing a signal of the simulation result with one of the reference, both
 * given as (time, value) points in time order, by {@link Observation.Method}.
 *
 * <p>A discrete signal is compared as the sequence of values it takes and the
 * instants it takes them - the form in which a Boolean, a period's open flag, an
 * event's time or a clock's ticks can be compared across two simulators. Two
 * results never sample at the same points, and an event instant may hold several
 * values in one result (before and after) and one in the other, so the steps are
 * normalised first: changes closer together than the time tolerance count as
 * one, which keeps the last value, and a step back to the value already held
 * disappears. That also removes zero-width pulses, which a result may or may not
 * record depending on where its output points fall.
 *
 * <p>A continuous (Real) signal is compared as a trajectory: at each of the
 * reference's time points, the result's value, interpolated, must agree within
 * a tolerance relative to the reference's range. Points within the time
 * tolerance of a discontinuity in either signal are skipped.
 */
final class Series {

    /** {@code true4} as a number: {@code Boolean4 = enumeration(undefined, undecided, false4, true4)}. */
    static final double TRUE4 = 4;
    /** Relative tolerance of a trajectory's values. */
    private static final double VALUE_TOLERANCE = 1e-3;

    private final List<double[]> steps = new ArrayList<>();

    private Series() {
    }

    /**
     * Null if {@code actual} agrees with {@code expected} compared by {@code method}, otherwise
     * how they differ. {@code timeTolerance} is how far apart two instants may be and still
     * count as the same.
     */
    static String difference(Observation.Method method, List<double[]> actual, List<double[]> expected,
            double timeTolerance) {
        switch (method) {
            case TRAJECTORY:
                return trajectoryDifference(actual, expected, timeTolerance);
            case PULSES:
                return rises(actual, timeTolerance).difference(rises(expected, timeTolerance), timeTolerance);
            case PULSES_TO_COUNT:
                return rises(actual, timeTolerance).difference(steps(expected, timeTolerance), timeTolerance);
            default:
                return steps(actual, timeTolerance).difference(steps(expected, timeTolerance), timeTolerance);
        }
    }

    // --- steps -------------------------------------------------------------

    /** The normalised steps of a discrete signal. */
    static Series steps(List<double[]> points, double tolerance) {
        Series result = new Series();
        for (double[] point : changes(points)) {
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
     * The instants at which the signal becomes {@link #TRUE4}, as a count of them
     * over time, normalised as {@link #steps} would normalise a tick counter: a
     * step {@code (t0, 0)} at the first point, then {@code (t, n)} at the n-th
     * instant. Instants closer together than the tolerance count as one, and one
     * at the start replaces the initial step.
     */
    static Series rises(List<double[]> points, double tolerance) {
        Series result = new Series();
        double[] previous = null;
        int count = 0;
        for (double[] point : changes(points)) {
            if (previous == null) {
                result.steps.add(new double[] {point[0], 0});
            }
            boolean rise = same(point[1], TRUE4) && (previous == null || !same(previous[1], TRUE4));
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

    /** The first point, and every point whose value differs from the one before. */
    private static List<double[]> changes(List<double[]> points) {
        List<double[]> changes = new ArrayList<>();
        for (int i = 0; i < points.size(); i++) {
            if (i == 0 || points.get(i)[1] != points.get(i - 1)[1]) {
                changes.add(points.get(i));
            }
        }
        return changes;
    }

    /** Null if equal within {@code tolerance}, otherwise what differs. */
    String difference(Series expected, double tolerance) {
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

    // --- trajectories ------------------------------------------------------

    static String trajectoryDifference(List<double[]> actual, List<double[]> expected, double timeTolerance) {
        if (actual.isEmpty() || expected.isEmpty()) {
            return actual.isEmpty() == expected.isEmpty() ? null
                : "expected " + expected.size() + " point(s), got " + actual.size();
        }
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        for (double[] point : expected) {
            min = Math.min(min, point[1]);
            max = Math.max(max, point[1]);
        }
        double tolerance = VALUE_TOLERANCE * Math.max(1, Math.max(max - min, Math.max(Math.abs(min), Math.abs(max))));
        for (double[] point : expected) {
            double t = point[0];
            if (t > actual.get(actual.size() - 1)[0] + timeTolerance
                    || nearDiscontinuity(expected, t, timeTolerance) || nearDiscontinuity(actual, t, timeTolerance)) {
                continue;
            }
            double value = interpolate(actual, t);
            if (Math.abs(value - point[1]) > tolerance) {
                return "at t=" + trim(t) + ": expected " + trim(point[1]) + ", got " + trim(value)
                    + " (tolerance " + trim(tolerance) + ")";
            }
        }
        return null;
    }

    /** True if the signal has two points closer together than {@code tolerance} near {@code t}: an event. */
    private static boolean nearDiscontinuity(List<double[]> points, double t, double tolerance) {
        for (int i = 1; i < points.size(); i++) {
            double[] a = points.get(i - 1);
            double[] b = points.get(i);
            if (b[0] - a[0] <= tolerance && a[1] != b[1] && Math.abs(a[0] - t) <= 2 * tolerance) {
                return true;
            }
        }
        return false;
    }

    /** The value at {@code t}, linear between the points around it, constant beyond the ends. */
    private static double interpolate(List<double[]> points, double t) {
        if (t <= points.get(0)[0]) {
            return points.get(0)[1];
        }
        for (int i = 1; i < points.size(); i++) {
            double[] a = points.get(i - 1);
            double[] b = points.get(i);
            if (t <= b[0]) {
                return b[0] == a[0] ? b[1] : a[1] + (b[1] - a[1]) * (t - a[0]) / (b[0] - a[0]);
            }
        }
        return points.get(points.size() - 1)[1];
    }

    // --- helpers -----------------------------------------------------------

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
