package org.openmodelica.crml.compiler.crmlcv2.verification.engine.discovery;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * One CRML model and where its fixtures are expected. Made by
 * {@link CaseDiscovery}; the predicates say how far it can be verified.
 *
 * <p>A model named {@code *_no_ext} binds all its variables itself, so it needs
 * no harness. Every other model has externals, and cannot be simulated without
 * the harness that binds them.
 */
public final class TestCase {

    static final String NO_EXTERNALS = "_no_ext";

    private final String suite;
    private final String name;
    private final Path source;
    private final Path harness;
    private final Path reference;

    TestCase(String suite, String name, Path source, Path harness, Path reference) {
        this.suite = suite;
        this.name = name;
        this.source = source;
        this.harness = harness;
        this.reference = reference;
    }

    public String suite() {
        return suite;
    }

    /** The model name, which is also the name of the generated class. */
    public String name() {
        return name;
    }

    public Path source() {
        return source;
    }

    /** Where the harness is expected, or null for a {@code *_no_ext} model. */
    public Path harness() {
        return harness;
    }

    /** Where the reference is expected. */
    public Path reference() {
        return reference;
    }

    /** True for a model with externals; false for a {@code *_no_ext} model. */
    public boolean needsHarness() {
        return harness != null;
    }

    public boolean hasHarness() {
        return harness != null && Files.isDirectory(harness);
    }

    public boolean hasReference() {
        return Files.isRegularFile(reference);
    }

    /** The highest level the fixtures allow. */
    public Level maxLevel() {
        return maxLevel(needsHarness(), hasHarness(), hasReference());
    }

    /**
     * The highest level fixtures allow. A model with externals cannot be
     * simulated without its harness, but its generated (partial) model still
     * checks on its own.
     */
    static Level maxLevel(boolean needsHarness, boolean hasHarness, boolean hasReference) {
        if (needsHarness && !hasHarness) {
            return Level.CHECK;
        }
        return hasReference ? Level.VERIFY : Level.SIMULATE;
    }

    public boolean allows(Level level) {
        return maxLevel().isAtLeast(level);
    }

    /** What the fixtures lack to reach {@code level}, or null if nothing. */
    public String missingFor(Level level) {
        if (needsHarness() && !hasHarness() && level.isAtLeast(Level.SIMULATE)) {
            return "no harness at " + harness + " (a model without one must be named *" + NO_EXTERNALS + ")";
        }
        if (level == Level.VERIFY && !hasReference()) {
            return "no reference result at " + reference;
        }
        return null;
    }

    /** The class to check and simulate, unqualified: the harness's if there is one, else the model. */
    public String simulatedClass() {
        return hasHarness() ? name + "_verif" : name;
    }

    /** The fixtures and whether they exist, for the report. */
    public String describeFixtures() {
        return (needsHarness() ? "model with externals; harness " + (hasHarness() ? "present" : "missing")
            : "*" + NO_EXTERNALS + " model") + "; reference " + (hasReference() ? "present" : "missing");
    }

    @Override
    public String toString() {
        return suite + "/" + name + ".crml";
    }
}
