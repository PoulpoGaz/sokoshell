package fr.valax.sokoshell.utils;

import fr.valax.sokoshell.solver.Tile;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;

import java.util.HashMap;
import java.util.Map;

import static org.jline.utils.AttributedStyle.*;

public class MapStyle {

    private final Map<Tile, AttributedString> styles = new HashMap<>();
    private final Map<Tile, AttributedString> playerStyles = new HashMap<>();

    public MapStyle() {
        setStyle(Tile.FLOOR, false, new AttributedString(" ", DEFAULT.background(GREEN)));
        setStyle(Tile.WALL, false, new AttributedString(" ", DEFAULT.background(100, 100, 100)));
        setStyle(Tile.TARGET, false, new AttributedString(" ", DEFAULT.background(RED)));
        setStyle(Tile.CRATE, false, new AttributedString(" ", DEFAULT.background(96, 61, 23))); // brown
        setStyle(Tile.CRATE_ON_TARGET, false, new AttributedString(" ", DEFAULT.background(YELLOW)));

        setStyle(Tile.FLOOR, true, new AttributedString("o", DEFAULT.background(GREEN).foreground(BLACK)));
        setStyle(Tile.TARGET, true, new AttributedString("o", DEFAULT.background(RED).foreground(BLACK)));
    }

    public AttributedString getStyle(Tile tile, boolean player) {
        if (player) {
            return playerStyles.get(tile);
        } else {
            return styles.get(tile);
        }
    }

    public void draw(AttributedStringBuilder builder, Tile tile, boolean player) {
        builder.append(getStyle(tile, player));
    }

    public void setStyle(Tile tile, boolean player, AttributedString str) {
        if (player) {
            playerStyles.put(tile, str);
        } else {
            styles.put(tile, str);
        }
    }
}
