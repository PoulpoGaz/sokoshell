package fr.valax.sokoshell.solver.heuristic;

import fr.valax.sokoshell.solver.Board;
import fr.valax.sokoshell.solver.State;

/**
 * According to <a href="http://sokobano.de/wiki/index.php?title=Solver#Simple_Lower_Bound">this article</a>
 */
public class SimpleHeuristic extends AbstractHeuristic {

    public SimpleHeuristic(Board board) {
        super(board);
    }

    /**
     * Sums the distances to the nearest goal of each of the crates of the state.
     */
    public int compute(State s) {
        int h = 0;
        for (int i : s.cratesIndices()) {
            h += board.getAt(i).getNearestTarget().distance();
        }
        return h;
    }
}
