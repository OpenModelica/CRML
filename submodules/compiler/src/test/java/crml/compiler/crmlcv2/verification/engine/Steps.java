package crml.compiler.crmlcv2.verification.engine;

import java.util.ArrayList;
import java.util.List;

/**
 * A discrete signal as the sequence of values it takes and the instants it takes
 * them - the form in which a clock's tick count, a period's open flag or an
 * event's time can be compared across two simulators.
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
