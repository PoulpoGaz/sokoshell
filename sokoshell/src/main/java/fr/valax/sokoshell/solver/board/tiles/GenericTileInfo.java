package fr.valax.sokoshell.solver.board.tiles;

import fr.valax.sokoshell.solver.board.*;
import fr.valax.sokoshell.solver.board.mark.Mark;

/**
 * {@link TileInfo} stores <i>static</i> information about a tile:
 * <ul>
 *     <li>the position</li>
 *     <li>the {@link Tile}</li>
 * </ul>
 *
 * These properties are immutable. See {@link MutableTileInfo} if you want to modify a {@link TileInfo}.
 *
 * @author PoulpoGaz
 */

/**
 * SolverTileInfo stores information about a Tile that are relevant for solving:
 * <ul>
 *     <li>
 *         Static information
 *         <ul>
 *             <li>the {@link SolverBoard}</li>
 *             <li>{@link MutableTileInfo} information</li>
 *         </ul>
 *     </li>
 *     <li>
 *         Dynamic information
 *         <ul>
 *             <li>if the tile is reachable</li>
 *             <li>a mark</li>
 *         </ul>
 *     </li>
 * </ul>
 *
 * @author PoulpoGaz
 */
public abstract class GenericTileInfo<T extends GenericTileInfo<T, B>, B extends GenericBoard<T>> implements ITileInfo<T, B> {

    protected final B board;

    protected final int x;
    protected final int y;

    protected Tile tile;


    /**
     * Create a new TileInfo
     *
     * @param tile the tile
     * @param x the position on the x-axis in the map
     * @param y the position on the y-axis in the map
     */
    public GenericTileInfo(B board, Tile tile, int x, int y) {
        this.board = board;
        this.tile = tile;
        this.x = x;
        this.y = y;
    }

    public GenericTileInfo(GenericTileInfo<T, B> tileInfo) {
        this(tileInfo.board, tileInfo.tile, tileInfo.x, tileInfo.y);
    }

    public GenericTileInfo(B board, GenericTileInfo<T, B> tileInfo) {
        this(board, tileInfo.tile, tileInfo.x, tileInfo.y);
    }
    
   @Override
    public boolean anyCrate() {
        return tile.isCrate();
    }

    @Override
    public boolean isSolid() {
        return tile.isSolid();
    }

    @Override
    public boolean isFloor() {
        return tile == Tile.FLOOR;
    }

    @Override
    public boolean isWall() {
        return tile == Tile.WALL;
    }

    @Override
    public boolean isTarget() {
        return tile == Tile.TARGET;
    }

    @Override
    public boolean isCrate() {
        return tile == Tile.CRATE;
    }

    @Override
    public boolean isCrateOnTarget() {
        return tile == Tile.CRATE_ON_TARGET;
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

    @Override
    public boolean isAt(T other) {
        return isAt(other.getX(), other.getY());
    }

    @Override
    public boolean isAt(int x, int y) {
        return x == this.x && y == this.y;
    }

    @Override
    public Direction direction(T other) {
        return Direction.of(other.getX() - x, other.getY() - y);
    }

    @Override
    public int manhattanDistance(T other) {
        return Math.abs(x - other.x) + Math.abs(y - other.y);
    }

    /**
     * @param dir the direction
     * @return the tile that is adjacent to this TileInfo in the {@link Direction} dir
     * @throws IndexOutOfBoundsException if this TileInfo is near the border of the map and
     * the direction point outside the emap
     */
    public T adjacent(Direction dir) {
        return board.getAt(x + dir.dirX(), y + dir.dirY());
    }

    /**
     * @param dir the direction
     * @return the tile that is adjacent to this TileInfo in the {@link Direction} dir
     * or {@code null} if the adjacent tile is outside the map
     */
    public T safeAdjacent(Direction dir) {
        return board.safeGetAt(x + dir.dirX(), y + dir.dirY());
    }

    /**
     * Returns the board in which this tile is
     *
     * @return the board in which this tile is
     */
    public B getBoard() {
        return board;
    }

    public int positionHashCode() {
        return y * board.getWidth() + x;
    }
}
