package fr.valax.sokoshell.solver.board.tiles;

import fr.valax.sokoshell.solver.board.*;
import fr.valax.sokoshell.solver.board.mark.Mark;
import fr.valax.sokoshell.solver.board.mark.MarkSystem;

import java.util.Arrays;

/**
 * Represents a tile that can be modified.
 */
public class MutableTileInfo extends GenericTileInfo {

    // Static information
    protected boolean deadTile;

    /**
     * The tunnel in which this tile is. A Tile is either in a room or in a tunnel
     */
    protected Tunnel tunnel;
    // contains for each direction, where is the outside of the tunnel from this tile
    protected Tunnel.Exit tunnelExit;
    protected Room room;

    /**
     * Remoteness data from this tile to every target on the map.
     */
    protected TargetRemoteness[] targets;

    /**
     * Nearest target on the map.
     */
    protected TargetRemoteness nearestTarget;


    // Dynamic information
    protected Mark reachable;
    protected Mark mark;

    public MutableTileInfo(MutableBoard board, Tile tile, int x, int y) {
        super(board, tile, x, y);

        this.reachable = board.getReachableMarkSystem().newMark();
        this.mark = board.getMarkSystem().newMark();
    }

    public MutableTileInfo(TileInfo other) {
        super(other);

        this.reachable = board.getReachableMarkSystem().newMark();
        this.mark = board.getMarkSystem().newMark();
    }

    public MutableTileInfo(MutableBoard board, TileInfo other) {
        super(board, other);

        this.reachable = board.getReachableMarkSystem().newMark();
        this.mark = board.getMarkSystem().newMark();
    }


    /**
     * Copy a TileInfo to another board
     *
     * @param other the map that will contain the copied TileInfo
     * @return the copied TileInfo in the new map
     */
    public TileInfo copiedTo(MutableBoard other) {
        TileInfo t = new MutableTileInfo(other, tile, x, y);
        t.set(this);
        return t;
    }


    // GETTERS //


    /**
     * @return {@code true} if this tile is a dead tile
     * @see MutableBoard#computeDeadTiles()
     */
    @Override
    public boolean isDeadTile() {
        return deadTile;
    }

    /**
     * @return {@code true} if this tile is reachable by the player.
     * @see MutableBoard#findReachableCases(int)
     */
    @Override
    public boolean isReachable() {
        return reachable.isMarked();
    }

    /**
     * Returns the tunnel in which this tile is
     *
     * @return the tunnel in which this tile is
     */
     @Override
    public Tunnel getTunnel() {
        return tunnel;
    }

    /**
     * Returns the {@link Tunnel.Exit} object associated with this tile info.
     * If the tile isn't in a tunnel, it returns null
     *
     * @return the {@link Tunnel.Exit} object associated with this tile info or {@code null
     * @see Tunnel.Exit
     */
    @Override
    public Tunnel.Exit getTunnelExit() {
        return tunnelExit;
    }

    /**
     * Returns {@code true} if this tile info is in a tunnel
     *
     * @return {@code true} if this tile info is in a tunnel
     */
    public boolean isInATunnel() {
        return tunnel != null;
    }

    /**
     * Returns the room in which this tile is
     *
     * @return the room in which this tile is
     */
    @Override
    public Room getRoom() {
        return room;
    }

    /**
     * Returns {@code true} if this tile info is in a room
     *
     * @return {@code true} if this tile info is in a room
     */
    @Override
    public boolean isInARoom() {
        return room != null;
    }

    /**
     * @return {@code true} if this tile is marked
     * @see Mark
     * @see MarkSystem
     */
    @Override
    public boolean isMarked() {
        return mark.isMarked();
    }

    @Override
    public TargetRemoteness getNearestTarget() {
        return nearestTarget;
    }

    @Override
    public TargetRemoteness[] getTargets() {
        return targets;
    }


    // SETTERS //

    public void set(TileInfo other) {
        tile = other.getTile();
        deadTile = other.isDeadTile();
        setReachable(other.isReachable());
        mark.setMarked(other.isMarked());
        if (other.getTargets() == null) {
            targets = null;
        } else {
            targets = Arrays.copyOf(other.getTargets(), other.getTargets().length);
        }
        nearestTarget = other.getNearestTarget();
    }

    @Override
    public void addCrate() {
        if (tile == Tile.FLOOR) {
            tile = Tile.CRATE;
        } else if (tile == Tile.TARGET) {
            tile = Tile.CRATE_ON_TARGET;
        }
    }

    @Override
    public void removeCrate() {
        if (tile == Tile.CRATE) {
            tile = Tile.FLOOR;
        } else if (tile == Tile.CRATE_ON_TARGET) {
            tile = Tile.TARGET;
        }
    }

    @Override
    public void setTile(Tile tile) {
        this.tile = tile;
    }

    @Override
    public void setDeadTile(boolean deadTile) {
        this.deadTile = deadTile;
    }

    @Override
    public void setReachable(boolean reachable) {
        this.reachable.setMarked(reachable);
    }

    @Override
    public void setTunnel(Tunnel tunnel) {
        this.tunnel = tunnel;
    }

    @Override
    public void setTunnelExit(Tunnel.Exit tunnelExit) {
        this.tunnelExit = tunnelExit;
    }

    @Override
    public void setRoom(Room room) {
        this.room = room;
    }

    @Override
    public void mark() {
        mark.mark();
    }

    @Override
    public void unmark() {
        mark.unmark();
    }

    @Override
    public void setMarked(boolean marked) {
        mark.setMarked(marked);
    }

    @Override
    public void setTargets(TargetRemoteness[] targets) {
        this.targets = targets;
    }

    @Override
    public void setNearestTarget(TargetRemoteness nearestTarget) {
        this.nearestTarget = nearestTarget;
    }
}
