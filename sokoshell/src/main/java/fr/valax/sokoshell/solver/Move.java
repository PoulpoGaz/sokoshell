package fr.valax.sokoshell.solver;

/**
 * An enumeration representing a move or a push in a solution. The {@code moveCrate} flag is needed to go back
 * in {@link fr.valax.sokoshell.commands.level.SolutionCommand}
 */
public enum Move {

    LEFT("l", Direction.LEFT, false),
    UP("u", Direction.UP, false),
    DOWN("d", Direction.DOWN, false),
    RIGHT("r", Direction.RIGHT, false),

    LEFT_PUSH("L", Direction.LEFT, true),
    UP_PUSH("U", Direction.UP, true),
    RIGHT_PUSH("R", Direction.RIGHT, true),
    DOWN_PUSH("D", Direction.DOWN, true);

    private final String shortName;
    private final Direction direction;
    private final boolean moveCrate;

    Move(String name, Direction direction, boolean moveCrate) {
        this.shortName = name;
        this.direction = direction;
        this.moveCrate = moveCrate;
    }

    public String shortName() {
        return shortName;
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
            case DOWN -> moveCrate ? DOWN_PUSH : DOWN;
            case RIGHT -> moveCrate ? RIGHT_PUSH : RIGHT;
        };
    }

    public static Move of(String shortName) {
        for (Move move : Move.values()) {
            if (move.shortName().equals(shortName)) {
                return move;
            }
        }

        return null;
    }
}