package fr.valax.tools;

import fr.valax.sokoshell.solver.board.Direction;
import fr.valax.sokoshell.solver.board.tiles.Tile;

public record SokToPNGTile(Tile tile, boolean player, Direction direction) {

    public SokToPNGTile(Tile tile) {
        this(tile, false, null);
    }
}
