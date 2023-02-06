package fr.valax.sokoshell.solver;

import fr.valax.sokoshell.solver.board.Board;
import fr.valax.sokoshell.solver.board.Direction;
import fr.valax.sokoshell.solver.board.tiles.Tile;
import fr.valax.sokoshell.solver.board.tiles.TileInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * May be removed in the future
 * @author darth-mole
 * @author PoulpoGaz
 */
public abstract class AbstractSolver implements Solver {

    protected final String name;

    AbstractSolver(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    // http://www.sokobano.de/wiki/index.php?title=How_to_detect_deadlocks
    protected boolean checkFreezeDeadlock(Board board, State state) {
        int[] crates = state.cratesIndices();

        for (int crate : crates) {
            TileInfo info = board.getAt(crate);

            if (info.isCrate() && checkFreezeDeadlock(info)) {
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

            if (left != null && left.anyCrate()) {
                deadlock = checkFreezeDeadlock(left);
            }

            if (!deadlock && right != null && right.anyCrate()) {
                deadlock = checkFreezeDeadlock(right);
            }

            current.setTile(oldCurr);
        }

        // ultimate check, the crate is frozen if it is only a crate and not a crate on target
        return deadlock;
    }

    @Override
    public List<SolverParameter> getParameters() {
        List<SolverParameter> params = new ArrayList<>();
        addParameters(params);
        return params;
    }

    /**
     * Add your parameters to the list returned by {@link #getParameters()}
     * @param parameters parameters that will be returned by {@link #getParameters()}
     */
    protected void addParameters(List<SolverParameter> parameters) {

    }
}
