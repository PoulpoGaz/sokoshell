package fr.valax.sokoshell.solver.board;

import fr.valax.sokoshell.solver.State;
import fr.valax.sokoshell.solver.board.mark.Mark;
import fr.valax.sokoshell.solver.board.mark.MarkSystem;
import fr.valax.sokoshell.solver.board.tiles.TileInfo;
import fr.valax.sokoshell.solver.board.tiles.Tile;
import fr.valax.sokoshell.solver.board.tiles.TileInfo;

import java.util.List;
import java.util.function.Consumer;

public interface Board {

    int MINIMUM_WIDTH = 5;
    int MINIMUM_HEIGHT = 5;

    // GETTERS //

    /**
     * Returns the width of the board
     *
     * @return the width of the board
     */
    int getWidth();

    /**
     * Returns the height of the board
     *
     * @return the height of the board
     */
    int getHeight();

    /**
     * Returns the number of target i.e. tiles on which a crate has to be pushed to solve the level) on the map
     *
     * @return the number of target i.e. tiles on which a crate has to be pushed to solve the level) on the map
     */
    int getTargetCount();

    /**
     * Convert an index to a position on the y-axis
     *
     * @param index the index to convert
     * @return the converted position
     */
    int getY(int index);

    /**
     * Convert an index to a position on the x-axis
     *
     * @param index the index to convert
     * @return the converted position
     */
    int getX(int index);

    /**
     * Convert a (x;y) position to an index
     *
     * @param x Coordinate on x-axis
     * @param y Coordinate on y-axis
     * @return the converted index
     */
    int getIndex(int x, int y);

    /**
     * Returns the {@link TileInfo} at the specific index
     *
     * @param index the index of the {@link TileInfo}
     * @return the TileInfo at the specific index
     * @throws IndexOutOfBoundsException if the index lead to a position outside the board
     * @see #getX(int)
     * @see #getY(int)
     * @see #safeGetAt(int)
     */
    TileInfo getAt(int index);

    /**
     * Returns the {@link TileInfo} at the specific index
     *
     * @param index the index of the {@link TileInfo}
     * @return the TileInfo at the specific index or {@code null}
     * if the index represent a position outside the board
     * @see #getX(int)
     * @see #getY(int)
     */
    TileInfo safeGetAt(int index);

    /**
     * Returns the {@link TileInfo} at the specific position
     *
     * @param x x the of the tile
     * @param y y the of the tile
     * @return the TileInfo at the specific coordinate
     * @throws IndexOutOfBoundsException if the position is outside the board
     * @see #safeGetAt(int, int)
     */
    TileInfo getAt(int x, int y);

    /**
     * Returns the {@link TileInfo} at the specific position
     *
     * @param x x the of the tile
     * @param y y the of the tile
     * @return the TileInfo at the specific index or {@code null}
     * if the index represent a position outside the board
     * @see #getX(int)
     * @see #getY(int)
     */
    TileInfo safeGetAt(int x, int y);

    /**
     * Returns the tile next to the tile at (x, y) according to dir
     */
    TileInfo safeGetAt(int x, int y, Direction dir);

    /**
     * Tells whether the case at (x,y) exists or not (i.e. if the case is in the board)
     *
     * @param x x-coordinate
     * @param y y-coordinate
     * @return {@code true} if the case exists, {@code false} otherwise
     */
    boolean caseExists(int x, int y);

    /**
     * Same than caseExists(x, y) but with an index
     *
     * @param index index of the case
     * @return {@code true} if the case exists, {@code false} otherwise
     * @see #caseExists(int, int)
     */
    boolean caseExists(int index);

    /**
     * Tells whether the tile at the given coordinates is empty or not.
     *
     * @param x x coordinate of the case
     * @param y y coordinate of the case
     * @return {@code true} if empty, {@code false} otherwise
     */
    boolean isTileEmpty(int x, int y);

    /**
     * Checks if the board is solved (i.e. all the crates are on a target).<br />
     * <strong>The crates MUSTileInfo have been put on the board for this function to work as expected.</strong>
     *
     * @return {@code true} if the board is completed, false otherwise
     */
    boolean isCompletedWith(State s);

    /**
     * Checks if the board is completed (i.e. all the crates are on a target)
     *
     * @return true if completed, false otherwise
     */
    boolean isCompleted();

    /**
     * Returns all tunnels that are in this map
     *
     * @return all tunnels that are in this map
     */
    List<Tunnel> getTunnels();

    /**
     * Returns all rooms that are in this map
     *
     * @return all rooms that are in this map
     */
    List<Room> getRooms();

    boolean isGoalRoomLevel();

    /**
     * Returns a {@linkplain MarkSystem mark system} that can be used to avoid checking twice  a tile
     *
     * @return a mark system
     * @see MarkSystem
     */
    MarkSystem getMarkSystem();

    /**
     * Returns the {@linkplain MarkSystem mark system} used by the {@link #findReachableCases(int)} algorithm
     *
     * @return the reachable mark system
     * @see MarkSystem
     */
    MarkSystem getReachableMarkSystem();


    // SETTERS //


    /**
     * Apply the consumer on every tile info
     *
     * @param consumer the consumer to apply
     */
    void forEach(Consumer<TileInfo> consumer);

    /**
     * Set at tile at the specified index. The index will be converted to
     * cartesian coordinate with {@link #getX(int)} and {@link  #getY(int)}
     *
     * @param index index in the map
     * @param tile  the new tile
     * @throws IndexOutOfBoundsException if the index lead to a position outside the map
     */
    void setAt(int index, Tile tile);

    /**
     * Set at tile at (x, y)
     *
     * @param x x position in the map
     * @param y y position in the map
     * @throws IndexOutOfBoundsException if the position is outside the map
     */
    void setAt(int x, int y, Tile tile);

    /**
     * Puts the crates of the given state in the content array.
     *
     * @param state The state with the crates
     */
    void addStateCrates(State state);

    /**
     * Removes the crates of the given state from the content array.
     *
     * @param state The state with the crates
     */
    void removeStateCrates(State state);

    /**
     * Puts the crates of the given state in the content array.
     * If a crate is outside the map, it doesn't throw an {@link IndexOutOfBoundsException}
     *
     * @param state The state with the crates
     */
    void safeAddStateCrates(State state);

    /**
     * Removes the crates of the given state from the content array.
     * If a crate is outside the map, it doesn't throw an {@link IndexOutOfBoundsException}
     *
     * @param state The state with the crates
     */
    void safeRemoveStateCrates(State state);

    // ===========================================
    // *         Methods used by solvers         *
    // * You need to call #initForSolver() first *
    // ===========================================

    /**
     * Initialize the map for solving:
     * <ul>
     *     <li>compute floor tiles: an array containing all non-wall tile</li>
     *     <li>compute {@linkplain #computeDeadTiles() dead tiles}</li>
     *     <li>find {@linkplain #findTunnels() tunnels}</li>
     * </ul>
     * <strong>The map must have no crate inside</strong>
     *
     * @see Tunnel
     */
    void initForSolver();

    /**
     * Creates or recreates the floor array. It is an array containing all tile info
     * that are not a wall
     */
    void computeFloors();

    /**
     * Apply the consumer on every tile info except walls
     *
     * @param consumer the consumer to apply
     */
    void forEachNotWall(Consumer<TileInfo> consumer);

    /**
     * Removes the crates of the given state from the content array.
     * It also does a small analyse of the state: set {@link Tunnel#crateInside()}
     * to true if there is effectively a crate inside
     *
     * @param state The state with the crates
     */
    void addStateCratesAndAnalyse(State state);

    /**
     * Removes the crates of the given state from the content array.
     * Also reset analyse did by {@link #addStateCratesAndAnalyse(State)}
     *
     * @param state The state with the crates
     */
    void removeStateCratesAndReset(State state);

    // ************
    // * ANALYSIS *
    // ************

    // * STATIC *

    /**
     * Detects the dead positions of a level. Dead positions are cases that make the level unsolvable
     * when a crate is put on them.
     * After this function has been called, to check if a given crate at (x,y) is a dead position,
     * you can use {@link TileInfo#isDeadTile()} to check in constant time.
     * The map <strong>MUST</strong> have <strong>NO CRATES</strong> for this function to work.
     */
    void computeDeadTiles();

    /**
     * Find tunnels. A tunnel is something like this:
     * <pre>
     *     $$$$$$
     *          $$$$$
     *     $$$$
     *        $$$$$$$
     * </pre>
     * <p>
     * A tunnel doesn't contain a target
     */
    void findTunnels();

    /**
     * Finds room based on tunnel. Basically all tile that aren't in a tunnel are in room.
     * This means that you need to call {@link #findTunnels()} before!
     * A room that contains a target is a packing room.
     */
    void findRooms();

    /**
     * Compute packing order. No crate should be on the map
     */
    void tryComputePackingOrder();

    // * DYNAMIC *

    /**
     * Find reachable tiles
     *
     * @param playerPos The indic of the case on which the player currently is.
     */
    void findReachableCases(int playerPos);

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
    int topLeftReachablePosition(int crateToMoveX, int crateToMoveY, int destX, int destY);

}
