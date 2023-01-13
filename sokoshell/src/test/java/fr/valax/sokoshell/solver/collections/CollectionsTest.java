package fr.valax.sokoshell.solver.collections;

import fr.valax.sokoshell.solver.State;
import fr.valax.sokoshell.solver.WeightedState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CollectionsTest {

    @Test
    void minHeapTest() {
        MinHeap<Integer> h = new MinHeap<>(5);

        assertEquals(0, h.size());
        assertTrue(h.isEmpty());

        State.initZobristValues(10);

        h.add(0, 0);
        h.add(3, 3);
        h.add(9, 9);
        h.add(2, 2);
        h.add(1, 1);

        assertEquals(5, h.size());
        assertFalse(h.isEmpty());

        int s = h.peek();
        assertEquals(0, s);

        assertEquals(5, h.size());
        assertFalse(h.isEmpty());

        s = h.pop();
        assertEquals(0, s);

        assertEquals(4, h.size());
        assertFalse(h.isEmpty());

        s = h.pop();
        assertEquals(1, s);

        assertEquals(3, h.size());
        assertFalse(h.isEmpty());

        s = h.pop();
        assertEquals(2, s);

        assertEquals(2, h.size());
        assertFalse(h.isEmpty());

        h.add(2, 2);

        assertEquals(3, h.size());
        assertFalse(h.isEmpty());

        s = h.pop();
        assertEquals(2, s);

        assertEquals(2, h.size());
        assertFalse(h.isEmpty());

        s = h.pop();
        assertEquals(3, s);

        assertEquals(1, h.size());
        assertFalse(h.isEmpty());

        s = h.pop();
        assertEquals(9, s);
    }

    @Test
    void solverPriorityQueueTest() {
        SolverPriorityQueue q = new SolverPriorityQueue();

        assertEquals(0, q.size());
        assertTrue(q.isEmpty());

        State.initZobristValues(10);

        q.addState(new WeightedState(0, new int[1], 0, null, 0, 0));
        q.addState(new WeightedState(3, new int[1], 0, null, 0, 3));
        q.addState(new WeightedState(9, new int[1], 0, null, 0, 9));
        q.addState(new WeightedState(2, new int[1], 0, null, 0, 2));
        q.addState(new WeightedState(1, new int[1], 0, null, 0, 1));

        assertEquals(5, q.size());
        assertFalse(q.isEmpty());

        State s = q.peekState();
        assertEquals(0, s.playerPos());

        assertEquals(5, q.size());
        assertFalse(q.isEmpty());

        s = q.popState();
        assertEquals(0, s.playerPos());

        assertEquals(4, q.size());
        assertFalse(q.isEmpty());

        s = q.popState();
        assertEquals(1, s.playerPos());

        assertEquals(3, q.size());
        assertFalse(q.isEmpty());

        s = q.popState();
        assertEquals(2, s.playerPos());

        assertEquals(2, q.size());
        assertFalse(q.isEmpty());

        q.addState(new WeightedState(2, new int[1], 0,null, 0, 2));

        assertEquals(3, q.size());
        assertFalse(q.isEmpty());

        s = q.popState();
        assertEquals(2, s.playerPos());

        assertEquals(2, q.size());
        assertFalse(q.isEmpty());

        s = q.popState();
        assertEquals(3, s.playerPos());

        assertEquals(1, q.size());
        assertFalse(q.isEmpty());

        s = q.popState();
        assertEquals(9, s.playerPos());
    }
}
