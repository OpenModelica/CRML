package org.openmodelica.crml.compiler.crmlcv2.verification.engine.discovery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

/** How far a case can be verified, from its fixtures; no omc. */
public class DiscoveryTest {

    @Test
    public void levelsAreOrdered() {
        assertTrue(Level.VERIFY.isAtLeast(Level.SIMULATE));
        assertTrue(Level.CHECK.isAtLeast(Level.CHECK));
        assertFalse(Level.TRANSLATE.isAtLeast(Level.CHECK));
        assertEquals(Level.CHECK, Level.min(Level.VERIFY, Level.CHECK));
    }

    @Test
    public void aModelWithExternalsNeedsItsHarnessToSimulate() {
        assertEquals(Level.CHECK, TestCase.maxLevel(true, false, false));
        assertEquals(Level.CHECK, TestCase.maxLevel(true, false, true));
        assertEquals(Level.SIMULATE, TestCase.maxLevel(true, true, false));
        assertEquals(Level.VERIFY, TestCase.maxLevel(true, true, true));
    }

    @Test
    public void aNoExtModelIsVerifiedIfItHasAReference() {
        assertEquals(Level.SIMULATE, TestCase.maxLevel(false, false, false));
        assertEquals(Level.VERIFY, TestCase.maxLevel(false, false, true));
    }

    @Test
    public void namingDecidesTheFixtures() {
        TestCase withExternals = CaseDiscovery.of("none", Paths.get("Model.crml"));
        assertTrue(withExternals.needsHarness());
        assertEquals("Model_verif_ref.mat", withExternals.reference().getFileName().toString());
        assertEquals(Level.CHECK, withExternals.maxLevel());
        assertNull(withExternals.missingFor(Level.CHECK));
        assertTrue(withExternals.missingFor(Level.SIMULATE).startsWith("no harness at"));

        TestCase noExt = CaseDiscovery.of("none", Paths.get("Model_no_ext.crml"));
        assertFalse(noExt.needsHarness());
        assertEquals("Model_no_ext_ref.mat", noExt.reference().getFileName().toString());
        assertEquals("Model_no_ext", noExt.simulatedClass());
        assertNull(noExt.missingFor(Level.SIMULATE));
        assertTrue(noExt.missingFor(Level.VERIFY).startsWith("no reference result at"));
    }
}
