package fr.valax.sokoshell.solver.board;

import fr.valax.sokoshell.solver.State;
import fr.valax.sokoshell.solver.board.tiles.GenericTileInfo;

/**
 *
 * @param <T>
 */
public interface IBoard<T extends GenericTileInfo<T, ?>> {

    int MINIMUM_WIDTH = 5;
    int MINIMUM_HEIGHT = 5;

    /**
     * Returns the width of the board
     *
     * @return the width of the board
     */
    int getWidth();

    /**
     * Returns the height of the board
     *
     * @return the height of the board
     */
    int getHeight();

    /**
     * Convert an index to a position on the y-axis
     *
     * @param index the index to convert
     * @return the converted position
     */
    int getY(int index);

    /**
     * Convert an index to a position on the x-axis
     *
     * @param index the index to convert
     * @return the converted position
     */
    int getX(int index);

    /**
     * Convert a (x;y) position to an index
     *
     * @param x Coordinate on x-axis
     * @param y Coordinate on y-axis
     * @return the converted index
     */
    int getIndex(int x, int y);

    /**
     * Returns the {@link T} at the specific index
     *
     * @param index the index of the {@link T}
     * @return the TileInfo at the specific index
     * @throws IndexOutOfBoundsException if the index lead to a position outside the board
     * @see #getX(int)
     * @see #getY(int)
     * @see #safeGetAt(int)
     */
    T getAt(int index);

    /**
     * Returns the {@link T} at the specific index
     *
     * @param index the index of the {@link T}
     * @return the TileInfo at the specific index or {@code null}
     * if the index represent a position outside the board
     * @see #getX(int)
     * @see #getY(int)
     */
    T safeGetAt(int index);

    /**
     * Returns the {@link T} at the specific position
     *
     * @param x x the of the tile
     * @param y y the of the tile
     * @return the TileInfo at the specific coordinate
     * @throws IndexOutOfBoundsException if the position is outside the board
     * @see #safeGetAt(int, int)
     */
    T getAt(int x, int y);

    /**
     * Returns the {@link T} at the specific position
     *
     * @param x x the of the tile
     * @param y y the of the tile
     * @return the TileInfo at the specific index or {@code null}
     * if the index represent a position outside the board
     * @see #getX(int)
     * @see #getY(int)
     */
    T safeGetAt(int x, int y);

    /**
     * Tells whether the case at (x,y) exists or not (i.e. if the case is in the board)
     *
     * @param x x-coordinate
     * @param y y-coordinate
     * @return {@code true} if the case exists, {@code false} otherwise
     */
    boolean caseExists(int x, int y);

    /**
     * Same than caseExists(x, y) but with an index
     *
     * @param index index of the case
     * @return {@code true} if the case exists, {@code false} otherwise
     * @see #caseExists(int, int)
     */
    boolean caseExists(int index);

    /**
     * Tells whether the tile at the given coordinates is empty or not.
     *
     * @param x x coordinate of the case
     * @param y y coordinate of the case
     * @return {@code true} if empty, {@code false} otherwise
     */
    boolean isTileEmpty(int x, int y);

    /**
     * Checks if the board is solved (i.e. all the crates are on a target).<br />
     * <strong>The crates MUST have been put on the board for this function to work as expected.</strong>
     *
     * @return {@code true} if the board is completed, false otherwise
     */
    boolean isCompletedWith(State s);

    /**
     * Checks if the board is completed (i.e. all the crates are on a target)
     *
     * @return true if completed, false otherwise
     */
    boolean isCompleted();
}
