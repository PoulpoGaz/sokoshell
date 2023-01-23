package fr.valax.sokoshell.solver.pathfinder;

import fr.valax.sokoshell.solver.board.MutableBoard;
import fr.valax.sokoshell.solver.board.tiles.MutableTileInfo;

/**
 * Find the shortest path between (player start, crate start) and (player dest, crate dest):
 * the player moves a crate from 'crate start' to 'crate dest' and then moves to 'player dest'.
 */
public class CratePlayerAStar extends CrateAStar {

    public CratePlayerAStar(MutableBoard board) {
        super(board);
    }

    @Override
    protected boolean isEndNode(Node node) {
        return node.getPlayer().isAt(playerDest) && node.getCrate().isAt(crateDest);
    }

    @Override
    protected int heuristic(MutableTileInfo newPlayer, MutableTileInfo newCrate) {
         /*
            Try to first move the player near the crate
            Then push the crate to his destination
            Finally moves the player to his destination
         */
        int remaining = newCrate.manhattanDistance(crateDest);
        if (remaining == 0) {
            remaining = newPlayer.manhattanDistance(playerDest);
        } else {
            if (newPlayer.manhattanDistance(newCrate) > 1) {
                remaining += newPlayer.manhattanDistance(newCrate);
            }

            remaining += crateDest.manhattanDistance(playerDest);
        }

        return remaining;
    }
}
