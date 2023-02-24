package fr.valax.sokoshell.solver.board.tiles;

import fr.valax.sokoshell.solver.board.Board;
import fr.valax.sokoshell.solver.board.Room;
import fr.valax.sokoshell.solver.board.Tunnel;

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

    // SETTERS: throw UnsupportedOperationException as this class is immutable //

    @Override
    public void addCrate() {
        throw new UnsupportedOperationException("Immutable object");
    }

    @Override
    public void removeCrate() {
        throw new UnsupportedOperationException("Immutable object");
    }

    @Override
    public void setTile(Tile tile) {
        throw new UnsupportedOperationException("Immutable object");
    }

    @Override
    public void setDeadTile(boolean deadTile) {
        throw new UnsupportedOperationException("Immutable object");
    }

    @Override
    public void setReachable(boolean reachable) {
        throw new UnsupportedOperationException("Immutable object");
    }

    @Override
    public void setTunnel(Tunnel tunnel) {
        throw new UnsupportedOperationException("Immutable object");
    }

    @Override
    public void setTunnelExit(Tunnel.Exit tunnelExit) {
        throw new UnsupportedOperationException("Immutable object");
    }

    @Override
    public void setRoom(Room room) {
        throw new UnsupportedOperationException("Immutable object");
    }

    @Override
    public void mark() {
        throw new UnsupportedOperationException("Immutable object");
    }

    @Override
    public void unmark() {
        throw new UnsupportedOperationException("Immutable object");
    }

    @Override
    public void setMarked(boolean marked) {
        throw new UnsupportedOperationException("Immutable object");
    }

    @Override
    public void setTargets(TargetRemoteness[] targets) {
        throw new UnsupportedOperationException("Immutable object");
    }

    @Override
    public void setNearestTarget(TargetRemoteness nearestTarget) {
        throw new UnsupportedOperationException("Immutable object");
    }

    @Override
    public int hashCode() {
        return y * board.getWidth() + x;
    }
}
