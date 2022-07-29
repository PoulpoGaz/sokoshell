package fr.valax.sokoshell.readers;

import fr.valax.sokoshell.solver.Level;
import fr.valax.sokoshell.solver.Tile;

public class ReaderUtils {

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
