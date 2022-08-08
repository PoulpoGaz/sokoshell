package fr.valax.sokoshell.solver;

public class TileInfo {

    private final Map map;
    private final int x;
    private final int y;

    private Tile tile;

    // Static information
    private boolean deadTile;

    // Dynamic information
    private boolean reachable; // from Map.playerX; Map.playerY


    // marker for CrateIterator
    private boolean mark = false;

    public TileInfo(Map map, Tile tile, int x, int y) {
        this.map = map;
        this.tile = tile;
        this.x = x;
        this.y = y;
    }

    public TileInfo(Map map, TileInfo tile) {
        this.map = map;
        this.x = tile.x;
        this.y = tile.y;
        this.tile = tile.tile;
        this.deadTile = tile.deadTile;
        this.reachable = tile.reachable;
        this.mark = tile.mark;
    }

    public void set(TileInfo other) {
        tile = other.tile;
        deadTile = other.deadTile;
        reachable = other.reachable;
        mark = other.mark;
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
        reachable = true;
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
        return reachable;
    }

    public void setReachable(boolean reachable) {
        this.reachable = reachable;
    }

    public void mark() {
        mark = true;
    }

    public void unmark() {
        mark = false;
    }

    public boolean isMarked() {
        return mark;
    }

    @Override
    public String toString() {
        return tile.toString();
    }
}
