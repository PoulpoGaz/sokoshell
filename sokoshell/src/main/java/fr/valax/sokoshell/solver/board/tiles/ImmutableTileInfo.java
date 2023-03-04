package fr.valax.sokoshell.solver.board.tiles;

import fr.valax.sokoshell.solver.board.ImmutableBoard;
import fr.valax.sokoshell.solver.board.Room;
import fr.valax.sokoshell.solver.board.Tunnel;

/**
 * Immutable implementation of {@link TileInfo}.
 *
 * This class basically extends {@link GenericTileInfo}. It implements the setters methods defined in
 * {@link TileInfo} by throwing an {@link UnsupportedOperationException}.
 * It also implements the 'solver-intended' properties by always returning the default value: for instance, a
 * {@link ImmutableTileInfo} is never a 'dead tile', so the {@link #isDeadTile} method will always return {@code false}.
 * The same policy is applied for each property.
 *
 * @see TileInfo
 * @see GenericTileInfo
 */
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

    @Override
    public int getCrateIndex() {
        return -1;
    }
}
