package fr.valax.sokoshell.solver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SolverPriorityQueueTest {

    @Test
    void test() {
        SolverPriorityQueue q = new SolverPriorityQueue();

        assert(q.size() == 0);
        assert(q.isEmpty());

        State.initZobristValues(10);

        q.addState(new State(0, new int[1], null), 0);
        q.addState(new State(3, new int[1], null), 3);
        q.addState(new State(9, new int[1], null), 9);
        q.addState(new State(2, new int[1], null), 2);
        q.addState(new State(1, new int[1], null), 1);


        assert(q.size() == 5);
        assert(!q.isEmpty());

        State s = q.topState();
        assert(s.playerPos() == 0);

        assert(q.size() == 5);
        assert(!q.isEmpty());

        s = q.popState();
        assert(s.playerPos() == 0);

        assert(q.size() == 4);
        assert(!q.isEmpty());

        s = q.popState();
        assert(s.playerPos() == 1);

        assert(q.size() == 3);
        assert(!q.isEmpty());

        s = q.popState();
        assert(s.playerPos() == 2);

        assert(q.size() == 2);
        assert(!q.isEmpty());

        q.addState(new State(2, new int[1], null), 2);

        assert(q.size() == 3);
        assert(!q.isEmpty());

        s = q.popState();
        assert(s.playerPos() == 2);

        assert(q.size() == 2);
        assert(!q.isEmpty());

        s = q.popState();
        assert(s.playerPos() == 3);

        assert(q.size() == 1);
        assert(!q.isEmpty());

        s = q.popState();
        assert(s.playerPos() == 9);
    }
}
