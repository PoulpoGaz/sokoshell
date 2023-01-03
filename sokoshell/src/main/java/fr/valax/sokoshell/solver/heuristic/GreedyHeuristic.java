package fr.valax.sokoshell.solver.heuristic;

import fr.valax.sokoshell.solver.Direction;
import fr.valax.sokoshell.solver.Map;
import fr.valax.sokoshell.solver.State;
import fr.valax.sokoshell.solver.TileInfo;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

/**
 * According to <a href="http://sokobano.de/wiki/index.php?title=Solver#Greedy_approach">this article</a>
 */
public class GreedyHeuristic extends AbstractHeuristic {

    private TileData[] tileData;

    public GreedyHeuristic(Map map) {
        super(map);

        tileData = new TileData[map.getHeight() * map.getWidth()];
        for (int i = 0; i < tileData.length; i++) {
            tileData[i] = new TileData(map.getTargetIndices().length);
        }

        for (int i = 0; i < map.getHeight() * map.getWidth(); i++) {
            final TileInfo t = map.getAt(i);
            if (!t.isTarget()) {
                continue;
            }
            computeDistancesTo(i, 0, i);
        }
    }

    private void computeDistancesTo(int index, int prevDistance, int target) {

        tileData[index].setDistanceTo(target, prevDistance);

        for (Direction d : Direction.VALUES) {

            final int nextX = map.getX(index) + d.dirX();
            final int nextY = map.getY(index) + d.dirY();
            final int nextIndex = map.getIndex(nextX, nextY);  // A bit ugly, feel free to improve it

            if (nextIndex < 0 || nextIndex >= tileData.length) {
                continue;
            }

            if (prevDistance + 1 >= tileData[nextIndex].distanceTo(target)) {
                continue;
            }

            computeDistancesTo(nextIndex, prevDistance + 1, target);
        }
    }

    @Override
    public int compute(State s) {
        return 0;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();

        for (int target : tileData[0].targets()) {
            s.append(target).append("\n");
            for (int i = 0; i < tileData.length; i++) {
                s.append(tileData[i].distanceTo(target));
                s.append("|");
                if ((i + 1) % map.getWidth() == 0) {
                    s.append('\n');
                }
            }
            s.append("\n");
        }

        return s.toString();
    }

    private static class TileData {

        private final HashMap<Integer, Integer> targetDistances;

        public TileData(int targetCount) {
            targetDistances = new HashMap<>(targetCount);
        }

        public void setDistanceTo(int targetIndex, int distance) {
            targetDistances.put(targetIndex, distance);
        }

        public int distanceTo(int targetIndex) {
            return targetDistances.getOrDefault(targetIndex, Integer.MAX_VALUE);
        }

        public Set<Integer> targets() {
            return targetDistances.keySet();
        }
    }
}
