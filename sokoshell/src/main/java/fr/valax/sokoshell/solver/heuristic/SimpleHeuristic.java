package fr.valax.sokoshell.solver.heuristic;

import fr.valax.sokoshell.solver.Map;
import fr.valax.sokoshell.solver.State;

/**
 * According to <a href="http://sokobano.de/wiki/index.php?title=Solver#Simple_Lower_Bound">this article</a>
 */
public class SimpleHeuristic extends AbstractHeuristic {

    public SimpleHeuristic(Map map) {
        super(map);
    }

    /**
     * Sums the distances to the nearest goal of each of the crates of the state.
     */
    public int compute(State s) {
        int h = 0;
        for (int i = 0; i < s.cratesIndices().length; i++) {
            h += map.getAt(s.cratesIndices()[i]).getNearestTarget().getDistance();
        }
        return h;
    }
}
