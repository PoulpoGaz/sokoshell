package fr.valax.sokoshell.solver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author darth-mole
 * @author PoulpoGaz
 */
public abstract class AbstractSolver implements Solver {

    protected final CrateIterator it = new CrateIterator();

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

        it.setCrates(crates);

        // maybe use CrateIterator to avoid checking twice the same crate
        while (it.hasNext()) {
            int crate = it.next();

            int x = map.getX(crate);
            int y = map.getY(crate);

            if (checkFreezeDeadlock(it, map, x, y)) {
                return true;
            }
        }

        return false;
    }


    private boolean checkFreezeDeadlock(CrateIterator it, Map map, int crateX, int crateY) {
        return checkAxisFreezeDeadlock(it, map, crateX, crateY, Direction.LEFT) &&
                checkAxisFreezeDeadlock(it, map, crateX, crateY, Direction.UP);
    }

    private boolean checkAxisFreezeDeadlock(CrateIterator it, Map map, int crateX, int crateY, Direction axis) {
        boolean deadlock = false;

        int leftX = crateX + axis.dirX();
        int leftY = crateY + axis.dirY();

        int rightX = crateX - axis.dirX();
        int rightY = crateY - axis.dirY();

        TileInfo current = map.getAt(crateX, crateY);

        TileInfo left = map.safeGetAt(leftX, leftY);
        TileInfo right = map.safeGetAt(rightX, rightY);

        if ((left != null && left.isWall()) || (right != null && right.isWall())) { // rule 1
            deadlock = true;

        } else if ((left == null || left.isDeadTile()) &&
                (right == null || right.isDeadTile())) { // rule 2

            deadlock = true;
        } else { // rule 3
            Tile oldCurr = current.getTile();
            map.setAt(crateX, crateY, Tile.WALL);

            if (left != null && left.isCrate()) {
                it.skipCrate(left);
                deadlock = checkFreezeDeadlock(it, map, leftX, leftY);
            }

            if (!deadlock && right != null && right.isCrate()) {
                it.skipCrate(right);
                deadlock = checkFreezeDeadlock(it, map, rightX, rightY);
            }

            map.setAt(crateX, crateY, oldCurr);
        }

        // ultimate check, the crate is frozen if it is only a crate and not a crate on target
        return deadlock && current.isCrate();
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
