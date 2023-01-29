package fr.valax.sokoshell.solver.board.tiles;

import fr.valax.sokoshell.solver.board.*;
import fr.valax.sokoshell.solver.board.mark.Mark;
import fr.valax.sokoshell.solver.board.mark.MarkSystem;

/**
 * Stores information about a tile.
 */
public interface TileInfo {

    // GETTERS //

    /**
     * @return true if there is a crate at this position
     */
    boolean anyCrate();

    /**
     * @return true if there is a wall or a crate at this position
     */
    boolean isSolid();

    /**
     * @return true if this TileInfo is exactly a floor
     */
    boolean isFloor();

    /**
     * @return true if this TileInfo is exactly a wall
     */
    boolean isWall();

    /**
     * @return true if this TileInfo is exactly a target
     */
    boolean isTarget();

    /**
     * @return true if this TileInfo is exactly a crate
     * @see #anyCrate()
     */
    boolean isCrate();

    /**
     * @return true if this TileInfo is exactly a crate on target
     * @see #anyCrate()
     */
    boolean isCrateOnTarget();

    /**
     * @return which tile is this TileInfo
     */
    Tile getTile();

    /**
     * @return the position of this TileInfo on the x-axis
     */
    int getX();

    /**
     * @return the position of this TileInfo on the y-axis
     */
    int getY();

    /**
     * Returns {@code true} if this tile is at the same position as 'other'
     * @param other other tile
     * @return {@code true} if this tile is at the same position as 'other'
     */
    boolean isAt(TileInfo other);

    /**
     * Returns {@code true} if this tile is at the position (x; y)
     * @param x x location
     * @param y y location
     * @return {@code true} if this tile is at the position (x; y)}
     */
    boolean isAt(int x, int y);

    /**
     * Returns the direction between this tile and other.
     *
     * @param other 'other' tile
     * @return the direction between this tile and other
     */
    Direction direction(TileInfo other);

    /**
     * Returns the distance of manhattan between this tile and other
     *
     * @param other 'other' tile
     * @return the distance of manhattan between this tile and other
     */
    int manhattanDistance(TileInfo other);

    /**
     * @return {@code true} if this tile is a dead tile
     * @see MutableBoard#computeDeadTiles()
     */
    boolean isDeadTile();

    /**
     * @return {@code true} if this tile is reachable by the player.
     * @see MutableBoard#findReachableCases(int)
     */
    boolean isReachable();

    /**
     * Returns the tunnel in which this tile is
     *
     * @return the tunnel in which this tile is
     */
    Tunnel getTunnel();

    /**
     * Returns the {@link Tunnel.Exit} object associated with this tile info.
     * If the tile isn't in a tunnel, it returns null
     *
     * @return the {@link Tunnel.Exit} object associated with this tile info or {@code null
     * @see Tunnel.Exit
     */
    Tunnel.Exit getTunnelExit();

    /**
     * Returns {@code true} if this tile info is in a tunnel
     *
     * @return {@code true} if this tile info is in a tunnel
     */
    boolean isInATunnel();

    /**
     * Returns the room in which this tile is
     *
     * @return the room in which this tile is
     */
    Room getRoom();

    /**
     * Returns {@code true} if this tile info is in a room
     *
     * @return {@code true} if this tile info is in a room
     */
    boolean isInARoom();

    /**
     * @return {@code true} if this tile is marked
     * @see Mark
     * @see MarkSystem
     */
    boolean isMarked();

    /**
     * @param dir the direction
     * @return the tile that is adjacent to this TileInfo in the {@link Direction} dir
     * @throws IndexOutOfBoundsException if this TileInfo is near the border of the board and
     * the direction point outside the board
     */
    TileInfo adjacent(Direction dir);

    /**
     * @param dir the direction
     * @return the tile that is adjacent to this TileInfo in the {@link Direction} dir
     * or {@code null} if the adjacent tile is outside the board
     */
    TileInfo safeAdjacent(Direction dir);

    /**
     * Returns the board in which this tile is
     *
     * @return the board in which this tile is
     */
    Board getBoard();

    int positionHashCode();

    TargetRemoteness getNearestTarget();

    TargetRemoteness[] getTargets();

    record TargetRemoteness(int index, int distance) implements Comparable<TargetRemoteness> {

        @Override
        public int compareTo(TargetRemoteness other) {
            return this.distance - other.distance;
        }

        @Override
        public String toString() {
            return "TR[d=" + distance + ", i=" + index + "]";
        }
    }


    // SETTERS //

    /**
     * Copy the information of other into this tile info
     *
     * @param other the TileInfo from which we extract information
     * @throws UnsupportedOperationException if the {@code set} operation isn't
     * supported by this TileInfo
     */
    void set(TileInfo other);

    /**
     * If this was a floor, this is now a crate
     * If this was a target, this is now a crate on target
     * @throws UnsupportedOperationException if the {@code addCrate} operation isn't
     * supported by this TileInfo
     */
    void addCrate();

    /**
     * If this was a crate, this is now a floor
     * If this was a crate on target, this is now a target
     * @throws UnsupportedOperationException if the {@code removeCrate} operation isn't
     * supported by this TileInfo
     */
    void removeCrate();

    /**
     * Sets the tile.
     * @param tile the new tile
     * @throws UnsupportedOperationException if the {@code setTile} operation isn't
     * supported by this TileInfo
     */
    void setTile(Tile tile);

    /**
     * Sets this tile as a dead tile or not
     * @throws UnsupportedOperationException if the {@code setDeadTile} operation isn't
     * supported by this TileInfo
     * @see MutableBoard#computeDeadTiles()
     */
    void setDeadTile(boolean deadTile);

    /**
     * Sets this tile as reachable or not by the player. It doesn't check if it's possible.
     * @throws UnsupportedOperationException if the {@code setReachable} operation isn't
     * supported by this TileInfo
     * @see MutableBoard#findReachableCases(int)
     */
    void setReachable(boolean reachable);

    /**
     * Sets the tunnel in which this tile is
     * @throws UnsupportedOperationException if the {@code setTunnel} operation isn't
     * supported by this TileInfo
     */
    void setTunnel(Tunnel tunnel);

    /**
     * Sets the {@link Tunnel.Exit} object associated with this tile info
     * @throws UnsupportedOperationException if the {@code setTunnelExit} operation isn't
     * supported by this TileInfo
     * @see Tunnel.Exit
     */
    void setTunnelExit(Tunnel.Exit tunnelExit);

    /**
     * Sets the room in which this tile is
     * @throws UnsupportedOperationException if the {@code setRoom} operation isn't
     * supported by this TileInfo
     */
    void setRoom(Room room);

    /**
     * Sets this tile as marked
     * @throws UnsupportedOperationException if the {@code mark} operation isn't
     * supported by this TileInfo
     * @see Mark
     * @see MarkSystem
     */
    void mark();

    /**
     * Sets this tile as unmarked
     * @throws UnsupportedOperationException if the {@code unmark} operation isn't
     * supported by this TileInfo
     * @see Mark
     * @see MarkSystem
     */
    void unmark();

    /**
     * Sets this tile as marked or not
     * @throws UnsupportedOperationException if the {@code setMarked} operation isn't
     * supported by this TileInfo
     * @see Mark
     * @see MarkSystem
     */
    void setMarked(boolean marked);

    /**
     * Set the distance to every targets
     * @param targets distance to every targets
     * @throws UnsupportedOperationException if the {@code setTargets} operation isn't
     * supported by this TileInfo
     */
    void setTargets(TargetRemoteness[] targets);

    /**
     * Set the nearest target
     * @param nearestTarget nearest target
     * @throws UnsupportedOperationException if the {@code setNearestTarget} operation isn't
     * supported by this TileInfo
     */
    void setNearestTarget(TargetRemoteness nearestTarget);
}
