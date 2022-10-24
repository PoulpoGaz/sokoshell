package fr.valax.sokoshell.solver;

/**
 * An object representing a move or a push in a solution. The {@code moveCrate} flag is needed to go back
 * in {@link fr.valax.sokoshell.commands.level.SolutionCommand}
 * @param direction the direction of the move
 * @param moveCrate {@code true} if the player push a crate
 */
public record Move(Direction direction, boolean moveCrate) {

}