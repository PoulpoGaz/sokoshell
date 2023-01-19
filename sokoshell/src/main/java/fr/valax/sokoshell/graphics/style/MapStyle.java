package fr.valax.sokoshell.graphics.style;

import fr.valax.sokoshell.graphics.Graphics;
import fr.valax.sokoshell.graphics.Surface;
import fr.valax.sokoshell.solver.Direction;
import fr.valax.sokoshell.solver.Level;
import fr.valax.sokoshell.solver.Map;
import fr.valax.sokoshell.solver.TileInfo;
import fr.valax.sokoshell.utils.Utils;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.PrintStream;
import java.util.List;
import java.util.Objects;

public abstract class MapStyle {

    private static int unnamedIndex = 0;

    protected final String name;
    protected final String author;
    protected final String version;

    public MapStyle(String name, String author, String version) {
        if (name == null) {
            this.name = "Unnamed n°" + (++unnamedIndex);
        } else {
            this.name = name;
        }

        this.author = Objects.requireNonNullElse(author, "none");
        this.version = Objects.requireNonNullElse(version, "0");

    }

    /**
     * Draw the tile at ({@code drawX}; {@code drawY}) with the specified {@code size}.
     * If {@code playerDir} isn't {@code null}, it will also draw the player wit the specified
     * direction. If the size isn't supported by the style, it will try to draw the tile, but
     * it may produce weird results
     *
     * @param g graphics to draw with
     * @param tile the tile to draw
     * @param playerDir not null to draw the player of this direction
     * @param drawX draw x
     * @param drawY draw y
     * @param size size of the tile
     */
    public abstract void draw(Graphics g, TileInfo tile, Direction playerDir, int drawX, int drawY, int size);

    /**
     * Draw the tile at ({@code drawX}; {@code drawY}) with the specified dimension with java 2D.
     * If {@code playerDir} isn't {@code null}, it will also draw the player wit the specified
     * direction. If the size isn't supported by the style, it will try to draw the tile, but
     * it may produce weird results.
     *
     * @param g2d        graphics to draw with
     * @param tile       the tile to draw
     * @param playerDir  not null to draw the player of this direction
     * @param drawX      draw x
     * @param drawY      draw y
     * @param size size
     * @param charWidth  width of a char
     * @param charHeight height of a char
     */
    public abstract void draw(Graphics2D g2d,
                              TileInfo tile, Direction playerDir,
                              int drawX, int drawY, int size, int charWidth, int charHeight);



    // Full map drawing to a Stream / Surface


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

        draw(new Graphics(s), 1, map, playerX, playerY, Direction.DOWN);

        return s;
    }



    public void drawCentered(Graphics g,
                             int x, int y, int width, int height,
                             Map map, int playerX, int playerY, Direction playerDir) {
        double yRatio = (double) height / map.getHeight();
        double xRatio = (double) width / map.getWidth();

        int s = (int) Math.min(xRatio, yRatio);

        if (s <= 0) {
            return;
        }

        s = findBestSize(s);

        int w = s * map.getWidth();
        int h = s * map.getHeight();

        int xOffset = x + (width - w) / 2;
        int yOffset = y + (height - h) / 2;

        g.getSurface().translate(xOffset, yOffset);
        drawWithLegend(g, s, map, playerX, playerY, playerDir);
        g.getSurface().translate(-xOffset, -yOffset);
    }

    public void drawCenteredWithLegend(Graphics g,
                                       int x, int y, int width, int height,
                                       Map map, int playerX, int playerY, Direction playerDir) {
        double yRatio = (double) (height - 1) / map.getHeight();
        double xRatio = (double) (width - 1) / map.getWidth();

        int s = (int) Math.min(xRatio, yRatio);

        if (s <= 0) {
            return;
        }

        s = findBestSize(s);
        if (s < 0) {
            return;
        }

        int w = s * map.getWidth();
        int h = s * map.getHeight();

        int xOffset = x + (width - 1 - w) / 2;
        int yOffset = y + (height - 1 - h) / 2;

        g.getSurface().translate(xOffset, yOffset);
        drawWithLegend(g, s, map, playerX, playerY, playerDir);
        g.getSurface().translate(-xOffset, -yOffset);
    }



    public void draw(Graphics g, int size,
                     Map map, int playerX, int playerY, Direction playerDir) {
        if (size <= 0) {
            return;
        }

        drawNoCheck(g, size, map, playerX, playerY, playerDir);
    }

    public void drawWithLegend(Graphics g, int size,
                               Map map, int playerX, int playerY, Direction playerDir) {
        int xOffset = Utils.nDigit(map.getHeight());
        int yOffset;
        boolean verticalDraw; // of value on the x-axis

        int maxLen = Utils.nDigit(map.getWidth());
        if (maxLen > size) {
            yOffset = maxLen;
            verticalDraw = true;
        } else {
            yOffset = 1;
            verticalDraw = false;
        }

        Surface s = g.getSurface();
        for (int i = 0; i < map.getWidth(); i++) {
            String str = Integer.toString(i + 1);

            if (verticalDraw) {
                int xDraw = xOffset + i * size + (size - 1) / 2;
                int yDraw = yOffset - 1;
                for (int j = str.length() - 1; j >= 0; j--) {
                    s.draw(str.charAt(j), AttributedStyle.DEFAULT, xDraw, yDraw);
                    yDraw--;
                }
            } else {
                s.draw(str, xOffset + i * size + (size - str.length()) / 2, 0);
            }
        }

        for (int i = 0; i < map.getHeight(); i++) {
            String str = Integer.toString(i + 1);
            s.draw(str, xOffset - str.length(), yOffset + i * size + (size - 1)  / 2);
        }

        g.getSurface().translate(xOffset, yOffset);
        drawNoCheck(g, size, map, playerX, playerY, playerDir);
        g.getSurface().translate(-xOffset, -yOffset);
    }


    private void drawNoCheck(Graphics g, int size,
                             Map map, int playerX, int playerY, Direction playerDir) {
        for (int y2 = 0; y2 < map.getHeight(); y2++) {
            for (int x2 = 0; x2 < map.getWidth(); x2++) {
                boolean player = playerY == y2 && playerX == x2;

                TileInfo tile = map.getAt(x2, y2);
                int drawX = x2 * size;
                int drawY = y2 * size;
                if (player) {
                    draw(g, tile, playerDir, drawX, drawY, size);
                } else {
                    draw(g, tile, null, drawX, drawY, size);
                }
            }
        }
    }


    // Full map drawing to an BufferedImage / Graphics2D (awt)

    public abstract BufferedImage createImage(Map map, int playerX, int playerY, Direction playerDir);

    public void draw(Graphics2D g2d, int size,
                     Map map, int playerX, int playerY, Direction playerDir) {
        Font font = g2d.getFont();
        Rectangle2D max = font.getMaxCharBounds(g2d.getFontRenderContext());

        int charWidth = (int) max.getWidth();
        int charHeight = (int) max.getHeight();

        for (int y2 = 0; y2 < map.getHeight(); y2++) {
            for (int x2 = 0; x2 < map.getWidth(); x2++) {
                boolean player = playerY == y2 && playerX == x2;

                TileInfo tile = map.getAt(x2, y2);
                int drawX = x2 * size;
                int drawY = y2 * size;
                if (player) {
                    draw(g2d, tile, playerDir, drawX, drawY, size, charWidth, charHeight);
                } else {
                    draw(g2d, tile, null, drawX, drawY, size, charWidth, charHeight);
                }
            }
        }
    }




    /**
     * Finds the best size with respect to {@code size} ie finds the
     * nearest and smallest size to size that is correctly supported by
     * the style
     *
     * @param size size
     * @return the best size
     */
    public abstract int findBestSize(int size);

    /**
     * Returns {@code true} if the size is supported by the style.
     * An unsupported size can still be passed to {@link #draw(Graphics, TileInfo, Direction, int, int, int)}
     * but it may produce weird results.
     *
     * @param size size
     * @return {@code true} if the size is supported by the style
     */
    public abstract boolean isSupported(int size);

    public final String getName() {
        return name;
    }

    public final String getAuthor() {
        return author;
    }

    public final String getVersion() {
        return version;
    }
}
