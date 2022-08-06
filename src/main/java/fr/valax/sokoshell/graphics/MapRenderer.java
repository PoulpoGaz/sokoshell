package fr.valax.sokoshell.graphics;

import fr.valax.sokoshell.solver.*;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;

import java.awt.*;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A class used to print or draw a map on a terminal or a surface
 * or even on system output. It uses a {@link MapStyle} to know how
 * to draw.
 */
public class MapRenderer {

    private MapStyle style;
    private boolean showDeadTiles;

    public void sysPrint(Level level) {
        sysPrint(level.getMap(), level.getPlayerX(), level.getPlayerY());
    }

    public void print(PrintStream ps, Level level) {
        print(ps, level.getMap(), level.getPlayerX(), level.getPlayerY());
    }

    public void print(PrintStream ps, Map map, int playerX, int playerY) {
        for (AttributedString str : draw(map, playerX, playerY)) {
            ps.println(str.toAnsi());
        }
    }


    public void print(Terminal terminal, Level level) {
        print(terminal, level.getMap(), level.getPlayerX(), level.getPlayerY());
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

    // Methods that return a String/List<AttributedString>

    public String toString(Level level) {
        return toString(level.getMap(), level.getPlayerX(), level.getPlayerY());
    }

    public List<AttributedString> draw(Level level) {
        return draw(level.getMap(), level.getPlayerX(), level.getPlayerY());
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

                TileInfo tile = map.getAt(x, y);

                TileStyle tileStyle = style.get(1, tile.getTile(), player ? Direction.DOWN : null);
                if (showDeadTiles && tile.isDeadTile() && !tile.isWall()) {
                    tileStyle = tileStyle.merge(createDeadTileStyle(1));
                }

                builder.append(tileStyle.getAsString()[0]);
            }

            out.add(builder.toAttributedString());
            builder.setLength(0);
        }
    }

    // Methods that use Graphics

    public void draw(Graphics g,
                     int x, int y, int width, int height,
                     Map map, int playerX, int playerY, Direction playerDir) {
        draw(g, x, y, width, height, map, playerX, playerY, playerDir, true);
    }

    public void draw(Graphics g,
                     int x, int y, int width, int height,
                     Map map, int playerX, int playerY, Direction playerDir, boolean center) {
        if (style == null) {
            throw new IllegalStateException("Please, set style before");
        }

        double yRatio = (double) height / map.getHeight();
        double xRatio = (double) width / map.getWidth();

        int s = (int) Math.min(xRatio, yRatio);

        if (s <= 0) {
            return;
        }

        if (center) {
            int w = s * map.getWidth();
            int h = s * map.getHeight();

            draw(g, x + (width - w) / 2,
                    y + (height - h) / 2,
                    s, map, playerX, playerY, playerDir);
        } else {
            draw(g, x, y, s, map, playerX, playerY, playerDir);
        }
    }

    public void draw(Graphics g,
                     int x, int y, int size,
                     Map map, int playerX, int playerY, Direction playerDir) {
        if (style == null) {
            throw new IllegalStateException("Please, set style before");
        }

        int s = findBestSize(size);

        if (s < 0) {
            s = size;
        }

        for (int y2 = 0; y2 < map.getHeight(); y2++) {
            for (int x2 = 0; x2 < map.getWidth(); x2++) {
                boolean player = playerY == y2 && playerX == x2;

                TileInfo tile = map.getAt(x2, y2);

                TileStyle tileStyle = style.get(s, tile.getTile(), player ? playerDir : null);
                if (showDeadTiles && tile.isDeadTile() && !tile.isWall()) {
                    tileStyle.merge(createDeadTileStyle(s)).draw(g, x2 * s + x, y2 * s + y);
                } else {
                    tileStyle.draw(g, x2 * s + x, y2 * s + y);
                }
            }
        }
    }

    private TileStyle createDeadTileStyle(int s) {
        BufferedImage image = new BufferedImage(s, s, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(new Color(0, 234, 255, 127));
        g2d.fillRect(0, 0, s, s);
        g2d.dispose();

        return new ImageTile(s, image);
    }

    public int findBestSize(int size) {
        int i = findBestSizeIndex(size);

        if (i < 0) {
            return -1;
        } else {
            return style.availableSizes()[i];
        }
    }

    private int findBestSizeIndex(int size) {
        int[] sizes = style.availableSizes();

        int bestI = -1;

        for (int i = 0; i < sizes.length; i++) {

            if (sizes[i] == size) {
                return i;
            } else if (sizes[i] < size) {
                if (bestI < 0) {
                    bestI = i;
                } else if (sizes[i] > sizes[bestI]) {
                    bestI = i;
                }
            }

        }

        return bestI;
    }

    public MapStyle getStyle() {
        return style;
    }

    public void setStyle(MapStyle style) {
        this.style = style;
    }

    public boolean isShowDeadTiles() {
        return showDeadTiles;
    }

    public void setShowDeadTiles(boolean showDeadTiles) {
        this.showDeadTiles = showDeadTiles;
    }
}
