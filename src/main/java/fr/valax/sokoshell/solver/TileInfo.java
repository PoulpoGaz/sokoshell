package fr.valax.sokoshell.solver;

import fr.valax.sokoshell.solver.mark.Mark;

import java.util.ArrayList;

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
 */
public class TileInfo {

    private final Map map;
    private final int x;
    private final int y;

    private Tile tile;

    // Static information
    private boolean deadTile;

    // Dynamic information
    private final Mark reachable;
    private final Mark mark;

    public TileInfo(Map map, Tile tile, int x, int y) {
        this.map = map;
        this.tile = tile;
        this.x = x;
        this.y = y;
        this.reachable = map.getReachableMarkSystem().newMark();
        this.mark = map.getMarkSystem().newMark();
    }

    public TileInfo(Map map, TileInfo tile) {
        this(map, tile.getTile(), tile.getX(), tile.getY());
        set(tile);
    }

    public void set(TileInfo other) {
        tile = other.tile;
        deadTile = other.deadTile;
        setReachable(other.isReachable());
        mark.setMarked(other.isMarked());
    }

    public void addCrate() {
        if (tile == Tile.FLOOR) {
            tile = Tile.CRATE;
        } else if (tile == Tile.TARGET) {
            tile = Tile.CRATE_ON_TARGET;
        }
    }

    public void removeCrate() {
        if (tile == Tile.CRATE) {
            tile = Tile.FLOOR;
        } else if (tile == Tile.CRATE_ON_TARGET) {
            tile = Tile.TARGET;
        }
    }

    public TileInfo adjacent(Direction dir) {
        return map.getAt(x + dir.dirX(), y + dir.dirY());
    }

    public TileInfo safeAdjacent(Direction dir) {
        return map.safeGetAt(x + dir.dirX(), y + dir.dirY());
    }

    public void resetDynamicInformation() {

    }

    public boolean anyCrate() {
        return tile.isCrate();
    }

    public boolean isSolid() {
        return tile.isSolid();
    }

    public boolean isFloor() {
        return tile == Tile.FLOOR;
    }

    public boolean isWall() {
        return tile == Tile.WALL;
    }

    public boolean isTarget() {
        return tile == Tile.TARGET;
    }

    public boolean isCrate() {
        return tile == Tile.CRATE;
    }

    public boolean isCrateOnTarget() {
        return tile == Tile.CRATE_ON_TARGET;
    }

    public Tile getTile() {
        return tile;
    }

    /**
     * WARNING: The method doesn't recalculate information.
     * @param tile the new tile
     */
    public void setTile(Tile tile) {
        this.tile = tile;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isDeadTile() {
        return deadTile;
    }

    public void setDeadTile(boolean deadTile) {
        this.deadTile = deadTile;
    }


    public boolean isReachable() {
        return reachable.isMarked();
    }

    public void setReachable(boolean reachable) {
        this.reachable.setMarked(reachable);
    }


    public void mark() {
        mark.mark();
    }

    public void unmark() {
        mark.unmark();
    }

    public void setMarked(boolean marked) {
        mark.setMarked(marked);
    }

    public boolean isMarked() {
        return mark.isMarked();
    }


    @Override
    public String toString() {
        return tile.toString();
    }
}
