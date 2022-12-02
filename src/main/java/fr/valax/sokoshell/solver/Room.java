package fr.valax.sokoshell.solver;

import java.util.ArrayList;
import java.util.List;

public class Room {

    private boolean goalRoom;

    private final List<TileInfo> tiles = new ArrayList<>();
    private final List<TileInfo> targets = new ArrayList<>();

    private final List<Tunnel> tunnels = new ArrayList<>();

    private List<TileInfo> packingOrder = new ArrayList<>();

    public Room() {
    }

    public void addTile(TileInfo tile) {
        tiles.add(tile);

        if (tile.isTarget()) {
            targets.add(tile);
        }
    }


    public List<TileInfo> getTiles() {
        return tiles;
    }

    public List<TileInfo> getTargets() {
        return targets;
    }


    public void addTunnel(Tunnel tunnel) {
        tunnels.add(tunnel);
    }

    public List<Tunnel> getTunnels() {
        return tunnels;
    }


    public boolean isGoalRoom() {
        return goalRoom;
    }

    public void setGoalRoom(boolean goalRoom) {
        this.goalRoom = goalRoom;
    }

    public List<TileInfo> getPackingOrder() {
        return packingOrder;
    }

    public void setPackingOrder(List<TileInfo> packingOrder) {
        this.packingOrder = packingOrder;
    }
}
