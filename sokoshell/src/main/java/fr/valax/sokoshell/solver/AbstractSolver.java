package fr.valax.sokoshell.solver;

import fr.valax.sokoshell.solver.board.Direction;
import fr.valax.sokoshell.solver.board.MutableBoard;
import fr.valax.sokoshell.solver.board.tiles.MutableTileInfo;
import fr.valax.sokoshell.solver.board.tiles.Tile;

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
    protected boolean checkFreezeDeadlock(MutableBoard board, State state) {
        int[] crates = state.cratesIndices();

        for (int crate : crates) {
            MutableTileInfo info = board.getAt(crate);

            if (info.isCrate() && checkFreezeDeadlock(info)) {
                return true;
            }
        }

        return false;
    }


    private boolean checkFreezeDeadlock(MutableTileInfo crate) {
        return checkAxisFreezeDeadlock(crate, Direction.LEFT) &&
                checkAxisFreezeDeadlock(crate, Direction.UP);
    }

    private boolean checkAxisFreezeDeadlock(MutableTileInfo current, Direction axis) {
        boolean deadlock = false;

        MutableTileInfo left = current.safeAdjacent(axis);
        MutableTileInfo right = current.safeAdjacent(axis.negate());

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
}
