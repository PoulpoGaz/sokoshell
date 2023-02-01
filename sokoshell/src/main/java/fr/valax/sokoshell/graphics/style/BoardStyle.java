package fr.valax.sokoshell.graphics.style;

import fr.valax.sokoshell.graphics.Graphics;
import fr.valax.sokoshell.graphics.Surface;
import fr.valax.sokoshell.solver.Level;
import fr.valax.sokoshell.solver.board.Board;
import fr.valax.sokoshell.solver.board.Direction;
import fr.valax.sokoshell.solver.board.tiles.TileInfo;
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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A board style defines how to draw ITileInfo on a {@link Surface} or with {@link Graphics2D}.
 */
public abstract class BoardStyle {

    private static final AtomicInteger unnamedIndex = new AtomicInteger(0);

    protected final String name;
    protected final String author;
    protected final String version;

    protected boolean drawDeadTiles = true;
    protected boolean drawRooms = false;
    protected boolean drawTunnels = false;

    /**
     * Creates a new style
     * @param name name of the style or "Unnamed n°i" where is an integer
     *             incremented each times this constructor is called
     * @param author author of the style or "none"
     * @param version version of the style or "0"
     */
    public BoardStyle(String name, String author, String version) {
        if (name == null) {
            this.name = "Unnamed n°" + unnamedIndex.incrementAndGet();
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



    // Full board drawing to a Stream / Surface


    public void print(Level level) {
        print(System.out, level);
    }

    public void print(Board board, int playerX, int playerY) {
        print(System.out, board, playerX, playerY);
    }

    public void print(Terminal terminal, Level level) {
        draw(level).print(terminal);
    }

    public void print(Terminal terminal, Board board, int playerX, int playerY) {
        draw(board, playerX, playerY).print(terminal);
    }

    public void print(PrintStream ps, Level level) {
        draw(level).print(ps);
    }

    public void print(PrintStream ps, Board board, int playerX, int playerY) {
        draw(board, playerX, playerY).print(ps);
    }



    public AttributedString drawToString(Level level) {
        return drawToString(level, level.getPlayerX(), level.getPlayerY());
    }

    public List<AttributedString> drawToList(Level level) {
        return drawToList(level, level.getPlayerX(), level.getPlayerY());
    }

    public Surface draw(Level level) {
        return draw(level, level.getPlayerX(), level.getPlayerY());
    }


    public AttributedString drawToString(Board board, int playerX, int playerY) {
        return draw(board, playerX, playerY).asString();
    }

    public List<AttributedString> drawToList(Board board, int playerX, int playerY) {
        return draw(board, playerX, playerY).asList();
    }

    public Surface draw(Board board, int playerX, int playerY) {
        Surface s = new Surface();
        s.resize(board.getWidth(), board.getHeight());

        draw(new Graphics(s), 1, board, playerX, playerY, Direction.DOWN);

        return s;
    }



    public void drawCentered(Graphics g,
                             int x, int y, int width, int height,
                             Board board, int playerX, int playerY, Direction playerDir) {
        double yRatio = (double) height / board.getHeight();
        double xRatio = (double) width / board.getWidth();

        int s = (int) Math.min(xRatio, yRatio);

        if (s <= 0) {
            return;
        }

        s = findBestSize(s);

        int w = s * board.getWidth();
        int h = s * board.getHeight();

        int xOffset = x + (width - w) / 2;
        int yOffset = y + (height - h) / 2;

        g.getSurface().translate(xOffset, yOffset);
        drawWithLegend(g, s, board, playerX, playerY, playerDir);
        g.getSurface().translate(-xOffset, -yOffset);
    }

    public void drawCenteredWithLegend(Graphics g,
                                       int x, int y, int width, int height,
                                       Board board, int playerX, int playerY, Direction playerDir) {
        double yRatio = (double) (height - 1) / board.getHeight();
        double xRatio = (double) (width - 1) / board.getWidth();

        int s = (int) Math.min(xRatio, yRatio);

        if (s <= 0) {
            return;
        }

        s = findBestSize(s);
        if (s < 0) {
            return;
        }

        int w = s * board.getWidth();
        int h = s * board.getHeight();

        int xOffset = x + (width - 1 - w) / 2;
        int yOffset = y + (height - 1 - h) / 2;

        g.getSurface().translate(xOffset, yOffset);
        drawWithLegend(g, s, board, playerX, playerY, playerDir);
        g.getSurface().translate(-xOffset, -yOffset);
    }



    public void draw(Graphics g, int size,
                     Board board, int playerX, int playerY, Direction playerDir) {
        if (size <= 0) {
            return;
        }

        drawNoCheck(g, size, board, playerX, playerY, playerDir);
    }

    public void drawWithLegend(Graphics g, int size,
                               Board board, int playerX, int playerY, Direction playerDir) {
        int xOffset = Utils.nDigit(board.getHeight());
        int yOffset;
        boolean verticalDraw; // of value on the x-axis

        int maxLen = Utils.nDigit(board.getWidth());
        if (maxLen > size) {
            yOffset = maxLen;
            verticalDraw = true;
        } else {
            yOffset = 1;
            verticalDraw = false;
        }

        Surface s = g.getSurface();
        for (int i = 0; i < board.getWidth(); i++) {
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

        for (int i = 0; i < board.getHeight(); i++) {
            String str = Integer.toString(i + 1);
            s.draw(str, xOffset - str.length(), yOffset + i * size + (size - 1)  / 2);
        }

        g.getSurface().translate(xOffset, yOffset);
        drawNoCheck(g, size, board, playerX, playerY, playerDir);
        g.getSurface().translate(-xOffset, -yOffset);
    }


    private void drawNoCheck(Graphics g, int size,
                             Board board, int playerX, int playerY, Direction playerDir) {
        for (int y2 = 0; y2 < board.getHeight(); y2++) {
            for (int x2 = 0; x2 < board.getWidth(); x2++) {
                boolean player = playerY == y2 && playerX == x2;

                TileInfo tile = board.getAt(x2, y2);
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


    // Full board drawing to an BufferedImage / Graphics2D (awt)

    /**
     * Creates an image and draw the board on. Implementations should choose
     * the best dimension and are free to choose the font they want
     *
     * @param board the board to draw
     * @param playerX player x
     * @param playerY player y
     * @param playerDir direction of the player
     * @return an image with the board drawn on
     */
    public abstract BufferedImage createImage(Board board, int playerX, int playerY, Direction playerDir);

    public abstract BufferedImage createImageWithLegend(Board board, int playerX, int playerY, Direction playerDir);

    public void draw(Graphics2D g2d, int size,
                     Board board, int playerX, int playerY, Direction playerDir) {
        Font font = g2d.getFont();
        Rectangle2D max = font.getMaxCharBounds(g2d.getFontRenderContext());

        draw(g2d, size,
                (int) max.getWidth(), (int) max.getHeight(),
                board, playerX, playerY, playerDir);
    }

    public void draw(Graphics2D g2d, int size, int charWidth, int charHeight,
                     Board board, int playerX, int playerY, Direction playerDir) {
        for (int y2 = 0; y2 < board.getHeight(); y2++) {
            for (int x2 = 0; x2 < board.getWidth(); x2++) {
                boolean player = playerY == y2 && playerX == x2;

                TileInfo tile = board.getAt(x2, y2);
                int drawX = x2 * size * charWidth;
                int drawY = y2 * size * charHeight;
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

    public boolean isDrawDeadTiles() {
        return drawDeadTiles;
    }

    public void setDrawDeadTiles(boolean drawDeadTiles) {
        this.drawDeadTiles = drawDeadTiles;
    }

    public boolean isDrawRooms() {
        return drawRooms;
    }

    public void setDrawRooms(boolean drawRooms) {
        this.drawRooms = drawRooms;
    }

    public boolean isDrawTunnels() {
        return drawTunnels;
    }

    public void setDrawTunnels(boolean drawTunnels) {
        this.drawTunnels = drawTunnels;
    }
}
