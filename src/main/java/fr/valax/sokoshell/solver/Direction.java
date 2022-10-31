package fr.valax.sokoshell.solver;

/**
 * A small but super useful enumeration. Contains all direction: left, up, right and down.
 *
 * @author PoulpogGaz
 * @author darth-mole
 */
public enum Direction {

    LEFT(-1, 0),
    UP(0, -1),
    RIGHT(1, 0),
    DOWN(0, 1);

    /**
     * Directions along the horizontal axis
     */
    public static final Direction[] HORIZONTAL = new Direction[] {LEFT, RIGHT};

    /**
     * Directions along the verical axis
     */
    public static final Direction[] VERTICAL = new Direction[] {UP, DOWN};

    private final int dirX;
    private final int dirY;

    Direction(int dirX, int dirY) {
        this.dirX = dirX;
        this.dirY = dirY;
    }

    public int dirX() { return dirX; }
    public int dirY() { return dirY; }

    public Direction negate() {
        return switch (this) {
            case DOWN -> UP;
            case UP -> DOWN;
            case LEFT -> RIGHT;
            case RIGHT -> LEFT;
        };
    }

    public static Direction of(int dirX, int dirY) {
        if (dirX == 0 && dirY == 0) {
            throw new IllegalArgumentException("(0,0) is not a direction");
        } else if (dirX == 0) {
            if (dirY < 0) {
                return UP;
            } else {
                return DOWN;
            }
        } else if (dirX < 0) {
            return LEFT;
        } else {
            return RIGHT;
        }
    }
}
