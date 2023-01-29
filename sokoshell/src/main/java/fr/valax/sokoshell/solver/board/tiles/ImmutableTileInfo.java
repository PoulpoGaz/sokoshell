package fr.valax.sokoshell.solver.board.tiles;

import fr.valax.sokoshell.solver.board.ImmutableBoard;
import fr.valax.sokoshell.solver.board.Room;
import fr.valax.sokoshell.solver.board.Tunnel;

public class ImmutableTileInfo extends GenericTileInfo {

    public ImmutableTileInfo(ImmutableBoard board, Tile tile, int x, int y) {
        super(board, tile, x, y);
    }

    public ImmutableTileInfo(TileInfo tileInfo) {
        super(tileInfo);
    }

    // GETTERS //

    @Override
    public boolean isDeadTile() {
        return false;
    }

    @Override
    public boolean isReachable() {
        return true;
    }

    @Override
    public Tunnel getTunnel() {
        return null;
    }

    @Override
    public Tunnel.Exit getTunnelExit() {
        return null;
    }

    @Override
    public boolean isInATunnel() {
        return false;
    }

    @Override
    public Room getRoom() {
        return null;
    }

    @Override
    public boolean isInARoom() {
        return false;
    }

    @Override
    public boolean isMarked() {
        return false;
    }

    @Override
    public String toString() {
        return tile.toString();
    }

    @Override
    public TargetRemoteness getNearestTarget() {
        return null;
    }

    @Override
    public TargetRemoteness[] getTargets() {
        return null;
    }

    // SETTERS: throw UnsupportedOperationException as this class is immutable //

    @Override
    public void set(TileInfo other) {
        throw new UnsupportedOperationException("Immutable object");
    }

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
}
