package fr.valax.sokoshell.solver;

public class Map {
    private Tile[][] content;
    private int width;
    private int height;

    public int getWidth() { return width; }
    public int getHeight() { return height; }

    /**
     * Loads the map from the given file
     * @param fileName the file from which the map is loaded
     */
    public void load(String fileName) {

    }

    public int getX(int index) { return index % width; }
    public int getY(int index) { return index / width; }

    public Tile getAt(int index) {
        return content[getY(index)][getX(index)];
    }
    public Tile getAt(int x, int y) {
        return content[y][x];
    }

    private void setAt(int index, Tile tile) { content[getY(index)][getX(index)] = tile; }
    private void setAt(int x, int y, Tile tile) {
        content[y][x] = tile;
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
}
