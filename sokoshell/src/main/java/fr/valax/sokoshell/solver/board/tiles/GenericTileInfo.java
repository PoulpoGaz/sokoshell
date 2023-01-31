package fr.valax.sokoshell.solver.board.tiles;

import fr.valax.sokoshell.commands.AbstractCommand;
import fr.valax.sokoshell.solver.board.Board;
import fr.valax.sokoshell.solver.board.Direction;

import java.util.AbstractList;

/**
 * A {@code package-private} class meant to be use as a base class for {@link TileInfo} implementations.
 * It defines all the basic properties and their corresponding getters
 * (position, tile, board, etc.)
 *
 * @see TileInfo
 */
public abstract class GenericTileInfo implements TileInfo {

    protected final Board board;

    protected final int x;
    protected final int y;

    protected Tile tile;


    /**
     * Create a new TileInfo
     *
     * @param tile the tile
     * @param x the position on the x-axis in the board
     * @param y the position on the y-axis in the board
     */
    public GenericTileInfo(Board board, Tile tile, int x, int y) {
        this.board = board;
        this.tile = tile;
        this.x = x;
        this.y = y;
    }

    public GenericTileInfo(TileInfo tileInfo) {
        this(tileInfo.getBoard(), tileInfo.getTile(), tileInfo.getX(), tileInfo.getY());
    }

    public GenericTileInfo(Board board, TileInfo tileInfo) {
        this(board, tileInfo.getTile(), tileInfo.getX(), tileInfo.getY());
    }

    @Override
    public Tile getTile() {
        return tile;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    /**
     * Returns the board in which this tile is
     *
     * @return the board in which this tile is
     */
    public Board getBoard() {
        return board;
    }
}
