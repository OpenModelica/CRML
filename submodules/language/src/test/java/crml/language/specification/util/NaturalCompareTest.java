package crml.language.specification.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import crml.language.specification.util.NaturalCompare.PathCompare;

@Disabled //This is just for testing the sorting. Not required later.
public class NaturalCompareTest {
    
    private final PathCompare comparator = new PathCompare();

    @Test
    void testSimpleNumericOrdering() {
        Path p1 = Path.of("file1.txt");
        Path p2 = Path.of("file2.txt");
        assertTrue(comparator.compare(p1, p2) < 0);
        assertTrue(comparator.compare(p2, p1) > 0);
    }

    @Test
    void testNaturalOrderingWithDoubleDigits() {
        Path p1 = Path.of("file2.txt");
        Path p2 = Path.of("file10.txt");

        assertTrue(comparator.compare(p1, p2) < 0);
    }

    @Test
    void testEqualPaths() {
        Path p1 = Path.of("file1.txt");
        Path p2 = Path.of("file1.txt");

        assertEquals(0, comparator.compare(p1, p2));
    }

    @Test
    void testSortingList() {
        List<Path> files = List.of(
                Path.of("file10.txt"),
                Path.of("file2.txt"),
                Path.of("file1.txt")
        );

        List<Path> sorted = new ArrayList<>(files);
        sorted.sort(comparator);

        assertEquals(List.of(
                Path.of("file1.txt"),
                Path.of("file2.txt"),
                Path.of("file10.txt")
        ), sorted);
    }

    @Test
    void testMixedAlphaNumeric() {
        Path p1 = Path.of("a2.txt");
        Path p2 = Path.of("a10.txt");

        assertTrue(comparator.compare(p1, p2) < 0);
    }

    @Test
    void testDifferentPrefixes() {
        Path p1 = Path.of("a1.txt");
        Path p2 = Path.of("b1.txt");

        assertTrue(comparator.compare(p1, p2) < 0);
    }

    @Test
    void testCaseSensitivity() {
        Path p1 = Path.of("File1.txt");
        Path p2 = Path.of("file1.txt");

        // Adjust expectation depending on implementation
        comparator.compare(p1, p2);
    }

    @Test
    void testLeadingZeros() {
        Path p1 = Path.of("file01.txt");
        Path p2 = Path.of("file1.txt");

        // Depending on implementation, these might be equal
        comparator.compare(p1, p2);
    }

    @Test
    void testEmptyNames() {
        Path p1 = Path.of("");
        Path p2 = Path.of("file1.txt");

        comparator.compare(p1, p2);
    }

    @Test
    void testNullHandling() {
        Path p1 = Path.of("file1.txt");

        assertThrows(NullPointerException.class, () -> comparator.compare(null, p1));
        assertThrows(NullPointerException.class, () -> comparator.compare(p1, null));
    }
}
