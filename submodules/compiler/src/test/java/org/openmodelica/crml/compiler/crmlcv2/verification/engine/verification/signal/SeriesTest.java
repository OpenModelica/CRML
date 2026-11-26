package org.openmodelica.crml.compiler.crmlcv2.verification.engine.verification.signal;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/** Decoding and comparing two signals sampled differently by two simulators; no omc. */
public class SeriesTest {

    private static final double TOLERANCE = 1e-3;

    @Test
    public void stepsIgnoreSamplingAndEventDuplicates() {
        // false4 until t=1, then true4; the reference has a before/after pair at the event.
        List<double[]> actual = points(0, 3, 0.4, 3, 0.9, 3, 1.0, 4, 1.5, 4, 2, 4);
        List<double[]> expected = points(0, 3, 0.5, 3, 1.0, 3, 1.0, 4, 2, 4);
        assertNull(difference(Encoding.BOOLEAN4, actual, Encoding.BOOLEAN4, expected));
    }

    @Test
    public void stepsAtDifferentInstantsDiffer() {
        List<double[]> actual = points(0, 3, 1.1, 4, 2, 4);
        List<double[]> expected = points(0, 3, 1.0, 4, 2, 4);
        assertNotNull(difference(Encoding.VALUE, actual, Encoding.VALUE, expected));
    }

    @Test
    public void pulsesAreCountedAgainstATickCounter() {
        // Clock out is true4 for an instant at t=1 and t=2; the reference counts the ticks.
        List<double[]> actual = points(0, 3, 1, 4, 1, 3, 2, 4, 2, 3, 3, 3);
        List<double[]> ticks = points(0, 0, 1, 1, 2, 2, 3, 2);
        assertNull(difference(Encoding.PULSES, actual, Encoding.COUNTER, ticks));
        assertNull(difference(Encoding.PULSES, actual, Encoding.PULSES, actual));
        assertNotNull(difference(Encoding.PULSES, points(0, 3, 1, 4, 1, 3, 3, 3), Encoding.COUNTER, ticks));
    }

    @Test
    public void aPlainBooleanIsReadAsBoolean4() {
        // false4/true4 in the compiled model, 0/1 in a reference that uses a Boolean block.
        List<double[]> actual = points(0, 3, 2.5, 3, 2.5, 4, 5, 4, 5, 3, 10, 3);
        List<double[]> expected = points(0, 0, 2.5, 0, 2.5, 1, 5, 1, 5, 0, 10, 0);
        assertNull(difference(Encoding.BOOLEAN4, actual, Encoding.BOOLEAN, expected));
        assertNotNull(difference(Encoding.BOOLEAN4, actual, Encoding.VALUE, expected));
    }

    @Test
    public void trajectoriesAreInterpolated() {
        List<double[]> actual = new ArrayList<>();
        List<double[]> expected = new ArrayList<>();
        for (int i = 0; i <= 100; i++) {
            actual.add(new double[] {i * 0.01, Math.sin(i * 0.01)});
        }
        for (int i = 0; i <= 37; i++) {
            expected.add(new double[] {i / 37.0, Math.sin(i / 37.0)});
        }
        assertNull(difference(Encoding.REAL, actual, Encoding.REAL, expected));

        List<double[]> shifted = new ArrayList<>();
        for (double[] point : expected) {
            shifted.add(new double[] {point[0], point[1] + 0.1});
        }
        assertNotNull(difference(Encoding.REAL, actual, Encoding.REAL, shifted));
    }

    private static String difference(Encoding actualEncoding, List<double[]> actual,
            Encoding expectedEncoding, List<double[]> expected) {
        return actualEncoding.decode(actual, TOLERANCE).difference(expectedEncoding.decode(expected, TOLERANCE),
            TOLERANCE);
    }

    /** (time, value) pairs. */
    private static List<double[]> points(double... values) {
        List<double[]> points = new ArrayList<>();
        for (int i = 0; i < values.length; i += 2) {
            points.add(new double[] {values[i], values[i + 1]});
        }
        return points;
    }
}
