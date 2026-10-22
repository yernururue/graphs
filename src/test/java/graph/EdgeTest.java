package graph;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

class EdgeTest {
    @Test
    void storesEndpointsAndWeight() {
        Edge edge = new Edge(2, 5, -7);
        assertEquals(2, edge.from);
        assertEquals(5, edge.to);
        assertEquals(-7, edge.weight);
    }

    @Test
    void fieldsArePublicFinalIntegers() throws NoSuchFieldException {
        for (String name : new String[]{"from", "to", "weight"}) {
            var field = Edge.class.getField(name);
            assertEquals(int.class, field.getType());
            assertTrue(Modifier.isFinal(field.getModifiers()));
        }
    }
}
