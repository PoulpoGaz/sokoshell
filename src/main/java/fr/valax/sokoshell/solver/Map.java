package fr.valax.sokoshell.solver;

import java.util.ArrayList;

/**
 * @author darth-mole
 * @author PoulpoGaz
 */
public class Map {
    private ArrayList<CaseContent> content = new ArrayList<>();
    private int width;
    private int height;

    /**
     * Loads the map from the given file
     * @param fileName the file from which the map is loaded
     */
    public void load(String fileName) {

    }
    public CaseContent getContentAt(int index) {
        assert 0 <= index && index <= content.size();
        return content.get(index);
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
}
