package fr.valax.sokoshell.solver;

import java.util.*;

/**
 * @author darth-mole
 * @author PoulpoGaz
 */
public abstract class AbstractSolver implements Solver {

    protected boolean[][] reachableCases;
    protected boolean[][] deadTiles;

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

    // http://www.sokobano.de/wiki/index.php?title=How_to_detect_deadlocks
    protected boolean checkFreezeDeadlock(Map map, State state) {
        int[] crates = state.cratesIndices();

        // maybe use CrateIterator to avoid checking twice the same crate
        for (int crate : crates) {
            int x = map.getX(crate);
            int y = map.getY(crate);

            if (checkFreezeDeadlock(map, x, y)) {
                return true;
            }
        }

        return false;
    }


    private boolean checkFreezeDeadlock(Map map, int crateX, int crateY) {
        return checkAxisFreezeDeadlock(map, crateX, crateY, Direction.LEFT) &&
                checkAxisFreezeDeadlock(map, crateX, crateY, Direction.UP);
    }

    private boolean checkAxisFreezeDeadlock(Map map, int crateX, int crateY, Direction axis) {
        boolean deadlock = false;

        int leftX = crateX + axis.dirX();
        int leftY = crateY + axis.dirY();

        int rightX = crateX - axis.dirX();
        int rightY = crateY - axis.dirY();

        Tile left = map.safeGetAt(leftX, leftY);
        Tile right = map.safeGetAt(rightX, rightY);

        // rule 1
        if (left == Tile.WALL || right == Tile.WALL) {
            deadlock = true;

        } else if ((left == null || deadTiles[leftY][leftX]) &&
                (right == null || deadTiles[rightY][leftY])) { // rule 2

            deadlock = true;
        } else { // rule 3

            if (left != null && left.isCrate()) {
                map.setAt(leftX, leftY, Tile.WALL);
                deadlock = checkFreezeDeadlock(map, leftX, leftY);
                map.setAt(leftX, leftY, left);
            }

            if (!deadlock && right != null && right.isCrate()) {
                map.setAt(rightX, rightY, Tile.WALL);
                deadlock = checkFreezeDeadlock(map, rightX, rightY);
                map.setAt(rightX, rightY, right);
            }
        }

        return deadlock;
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
