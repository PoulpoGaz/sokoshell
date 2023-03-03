package fr.valax.sokoshell.solver.board;

import fr.valax.sokoshell.solver.Corral;
import fr.valax.sokoshell.solver.CorralDetector;
import fr.valax.sokoshell.solver.State;
import fr.valax.sokoshell.solver.board.mark.Mark;
import fr.valax.sokoshell.solver.board.mark.MarkSystem;
import fr.valax.sokoshell.solver.board.tiles.Tile;
import fr.valax.sokoshell.solver.board.tiles.TileInfo;

import java.util.List;
import java.util.function.Consumer;

/**
 * Represents the Sokoban board.<br />
 * This interface defines getters setters for the properties of a Sokoban board, e.g. the width, the height etc.
 * Implementations of this interface are meant to be used with a {@link TileInfo} implementation.
 * This class also defines static and dynamic analysis of the Sokoban board, for instance for solving purposes.
 * Such properties are the following:
 * <ul>
 *     <li>Static</li>
 *     <ul>
 *         <li>Dead positions: cases that make the level unsolvable when a crate is pushed on them</li>
 *     </ul>
 *     <li>Dynamic</li>
 *     <ul>
 *         <li>Reachable cases: cases that the player can reach according to his position</li>
 *     </ul>
 * </ul>
 *
 * @see TileInfo
 */
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
     * Returns the number of target i.e. tiles on which a crate has to be pushed to solve the level on the board
     *
     * @return the number of target i.e. tiles on which a crate has to be pushed to solve the level on the board
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
    default TileInfo safeGetAt(int index) {
        int x = getX(index);
        int y = getY(index);

        if (caseExists(x, y)) {
            return getAt(x, y);
        } else {
            return null;
        }
    }

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
    default TileInfo safeGetAt(int x, int y) {
        if (caseExists(x, y)) {
            return getAt(x, y);
        } else {
            return null;
        }
    }

    /**
     * Tells whether the case at (x,y) exists or not (i.e. if the case is in the board)
     *
     * @param x x-coordinate
     * @param y y-coordinate
     * @return {@code true} if the case exists, {@code false} otherwise
     */
    default boolean caseExists(int x, int y) {
        return (0 <= x && x < getWidth()) && (0 <= y && y < getHeight());
    }

    /**
     * Same than caseExists(x, y) but with an index
     *
     * @param index index of the case
     * @return {@code true} if the case exists, {@code false} otherwise
     * @see #caseExists(int, int)
     */
    default boolean caseExists(int index) {
        return caseExists(getX(index), getY(index));
    }

    /**
     * Tells whether the tile at the given coordinates is empty or not.
     *
     * @param x x coordinate of the case
     * @param y y coordinate of the case
     * @return {@code true} if empty, {@code false} otherwise
     */
    default boolean isTileEmpty(int x, int y) {
        TileInfo t = getAt(x, y);
        return !t.isSolid();
    }

    /**
     * Checks if the board is solved (i.e. all the crates are on a target).<br />
     * <strong>The crates MUSTileInfo have been put on the board for this function to work as expected.</strong>
     *
     * @return {@code true} if the board is completed, false otherwise
     */
    default boolean isCompletedWith(State s) {
        for (int i : s.cratesIndices()) {
            if (!getAt(i).isCrateOnTarget()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the board is completed (i.e. all the crates are on a target)
     *
     * @return true if completed, false otherwise
     */
    default boolean isCompleted() {
        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++) {
                if (getAt(x, y).isCrate()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Returns all tunnels that are in this board
     *
     * @return all tunnels that are in this board
     */
    List<Tunnel> getTunnels();

    /**
     * Returns all rooms that are in this board
     *
     * @return all rooms that are in this board
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
     * @param index index in the board
     * @param tile  the new tile
     * @throws IndexOutOfBoundsException if the index lead to a position outside the board
     */
    void setAt(int index, Tile tile);

    /**
     * Set at tile at (x, y)
     *
     * @param x x position in the board
     * @param y y position in the board
     * @throws IndexOutOfBoundsException if the position is outside the board
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
     * If a crate is outside the board, it doesn't throw an {@link IndexOutOfBoundsException}
     *
     * @param state The state with the crates
     */
    void safeAddStateCrates(State state);

    /**
     * Removes the crates of the given state from the content array.
     * If a crate is outside the board, it doesn't throw an {@link IndexOutOfBoundsException}
     *
     * @param state The state with the crates
     */
    void safeRemoveStateCrates(State state);

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
     * Compute which tunnel contains a crate
     * @param state current state
     */
    void computeTunnelStatus(State state);

    /**
     * Compute packing order progress for each room if the level
     * is a goal room level
     * @param state current state
     */
    void computePackingOrderProgress(State state);

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
     * Compute packing order. No crate should be on the board
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
     * at crate to crateDest. It is used to calculate the position
     * of the player in a {@link State}.
     * This is also an example of use of {@link MarkSystem}
     *
     * @return the top left reachable position after pushing the crate
     * @see MarkSystem
     * @see Mark
     */
    int topLeftReachablePosition(TileInfo crate, TileInfo crateDest);

    /**
     * @param tile tile
     * @return the corral in which {@code tile} is
     */
    Corral getCorral(TileInfo tile);

    /**
     * @return the {@link CorralDetector} used to find corrals
     */
    CorralDetector getCorralDetector();
}
