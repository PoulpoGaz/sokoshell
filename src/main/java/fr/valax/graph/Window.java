package fr.valax.graph;

public class Window {

    public int minX;
    public int minY;

    public int maxX;
    public int maxY;

    public int getWidth(Scale scale) {
        return scale.getInGraph(maxX - minX);
    }

    public int getHeight(Scale scale) {
        return scale.getInGraph(maxY - minY);
    }

    public boolean isValid() {
        return maxX - minX > 0 && maxY - minY > 0;
    }

    @Override
    public String toString() {
        return "Window{" +
                "minX=" + minX +
                ", minY=" + minY +
                ", maxX=" + maxX +
                ", maxY=" + maxY +
                '}';
    }
}