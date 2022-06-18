package fr.valax.sokoshell.solver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author darth-mole
 * @author PoulpoGaz
 */
public abstract class AbstractSolver implements Solver {

    protected boolean[][] reachableCases;
    protected boolean[][] deadPositions;

    /**
     * Detects the dead positions of a level. Dead positions are cases that make the level unsolvable
     * when a crate is put on them.
     * After this function has been called, to check if a given crate at (x,y) is a dead position,
     * you can use {@link AbstractSolver#deadPositions}[y][x] to check in constant time.
     * @param map The map. It MUST have NO CRATES for this function to work.
     */
    protected void findDeadPositions(Map map) {
        deadPositions = new boolean[map.getHeight()][map.getWidth()];
        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                final Tile tile = map.getAt(x, y);
                deadPositions[y][x] = !tile.isCrate();
                if (map.getAt(x, y) != Tile.TARGET) {
                    continue;
                }
                Map mutMap = new Map(map);
                mutMap.setAt(x, y, Tile.CRATE_ON_TARGET);
                findNonDeadCases(x, y, mutMap);
                mutMap.setAt(x, y, Tile.TARGET);
            }
        }
    }

    /**
     * Discovers all the reachable cases from (x, y) to find dead positions, as described
     * <a href="www.sokobano.de/wiki/index.php?title=How_to_detect_deadlocks#Detecting_simple_deadlocks">here</a>
     * @param x x-coordinate
     * @param y y-coordinate
     * @param map the map
     */
    protected void findNonDeadCases(int x, int y, Map map) {
        deadPositions[y][x] = false;
        for (Direction d : Direction.values()) {
            final int nextX = x + d.dirX();
            final int nextY = y + d.dirY();

            // the last part of the condition avoids to check already processed cases
            if (map.isTileEmpty(nextX, nextY) && map.isTileEmpty(nextX + d.dirX(), nextY + d.dirY()) && deadPositions[nextY][nextX]) {
                /*map.setAt(x, y,
                    switch (map.getAt(x, y)) {
                        case CRATE_ON_TARGET -> Tile.TARGET;
                        default -> Tile.FLOOR;
                });*/

                map.setAt(nextX, nextY,
                        switch (map.getAt(nextX, nextY)) {
                            case TARGET -> Tile.CRATE_ON_TARGET;
                            default -> Tile.CRATE;
                });

                findNonDeadCases(nextX, nextY, map);

                /*map.setAt(nextX, nextY,
                        switch (map.getAt(nextX, nextY)) {
                            case CRATE_ON_TARGET -> Tile.TARGET;
                            default -> Tile.FLOOR;
                  -      });*/
            }
        }
    }

    // <TEST> //

    public boolean[][] computeDeadPositions(Map map) {
        findDeadPositions(map);
        return deadPositions;
    }

    // </TEST>

    protected void findReachableCases(int playerPos, Map map) {
        for (int i = 0; i < map.getWidth(); i++) {
            for (int j = 0; j < map.getHeight(); j++) {
                reachableCases[j][i] = false;
            }
        }
        findReachableCases_aux(map.getX(playerPos), map.getY(playerPos), map);
    }

    protected void findReachableCases_aux(int x, int y, Map map) {
        reachableCases[y][x] = true;
        for (Direction d : Direction.values()) {
            int i = x + d.dirX();
            int j = y + d.dirY();
            // the second part of the condition avoids to check already processed cases
            if (map.isTileEmpty(i, j) && !reachableCases[j][i]) {
                findReachableCases_aux(i, j, map);
            }
        }
    }

    protected Solution buildSolution(State finalState, SolverParameters params, SolverStatistics stats) {
        List<State> solution = new ArrayList<>();

        State s = finalState;
        while (s.parent() != null)
        {
            solution.add(s);
            s = s.parent();
        }
        solution.add(s);
        Collections.reverse(solution);

        return new Solution(getSolverType(), params, stats, solution, SolverStatus.SOLUTION_FOUND);
    }

    protected Solution create(SolverParameters params, SolverStatistics stats, SolverStatus status) {
        return new Solution(getSolverType(), params, stats, null, status);
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

        } else if ((left == null || deadPositions[leftY][leftX]) &&
                (right == null || deadPositions[rightY][leftY])) { // rule 2

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

    protected long getTimeout(SolverParameters params) {
        Object timeout = params.get(SolverParameters.TIMEOUT);

        if (timeout instanceof Long l) {
            return l;
        } else {
            return -1;
        }
    }
}
