package fr.valax.sokoshell.solver.board;

import fr.valax.sokoshell.solver.board.tiles.TileInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * A tunnel is a zone of the board like this:
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

    protected TileInfo start;
    protected TileInfo end;

    // the tile outside the tunnel adjacent to start
    protected TileInfo startOut;

    // the tile outside the tunnel adjacent to end
    protected TileInfo endOut;
    protected List<Room> rooms;

    // true if the tunnel can only be taken by the player
    protected boolean playerOnlyTunnel;
    protected boolean isOneway;


    // DYNAMIC
    protected boolean crateInside = false;



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

    private void create(TileInfo tile, Direction startDir, TileInfo startOut) {
        TileInfo t = tile;

        Direction nextDir = startDir.negate();
        while (true) {
            TileInfo next = t.adjacent(nextDir);

            if (next.isWall() || t.getTunnel() != this) {
                break;
            }

            setExit(t, startDir, startOut);

            t = next;
        }
    }

    private void setExit(TileInfo tile, Direction dir, TileInfo out) {
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
        if (rooms == null) {
            rooms = new ArrayList<>();
        }
        rooms.add(room);
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public TileInfo getStart() {
        return start;
    }

    public void setStart(TileInfo start) {
        this.start = start;
    }

    public TileInfo getEnd() {
        return end;
    }

    public void setEnd(TileInfo end) {
        this.end = end;
    }

    public TileInfo getStartOut() {
        return startOut;
    }

    public void setStartOut(TileInfo startOut) {
        this.startOut = startOut;
    }

    public TileInfo getEndOut() {
        return endOut;
    }

    public void setEndOut(TileInfo endOut) {
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

    @Override
    public String toString() {
        if (startOut == null) {
            return "closed - (%d; %d) --> (%d; %d) - (%d; %d). only player? %s. one way? %s"
                    .formatted(start.getX(), start.getY(),
                            end.getX(), end.getY(),
                            endOut.getX(), endOut.getY(),
                            playerOnlyTunnel, isOneway);
        } else if (endOut == null) {
            return "(%d; %d) - (%d; %d) --> (%d; %d) - closed. only player? %s. one way? %s"
                    .formatted(startOut.getX(), startOut.getY(),
                            start.getX(), start.getY(),
                            end.getX(), end.getY(),
                            playerOnlyTunnel, isOneway);
        } else {
            return "(%d; %d) - (%d; %d) --> (%d; %d) - (%d; %d). only player? %s. one way? %s"
                    .formatted(startOut.getX(), startOut.getY(),
                            start.getX(), start.getY(),
                            end.getX(), end.getY(),
                            endOut.getX(), endOut.getY(),
                            playerOnlyTunnel, isOneway);
        }
    }

    /**
     * Added to every tile that is inside a tunnel.
     * It contains for each direction where is the exit:
     * if you push a crate inside the tunnel to the left, the
     * method {@link #getExit(Direction)} wile return where you will
     * be after pushing the crate until you aren't outside the tunnel.
     *
     * @implNote This object isn't immutable but is assumed as
     * immutable by MutableBoard.StaticBoard#linkTunnelsRoomsAndTileInfos(MutableBoard.StaticTile[][])
     */
    public static class Exit {

        private TileInfo leftExit;
        private TileInfo upExit;
        private TileInfo rightExit;
        private TileInfo downExit;

        public Exit() {
        }

        public Exit(TileInfo leftExit, TileInfo upExit, TileInfo rightExit, TileInfo downExit) {
            this.leftExit = leftExit;
            this.upExit = upExit;
            this.rightExit = rightExit;
            this.downExit = downExit;
        }

        public TileInfo getExit(Direction dir) {
            return switch (dir) {
                case LEFT -> leftExit;
                case UP -> upExit;
                case RIGHT -> rightExit;
                case DOWN -> downExit;
            };
        }

        public TileInfo getLeftExit() {
            return leftExit;
        }

        private void setLeftExit(TileInfo leftExit) {
            this.leftExit = leftExit;
        }

        public TileInfo getUpExit() {
            return upExit;
        }

        private void setUpExit(TileInfo upExit) {
            this.upExit = upExit;
        }

        public TileInfo getRightExit() {
            return rightExit;
        }

        private void setRightExit(TileInfo rightExit) {
            this.rightExit = rightExit;
        }

        public TileInfo getDownExit() {
            return downExit;
        }

        private void setDownExit(TileInfo downExit) {
            this.downExit = downExit;
        }
    }
}
