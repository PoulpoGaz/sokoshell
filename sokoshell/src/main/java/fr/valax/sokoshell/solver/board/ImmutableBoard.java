package fr.valax.sokoshell.solver.board;

import fr.valax.sokoshell.solver.board.mark.MarkSystem;
import fr.valax.sokoshell.solver.board.tiles.ImmutableTileInfo;
import fr.valax.sokoshell.solver.board.tiles.Tile;
import fr.valax.sokoshell.solver.board.tiles.TileInfo;

import java.util.List;

/**
 * Immutable implementation of {@link Board}.
 *
 * This class extends {@link GenericBoard}. It internally uses {@link ImmutableTileInfo} to store the board content
 * in {@link GenericBoard#content}. As it is immutable, it implements the setters methods always throws a
 * {@link UnsupportedOperationException} when such a method is called.
 *
 * @see Board
 * @see GenericBoard
 * @see TileInfo
 */
public class ImmutableBoard extends GenericBoard {

    public ImmutableBoard(Tile[][] content, int width, int height) {
        super(width, height);

        this.content = new ImmutableTileInfo[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                this.content[y][x] = new ImmutableTileInfo(this, content[y][x], x, y);
            }
        }
    }

    public ImmutableBoard(Board other) {
        super(other.getWidth(), other.getHeight());

        this.content = new ImmutableTileInfo[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                this.content[y][x] = new ImmutableTileInfo(other.getAt(x, y));
            }
        }
    }

    // GETTERS //

    @Override
    public int getTargetCount() {
        return 0;
    }

    @Override
    public List<Tunnel> getTunnels() {
        return null;
    }

    @Override
    public List<Room> getRooms() {
        return null;
    }

    @Override
    public boolean isGoalRoomLevel() {
        return false;
    }

    @Override
    public MarkSystem getMarkSystem() {
        return null;
    }

    @Override
    public MarkSystem getReachableMarkSystem() {
        return null;
    }
}
