package fr.valax.sokoshell.solver.board.tiles;

import fr.valax.sokoshell.solver.board.Board;
import fr.valax.sokoshell.solver.board.Direction;

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
    public boolean isAt(TileInfo other) {
        return isAt(other.getX(), other.getY());
    }

    @Override
    public boolean isAt(int x, int y) {
        return x == this.x && y == this.y;
    }

    @Override
    public Direction direction(TileInfo other) {
        return Direction.of(other.getX() - x, other.getY() - y);
    }

    @Override
    public int manhattanDistance(TileInfo other) {
        return Math.abs(x - other.getX()) + Math.abs(y - other.getY());
    }

    /**
     * @param dir the direction
     * @return the tile that is adjacent to this TileInfo in the {@link Direction} dir
     * @throws IndexOutOfBoundsException if this TileInfo is near the border of the board and
     * the direction point outside the board
     */
    public TileInfo adjacent(Direction dir) {
        return board.getAt(x + dir.dirX(), y + dir.dirY());
    }

    /**
     * @param dir the direction
     * @return the tile that is adjacent to this TileInfo in the {@link Direction} dir
     * or {@code null} if the adjacent tile is outside the board
     */
    public TileInfo safeAdjacent(Direction dir) {
        return board.safeGetAt(x + dir.dirX(), y + dir.dirY());
    }

    /**
     * Returns the board in which this tile is
     *
     * @return the board in which this tile is
     */
    public Board getBoard() {
        return board;
    }

    public int positionHashCode() {
        return y * board.getWidth() + x;
    }
}
