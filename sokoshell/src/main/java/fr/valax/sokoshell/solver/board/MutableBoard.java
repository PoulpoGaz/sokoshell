package fr.valax.sokoshell.solver.board;

import fr.valax.sokoshell.solver.Corral;
import fr.valax.sokoshell.solver.State;
import fr.valax.sokoshell.solver.CorralDetector;
import fr.valax.sokoshell.solver.board.mark.AbstractMarkSystem;
import fr.valax.sokoshell.solver.board.mark.Mark;
import fr.valax.sokoshell.solver.board.mark.MarkSystem;
import fr.valax.sokoshell.solver.board.tiles.*;
import fr.valax.sokoshell.solver.pathfinder.CrateAStar;
import fr.valax.sokoshell.solver.pathfinder.CratePlayerAStar;
import fr.valax.sokoshell.solver.pathfinder.PlayerAStar;

import java.util.*;
import java.util.function.Consumer;


/**
 * Mutable implementation of {@link Board}.
 *
 * This class extends {@link GenericBoard} by defining all the setters methods. It internally uses {@link MutableTileInfo} to store the board content
 * in {@link GenericBoard#content}.
 *
 * @see Board
 * @see GenericBoard
 * @see MutableTileInfo
 */
@SuppressWarnings("ForLoopReplaceableByForEach")
public class MutableBoard extends GenericBoard {

    private final MarkSystem markSystem = newMarkSystem(TileInfo::unmark);
    private final MarkSystem reachableMarkSystem = newMarkSystem((t) -> t.setReachable(false));

    private int targetCount;

    /**
     * Tiles that can be 'target' or 'floor'
     */
    private TileInfo[] floors;

    private final List<Tunnel> tunnels = new ArrayList<>();
    private final List<Room> rooms = new ArrayList<>();


    /**
     * True if all rooms are goal room with only one entrance
     */
    private boolean isGoalRoomLevel;

    private PlayerAStar playerAStar;
    private CrateAStar crateAStar;
    private CratePlayerAStar cratePlayerAStar;

    private final CorralDetector corralDetector;

    private StaticBoard staticBoard;

    /**
     * Creates a SolverBoard with the specified width, height and tiles
     *
     * @param content a rectangular matrix of size width * height. The first index is for the rows
     *                and the second for the columns
     * @param width board width
     * @param height board height
     */
    public MutableBoard(Tile[][] content, int width, int height) {
        super(width, height);

        this.content = new TileInfo[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                this.content[y][x] = new MutableTileInfo(this, content[y][x], x, y);
            }
        }

        corralDetector = new CorralDetector(this);
    }

    public MutableBoard(int width, int height) {
        super(width, height);

        this.content = new TileInfo[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                this.content[y][x] = new MutableTileInfo(this, Tile.FLOOR, x, y);
            }
        }

        corralDetector = new CorralDetector(this);
    }

    /**
     * Creates a copy of 'other'. It doesn't copy solver information
     *
     * @param other the board to copy
     */
    public MutableBoard(Board other) {
        this(other, false);
    }

    public MutableBoard(Board other, boolean copyStatic) {
        super(other.getWidth(), other.getHeight());

        content = new TileInfo[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                content[y][x] = new MutableTileInfo(this, other.getAt(x, y));
            }
        }

        corralDetector = new CorralDetector(this);

        if (copyStatic) {
            copyStaticInformation(other);
        }
    }

    private void copyStaticInformation(Board other) {
        // map room in other board and in this board
        Map<Room, Room> roomMap = new HashMap<>(rooms.size());
        Map<Tunnel, Tunnel> tunnelMap = new HashMap<>(rooms.size());

        // copy tunnels, rooms
        for (Room room : other.getRooms()) {
            Room copy = copyRoom(room);
            roomMap.put(room, copy);
            rooms.add(copy);
        }
        for (Tunnel tunnel : other.getTunnels()) {
            Tunnel copy = copyTunnel(tunnel);
            tunnelMap.put(tunnel, copy);
            tunnels.add(copy);
        }

        // copy tile info
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                TileInfo otherTile = other.getAt(x, y);
                TileInfo tile = content[y][x];
                tile.setDeadTile(otherTile.isDeadTile());

                if (tile.getTargets() != null) {
                    tile.setTargets(Arrays.copyOf(tile.getTargets(), tile.getTargets().length));
                }
                tile.setNearestTarget(otherTile.getNearestTarget());

                tile.setTunnel(tunnelMap.get(otherTile.getTunnel()));
                tile.setRoom(roomMap.get(otherTile.getRoom()));
                if (otherTile.getTunnelExit() != null) {
                    tile.setTunnelExit(otherTile.getTunnelExit()); // it is immutable !
                }
            }
        }

        // link rooms and tunnels
        for (Tunnel tunnel : other.getTunnels()) {
            Tunnel newTunnel = tunnelMap.get(tunnel);
            for (Room room : other.getRooms()) {
                Room newRoom = roomMap.get(room);
                newTunnel.addRoom(newRoom);
                newRoom.addTunnel(newTunnel);
            }
        }
    }

    private Room copyRoom(Room room) {
        Room newRoom = new Room();
        newRoom.setGoalRoom(room.isGoalRoom());

        for (TileInfo t : room.getTiles()) {
            newRoom.addTile(getAt(t.getIndex()));
        }
        if (room.getPackingOrder() != null) {
            List<TileInfo> packingOrder = new ArrayList<>();
            for (TileInfo t : room.getPackingOrder()) {
                packingOrder.add(getAt(t.getIndex()));
            }
            newRoom.setPackingOrder(packingOrder);
        }

        return newRoom;
    }

    private Tunnel copyTunnel(Tunnel tunnel) {
        Tunnel newTunnel = new Tunnel();

        newTunnel.setStart(getAt(tunnel.getStart().getIndex()));
        newTunnel.setEnd(getAt(tunnel.getEnd().getIndex()));

        if (tunnel.getStartOut() != null) {
            newTunnel.setStartOut(getAt(tunnel.getStartOut().getIndex()));
        }
        if (tunnel.getEndOut() != null) {
            newTunnel.setEndOut(getAt(tunnel.getEndOut().getIndex()));
        }
        newTunnel.setPlayerOnlyTunnel(tunnel.isPlayerOnlyTunnel());
        newTunnel.setOneway(tunnel.isOneway());

        return newTunnel;
    }

    /**
     * Apply the consumer on every tile info
     *
     * @param consumer the consumer to apply
     */
    public void forEach(Consumer<TileInfo> consumer) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                consumer.accept(content[y][x]);
            }
        }
    }

    /**
     * Set at tile at the specified index. The index will be converted to
     * cartesian coordinate with {@link #getX(int)} and {@link  #getY(int)}
     *
     * @param index index in the board
     * @param tile the new tile
     * @throws IndexOutOfBoundsException if the index lead to a position outside the board
     */
    public void setAt(int index, Tile tile) { content[getY(index)][getX(index)].setTile(tile); }

    /**
     * Set at tile at (x, y)
     *
     * @param x x position in the board
     * @param y y position in the board
     * @throws IndexOutOfBoundsException if the position is outside the board
     */
    public void setAt(int x, int y, Tile tile) {
        content[y][x].setTile(tile);
    }

    /**
     * Puts the crates of the given state in the content array.
     *
     * @param state The state with the crates
     */
    public void addStateCrates(State state) {
        for (int i : state.cratesIndices()) {
            getAt(i).addCrate();
        }
    }

    /**
     * Removes the crates of the given state from the content array.
     *
     * @param state The state with the crates
     */
    public void removeStateCrates(State state) {
        for (int i : state.cratesIndices()) {
            getAt(i).removeCrate();
        }
    }

    /**
     * Puts the crates of the given state in the content array.
     * If a crate is outside the board, it doesn't throw an {@link IndexOutOfBoundsException}
     *
     * @param state The state with the crates
     */
    public void safeAddStateCrates(State state) {
        for (int i : state.cratesIndices()) {
            TileInfo info = safeGetAt(i);

            if (info != null) {
                info.addCrate();
            }
        }
    }

    /**
     * Removes the crates of the given state from the content array.
     * If a crate is outside the board, it doesn't throw an {@link IndexOutOfBoundsException}
     *
     * @param state The state with the crates
     */
    public void safeRemoveStateCrates(State state) {
        for (int i : state.cratesIndices()) {
            TileInfo info = safeGetAt(i);

            if (info != null) {
                info.removeCrate();
            }
        }
    }

    // ===========================================
    // *         Methods used by solvers         *
    // * You need to call #initForSolver() first *
    // ===========================================

    /**
     * Initialize the board for solving:
     * <ul>
     *     <li>compute floor tiles: an array containing all non-wall tile</li>
     *     <li>compute {@linkplain #computeDeadTiles() dead tiles}</li>
     *     <li>find {@linkplain #findTunnels() tunnels}</li>
     * </ul>
     * <strong>The board must have no crate inside</strong>
     * @see Tunnel
     */
    public void initForSolver() {
        playerAStar = new PlayerAStar(this);
        crateAStar = new CrateAStar(this);
        cratePlayerAStar = new CratePlayerAStar(this);

        computeFloors();
        computeDeadTiles();
        findTunnels();
        findRooms();
        removeUselessTunnels();
        finishComputingTunnels();
        tryComputePackingOrder();
        computeTileToTargetsDistances();

        // we must compute the static board here
        // this is the unique point where the board
        // information are guaranteed to be true.
        // For example, the freeze deadlock detector
        // places wall on the map but this object
        // has no information about this.
        staticBoard = new StaticBoard();
    }

    /**
     * Creates or recreates the floor array. It is an array containing all tile info
     * that are not a wall
     */
    public void computeFloors() {
        int nFloor = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                TileInfo t = getAt(x, y);

                if (!t.isSolid() || t.isCrate()) {
                    nFloor++;
                }
            }
        }

        this.floors = new TileInfo[nFloor];
        int i = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (!this.content[y][x].isSolid() || this.content[y][x].isCrate()) {
                    this.floors[i] = this.content[y][x];
                    i++;
                }
            }
        }
    }

    /**
     * Apply the consumer on every tile info except walls
     *
     * @param consumer the consumer to apply
     */
    public void forEachNotWall(Consumer<TileInfo> consumer) {
        for (TileInfo floor : floors) {
            consumer.accept(floor);
        }
    }

    /**
     * Removes the crates of the given state from the content array.
     * It also does a small analyse of the state: set {@link Tunnel#crateInside()}
     * to true if there is effectively a crate inside
     *
     * @param state The state with the crates
     */
    public void addStateCratesAndAnalyse(State state) {
        for (int i : state.cratesIndices()) {
            TileInfo tile = getAt(i);
            tile.addCrate();

            Tunnel t = tile.getTunnel();
            if (t != null) {
                // TODO: do the check but need to check if player is between two crates in a tunnel: see boxxle 53
                /*if (t.crateInside()) { // THIS IS VERY IMPORTANT -> see tunnels
                    throw new IllegalStateException();
                }*/

                t.setCrateInside(true);
            }

            if (isGoalRoomLevel) {
                Room r = tile.getRoom();
                if (r != null && r.isGoalRoom() && tile.isCrate()) { // crate whereas a goal room must contain crate on target
                    r.setPackingOrderIndex(-1);
                }
            }
        }

        if (isGoalRoomLevel) {
            for (int i = 0; i < rooms.size(); i++) {
                Room r = rooms.get(i);

                if (r.isGoalRoom() && r.getPackingOrderIndex() >= 0) {
                    List<TileInfo> order = r.getPackingOrder();

                    // find the first non crate on target tile
                    // if the room is completed, then index is equals to -1
                    int index = -1;
                    for (int j = 0; j < order.size(); j++) {
                        TileInfo tile = order.get(j);

                        if (!tile.isCrateOnTarget()) {
                            index = j;
                            break;
                        }
                    }

                    // checks that remaining aren't crate on target
                    for (int j = index + 1; j < order.size(); j++) {
                        TileInfo tile = order.get(j);

                        if (tile.isCrateOnTarget()) {
                            index = -1;
                            break;
                        }
                    }

                    r.setPackingOrderIndex(index);
                } else {
                    r.setPackingOrderIndex(-1);
                }
            }
        }
    }

    /**
     * Removes the crates of the given state from the content array.
     * Also reset analyse did by {@link #addStateCratesAndAnalyse(State)}
     *
     * @param state The state with the crates
     */
    public void removeStateCratesAndReset(State state) {
        for (int i : state.cratesIndices()) {
            getAt(i).removeCrate();
        }

        for (int i = 0; i < tunnels.size(); i++) {
            tunnels.get(i).setCrateInside(false);
        }

        if (isGoalRoomLevel) {
            for (int i = 0; i < rooms.size(); i++) {
                rooms.get(i).setPackingOrderIndex(0);
            }
        }
    }

    // ************
    // * ANALYSIS *
    // ************

    // * STATIC *

    /**
     * Detects the dead positions of a level. Dead positions are cases that make the level unsolvable
     * when a crate is put on them.
     * After this function has been called, to check if a given crate at (x,y) is a dead position,
     * you can use {@link TileInfo#isDeadTile()} to check in constant time.
     * The board <strong>MUST</strong> have <strong>NO CRATES</strong> for this function to work.
     */
    public void computeDeadTiles() {
        // reset
        forEachNotWall(tile -> tile.setDeadTile(true));

        // loop
        forEachNotWall((tile) -> {
            if (!tile.isDeadTile()) {
                return;
            }

            if (tile.anyCrate()) {
                tile.setDeadTile(true);
                return;
            }

            if (!tile.isTarget()) {
                return;
            }

            findNonDeadCases(tile, null);
        });
    }
    /**
     * Discovers all the reachable cases from (x, y) to find dead positions, as described
     * <a href="www.sokobano.de/wiki/index.php?title=How_to_detect_deadlocks#Detecting_simple_deadlocks">here</a>
     */
    private void findNonDeadCases(TileInfo tile, Direction lastDir) {
        tile.setDeadTile(false);
        for (Direction d : Direction.VALUES) {
            if (d == lastDir) { // do not go backwards
                continue;
            }

            final int nextX = tile.getX() + d.dirX();
            final int nextY = tile.getY() + d.dirY();
            final int nextNextX = nextX + d.dirX();
            final int nextNextY = nextY + d.dirY();

            if (getAt(nextX, nextY).isDeadTile()    // avoids to check already processed cases
                    && isTileEmpty(nextX, nextY)
                    && isTileEmpty(nextNextX, nextNextY)) {
                findNonDeadCases(getAt(nextX, nextY), d.negate());
            }
        }
    }

    /**
     * Find tunnels. A tunnel is something like this:
     * <pre>
     *     $$$$$$
     *          $$$$$
     *     $$$$
     *        $$$$$$$
     * </pre>
     *
     * A tunnel doesn't contain a target
     */
    public void findTunnels() {
        tunnels.clear();

        markSystem.unmarkAll();
        forEachNotWall((t) -> {
            if (t.isInATunnel() || t.isMarked() || t.isTarget()) {
                return;
            }

            Tunnel tunnel = buildTunnel(t);

            if (tunnel != null) {
                tunnels.add(tunnel);
            }
        });
    }

    /**
     * Try to create a tunnel that contains the specified tile.
     *
     * @param init a tile in the tunnel
     * @return a tunnel that contains the tile or {@code null}
     */
    private Tunnel buildTunnel(TileInfo init) {
        Direction pushDir1 = null;
        Direction pushDir2 = null;

        for (Direction dir : Direction.VALUES) {
            TileInfo adj = init.adjacent(dir);

            if (!adj.isSolid()) {
                if (pushDir1 == null) {
                    pushDir1 = dir;
                } else if (pushDir2 == null) {
                    pushDir2 = dir;
                } else {
                    return null; // too many direction
                }
            }
        }

        if (pushDir1 == null) { // all adjacents tiles are wall, ie init is alone, nerver happen see LevelBuilder
            return null;
        } else if (pushDir2 == null) {
            /*
                We are in this case:
                  |$|
                 $| |$
             */

            Tunnel tunnel = new Tunnel();
            tunnel.setStart(init);
            tunnel.setEnd(init);
            tunnel.setEndOut(init.adjacent(pushDir1));
            init.setTunnel(tunnel);

            growTunnel(tunnel, init.adjacent(pushDir1), pushDir1);
            return tunnel;
        } else {
            /*
                Either:
                #| |#
                Either:
                 |#|
                #| |
             */
            boolean onlyPlayer = false;

            if (pushDir1.negate() != pushDir2) {
                /*
                    First case:
                      |#|
                     #|i|
                      | |#
                    if init is like this, then this is a tunnel and a crate
                    mustn't be pushed inside.

                    Second case:
                      |#|
                     #|i|
                      | |
                    ie not tunnel
                */
                if (init.adjacent(pushDir1).adjacent(pushDir2).isSolid()) {
                    onlyPlayer = true;
                } else {
                    return null;
                }
            }

            Tunnel tunnel = new Tunnel();
            tunnel.setEnd(init);
            tunnel.setEndOut(init.adjacent(pushDir1));
            tunnel.setPlayerOnlyTunnel(onlyPlayer);
            init.setTunnel(tunnel);

            growTunnel(tunnel, init.adjacent(pushDir1), pushDir1);
            tunnel.setStart(tunnel.getEnd());
            tunnel.setStartOut(tunnel.getEndOut());
            growTunnel(tunnel, init.adjacent(pushDir2), pushDir2);

            return tunnel;
        }
    }

    /**
     * Try to grow a tunnel by the end ie Tunnel#end and Tunnel#endOut are modified.
     * The tile adjacent to pos according to -dir is assumed to
     * be a part of a tunnel. So we are in the following situations:
     * <pre>
     *             $$$     $$$
     *     $ $       $     $
     *     $@$     $@$     $@$
     * </pre>
     *
     * @param pos position of the player
     * @param dir the move the player did to go to pos
     */
    private void growTunnel(Tunnel t, TileInfo pos, Direction dir) {
        pos.mark();

        Direction leftDir = dir.left();
        Direction rightDir = dir.right();
        TileInfo left = pos.adjacent(leftDir);
        TileInfo right = pos.adjacent(rightDir);
        TileInfo front = pos.adjacent(dir);

        if (!pos.isTarget()) {
            pos.setTunnel(t);
            if (left.isSolid() && right.isSolid() && front.isSolid()) {
                t.setPlayerOnlyTunnel(true);
                t.setEnd(pos);
                t.setEndOut(null);
            } else if (left.isSolid() && right.isSolid()) {
                if (front.isMarked()) {
                    t.setEnd(pos);
                    t.setEndOut(front);
                } else {
                    growTunnel(t, front, dir);
                }
                return;
            } else if (right.isSolid() && front.isSolid()) {
                t.setPlayerOnlyTunnel(true);
                if (left.isMarked()) {
                    t.setEnd(pos);
                    t.setEndOut(left);
                } else {
                    growTunnel(t, left, leftDir);
                }
                return;
            } else if (left.isSolid() && front.isSolid()) {
                t.setPlayerOnlyTunnel(true);
                if (right.isMarked()) {
                    t.setEnd(pos);
                    t.setEndOut(right);
                } else {
                    growTunnel(t, right, rightDir);
                }
                return;
            }
        }

        pos.setTunnel(null);
        pos.unmark();
        t.setEndOut(pos);
        t.setEnd(pos.adjacent(dir.negate()));
    }

    /**
     * Finds room based on tunnel. Basically all tile that aren't in a tunnel are in room.
     * This means that you need to call {@link #findTunnels()} before!
     * A room that contains a target is a packing room.
     */
    public void findRooms() {
        forEachNotWall((t) -> {
            if (t.isInATunnel() || t.isInARoom()) {
                return;
            }

            Room room = new Room();
            expandRoom(room, t);
            rooms.add(room);
        });
    }

    private void expandRoom(Room room, TileInfo tile) {
        room.addTile(tile);
        tile.setRoom(room);

        if (tile.isTarget()) {
            room.setGoalRoom(true);
        }

        for (Direction dir : Direction.VALUES) {
            TileInfo adj = tile.safeAdjacent(dir);

            if (adj != null && !adj.isSolid()) {
                if (!adj.isInATunnel() && !adj.isInARoom()) {
                    expandRoom(room, adj);
                } else if (adj.isInATunnel()) {
                    room.addTunnel(adj.getTunnel());
                    adj.getTunnel().addRoom(room);
                }
            }
        }
    }

    private void removeUselessTunnels() {
        for (int i = 0; i < tunnels.size(); i++) {
            Tunnel t = tunnels.get(i);
            if (t.getStartOut() == null || t.getEndOut() == null) {
                Room room = t.getRooms().get(0); // tunnel is linked to exactly one room
                room.tunnels.remove(t); // detach the tunnel

                if (room.tunnels.size() == 2 && room.tiles.size() == 1) {
                    // room is now useless
                    // we are in one of the following cases:
                    // ###    # #
                    //     or #
                    // #_#    #_#
                    // _ indicates the tunnel to remove

                    // dir is the direction the player need to take to exit the tunnel
                    Direction dir;
                    if (t.getStartOut() == null) {
                        dir = t.getEnd().direction(t.getEndOut());
                    } else {
                        dir = t.getStart().direction(t.getStartOut());
                    }

                    Tunnel t1 = room.tunnels.get(0);
                    Tunnel t2 = room.tunnels.get(1);
                    TileInfo roomTile = room.getTiles().get(0);

                    roomTile.setRoom(null);
                    merge(t1, t2, roomTile);
                    if (!roomTile.adjacent(dir).isSolid()) {
                        // second case
                        // tunnel became in every case player only
                        t1.setPlayerOnlyTunnel(false);
                    }

                    // remove t2, taking care of i
                    int j = tunnels.indexOf(t2);
                    tunnels.remove(j);
                    if (j < i) {
                        i--;
                    }
                }

                tunnels.remove(i);
                i--;
            }
        }
    }

    /**
     * Merge two tunnels, t1 will hold the result.
     * start, end, startOut, endOut, playerOnlyTunnel are updated.
     * For each tile in t2, tunnel is replaced by t1
     */
    private void merge(Tunnel t1, Tunnel t2, TileInfo toAdd) {
        if (t1.getStartOut() == toAdd) {
            if (t2.getStartOut() == toAdd) {
                t1.setStart(t2.getEnd());
                t1.setStartOut(t2.getEndOut());
            } else {
                t1.setStart(t2.getStart());
                t1.setStartOut(t2.getStartOut());
            }
        } else {
            if (t2.getStartOut() == toAdd) {
                t1.setEnd(t2.getEnd());
                t1.setEndOut(t2.getEndOut());
            } else {
                t1.setEnd(t2.getStart());
                t1.setEndOut(t2.getStartOut());
            }
        }

        forEachNotWall((t) -> {
            if (t.getTunnel() == t2) {
                t.setTunnel(t1);
            }
        });

        toAdd.setTunnel(t1);
        t1.setPlayerOnlyTunnel(t1.isPlayerOnlyTunnel() && t2.isPlayerOnlyTunnel());
    }

    private void finishComputingTunnels() {
        for (int i = 0; i < tunnels.size(); i++) {
            Tunnel tunnel = tunnels.get(i);

            // compute tunnel exits
            tunnel.createTunnelExits();

            // compute oneway property
            if (tunnel.getStartOut() == null || tunnel.getEndOut() == null) {
                tunnel.setOneway(true);
            } else {
                tunnel.getStart().addCrate();
                corralDetector.findCorral(this, tunnel.getStartOut().getX(), tunnel.getStartOut().getY());
                tunnel.getStart().removeCrate();

                tunnel.setOneway(!tunnel.getEndOut().isReachable());
            }
        }
    }

    /**
     * Compute packing order. No crate should be on the board
     */
    public void tryComputePackingOrder() {
        isGoalRoomLevel = rooms.size() > 1;

        if (!isGoalRoomLevel) {
            return;
        }

        for (int i = 0; i < rooms.size(); i++) {
            Room r = rooms.get(i);
            if (r.isGoalRoom() && r.getTunnels().size() != 1) {
                isGoalRoomLevel = false;
                break;
            }
        }

        if (isGoalRoomLevel) {
            for (Room r : rooms) {
                if (r.isGoalRoom() && !computePackingOrder(r)) {
                    isGoalRoomLevel = false; // failed to compute packing order for a room...
                    break;
                }
            }
        }
    }

    /**
     * The room must have only one entrance and a packing room
     * @param room a room
     */
    private boolean computePackingOrder(Room room) {
        markSystem.unmarkAll();

        Tunnel tunnel = room.getTunnels().get(0);
        TileInfo entrance;
        TileInfo inRoom;
        if (tunnel.getStartOut() != null && tunnel.getStartOut().getRoom() == room) {
            entrance = tunnel.getStart();
            inRoom = tunnel.getStartOut();
        } else {
            entrance = tunnel.getEnd();
            inRoom = tunnel.getEndOut();
        }

        List<TileInfo> targets = room.getTargets();
        for (TileInfo t : targets) {
            t.addCrate();
        }

        List<TileInfo> packingOrder = new ArrayList<>();


        List<TileInfo> frontier = new ArrayList<>();
        List<TileInfo> newFrontier = new ArrayList<>();
        frontier.add(entrance);

        List<TileInfo> accessibleCrates = new ArrayList<>();
        findAccessibleCrates(frontier, newFrontier, accessibleCrates);

        while (!accessibleCrates.isEmpty()) {
            boolean hasChanged = false;

            for (int i = 0; i < accessibleCrates.size(); i++) {
                TileInfo crate = accessibleCrates.get(i);
                crate.removeCrate();
                inRoom.addCrate();

                if (crateAStar.hasPath(entrance, null, inRoom, crate)) {
                    accessibleCrates.remove(i);
                    i--;
                    crate.unmark();
                    crate.removeCrate();

                    // discover new accessible crates
                    frontier.add(crate);
                    findAccessibleCrates(frontier, newFrontier, accessibleCrates);

                    packingOrder.add(crate);
                    hasChanged = true;
                } else {
                    crate.addCrate();
                }

                inRoom.removeCrate();
            }

            if (!hasChanged) {
                for (TileInfo t : targets) {
                    t.removeCrate();
                }

                return false;
            }
        }


        for (TileInfo t : targets) {
            t.removeCrate();
        }

        Collections.reverse(packingOrder);
        room.setPackingOrder(packingOrder);

        return true;
    }

    /**
     * Find accessible crates using bfs from lastFrontier.
     *
     * @param lastFrontier starting point of the bfs
     * @param newFrontier a non-null list that will contain the next tile info to visit
     * @param out a list that will contain accessible crates
     */
    private void findAccessibleCrates(List<TileInfo> lastFrontier, List<TileInfo> newFrontier, List<TileInfo> out) {
        newFrontier.clear();

        for (int i = 0; i < lastFrontier.size(); i++) {
            TileInfo tile = lastFrontier.get(i);

            if (!tile.isMarked()) {
                tile.mark();
                if (tile.anyCrate()) {
                    out.add(tile);
                } else {
                    for (Direction dir : Direction.VALUES) {
                        TileInfo adj = tile.adjacent(dir);

                        if (!adj.isMarked() && !adj.isWall()) {
                            newFrontier.add(adj);
                        }
                    }
                }
            }
        }

        if (!newFrontier.isEmpty()) {
            findAccessibleCrates(newFrontier, lastFrontier, out);
        } else {
            lastFrontier.clear();
        }
    }

    private void computeTileToTargetsDistances() {

        List<Integer> targetIndices = new ArrayList<>();

        targetCount = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (this.content[y][x].isTarget() || this.content[y][x].isCrateOnTarget()) {
                    targetCount++;
                    targetIndices.add(getIndex(x, y));
                }
            }
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                final TileInfo t = getAt(x, y);

                int minDistToTarget = Integer.MAX_VALUE;
                int minDistToTargetIndex = -1;

                getAt(x, y).setTargets(new TileInfo.TargetRemoteness[targetIndices.size()]);

                for (int j = 0; j < targetIndices.size(); j++) {

                    final int targetIndex = targetIndices.get(j);
                    final int d = (t.isFloor() || t.isTarget()
                                   ? playerAStar.findPath(t, getAt(targetIndex), null, null).getDist() //t.manhattanDistance(getAt(targetIndex))
                                   : 0);


                    if (d < minDistToTarget) {
                        minDistToTarget = d;
                        minDistToTargetIndex = j;
                    }

                    getAt(x, y).getTargets()[j] = new TileInfo.TargetRemoteness(targetIndex, d);
                }
                Arrays.sort(getAt(x, y).getTargets());
                getAt(x, y).setNearestTarget(new TileInfo.TargetRemoteness(minDistToTargetIndex, minDistToTarget));
            }
        }
    }





    // * DYNAMIC *

    /**
     * Find reachable tiles
     * @param playerPos The indic of the case on which the player currently is.
     */
    public void findReachableCases(int playerPos) {
        findReachableCases(getAt(playerPos));
    }

    public void findReachableCases(TileInfo tile) {
        reachableMarkSystem.unmarkAll();
        findReachableCases_aux(tile);
    }

    private void findReachableCases_aux(TileInfo tile) {
        tile.setReachable(true);
        for (Direction d : Direction.VALUES) {
            TileInfo adjacent = tile.adjacent(d);

            // the second part of the condition avoids to check already processed cases
            if (!adjacent.isSolid() && !adjacent.isReachable()) {
                findReachableCases_aux(adjacent);
            }
        }
    }



    private int topX = 0;
    private int topY = 0;

    /**
     * This method compute the top left reachable position of the player of pushing a crate
     * at (crateToMoveX, crateToMoveY) to (destX, destY). It is used to calculate the position
     * of the player in a {@link State}.
     * This is also an example of use of {@link MarkSystem}
     *
     * @return the top left reachable position after pushing the crate
     * @see MarkSystem
     * @see Mark
     */
    @Override
    public int topLeftReachablePosition(TileInfo crate, TileInfo crateDest) {
        // temporary move the crate
        crate.removeCrate();
        crateDest.addCrate();

        topX = width;
        topY = height;

        markSystem.unmarkAll();
        topLeftReachablePosition_aux(crate);

        // undo
        crate.addCrate();
        crateDest.removeCrate();

        return topY * width + topX;
    }

    private void topLeftReachablePosition_aux(TileInfo tile) {
        if (tile.getY() < topY || (tile.getY() == topY && tile.getX() < topX)) {
            topX = tile.getX();
            topY = tile.getY();
        }

        tile.mark();
        for (Direction d : Direction.VALUES) {
            TileInfo adjacent = tile.adjacent(d);

            if (!adjacent.isSolid() && !adjacent.isMarked()) {
                topLeftReachablePosition_aux(adjacent);
            }
        }
    }


    // *********************
    // * GETTERS / SETTERS *
    // *********************

    public StaticBoard staticBoard() {
        return staticBoard;
    }

    /**
     * Returns the number of target i.e. tiles on which a crate has to be pushed to solve the level on the board
     * @return the number of target i.e. tiles on which a crate has to be pushed to solve the level on the board
     */
    public int getTargetCount() {
        return targetCount;
    }


    /**
     * Returns all tunnels that are in this board
     *
     * @return all tunnels that are in this board
     */
    public List<Tunnel> getTunnels() {
        return tunnels;
    }

    /**
     * Returns all rooms that are in this board
     *
     * @return all rooms that are in this board
     */
    public List<Room> getRooms() {
        return rooms;
    }

    public boolean isGoalRoomLevel() {
        return isGoalRoomLevel;
    }

    @Override
    public Corral getCorral(TileInfo tile) {
        return corralDetector.findCorral(tile);
    }

    @Override
    public CorralDetector getCorralDetector() {
        return corralDetector;
    }

    /**
     * Returns a {@linkplain MarkSystem mark system} that can be used to avoid checking twice  a tile
     *
     * @return a mark system
     * @see MarkSystem
     */
    public MarkSystem getMarkSystem() {
        return markSystem;
    }

    /**
     * Returns the {@linkplain MarkSystem mark system} used by the {@link #findReachableCases(int)} algorithm
     *
     * @return the reachable mark system
     * @see MarkSystem
     */
    public MarkSystem getReachableMarkSystem() {
        return reachableMarkSystem;
    }

    /**
     * Creates a {@linkplain MarkSystem mark system} that apply the specified reset
     * consumer to every <strong>non-wall</strong> {@linkplain TileInfo tile info}
     * that are in this {@linkplain  Board board}.
     *
     * @param reset the reset function
     * @return a new MarkSystem
     * @see MarkSystem
     * @see Mark
     */
    private MarkSystem newMarkSystem(Consumer<TileInfo> reset) {
        return new AbstractMarkSystem() {
            @Override
            public void reset() {
                mark = 0;
                forEachNotWall(reset);
            }
        };
    }

    protected class StaticBoard extends GenericBoard {

        private final List<ImmutableTunnel> tunnels;
        private final List<ImmutableRoom> rooms;

        public StaticBoard() {
            super(MutableBoard.this.width, MutableBoard.this.height);

            StaticTile[][] content = new StaticTile[height][width];
            this.content = content;

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    content[y][x] = new StaticTile(this, MutableBoard.this.content[y][x]);
                }
            }

            tunnels = MutableBoard.this.tunnels.stream()
                    .map((t) -> new ImmutableTunnel(this, t)).toList();
            rooms = MutableBoard.this.rooms.stream()
                    .map((r) -> new ImmutableRoom(this, r)).toList();

            linkTunnelsRoomsAndTileInfos(content);
        }

        private void linkTunnelsRoomsAndTileInfos(StaticTile[][] content) {
            Map<Room, ImmutableRoom> roomMap = new HashMap<>(rooms.size());
            for (int i = 0; i < rooms.size(); i++) {
                roomMap.put(MutableBoard.this.rooms.get(i), rooms.get(i));
            }

            Map<Tunnel, ImmutableTunnel> tunnelMap = new HashMap<>(tunnels.size());
            for (int i = 0; i < tunnels.size(); i++) {
                tunnelMap.put(MutableBoard.this.tunnels.get(i), tunnels.get(i));
            }

            // add rooms to tunnels
            List<Tunnel> originalTunnel = MutableBoard.this.tunnels;
            for (int i = 0; i < tunnels.size(); i++) {
                ImmutableTunnel t = tunnels.get(i);
                if (originalTunnel.get(i).rooms != null) {
                    t.rooms = originalTunnel.get(i).rooms.stream()
                            .map(r -> (Room) roomMap.get(r)).toList();
                }
            }

            // add tunnels to rooms
            List<Room> originalRooms = MutableBoard.this.rooms;
            for (int i = 0; i < rooms.size(); i++) {
                ImmutableRoom r = rooms.get(i);
                if (originalRooms.get(i).tunnels != null) {
                    r.tunnels = originalRooms.get(i).tunnels.stream()
                            .map(t -> (Tunnel) tunnelMap.get(t)).toList();
                }
            }

            // add tunnels, rooms to tile info
            for (int y = 0; y < getHeight(); y++) {
                for (int x = 0; x < getWidth(); x++) {
                    TileInfo original = MutableBoard.this.content[y][x];
                    StaticTile dest = content[y][x];

                    dest.tunnel = tunnelMap.get(original.getTunnel());
                    dest.room = roomMap.get(original.getRoom());

                    if (original.getTunnelExit() != null) {
                        dest.exit = original.getTunnelExit(); // it is immutable !
                    }
                }
            }
        }

        @Override
        public int getWidth() {
            return MutableBoard.this.getWidth();
        }

        @Override
        public int getHeight() {
            return MutableBoard.this.getHeight();
        }

        @Override
        public int getTargetCount() {
            return MutableBoard.this.getTargetCount();
        }

        @SuppressWarnings("unchecked")
        @Override
        public List<Tunnel> getTunnels() {
            return (List<Tunnel>) ((List<?>) tunnels); // this is black magic
        }

        @SuppressWarnings("unchecked")
        @Override
        public List<Room> getRooms() {
            return (List<Room>) ((List<?>) rooms); // more black magic !
        }

        @Override
        public boolean isGoalRoomLevel() {
            return MutableBoard.this.isGoalRoomLevel();
        }

        @Override
        public MarkSystem getMarkSystem() {
            return null;
        }

        @Override
        public MarkSystem getReachableMarkSystem() {
            return null;
        }
    }

    /**
     * A TileInfo that contains only static information
     */
    protected static class StaticTile extends GenericTileInfo {

        private final boolean deadTile;

        private final TargetRemoteness[] targets;
        private final TargetRemoteness nearestTarget;

        private ImmutableTunnel tunnel;
        private ImmutableRoom room;
        private Tunnel.Exit exit;

        public StaticTile(StaticBoard staticBoard, TileInfo tile) {
            super(staticBoard, removeCrate(tile.getTile()), tile.getX(), tile.getY());
            this.deadTile = tile.isDeadTile();

            if (tile.getTargets() == null) {
                targets = null;
            } else {
                targets = Arrays.copyOf(tile.getTargets(), tile.getTargets().length);
            }

            this.nearestTarget = tile.getNearestTarget();
        }

        private static Tile removeCrate(Tile tile) {
            if (tile == Tile.CRATE) {
                return Tile.FLOOR;
            } else if (tile == Tile.CRATE_ON_TARGET) {
                return Tile.TARGET;
            } else {
                return tile;
            }
        }

        @Override
        public boolean isDeadTile() {
            return deadTile;
        }

        @Override
        public boolean isReachable() {
            return false;
        }

        @Override
        public Tunnel getTunnel() {
            return tunnel;
        }

        @Override
        public Tunnel.Exit getTunnelExit() {
            return exit;
        }

        @Override
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
            return false;
        }

        @Override
        public TargetRemoteness getNearestTarget() {
            return nearestTarget;
        }

        @Override
        public TargetRemoteness[] getTargets() {
            return targets;
        }

        @Override
        public List<Corral> getAdjacentCorrals() {
            return null;
        }

        @Override
        public boolean isInABarrier() {
            return false;
        }
    }

    private static class ImmutableTunnel extends Tunnel {

        public ImmutableTunnel(StaticBoard board, Tunnel tunnel) {
            start = board.getAt(tunnel.start.getIndex());
            end = board.getAt(tunnel.end.getIndex());

            if (startOut != null) {
                startOut = board.getAt(tunnel.startOut.getIndex());
            }
            if (endOut != null) {
                endOut = board.getAt(tunnel.endOut.getIndex());
            }
            playerOnlyTunnel = tunnel.isPlayerOnlyTunnel();
            isOneway = tunnel.isOneway();
        }

        @Override
        public void createTunnelExits() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addRoom(Room room) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setStart(TileInfo start) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setEnd(TileInfo end) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setStartOut(TileInfo startOut) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setEndOut(TileInfo endOut) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setPlayerOnlyTunnel(boolean playerOnlyTunnel) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setCrateInside(boolean crateInside) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setOneway(boolean oneway) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean crateInside() {
            return false;
        }
    }

    private static class ImmutableRoom extends Room {

        public ImmutableRoom(StaticBoard board, Room room) {
            goalRoom = room.isGoalRoom();

            for (TileInfo t : room.getTiles()) {
                tiles.add(board.getAt(t.getIndex()));
            }
            for (TileInfo t : room.getTargets()) {
                targets.add(board.getAt(t.getIndex()));
            }
            if (room.getPackingOrder() != null) {
                packingOrder = new ArrayList<>();
                for (TileInfo t : room.getPackingOrder()) {
                    packingOrder.add(board.getAt(t.getIndex()));
                }
            }
        }

        @Override
        public void addTunnel(Tunnel tunnel) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addTile(TileInfo tile) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setGoalRoom(boolean goalRoom) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setPackingOrder(List<TileInfo> packingOrder) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setPackingOrderIndex(int packingOrderIndex) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getPackingOrderIndex() {
            return -1;
        }
    }
}
