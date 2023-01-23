package fr.valax.sokoshell.solver.board;

/**
 * A small but super useful enumeration. Contains all direction: {@link Direction#LEFT}, {@link Direction#UP},
 * {@link Direction#RIGHT} and {@link Direction#DOWN}.
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
     * Directions along the vertical axis
     */
    public static final Direction[] VERTICAL = new Direction[] {UP, DOWN};

    public static final Direction[] VALUES = new Direction[] {LEFT, UP, RIGHT, DOWN};

    private final int dirX;
    private final int dirY;

    Direction(int dirX, int dirY) {
        this.dirX = dirX;
        this.dirY = dirY;
    }

    public int dirX() { return dirX; }
    public int dirY() { return dirY; }

    /**
     * Rotate the rotation by 90°. For {@link Direction#UP} it returns {@link Direction#LEFT}
     *
     * @return the direction rotated by 90°
     */
    public Direction left() {
        return switch (this) {
            case DOWN -> RIGHT;
            case LEFT -> DOWN;
            case UP -> LEFT;
            case RIGHT -> UP;
        };
    }

    /**
     * Rotate the rotation by -90°. For {@link Direction#UP} it returns {@link Direction#RIGHT}
     *
     * @return the direction rotated by -90°
     */
    public Direction right() {
        return switch (this) {
            case DOWN -> LEFT;
            case LEFT -> UP;
            case UP -> RIGHT;
            case RIGHT -> DOWN;
        };
    }

    /**
     * @return The opposite direction (e.g for {@link Direction#LEFT} it returns {@link Direction#LEFT} etc.)
     */
    public Direction negate() {
        return switch (this) {
            case DOWN -> UP;
            case UP -> DOWN;
            case LEFT -> RIGHT;
            case RIGHT -> LEFT;
        };
    }

    /**
     * Creates a direction from two coordinates.
     * @param dirX If negative, returns {@link Direction#LEFT}, otherwise returns {@link Direction#RIGHT}
     * @param dirY If negative, return {@link Direction#UP}, otherwise returns {@link Direction#DOWN}
     * @return the direction
     */
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
