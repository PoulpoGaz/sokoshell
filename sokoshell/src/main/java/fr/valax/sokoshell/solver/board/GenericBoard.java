package fr.valax.sokoshell.solver.board;

import fr.valax.sokoshell.solver.board.tiles.TileInfo;

/**
 * A {@code package-private} class meant to be use as a base class for {@link Board} implementations.
 * It defines all read-only methods, as well as a way to store the tiles. It is essentially a 2D-array of
 * {@link TileInfo}, the indices being the y and x coordinates (i.e. {@code content[y][x]} is the tile at (x;y)).
 *
 * @see Board
 * @see TileInfo
 */
abstract class GenericBoard implements Board {

    protected final int width;

    protected final int height;

    protected TileInfo[][] content;

    public GenericBoard(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @SuppressWarnings("CopyConstructorMissesField")
    public GenericBoard(Board other) {
        this(other.getWidth(), other.getHeight());
    }

    @Override
    public int getWidth() { return width; }

    @Override
    public int getHeight() { return height; }

    @Override
    public int getY(int index) { return index / width; }

    @Override
    public int getX(int index) { return index % width; }

    @Override
    public int getIndex(int x, int y) { return y * width + x; }

    @Override
    public TileInfo getAt(int index) {
        return content[getY(index)][getX(index)];
    }

    @Override
    public TileInfo getAt(int x, int y) {
        return content[y][x];
    }
}
