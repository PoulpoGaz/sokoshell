package fr.valax.sokoshell.solver.board.tiles;

import fr.valax.sokoshell.solver.board.MutableBoard;
import fr.valax.sokoshell.solver.board.Room;
import fr.valax.sokoshell.solver.board.Tunnel;
import fr.valax.sokoshell.solver.board.mark.Mark;

import java.util.Arrays;

/**
 * Mutable implementation of {@link TileInfo}.
 *
 * This class extends {@link GenericTileInfo} and implements the setters methods defined in
 * {@link TileInfo}.
 * It also implements getters and setters for the 'solver-intended' properties.
 *
 * @see TileInfo
 * @see GenericTileInfo
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
     * Remoteness data from this tile to every target on the board.
     */
    protected TargetRemoteness[] targets;

    /**
     * Nearest target on the board.
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

    // GETTERS //

    @Override
    public boolean isDeadTile() {
        return deadTile;
    }

    @Override
    public boolean isReachable() {
        return reachable.isMarked();
    }

     @Override
    public Tunnel getTunnel() {
        return tunnel;
    }

    @Override
    public Tunnel.Exit getTunnelExit() {
        return tunnelExit;
    }

    public boolean isInATunnel() {
        return tunnel != null;
    }

    @Override
    public Room getRoom() {
        return room;
    }

    @Override
    public boolean isInARoom() {
        return room != null;
    }

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
