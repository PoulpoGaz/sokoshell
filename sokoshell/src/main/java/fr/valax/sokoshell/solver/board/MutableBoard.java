package fr.valax.sokoshell.solver.board;

import fr.valax.sokoshell.solver.State;
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

    private StaticBoard staticBoard;
    private boolean initialized = false; // was this board initialized by a solver ?

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
    }

    /**
     * Creates a copy of 'other'. It doesn't copy solver information
     *
     * @param other the board to copy
     */
    @SuppressWarnings("CopyConstructorMissesField")
    public MutableBoard(Board other) {
        super(other);

        this.content = new TileInfo[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                this.content[y][x] = new MutableTileInfo(this, other.getAt(x, y));
            }
        }
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
        tryComputePackingOrder();
        computeTileToTargetsDistances();
        initialized = true;
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
                tunnel.createTunnelExits();
                tunnels.add(tunnel);
            }
        });

        for (int i = 0; i < tunnels.size(); i++) {
            Tunnel t = tunnels.get(i);

            if (t.getStartOut() == null || t.getEndOut() == null) {
                t.setOneway(true);
            } else {
                t.getStart().addCrate();
                findReachableCases(t.getStartOut());
                t.getStart().removeCrate();

                t.setOneway(!t.getEndOut().isReachable());
            }
        }
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
                    return null;
                }
            }
        }

        if (pushDir1 == null) {
            return null;
        } else if (pushDir2 == null) {
            Tunnel tunnel = new Tunnel();
            tunnel.setStart(init);
            tunnel.setEnd(init);
            tunnel.setEnd(init.adjacent(pushDir1));
            init.setTunnel(tunnel);

            growTunnel(tunnel, init.adjacent(pushDir1), pushDir1);
            return tunnel;
        } else {
            boolean onlyPlayer = false;
            if (pushDir1.negate() != pushDir2) {
                if (init.adjacent(pushDir1).adjacent(pushDir2).isSolid()) {
                    onlyPlayer = true;
                } else {
                    return null;
                }
            }

            Tunnel tunnel = new Tunnel();
            tunnel.setEnd(init);
            tunnel.setEnd(init.adjacent(pushDir1));
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
            if (left.isSolid() && right.isSolid()) {
                if (front.isSolid() || front.isMarked()) {
                    t.setEnd(pos);
                    t.setEndOut(front);
                } else {
                    growTunnel(t, front, dir);
                }
                return;
            } else if (right.isSolid() && front.isSolid()) {
                t.setPlayerOnlyTunnel(true);
                if (left.isSolid() || left.isMarked()) {
                    t.setEnd(pos);
                    t.setEndOut(left);
                } else {
                    growTunnel(t, left, leftDir);
                }
                return;
            } else if (left.isSolid() && front.isSolid()) {
                t.setPlayerOnlyTunnel(true);
                if (right.isSolid() || right.isMarked()) {
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


    /**
     * Compute packing order. No crate should be on the board
     */
    public void tryComputePackingOrder() {
        isGoalRoomLevel = rooms.size() > 1;
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
    public int topLeftReachablePosition(int crateToMoveX, int crateToMoveY, int destX, int destY) {
        // temporary move the crate
        getAt(crateToMoveX, crateToMoveY).removeCrate();
        getAt(destX, destY).addCrate();

        topX = width;
        topY = height;

        markSystem.unmarkAll();
        topLeftReachablePosition_aux(getAt(crateToMoveX, crateToMoveY));

        // undo
        getAt(crateToMoveX, crateToMoveY).addCrate();
        getAt(destX, destY).removeCrate();

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
        if (staticBoard == null && initialized) {
            staticBoard = new StaticBoard();
        }

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

        public StaticBoard() {
            super(MutableBoard.this.width, MutableBoard.this.height);
            content = new TileInfo[height][width];

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    content[y][x] = new StaticTile(this, MutableBoard.this.content[y][x]);
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

        @Override
        public List<Tunnel> getTunnels() {
            return null;
        }

        @Override
        public List<Room> getRooms() {
            return null;
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

        public StaticTile(StaticBoard staticBoard, TileInfo tile) {
            super(staticBoard, removeCrate(tile.getTile()), tile.getX(), tile.getY());
            this.deadTile = tile.isDeadTile();

            this.targets = tile.getTargets();
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
        public TargetRemoteness getNearestTarget() {
            return nearestTarget;
        }

        @Override
        public TargetRemoteness[] getTargets() {
            return targets;
        }
    }

    private static class ImmutableTunnel extends Tunnel {

    }

    private static class ImmutableRoom extends Room {

    }
}
