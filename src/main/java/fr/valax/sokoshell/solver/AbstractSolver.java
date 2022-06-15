package fr.valax.sokoshell.solver;

import java.util.*;

/**
 * @author darth-mole
 * @author PoulpoGaz
 */
public abstract class AbstractSolver implements Solver {

    protected boolean[][] reachableCases;

    protected void findAccessibleCases(int playerPos, Map map) {
        for (int i = 0; i < map.getWidth(); i++) {
            for (int j = 0; j < map.getHeight(); j++) {
                reachableCases[j][i] = false;
            }
        }
        findAccessibleCases_aux(map.getX(playerPos), map.getY(playerPos), map);
    }

    protected void findAccessibleCases_aux(int x, int y, Map map) {
        reachableCases[y][x] = true;
        for (Direction d : Direction.values()) {
            int i = x + d.dirX();
            int j = y + d.dirY();
            // the second part of the condition avoids to check already processed cases
            if (map.isTileEmpty(i, j) && !reachableCases[j][i]) {
                findAccessibleCases_aux(i, j, map);
            }
        }
    }

    protected Solution buildSolution(State finalState) {
        List<State> solution = new ArrayList<>();

        State s = finalState;
        while (s.parent() != null)
        {
            solution.add(s);
            s = s.parent();
        }
        solution.add(s);
        Collections.reverse(solution);

        return new Solution(solution);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void stop() {

    }
}
