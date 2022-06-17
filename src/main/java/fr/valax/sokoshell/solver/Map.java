package fr.valax.sokoshell.solver;

/**
 * @author darth-mole
 * @author PoulpoGaz
 */
public class Map {

    public static final int MINIMUM_WIDTH = 5;
    public static final int MINIMUM_HEIGHT = 5;

    private final Tile[][] content;
    private final int width;
    private final int height;

    public Map(Tile[][] content, int width, int height) {
        this.content = content;
        this.width = width;
        this.height = height;
    }

    public Map(Map other) {
        this.width = other.width;
        this.height = other.height;
        this.content = new Tile[height][width];

        for (int y = 0; y < height; y++) {
            System.arraycopy(other.content[y], 0, content[y], 0, width);
        }
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public int getX(int index) { return index % width; }
    public int getY(int index) { return index / width; }

    public Tile getAt(int index) {
        return content[getY(index)][getX(index)];
    }
    public Tile getAt(int x, int y) {
        return content[y][x];
    }

    public Tile safeGetAt(int x, int y, Tile ifOutside) {
        if (caseExists(x, y)) {
            return getAt(x, y);
        } else {
            return ifOutside;
        }
    }

    public Tile safeGetAt(int x, int y) {
        return safeGetAt(x, y, Tile.WALL);
    }

    public Tile safeGetAt(int x, int y, Direction dir, Tile ifOutside) {
        int x2 = x + dir.dirX();
        int y2 = y + dir.dirY();

        return safeGetAt(x2, y2, ifOutside);
    }

    public Tile safeGetAt(int x, int y, Direction dir) {
        return safeGetAt(x, y, dir, Tile.WALL);
    }

    public void setAt(int index, Tile tile) { content[getY(index)][getX(index)] = tile; }
    public void setAt(int x, int y, Tile tile) {
        content[y][x] = tile;
    }

    /**
     * Tells whether the case at (x,y) exists or not (i.e. if the case is in the map)
     * @param x x-coordinate
     * @param y y-coordinate
     * @return true if the case exists, false otherwise
     */
    public boolean caseExists(int x, int y) {
        return (0 <= x && x < width) && (0 <= y && y < height);
    }

    /**
     * Same than caseExists(x, y) but with an index
     * @param index index of the case
     * @return true if the case exists, false otherwise
     */
    public boolean caseExists(int index) {
        return caseExists(getX(index), getY(index));
    }

    /**
     * Tells whether the tile at the given coordinates is empty or not.
     * @param x x coordinate of the case
     * @param y y coordinate of the case
     * @return true if empty, false otherwise
     */
    public boolean isTileEmpty(int x, int y) {
        Tile t = getAt(x, y);
        return (t != Tile.WALL && t != Tile.CRATE && t != Tile.CRATE_ON_TARGET);
    }

    /**
     * Puts the crates of the given state in the content array.
     * @param state The state with the crates
     */
    public void addStateCrates(State state) {
        for (int i : state.cratesIndices()) {
            if (getAt(i) == Tile.TARGET) {
                setAt(i, Tile.CRATE_ON_TARGET);
            } else {
                setAt(i, Tile.CRATE);
            }
        }
    }

    /**
     * Removes the crates of the given state from the content array.
     * @param state The state with the crates
     */
    public void removeStateCrates(State state) {
        for (int i : state.cratesIndices()) {
            if (getAt(i) == Tile.CRATE_ON_TARGET) {
                setAt(i, Tile.TARGET);
            } else {
                setAt(i, Tile.FLOOR);
            }
        }
    }

    /**
     * Checks if the map is solved (i.e. all the crates are on a target)
     * /!\ The crates MUST have been put on the map for this function to work as expected.
     * @return true if the map is completed, false otherwise
     */
    public boolean isCompletedWith(State s) {
        for (int i : s.cratesIndices()) {
            if (getAt(i) != Tile.CRATE_ON_TARGET) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the map is completed (i.e. all the crates are on a target)
     * @return true if completed, false otherwise
     */
    public boolean isCompleted() {
        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++) {
                if (getAt(x, y) == Tile.CRATE) {
                    return false;
                }
            }
        }
        return true;
    }
}
