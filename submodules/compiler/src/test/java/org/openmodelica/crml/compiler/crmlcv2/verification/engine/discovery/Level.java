package org.openmodelica.crml.compiler.crmlcv2.verification.engine.discovery;

/**
 * How far a case is verified. Declared in order: each level includes the ones
 * before it, and enums compare in declaration order.
 */
public enum Level {
    /** CRML translates to Modelica. No omc. */
    TRANSLATE,
    /** omc loads the generated package (and the harness, if any), and {@code checkModel} passes. */
    CHECK,
    /** The simulated class simulates. */
    SIMULATE,
    /** The outputs match the reference. */
    VERIFY;

    public boolean isAtLeast(Level other) {
        return compareTo(other) >= 0;
    }

    public static Level min(Level a, Level b) {
        return a.compareTo(b) <= 0 ? a : b;
    }
}
