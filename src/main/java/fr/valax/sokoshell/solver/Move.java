package fr.valax.sokoshell.solver;

/**
 * An enumeration representing a move or a push in a solution. The {@code moveCrate} flag is needed to go back
 * in {@link fr.valax.sokoshell.commands.level.SolutionCommand}
 */
public enum Move {

    LEFT(Direction.LEFT, false),
    UP(Direction.UP, false),
    DOWN(Direction.DOWN, false),
    RIGHT(Direction.RIGHT, false),

    LEFT_PUSH(Direction.LEFT, true),
    UP_PUSH(Direction.UP, true),
    RIGHT_PUSH(Direction.RIGHT, true),
    DOWN_PUSH(Direction.DOWN, true);

    private final Direction direction;
    private final boolean moveCrate;

    Move(Direction direction, boolean moveCrate) {
        this.direction = direction;
        this.moveCrate = moveCrate;
    }

    public Direction direction() {
        return direction;
    }

    public boolean moveCrate() {
        return moveCrate;
    }

    public static Move of(Direction dir, boolean moveCrate) {
        return switch (dir) {
            case LEFT -> moveCrate ? LEFT_PUSH : LEFT;
            case UP -> moveCrate ? UP_PUSH : UP;
            case DOWN -> moveCrate ? RIGHT_PUSH : DOWN;
            case RIGHT -> moveCrate ? DOWN_PUSH : RIGHT;
        };
    }
}