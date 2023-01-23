package fr.valax.sokoshell.solver.board;

import fr.valax.sokoshell.solver.board.tiles.MutableTileInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * A tunnel is a zone of the map like this:
 *
 * <pre>
 *     $$$$$$
 *          $$$$$
 *     $$$$
 *        $$$$$$$
 * </pre>
 */
public class Tunnel {

    // STATIC

    private MutableTileInfo start;
    private MutableTileInfo end;

    // the tile outside the tunnel adjacent to start
    private MutableTileInfo startOut;

    // the tile outside the tunnel adjacent to end
    private MutableTileInfo endOut;
    private final List<Room> rooms = new ArrayList<>();

    // true if the tunnel can only be taken by the player
    private boolean playerOnlyTunnel;
    private boolean isOneway;


    // DYNAMIC
    private boolean crateInside = false;



    public void createTunnelExits() {
        if (this.startOut != null) {
            Direction initDir = start.direction(startOut);
            create(start, initDir, startOut);
        }

        if (endOut != null) {
            Direction endDir = end.direction(endOut);
            create(end, endDir, endOut);
        }
    }

    private void create(MutableTileInfo tile, Direction startDir, MutableTileInfo startOut) {
        MutableTileInfo t = tile;

        Direction nextDir = startDir.negate();
        while (true) {
            MutableTileInfo next = t.adjacent(nextDir);

            if (next.isWall() || t.getTunnel() != this) {
                break;
            }

            setExit(t, startDir, startOut);

            t = next;
        }
    }

    private void setExit(MutableTileInfo tile, Direction dir, MutableTileInfo out) {
        if (dir != null) {
            Exit exit = tile.getTunnelExit();

            if (exit == null) {
                exit = new Exit();
                tile.setTunnelExit(exit);
            }

            switch (dir) {
                case RIGHT -> exit.setRightExit(out);
                case UP -> exit.setUpExit(out);
                case DOWN -> exit.setDownExit(out);
                case LEFT -> exit.setLeftExit(out);
            }
        }
    }

    public void addRoom(Room room) {
        rooms.add(room);
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public MutableTileInfo getStart() {
        return start;
    }

    public void setStart(MutableTileInfo start) {
        this.start = start;
    }

    public MutableTileInfo getEnd() {
        return end;
    }

    public void setEnd(MutableTileInfo end) {
        this.end = end;
    }

    public MutableTileInfo getStartOut() {
        return startOut;
    }

    public void setStartOut(MutableTileInfo startOut) {
        this.startOut = startOut;
    }

    public MutableTileInfo getEndOut() {
        return endOut;
    }

    public void setEndOut(MutableTileInfo endOut) {
        this.endOut = endOut;
    }

    public boolean isPlayerOnlyTunnel() {
        return playerOnlyTunnel;
    }

    public void setPlayerOnlyTunnel(boolean playerOnlyTunnel) {
        this.playerOnlyTunnel = playerOnlyTunnel;
    }

    public boolean crateInside() {
        return crateInside;
    }

    public void setCrateInside(boolean crateInside) {
        this.crateInside = crateInside;
    }

    public boolean isOneway() {
        return isOneway;
    }

    public void setOneway(boolean oneway) {
        isOneway = oneway;
    }

    /**
     * Added to every tile that is inside a tunnel.
     * It contains for each direction where is the exit:
     * if you push a crate inside the tunnel to the left, the
     * method {@link #getExit(Direction)} wile return where you will
     * be after pushing the crate until you aren't outside the tunnel.
     */
    public static class Exit {

        private MutableTileInfo leftExit;
        private MutableTileInfo upExit;
        private MutableTileInfo rightExit;
        private MutableTileInfo downExit;

        public Exit() {
        }

        public Exit(MutableTileInfo leftExit, MutableTileInfo upExit, MutableTileInfo rightExit, MutableTileInfo downExit) {
            this.leftExit = leftExit;
            this.upExit = upExit;
            this.rightExit = rightExit;
            this.downExit = downExit;
        }

        public MutableTileInfo getExit(Direction dir) {
            return switch (dir) {
                case LEFT -> leftExit;
                case UP -> upExit;
                case RIGHT -> rightExit;
                case DOWN -> downExit;
            };
        }

        public MutableTileInfo getLeftExit() {
            return leftExit;
        }

        private void setLeftExit(MutableTileInfo leftExit) {
            this.leftExit = leftExit;
        }

        public MutableTileInfo getUpExit() {
            return upExit;
        }

        private void setUpExit(MutableTileInfo upExit) {
            this.upExit = upExit;
        }

        public MutableTileInfo getRightExit() {
            return rightExit;
        }

        private void setRightExit(MutableTileInfo rightExit) {
            this.rightExit = rightExit;
        }

        public MutableTileInfo getDownExit() {
            return downExit;
        }

        private void setDownExit(MutableTileInfo downExit) {
            this.downExit = downExit;
        }
    }
}
