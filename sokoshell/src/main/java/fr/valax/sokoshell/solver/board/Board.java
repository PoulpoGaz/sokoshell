package fr.valax.sokoshell.solver.board;

import fr.valax.sokoshell.solver.board.tiles.Tile;
import fr.valax.sokoshell.solver.board.tiles.TileInfo;

public class Board extends GenericBoard<TileInfo> {

    public Board(Tile[][] content, int width, int height) {
        super(width, height);

        this.content = new TileInfo[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                this.content[y][x] = new TileInfo(this, content[y][x], x, y);
            }
        }
    }

    public Board(Board other) {
        super(other.getWidth(), other.getHeight());

        this.content = new TileInfo[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                this.content[y][x] = new TileInfo(other.getAt(x, y));
            }
        }
    }

}
