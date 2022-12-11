package fr.valax.sokoshell.readers;

import fr.valax.sokoshell.solver.Level;
import fr.valax.sokoshell.solver.Tile;

/**
 * Utility class for readers
 */
public class ReaderUtils {

    /**
     * Convert a char into a Tile and sets the tile at (x, y) in the specified {@link Level.Builder}
     *
     * @param c input char
     * @param builder builder
     * @param x x position
     * @param y y position
     * @return {@code true} if the char is a valid and the tile was set
     */
    public static boolean set(char c, Level.Builder builder, int x, int y) {
        switch (c) {
            case ' ', '-' -> builder.set(Tile.FLOOR, x, y);
            case '#', '_' -> builder.set(Tile.WALL, x, y);
            case '$' -> builder.set(Tile.CRATE, x, y);
            case '.' -> builder.set(Tile.TARGET, x, y);
            case '*' -> builder.set(Tile.CRATE_ON_TARGET, x, y);
            case '@' -> {
                builder.set(Tile.FLOOR, x, y);
                builder.setPlayerPos(x, y);
            }
            case '+' -> {
                builder.set(Tile.TARGET, x, y);
                builder.setPlayerPos(x, y);
            }
            default -> {
                return false;
            }
        }

        return true;
    }
}
