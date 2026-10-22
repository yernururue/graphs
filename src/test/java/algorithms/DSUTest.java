package algorithms;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DSUTest {
    @Test
    void startsWithSeparateSingletonSetsAndZeroCounters() {
        DSU dsu = new DSU(4);
        assertEquals(4, dsu.components());
        assertEquals(0, dsu.findCalls());
        assertEquals(0, dsu.successfulUnions());
        assertEquals(0, dsu.dsuOps());
        for (int x = 0; x < 4; x++) {
            assertEquals(x, dsu.find(x));
        }
        assertEquals(4, dsu.findCalls());
    }

    @Test
    void mergesSetsAndPreservesOtherComponents() {
        DSU dsu = new DSU(5);
        assertTrue(dsu.union(0, 1));
        assertTrue(dsu.union(1, 2));
        assertTrue(dsu.union(3, 4));
        assertEquals(dsu.find(0), dsu.find(2));
        assertEquals(dsu.find(3), dsu.find(4));
        assertNotEquals(dsu.find(0), dsu.find(3));
        assertEquals(2, dsu.components());
    }

    @Test
    void attachesSmallerSetToLargerEvenWhenSmallerIsFirstArgument() {
        DSU dsu = new DSU(6);
        dsu.union(0, 1);
        dsu.union(0, 2);
        dsu.union(3, 4);
        int largerRoot = dsu.find(0);
        assertTrue(dsu.union(3, 0));
        assertEquals(largerRoot, dsu.find(3));
        assertEquals(largerRoot, dsu.find(4));
        assertNotEquals(largerRoot, dsu.find(5));
        assertEquals(2, dsu.components());
    }

    @Test
    void countsFindCallsAndOnlySuccessfulUnions() {
        DSU dsu = new DSU(3);
        dsu.find(2);
        assertEquals(1, dsu.findCalls());
        assertEquals(1, dsu.dsuOps());
        assertTrue(dsu.union(0, 1));
        assertEquals(3, dsu.findCalls());
        assertEquals(1, dsu.successfulUnions());
        assertEquals(4, dsu.dsuOps());
        assertFalse(dsu.union(0, 1));
        assertFalse(dsu.union(1, 0));
        assertFalse(dsu.union(2, 2));
        assertEquals(9, dsu.findCalls());
        assertEquals(1, dsu.successfulUnions());
        assertEquals(10, dsu.dsuOps());
        assertEquals(2, dsu.components());
        dsu.find(1);
        assertEquals(10, dsu.findCalls());
        assertEquals(11, dsu.dsuOps());
    }

    @Test
    void findFullyCompressesEveryVertexOnThePath() throws ReflectiveOperationException {
        DSU dsu = new DSU(8);
        dsu.union(0, 1);
        dsu.union(2, 3);
        dsu.union(4, 5);
        dsu.union(6, 7);
        dsu.union(0, 2);
        dsu.union(4, 6);
        dsu.union(0, 4);
        int[] before = parents(dsu);
        int first = before[7];
        int second = before[first];
        int root = before[second];
        assertNotEquals(root, first);
        assertNotEquals(root, second);
        assertEquals(root, before[root]);

        assertEquals(root, dsu.find(7));

        int[] after = parents(dsu);
        assertEquals(root, after[7]);
        assertEquals(root, after[first]);
        assertEquals(root, after[second]);
        assertEquals(15, dsu.findCalls());
        assertEquals(7, dsu.successfulUnions());
    }

    @Test
    void makeSetResizesAndResetsStateAndCounters() {
        DSU dsu = new DSU(4);
        dsu.union(0, 1);
        dsu.find(3);
        dsu.makeSet(2);
        assertEquals(2, dsu.components());
        assertEquals(0, dsu.findCalls());
        assertEquals(0, dsu.successfulUnions());
        assertEquals(0, dsu.dsuOps());
        assertEquals(0, dsu.find(0));
        assertEquals(1, dsu.find(1));
        assertThrows(IndexOutOfBoundsException.class, () -> dsu.find(2));
        dsu.makeSet(5);
        assertEquals(5, dsu.components());
        assertEquals(0, dsu.dsuOps());
        assertEquals(4, dsu.find(4));
    }

    @Test
    void zeroElementsAreValidAndHaveNoValidMember() {
        DSU dsu = new DSU(0);
        assertEquals(0, dsu.components());
        assertThrows(IndexOutOfBoundsException.class, () -> dsu.find(0));
        assertThrows(IndexOutOfBoundsException.class, () -> dsu.union(0, 0));
        assertEquals(0, dsu.dsuOps());
        dsu.makeSet(2);
        dsu.union(0, 1);
        dsu.makeSet(0);
        assertEquals(0, dsu.components());
        assertEquals(0, dsu.findCalls());
        assertEquals(0, dsu.successfulUnions());
        assertThrows(IndexOutOfBoundsException.class, () -> dsu.find(0));
    }

    @Test
    void negativeSizeIsRejectedWithoutResettingExistingState() throws ReflectiveOperationException {
        assertThrows(IllegalArgumentException.class, () -> new DSU(-1));
        DSU dsu = new DSU(3);
        dsu.union(0, 1);
        int[] before = parents(dsu);
        assertThrows(IllegalArgumentException.class, () -> dsu.makeSet(-1));
        assertArrayEquals(before, parents(dsu));
        assertEquals(2, dsu.components());
        assertEquals(2, dsu.findCalls());
        assertEquals(1, dsu.successfulUnions());
        assertEquals(3, dsu.dsuOps());
        assertTrue(dsu.union(1, 2));
        assertEquals(1, dsu.components());
    }

    @Test
    void invalidArgumentsNeverChangeParentsComponentsOrCounters() throws ReflectiveOperationException {
        DSU dsu = new DSU(4);
        dsu.union(0, 1);
        dsu.union(2, 3);
        dsu.union(0, 2);
        int[] before = parents(dsu);
        for (int invalid : new int[]{-1, 4, Integer.MIN_VALUE, Integer.MAX_VALUE}) {
            assertThrows(IndexOutOfBoundsException.class, () -> dsu.find(invalid));
            assertThrows(IndexOutOfBoundsException.class, () -> dsu.union(invalid, 3));
            assertThrows(IndexOutOfBoundsException.class, () -> dsu.union(3, invalid));
            assertArrayEquals(before, parents(dsu));
            assertEquals(1, dsu.components());
            assertEquals(6, dsu.findCalls());
            assertEquals(3, dsu.successfulUnions());
            assertEquals(9, dsu.dsuOps());
        }
    }

    @Test
    void randomUnionsMatchIndependentLabelArrayReference() {
        int n = 25;
        DSU dsu = new DSU(n);
        int[] labels = new int[n];
        for (int x = 0; x < n; x++) {
            labels[x] = x;
        }
        Random random = new Random(42);
        int merges = 0;
        for (int operation = 0; operation < 200; operation++) {
            int x = random.nextInt(n);
            int y = random.nextInt(n);
            int oldLabel = labels[y];
            int newLabel = labels[x];
            boolean expectedMerge = oldLabel != newLabel;
            assertEquals(expectedMerge, dsu.union(x, y));
            if (expectedMerge) {
                merges++;
                for (int element = 0; element < n; element++) {
                    if (labels[element] == oldLabel) {
                        labels[element] = newLabel;
                    }
                }
            }
            Set<Integer> referenceSets = new HashSet<>();
            for (int label : labels) {
                referenceSets.add(label);
            }
            assertEquals(referenceSets.size(), dsu.components());
            assertEquals(merges, dsu.successfulUnions());
            for (int a = 0; a < n; a++) {
                for (int b = 0; b < n; b++) {
                    assertEquals(labels[a] == labels[b], dsu.find(a) == dsu.find(b));
                }
            }
        }
    }

    @Test
    void handlesOneHundredThousandElements() {
        int n = 100_000;
        DSU dsu = new DSU(n);
        for (int x = 1; x < n; x++) {
            assertTrue(dsu.union(x, x - 1));
        }
        assertEquals(1, dsu.components());
        int root = dsu.find(0);
        for (int x = 1; x < n; x++) {
            assertEquals(root, dsu.find(x));
        }
        assertEquals(n - 1, dsu.successfulUnions());
        assertEquals(299_998, dsu.findCalls());
        assertEquals(399_997, dsu.dsuOps());
    }

    private static int[] parents(DSU dsu) throws ReflectiveOperationException {
        Field field = DSU.class.getDeclaredField("parent");
        field.setAccessible(true);
        return ((int[]) field.get(dsu)).clone();
    }
}
