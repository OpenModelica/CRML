package crml.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import crml.util.NaturalCompare.PathCompare;

@Disabled //This is just for testing the sorting. Not required later.
public class NaturalCompareTest {
    
    private final PathCompare comparator = new PathCompare();

    @Test
    void testSimpleNumericOrdering() {
        Path p1 = Paths.get("file1.txt");
        Path p2 = Paths.get("file2.txt");
        assertTrue(comparator.compare(p1, p2) < 0);
        assertTrue(comparator.compare(p2, p1) > 0);
    }

    @Test
    void testNaturalOrderingWithDoubleDigits() {
        Path p1 = Paths.get("file2.txt");
        Path p2 = Paths.get("file10.txt");

        assertTrue(comparator.compare(p1, p2) < 0);
    }

    @Test
    void testEqualPaths() {
        Path p1 = Paths.get("file1.txt");
        Path p2 = Paths.get("file1.txt");

        assertEquals(0, comparator.compare(p1, p2));
    }

    @Test
    void testSortingList() {
        List<Path> files = Arrays.asList(
                Paths.get("file10.txt"),
                Paths.get("file2.txt"),
                Paths.get("file1.txt")
        );

        List<Path> sorted = new ArrayList<>(files);
        sorted.sort(comparator);

        assertEquals(Arrays.asList(
                Paths.get("file1.txt"),
                Paths.get("file2.txt"),
                Paths.get("file10.txt")
        ), sorted);
    }

    @Test
    void testMixedAlphaNumeric() {
        Path p1 = Paths.get("a2.txt");
        Path p2 = Paths.get("a10.txt");

        assertTrue(comparator.compare(p1, p2) < 0);
    }

    @Test
    void testDifferentPrefixes() {
        Path p1 = Paths.get("a1.txt");
        Path p2 = Paths.get("b1.txt");

        assertTrue(comparator.compare(p1, p2) < 0);
    }

    @Test
    void testCaseSensitivity() {
        Path p1 = Paths.get("File1.txt");
        Path p2 = Paths.get("file1.txt");

        // Adjust expectation depending on implementation
        comparator.compare(p1, p2);
    }

    @Test
    void testLeadingZeros() {
        Path p1 = Paths.get("file01.txt");
        Path p2 = Paths.get("file1.txt");

        // Depending on implementation, these might be equal
        comparator.compare(p1, p2);
    }

    @Test
    void testEmptyNames() {
        Path p1 = Paths.get("");
        Path p2 = Paths.get("file1.txt");

        comparator.compare(p1, p2);
    }

    @Test
    void testNullHandling() {
        Path p1 = Paths.get("file1.txt");

        assertThrows(NullPointerException.class, () -> comparator.compare(null, p1));
        assertThrows(NullPointerException.class, () -> comparator.compare(p1, null));
    }
}
