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
            if (t.isSolid() || !t.isTarget()) {
                continue;
            }
            computeDistancesFrom(i, 0);
        }
    }

    private void computeDistancesFrom(int index, int prevValue) {

        if (prevValue >= distances[index]) {
            return;
        }
        distances[index] = prevValue;

        for (Direction d : Direction.VALUES) {
            final int nextX = map.getX(index) + d.dirX();
            final int nextY = map.getY(index) + d.dirY();
            final int nextIndex = map.getIndex(nextX, nextY);
            if (nextIndex < 0 || nextIndex >= distances.length) {
                continue;
            }

            computeDistancesFrom(nextIndex, prevValue + 1);
        }
    }

    public int compute(State s) {
        return 0;
    }

    /**
     * @return The distances a displayed as a grid (test purpose only).
     */
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
