package fr.valax.sokoshell.solver.board.tiles;

import fr.valax.sokoshell.solver.board.Board;
import fr.valax.sokoshell.solver.board.Room;
import fr.valax.sokoshell.solver.board.Tunnel;

public class TileInfo extends GenericTileInfo<TileInfo, Board> {

    public TileInfo(Board board, Tile tile, int x, int y) {
        super(board, tile, x, y);
    }

    public TileInfo(TileInfo tileInfo) {
        super(tileInfo);
    }

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
}
