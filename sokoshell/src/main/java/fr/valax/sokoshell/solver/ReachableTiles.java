package fr.valax.sokoshell.solver;

import fr.valax.sokoshell.solver.board.Board;
import fr.valax.sokoshell.solver.board.Direction;
import fr.valax.sokoshell.solver.board.mark.FixedSizeMarkSystem;
import fr.valax.sokoshell.solver.board.tiles.TileInfo;

public class ReachableTiles {

    protected final FixedSizeMarkSystem reachable;

    public ReachableTiles(Board board) {
        reachable = new FixedSizeMarkSystem(board.getWidth() * board.getHeight());
    }

    public boolean isReachable(TileInfo tile) {
        return reachable.isMarked(tile.getIndex());
    }

    public void findReachableCases(TileInfo origin) {
        reachable.unmarkAll();
        findReachableCases_aux(origin);
    }

    private void findReachableCases_aux(TileInfo tile) {
        reachable.mark(tile.getIndex());
        for (Direction d : Direction.VALUES) {
            TileInfo adjacent = tile.adjacent(d);

            // the second part of the condition avoids to check already processed cases
            if (!adjacent.isSolid() && !isReachable(adjacent)) {
                findReachableCases_aux(adjacent);
            }
        }
    }
}
