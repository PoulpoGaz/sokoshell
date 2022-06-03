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
}
