package fr.valax.sokoshell.utils;

import fr.valax.sokoshell.solver.Level;
import fr.valax.sokoshell.solver.Map;
import fr.valax.sokoshell.solver.Tile;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MapRenderer {

    private MapStyle style;

    public void sysPrint(Level level) {
        sysPrint(level.getMap(), level.getPlayerX(), level.getPlayerY());
    }

    public void print(Terminal terminal, Level level) {
        print(terminal, level.getMap(), level.getPlayerX(), level.getPlayerY());
    }

    public String toString(Level level) {
        return toString(level.getMap(), level.getPlayerX(), level.getPlayerY());
    }

    public List<AttributedString> draw(Level level) {
        return draw(level.getMap(), level.getPlayerX(), level.getPlayerY());
    }

    public void draw(Level level, List<AttributedString> out) {
        draw(level.getMap(), level.getPlayerX(), level.getPlayerY(), out);
    }


    public void sysPrint(Map map, int playerX, int playerY) {
        for (AttributedString str : draw(map, playerX, playerY)) {
            System.out.println(str.toAnsi());
        }
    }

    public void print(Terminal terminal, Map map, int playerX, int playerY) {
        for (AttributedString str : draw(map, playerX, playerY)) {
            str.println(terminal);
        }
    }

    public String toString(Map map, int playerX, int playerY) {
        return draw(map, playerX, playerY)
                .stream()
                .map(AttributedString::toAnsi)
                .collect(Collectors.joining("\n"));
    }

    public List<AttributedString> draw(Map map, int playerX, int playerY) {
        List<AttributedString> out = new ArrayList<>();
        draw(map, playerX, playerY, out);
        return out;
    }

    public void draw(Map map, int playerX, int playerY, List<AttributedString> out) {
        if (style == null) {
            throw new IllegalStateException("Please, set style before");
        }

        AttributedStringBuilder builder = new AttributedStringBuilder();
        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                boolean player = playerY == y && playerX == x;

                Tile tile = map.getAt(x, y);
                builder.append(style.getStyle(tile, player));
            }

            out.add(builder.toAttributedString());
            builder.setLength(0);
        }
    }

    public MapStyle getStyle() {
        return style;
    }

    public void setStyle(MapStyle style) {
        this.style = style;
    }
}
