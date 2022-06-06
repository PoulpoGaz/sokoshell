package fr.valax.sokoshell.solver;

public class MutableMap extends Map {

    public MutableMap(Tile[][] content, int width, int height) {
        super(content, width, height);
    }

    public MutableMap(Map other) {
        super(other);
    }

    @Override
    public void setAt(int x, int y, Tile tile) {
        super.setAt(x, y, tile);
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
