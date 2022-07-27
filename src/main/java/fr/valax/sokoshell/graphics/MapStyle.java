package fr.valax.sokoshell.graphics;

import fr.valax.sokoshell.SokoShell;
import fr.valax.sokoshell.solver.Direction;
import fr.valax.sokoshell.solver.Tile;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.jline.utils.AttributedStyle.*;

public class MapStyle {

    public static final MapStyle DEFAULT_STYLE = createDefault();

    private static MapStyle createDefault() {
        Map<Element, TileStyle> style = new HashMap<>();
        style.put(Element.FLOOR, create(' ', DEFAULT.background(GREEN)));
        style.put(Element.WALL, create(' ', DEFAULT.background(BLACK + BRIGHT)));
        style.put(Element.CRATE, create(' ', DEFAULT.background(YELLOW)));
        style.put(Element.CRATE_ON_TARGET, create(' ', DEFAULT.background(YELLOW + BRIGHT)));
        style.put(Element.TARGET, create(' ', DEFAULT.background(RED)));

        TileStyle playerFloor = create('o', DEFAULT.background(GREEN).foreground(BLACK));
        style.put(Element.PLAYER_FLOOR_DOWN, playerFloor);
        style.put(Element.PLAYER_FLOOR_UP, playerFloor);
        style.put(Element.PLAYER_FLOOR_LEFT, playerFloor);
        style.put(Element.PLAYER_FLOOR_RIGHT, playerFloor);

        TileStyle playerTarget = create('o', DEFAULT.background(RED).foreground(BLACK));
        style.put(Element.PLAYER_ON_TARGET_DOWN, playerTarget);
        style.put(Element.PLAYER_ON_TARGET_UP, playerTarget);
        style.put(Element.PLAYER_ON_TARGET_LEFT, playerTarget);
        style.put(Element.PLAYER_ON_TARGET_RIGHT, playerTarget);

        return new MapStyle("default", SokoShell.NAME, SokoShell.VERSION, Map.of(1, style));
    }

    private static TileStyle create(char c, AttributedStyle style) {
        return new AnsiTile(1,
                new AttributedString[] {new AttributedString(c + "", style)}
        );
    }

    private static int unnamedIndex = 0;

    private final String name;
    private final String author;
    private final String version;

    private final Map<Integer, Map<Element, TileStyle>> styles;
    private final int[] availableSizes;

    public MapStyle(String name, String author, String version, Map<Integer, Map<Element, TileStyle>> styles) {
        if (name == null) {
            this.name = "Unnamed n°" + unnamedIndex;
            unnamedIndex++;
        } else {
            this.name = name;
        }

        this.author = Objects.requireNonNullElse(author, "none");
        this.version = Objects.requireNonNullElse(version, "0");
        this.styles = styles;

        availableSizes = new int[styles.size()];

        int i = 0;
        for (Integer v : styles.keySet()) {
            availableSizes[i] = v;
            i++;
        }

        Arrays.sort(availableSizes);

        if (availableSizes[0] != 1) {
            throw new IllegalArgumentException("Map style must have style of size 1");
        }
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
