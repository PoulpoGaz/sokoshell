package fr.valax.sokoshell.solver.board.tiles;

import fr.valax.sokoshell.solver.board.*;
import fr.valax.sokoshell.solver.board.mark.Mark;
import fr.valax.sokoshell.solver.board.mark.MarkSystem;

public interface ITileInfo<T extends GenericTileInfo<T, B>, B extends GenericBoard<T>> {

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
    boolean isAt(T other);

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
    Direction direction(T other);

    /**
     * Returns the distance of manhattan between this tile and other
     *
     * @param other 'other' tile
     * @return the distance of manhattan between this tile and other
     */
    int manhattanDistance(T other);

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
     * @throws IndexOutOfBoundsException if this TileInfo is near the border of the map and
     * the direction point outside the emap
     */
    T adjacent(Direction dir);

    /**
     * @param dir the direction
     * @return the tile that is adjacent to this TileInfo in the {@link Direction} dir
     * or {@code null} if the adjacent tile is outside the map
     */
    T safeAdjacent(Direction dir);

    /**
     * Returns the board in which this tile is
     *
     * @return the board in which this tile is
     */
    B getBoard();

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

}
