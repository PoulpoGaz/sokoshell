package fr.valax.sokoshell.graphics.style2;

import fr.valax.sokoshell.graphics.Graphics;
import fr.valax.sokoshell.graphics.Surface;
import fr.valax.sokoshell.solver.Direction;
import fr.valax.sokoshell.solver.Level;
import fr.valax.sokoshell.solver.Map;
import fr.valax.sokoshell.solver.TileInfo;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedString;

import java.io.PrintStream;
import java.util.List;
import java.util.Objects;

public class MapRenderer {

    private MapStyle style = new DefaultStyle();


    public void print(Level level) {
        print(System.out, level);
    }

    public void print(Map map, int playerX, int playerY) {
        print(System.out, map, playerX, playerY);
    }

    public void print(Terminal terminal, Level level) {
        draw(level).print(terminal);
    }

    public void print(Terminal terminal, Map map, int playerX, int playerY) {
        draw(map, playerX, playerY).print(terminal);
    }

    public void print(PrintStream ps, Level level) {
        draw(level).print(ps);
    }

    public void print(PrintStream ps, Map map, int playerX, int playerY) {
        draw(map, playerX, playerY).print(ps);
    }



    public AttributedString drawToString(Level level) {
        return drawToString(level.getMap(), level.getPlayerX(), level.getPlayerY());
    }

    public List<AttributedString> drawToList(Level level) {
        return drawToList(level.getMap(), level.getPlayerX(), level.getPlayerY());
    }

    public Surface draw(Level level) {
        return draw(level.getMap(), level.getPlayerX(), level.getPlayerY());
    }


    public AttributedString drawToString(Map map, int playerX, int playerY) {
        return draw(map, playerX, playerY).asString();
    }

    public List<AttributedString> drawToList(Map map, int playerX, int playerY) {
        return draw(map, playerX, playerY).asList();
    }

    public Surface draw(Map map, int playerX, int playerY) {
        Surface s = new Surface();
        s.resize(map.getWidth(), map.getHeight());

        draw(new Graphics(s), 0, 0, 1, map, playerX, playerY, Direction.DOWN);

        return s;
    }



    public void drawCentered(Graphics g,
                     int x, int y, int width, int height,
                     Map map, int playerX, int playerY, Direction playerDir) {
        Objects.requireNonNull(style);

        double yRatio = (double) height / map.getHeight();
        double xRatio = (double) width / map.getWidth();

        int s = (int) Math.min(xRatio, yRatio);

        if (s <= 0) {
            return;
        }

        s = style.findBestSize(s);

        int w = s * map.getWidth();
        int h = s * map.getHeight();

        draw(g, x + (width - w) / 2,
                y + (height - h) / 2,
                s, map, playerX, playerY, playerDir);
    }

    public void draw(Graphics g,
                     int x, int y, int size,
                     Map map, int playerX, int playerY, Direction playerDir) {
        Objects.requireNonNull(style);

        int s = style.findBestSize(size);

        if (s < 0) {
            s = size;
        }

        for (int y2 = 0; y2 < map.getHeight(); y2++) {
            for (int x2 = 0; x2 < map.getWidth(); x2++) {
                boolean player = playerY == y2 && playerX == x2;

                TileInfo tile = map.getAt(x2, y2);
                int drawX = x2 * s + x;
                int drawY = y2 * s + y;
                if (player) {
                    style.draw(g, tile, playerDir, drawX, drawY, s);
                } else {
                    style.draw(g, tile, null, drawX, drawY, s);
                }
            }
        }
    }

    public MapStyle getStyle() {
        return style;
    }

    public void setStyle(MapStyle style) {
        this.style = Objects.requireNonNull(style);
    }
}
