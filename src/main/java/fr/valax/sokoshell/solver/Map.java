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

    public Tile[][] getContent() {
        return content;
    }

    /**
     * Give the index in the content array from (x,y) coordinates
     * @param x x-coordinate of the case
     * @param y y-coordinate of the case
     * @return the corresponding index
     */
    public int coordsToIndex(int x, int y) {
        return y * width + x;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
