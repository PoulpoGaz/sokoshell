package fr.valax.sokoshell.solver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author darth-mole
 * @author PoulpoGaz
 */
public abstract class AbstractSolver implements Solver {

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

        return new Solution(params, stats, solution, SolverStatus.SOLUTION_FOUND);
    }

    // http://www.sokobano.de/wiki/index.php?title=How_to_detect_deadlocks
    protected boolean checkFreezeDeadlock(Map map, State state) {
        int[] crates = state.cratesIndices();

        map.getMarkSystem().unmarkAll();
        for (int crate : crates) {
            TileInfo info = map.getAt(crate);

            if (!info.isMarked() && checkFreezeDeadlock(info)) {
                return true;
            }
        }

        return false;
    }


    private boolean checkFreezeDeadlock(TileInfo crate) {
        return checkAxisFreezeDeadlock(crate, Direction.LEFT) &&
                checkAxisFreezeDeadlock(crate, Direction.UP);
    }

    private boolean checkAxisFreezeDeadlock(TileInfo current, Direction axis) {
        boolean deadlock = false;

        TileInfo left = current.safeAdjacent(axis);
        TileInfo right = current.safeAdjacent(axis.negate());

        if ((left != null && left.isWall()) || (right != null && right.isWall())) { // rule 1
            deadlock = true;

        } else if ((left == null || left.isDeadTile()) &&
                (right == null || right.isDeadTile())) { // rule 2

            deadlock = true;
        } else { // rule 3
            Tile oldCurr = current.getTile();
            current.setTile(Tile.WALL);

            if (left != null && left.isCrate()) {
                left.mark();
                deadlock = checkFreezeDeadlock(left);
            }

            if (!deadlock && right != null && right.isCrate()) {
                right.mark();
                deadlock = checkFreezeDeadlock(right);
            }

            current.setTile(oldCurr);
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
