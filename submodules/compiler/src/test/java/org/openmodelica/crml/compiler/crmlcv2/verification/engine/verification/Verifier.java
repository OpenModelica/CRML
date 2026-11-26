package org.openmodelica.crml.compiler.crmlcv2.verification.engine.verification;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openmodelica.crml.compiler.crmlcv2.verification.engine.evaluation.OmcRun;
import org.openmodelica.crml.compiler.crmlcv2.verification.engine.evaluation.Simulation;
import org.openmodelica.crml.compiler.crmlcv2.verification.engine.evaluation.Translation;
import org.openmodelica.crml.compiler.crmlcv2.verification.engine.discovery.TestCase;
import org.openmodelica.crml.compiler.crmlcv2.verification.engine.verification.reference.ReferenceResolver;
import org.openmodelica.crml.compiler.crmlcv2.verification.engine.verification.signal.Encoding;
import org.openmodelica.crml.compiler.crmlcv2.verification.engine.verification.signal.Series;

/**
 * Stage III: the model's outputs against the reference.
 * <ol>
 * <li>What is compared: the model's CRML variables ({@link Outputs}).</li>
 * <li>How each is seen in the two results ({@link Observation}): the signals the
 * compiler generates ({@link Contract}), and their counterparts in the reference
 * ({@link ReferenceResolver}).</li>
 * <li>Whether they agree: the signals are read into Java ({@link Signals}),
 * decoded into a canonical form ({@link Encoding}) and compared there ({@link Series}).</li>
 * </ol>
 * An output the reference records but the simulation does not produce is
 * {@code MISSING}, not skipped. A wrong alias file of the reference throws.
 */
public final class Verifier {

    private Verifier() {
    }

    public static VerificationResult verify(TestCase testCase, Translation translation, Simulation simulation)
            throws IOException, InterruptedException {
        Path directory = simulation.workspace().directory();
        List<OmcRun> runs = new ArrayList<>();

        OmcRun actualNames = Signals.names(directory, "result-names.mos", simulation.resultFile());
        OmcRun expectedNames = Signals.names(directory, "reference-names.mos", simulation.reference());
        runs.add(actualNames);
        runs.add(expectedNames);
        for (OmcRun run : runs) {
            if (!run.ok()) {
                return new VerificationResult(directory, Collections.<OutputResult>emptyList(), runs, run.reason());
            }
        }

        ReferenceResolver reference = ReferenceResolver.of(testCase.reference(), Signals.names(expectedNames));
        Set<String> actualNamesSet = Signals.names(actualNames);
        List<Entry> entries = new ArrayList<>();
        Set<String> actualSignals = new LinkedHashSet<>();
        Set<String> expectedSignals = new LinkedHashSet<>();
        for (Outputs.Output output : Outputs.of(translation.model(), translation.trace())) {
            Entry entry = classify(output, actualNamesSet, reference);
            entries.add(entry);
            if (entry.observation != null) {
                for (Observation.Check check : entry.observation.checks) {
                    actualSignals.add(check.actual.signal());
                    expectedSignals.add(check.expected.signal());
                }
            }
        }
        reference.checkAllAliasesUsed();

        Map<String, List<double[]>> actual = Collections.emptyMap();
        Map<String, List<double[]>> expected = Collections.emptyMap();
        if (!actualSignals.isEmpty()) {
            OmcRun actualRead = Signals.read(directory, "result-signals.mos", simulation.resultFile(), actualSignals);
            OmcRun expectedRead = Signals.read(directory, "reference-signals.mos", simulation.reference(),
                expectedSignals);
            runs.add(actualRead);
            runs.add(expectedRead);
            for (OmcRun run : new OmcRun[] {actualRead, expectedRead}) {
                if (!run.ok()) {
                    return new VerificationResult(directory, Collections.<OutputResult>emptyList(), runs, run.reason());
                }
            }
            actual = Signals.series(actualRead);
            expected = Signals.series(expectedRead);
        }

        double tolerance = 1e-4 * Math.max(1, parse(simulation.stopTime()));
        List<OutputResult> results = new ArrayList<>();
        for (Entry entry : entries) {
            results.add(entry.result(actual, expected, tolerance));
        }
        return new VerificationResult(directory, results, runs, null);
    }

    /** An output, either settled from the names alone ({@code status} set) or to be compared ({@code observation} set). */
    private static final class Entry {
        final Outputs.Output output;
        final OutputResult.Status status;
        final String detail;
        final Observation observation;

        Entry(Outputs.Output output, OutputResult.Status status, String detail, Observation observation) {
            this.output = output;
            this.status = status;
            this.detail = detail;
            this.observation = observation;
        }

        OutputResult result(Map<String, List<double[]>> actual, Map<String, List<double[]>> expected,
                double tolerance) {
            if (observation == null) {
                return OutputResult.of(output, status, "", detail);
            }
            StringBuilder comparedAs = new StringBuilder();
            StringBuilder differences = new StringBuilder();
            for (Observation.Check check : observation.checks) {
                comparedAs.append(comparedAs.length() == 0 ? "" : ", ").append(check.describe());
                Series actualSeries = check.actual.encoding().decode(points(actual, check.actual.signal()), tolerance);
                Series expectedSeries = check.expected.encoding().decode(points(expected, check.expected.signal()),
                    tolerance);
                String difference = actualSeries.difference(expectedSeries, tolerance);
                if (difference != null) {
                    differences.append(differences.length() == 0 ? "" : "; ")
                        .append(check.describe()).append(": ").append(difference);
                }
            }
            return OutputResult.of(output,
                differences.length() == 0 ? OutputResult.Status.MATCH : OutputResult.Status.DIFFERS,
                comparedAs.toString(), differences.toString());
        }
    }

    private static Entry classify(Outputs.Output output, Collection<String> actual, ReferenceResolver reference) {
        switch (output.kind) {
            case INPUT:
                return new Entry(output, OutputResult.Status.INPUT, "", null);
            case NO_RULE:
                return new Entry(output, OutputResult.Status.NO_RULE, "", null);
            default:
                break;
        }
        Observation observation = Observation.of(output, actual, reference);
        if (observation.unobservable != null) {
            return new Entry(output, OutputResult.Status.NOT_IN_REFERENCE, observation.unobservable, null);
        }
        if (output.dropped) {
            return new Entry(output, OutputResult.Status.DROPPED, "the translation produced no component for it", null);
        }
        if (observation.missing != null) {
            return new Entry(output, OutputResult.Status.MISSING, "the result has no " + observation.missing, null);
        }
        return new Entry(output, null, "", observation);
    }

    private static List<double[]> points(Map<String, List<double[]>> series, String signal) {
        List<double[]> points = series.get(signal);
        return points == null ? Collections.<double[]>emptyList() : points;
    }

    private static double parse(String number) {
        try {
            return number == null ? 0 : Double.parseDouble(number.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
