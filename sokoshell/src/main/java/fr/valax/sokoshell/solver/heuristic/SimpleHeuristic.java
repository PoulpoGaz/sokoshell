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
        Arrays.fill(distances, Integer.MAX_VALUE);

        for (int i = 0; i < distances.length; i++) {
            final TileInfo t = map.getAt(i);
            if (!t.isTarget()) {
                continue;
            }
            computeDistancesFrom(i, 0);
        }
    }

    /**
     * Computes the distance from the given tile to the nearest goal.
     * To do this, we perform a simple BFS from each target tile, updating the distance only when we find a lower one.
     */
    private void computeDistancesFrom(int index, int prevDistance) {

        distances[index] = prevDistance;

        for (Direction d : Direction.VALUES) {

            final int nextX = map.getX(index) + d.dirX();
            final int nextY = map.getY(index) + d.dirY();
            final int nextIndex = map.getIndex(nextX, nextY);  // A bit ugly, feel free to improve it

            if (nextIndex < 0 || nextIndex >= distances.length) {
                continue;
            }

            if (prevDistance + 1 >= distances[nextIndex]) {
                continue;
            }

            computeDistancesFrom(nextIndex, prevDistance + 1);
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
