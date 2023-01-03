package fr.valax.sokoshell.solver.heuristic;

import fr.valax.sokoshell.solver.Direction;
import fr.valax.sokoshell.solver.Map;
import fr.valax.sokoshell.solver.State;
import fr.valax.sokoshell.solver.TileInfo;

import java.util.Arrays;

/**
 * According to <a href="http://sokobano.de/wiki/index.php?title=Solver#Simple_Lower_Bound">this article</a>
 */
public class SimpleHeuristic extends AbstractHeuristic {

    /**
     * The distances from each tile to the nearest goal. Pre-calculated in the constructor.
     */
    private final int[] distances;

    public SimpleHeuristic(Map map) {
        super(map);

        distances = new int[map.getHeight() * map.getWidth()];
        for (int i = 0; i < distances.length; i++) {
            final TileInfo t = map.getAt(i);
            if (t.isTarget()) {
                distances[i] = 0;
            } else {
                int minDistToTarget = Integer.MAX_VALUE;
                for (int j = 0; j < map.getTargetIndices().length; j++) {
                    final int d = t.manhattanDistance(map.getAt(map.getTargetIndices()[j]));
                    if (d < minDistToTarget) {
                        minDistToTarget = d;
                    }
                }
                distances[i] = minDistToTarget;
            }
        }
    }

    /**
     * Sums the distances to the nearest goal of each of the crates of the state.
     */
    public int compute(State s) {
        int h = 0;
        for (int i = 0; i < s.cratesIndices().length; i++) {
            h += distances[s.cratesIndices()[i]];
        }
        return h;
    }

    /**
     * @return The distances displayed as a grid (test purpose only).
     */
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < distances.length; i++) {
            s.append(distances[i]);
            if ((i + 1) % map.getWidth() == 0) {
                s.append('\n');
            }
        }
        return s.toString();
    }

}
