package fr.valax.sokoshell.solver.board;

import fr.valax.sokoshell.solver.board.tiles.MutableTileInfo;

import java.util.ArrayList;
import java.util.List;

public class Room {

    private boolean goalRoom;

    private final List<MutableTileInfo> tiles = new ArrayList<>();
    private final List<MutableTileInfo> targets = new ArrayList<>();

    private final List<Tunnel> tunnels = new ArrayList<>();

    /**
     * Only computed is the level is a goal room level as defined by {@link SolverBoard#isGoalRoomLevel()}
     */
    private List<MutableTileInfo> packingOrder;

    // dynamic
    // the index in packingOrder of the position of the next crate that will be pushed inside the room
    // negative if it is not possible because a crate isn't at the correct position
    // or if the room isn't a goal room
    private int packingOrderIndex;

    public Room() {
    }

    public void addTile(MutableTileInfo tile) {
        tiles.add(tile);

        if (tile.isTarget()) {
            targets.add(tile);
        }
    }


    public List<MutableTileInfo> getTiles() {
        return tiles;
    }

    public List<MutableTileInfo> getTargets() {
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

    public List<MutableTileInfo> getPackingOrder() {
        return packingOrder;
    }

    public void setPackingOrder(List<MutableTileInfo> packingOrder) {
        this.packingOrder = packingOrder;
    }

    public boolean isInPackingOrder(MutableTileInfo tile) {
        return packingOrder != null && packingOrder.contains(tile);
    }

    public int getPackingOrderIndex() {
        return packingOrderIndex;
    }

    public void setPackingOrderIndex(int packingOrderIndex) {
        this.packingOrderIndex = packingOrderIndex;
    }
}
