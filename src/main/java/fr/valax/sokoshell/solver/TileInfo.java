package fr.valax.sokoshell.solver;

import fr.valax.sokoshell.solver.mark.Mark;

/**
 * TileInfo stores information about a Tile. It stores:
 * <ul>
 *     <li>
 *         Static information
 *         <ul>
 *             <li>the {@link Map}</li>
 *             <li>the position</li>
 *             <li>the {@link Tile}</li>
 *             <li>if tile is a 'dead tile'</li>
 *         </ul>
 *     </li>
 *     <li>
 *         Dynamic information
 *         <ul>
 *             <li>if the tile is reachable</li>
 *             <li>a mark</li>
 *         </ul>
 *     </li>
 * </ul>
 *
 * @author PoulpoGaz
 */
public class TileInfo {

    private final Map map;
    private final int x;
    private final int y;

    private Tile tile;

    // Static information
    private boolean deadTile;

    /**
     * The tunnel in which this tile is. A Tile is either in a room or in a tunnel
     */
    private Tunnel tunnel;
    private Room room;

    // Dynamic information
    private final Mark reachable;
    private final Mark mark;

    /**
     * Create a new TileInfo
     *
     * @param map the map in which this TileInfo is
     * @param tile the tile
     * @param x the position on the x-axis in the map
     * @param y the position on the y-axis in the map
     */
    public TileInfo(Map map, Tile tile, int x, int y) {
        this.map = map;
        this.tile = tile;
        this.x = x;
        this.y = y;
        this.reachable = map.getReachableMarkSystem().newMark();
        this.mark = map.getMarkSystem().newMark();
    }

    /**
     * Copy a TileInfo to another map
     *
     * @param map the map that will contain the copied TileInfo
     * @return the copied TileInfo in the new map
     */
    public TileInfo copyTo(Map map) {
        TileInfo t = new TileInfo(map, tile, x, y);
        t.set(this);
        return t;
    }

    /**
     * Copy the information of other into this tile info
     *
     * @param other the TileInfo from which we extract information
     */
    public void set(TileInfo other) {
        tile = other.tile;
        deadTile = other.deadTile;
        setReachable(other.isReachable());
        mark.setMarked(other.isMarked());
    }

    /**
     * If this was a floor, this is now a crate
     * If this was a target, this is now a crate on target
     */
    public void addCrate() {
        if (tile == Tile.FLOOR) {
            tile = Tile.CRATE;
        } else if (tile == Tile.TARGET) {
            tile = Tile.CRATE_ON_TARGET;
        }
    }

    /**
     * If this was a crate, this is now a floor
     * If this was a crate on target, this is now a target
     */
    public void removeCrate() {
        if (tile == Tile.CRATE) {
            tile = Tile.FLOOR;
        } else if (tile == Tile.CRATE_ON_TARGET) {
            tile = Tile.TARGET;
        }
    }

    /**
     * @param dir the direction
     * @return the tile that is adjacent to this TileInfo in the {@link Direction} dir
     * @throws IndexOutOfBoundsException if this TileInfo is near the border of the map and
     * the direction point outside the emap
     */
    public TileInfo adjacent(Direction dir) {
        return map.getAt(x + dir.dirX(), y + dir.dirY());
    }

    /**
     * @param dir the direction
     * @return the tile that is adjacent to this TileInfo in the {@link Direction} dir
     * or {@code null} if the adjacent tile is outside the map
     */
    public TileInfo safeAdjacent(Direction dir) {
        return map.safeGetAt(x + dir.dirX(), y + dir.dirY());
    }

    public Direction direction(TileInfo other) {
        return Direction.of(other.x - x, other.y - y);
    }

    /**
     * @return true if there is a crate at this position
     */
    public boolean anyCrate() {
        return tile.isCrate();
    }

    /**
     * @return true if there is a wall or a crate at this position
     */
    public boolean isSolid() {
        return tile.isSolid();
    }

    /**
     * @return true if this TileInfo is exactly a floor
     */
    public boolean isFloor() {
        return tile == Tile.FLOOR;
    }

    /**
     * @return true if this TileInfo is exactly a wall
     */
    public boolean isWall() {
        return tile == Tile.WALL;
    }

    /**
     * @return true if this TileInfo is exactly a target
     */
    public boolean isTarget() {
        return tile == Tile.TARGET;
    }

    /**
     * @return true if this TileInfo is exactly a crate
     * @see #anyCrate()
     */
    public boolean isCrate() {
        return tile == Tile.CRATE;
    }

    /**
     * @return true if this TileInfo is exactly a crate on target
     * @see #anyCrate()
     */
    public boolean isCrateOnTarget() {
        return tile == Tile.CRATE_ON_TARGET;
    }

    /**
     * @return which tile is this TileInfo
     */
    public Tile getTile() {
        return tile;
    }

    /**
     * WARNING: This method doesn't recalculate information.
     * @param tile the new tile
     */
    public void setTile(Tile tile) {
        this.tile = tile;
    }

    /**
     * @return the position of this TileInfo on the x-axis
     */
    public int getX() {
        return x;
    }

    /**
     * @return the position of this TileInfo on the y-axis
     */
    public int getY() {
        return y;
    }

    /**
     * @return {@code true} if this tile is a dead tile
     * @see Map#computeDeadTiles()
     */
    public boolean isDeadTile() {
        return deadTile;
    }

    /**
     * Sets this tile as a dead tile or not
     * @see Map#computeDeadTiles() ()
     */
    public void setDeadTile(boolean deadTile) {
        this.deadTile = deadTile;
    }


    /**
     * @return {@code true} if this tile is reachable by the player.
     * @see Map#findReachableCases(int)
     */
    public boolean isReachable() {
        return reachable.isMarked();
    }

    /**
     * Sets this tile as reachable or not by the player. It doesn't check if it's possible.
     * @see Map#findReachableCases(int)
     */
    public void setReachable(boolean reachable) {
        this.reachable.setMarked(reachable);
    }

    /**
     * Returns the tunnel in which this tile is
     *
     * @return the tunnel in which this tile is
     */
    public Tunnel getTunnel() {
        return tunnel;
    }

    /**
     * Sets the tunnel in which this tile is
     */
    public void setTunnel(Tunnel tunnel) {
        this.tunnel = tunnel;
    }

    /**
     * Returns {@code true} if this tile info is in a tunnel
     *
     * @return {@code true} if this tile info is in a tunnel
     */
    public boolean isInATunnel() {
        return tunnel != null;
    }

    /**
     * Returns the room in which this tile is
     *
     * @return the room in which this tile is
     */
    public Room getRoom() {
        return room;
    }

    /**
     * Sets the room in which this tile is
     */
    public void setRoom(Room room) {
        this.room = room;
    }

    /**
     * Returns {@code true} if this tile info is in a room
     *
     * @return {@code true} if this tile info is in a room
     */
    public boolean isInARoom() {
        return room != null;
    }

    /**
     * Sets this tile as marked
     * @see Mark
     * @see fr.valax.sokoshell.solver.mark.MarkSystem
     */
    public void mark() {
        mark.mark();
    }

    /**
     * Sets this tile as unmarked
     * @see Mark
     * @see fr.valax.sokoshell.solver.mark.MarkSystem
     */
    public void unmark() {
        mark.unmark();
    }

    /**
     * Sets this tile as marked or not
     * @see Mark
     * @see fr.valax.sokoshell.solver.mark.MarkSystem
     */
    public void setMarked(boolean marked) {
        mark.setMarked(marked);
    }

    /**
     * @return {@code true} if this tile is marked
     * @see Mark
     * @see fr.valax.sokoshell.solver.mark.MarkSystem
     */
    public boolean isMarked() {
        return mark.isMarked();
    }


    /**
     * Returns {@code true} if this tile is at the same position as 'other'
     * @param other other tile
     * @return {@code true} if this tile is at the same position as 'other'
     */
    public boolean isAt(TileInfo other) {
        return isAt(other.x, other.y);
    }

    /**
     * Returns {@code true} if this tile is at the position (x; y)
     * @param x x location
     * @param y y location
     * @return {@code true} if this tile is at the position (x; y)}
     */
    public boolean isAt(int x, int y) {
        return x == this.x && y == this.y;
    }

    @Override
    public String toString() {
        return tile.toString();
    }
}
