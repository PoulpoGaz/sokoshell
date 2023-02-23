package fr.valax.sokoshell.solver;

import fr.valax.sokoshell.solver.board.Board;
import fr.valax.sokoshell.solver.board.Direction;
import fr.valax.sokoshell.solver.board.tiles.Tile;
import fr.valax.sokoshell.solver.board.tiles.TileInfo;

public class FreezeDeadlockDetector {

    // http://www.sokobano.de/wiki/index.php?title=How_to_detect_deadlocks
    public static boolean checkFreezeDeadlock(Board board, State state) {
        int[] crates = state.cratesIndices();

        for (int crate : crates) {
            TileInfo info = board.getAt(crate);

            if (checkFreezeDeadlock(info)) {
                return true;
            }
        }

        return false;
    }

    public static boolean checkFreezeDeadlock(TileInfo crate) {
        return crate.isCrate() &&
                checkFreezeDeadlockRec(crate, Direction.LEFT) &&
                checkFreezeDeadlockRec(crate, Direction.UP);
    }

    private static boolean checkFreezeDeadlockRec(TileInfo crate) {
        return checkFreezeDeadlockRec(crate, Direction.LEFT) &&
                checkFreezeDeadlockRec(crate, Direction.UP);
    }

    private static boolean checkFreezeDeadlockRec(TileInfo current, Direction axis) {
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
                deadlock = checkFreezeDeadlockRec(left);
            }

            if (!deadlock && right != null && right.anyCrate()) {
                deadlock = checkFreezeDeadlockRec(right);
            }

            current.setTile(oldCurr);
        }

        return deadlock;
    }
}
