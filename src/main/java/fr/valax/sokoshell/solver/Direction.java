package fr.valax.sokoshell.solver;

public enum Direction {

    LEFT(-1, 0),
    UP(0, -1),
    RIGHT(1, 0),
    DOWN(0, 1);

    private final int dirX;
    private final int dirY;

    public int dirX() { return dirX; }
    public int dirY() { return dirY; }

    Direction(int dirX, int dirY) {
        this.dirX = dirX;
        this.dirY = dirY;
    }
}
