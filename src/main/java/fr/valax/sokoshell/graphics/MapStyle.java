package fr.valax.sokoshell.graphics;

import fr.valax.sokoshell.solver.Direction;
import fr.valax.sokoshell.solver.Tile;

import java.util.Arrays;
import java.util.Map;

public class MapStyle {

    private final String name;
    private final String author;
    private final String version;

    private final Map<Integer, Map<Element, TileStyle>> styles;
    private final int[] availableSizes;

    public MapStyle(String name, String author, String version, Map<Integer, Map<Element, TileStyle>> styles) {
        this.name = name;
        this.author = author;
        this.version = version;
        this.styles = styles;

        availableSizes = new int[styles.size()];

        int i = 0;
        for (Integer v : styles.keySet()) {
            availableSizes[i] = v;
            i++;
        }

        Arrays.sort(availableSizes);
    }

    public TileStyle get(Tile tile, Direction direction) {
        return get(availableSizes[0], tile, direction);
    }

    public TileStyle get(int size, Tile tile, Direction direction) {
        Map<Element, TileStyle> tileStyles = styles.get(size);

        if (tileStyles == null) {
            throw new IllegalArgumentException("size not supported (" + size + ")");
        }

        return tileStyles.get(Element.convert(tile, direction));
    }

    public int[] availableSizes() {
        return availableSizes;
    }

    public String getName() {
        return name;
    }

    public String getAuthor() {
        return author;
    }

    public String getVersion() {
        return version;
    }

    enum Element {
        PLAYER_FLOOR_LEFT,
        PLAYER_FLOOR_RIGHT,
        PLAYER_FLOOR_DOWN,
        PLAYER_FLOOR_UP,
        PLAYER_ON_TARGET_LEFT,
        PLAYER_ON_TARGET_RIGHT,
        PLAYER_ON_TARGET_DOWN,
        PLAYER_ON_TARGET_UP,
        FLOOR,
        CRATE,
        CRATE_ON_TARGET,
        WALL,
        TARGET;

        private static Element convert(Tile tile, Direction playerDir) {
            if (playerDir != null) {
                if (tile == Tile.FLOOR) {
                    return switch (playerDir) {
                        case DOWN -> PLAYER_FLOOR_DOWN;
                        case UP -> PLAYER_FLOOR_UP;
                        case LEFT -> PLAYER_FLOOR_LEFT;
                        case RIGHT -> PLAYER_FLOOR_RIGHT;
                    };
                } else {
                    return switch (playerDir) {
                        case DOWN -> PLAYER_ON_TARGET_DOWN;
                        case UP -> PLAYER_ON_TARGET_UP;
                        case LEFT -> PLAYER_ON_TARGET_LEFT;
                        case RIGHT -> PLAYER_ON_TARGET_RIGHT;
                    };
                }
            } else {
                return switch (tile) {
                    case CRATE -> CRATE;
                    case CRATE_ON_TARGET -> CRATE_ON_TARGET;
                    case WALL -> WALL;
                    case FLOOR -> FLOOR;
                    case TARGET -> TARGET;
                };
            }
        }
    }
}
