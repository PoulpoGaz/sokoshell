package fr.valax.sokoshell.graphics.style;

import fr.valax.sokoshell.solver.board.Board;
import fr.valax.sokoshell.solver.board.Direction;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import static fr.valax.sokoshell.graphics.GraphicsUtils.*;

/**
 * A helper object to export a board to an image.
 */
public class CreateImageHelper {

    public Font legendFont = DEFAULT_FONT;
    public Color legendColor = new Color(200, 200, 200);
    public Font tileFont = DEFAULT_FONT;

    public int offsetX = 0;
    public int offsetY = 0;

    public int charWidth = CHAR_WIDTH;
    public int charHeight = CHAR_HEIGHT;

    public int tileWidth;
    public int tileHeight;

    public void init(Board board, int size) {
        init(board, size, true);
    }

    /**
     * Initialize fields: compute tileWidth, tileHeight. If legend is set to true,
     * grow tileFont or legendFont if necessary, compute charWidth, charHeight, offsetX
     * and offsetY.
     * Please note that you are not obliged to call this method
     */
    public void init(Board board, int size, boolean legend) {
        if (legend) {
            String widthStr = Integer.toString(board.getWidth());
            Rectangle2D maxTop = getStringBounds(legendFont, widthStr);

            if (maxTop.getWidth() + 4 > size * charWidth) {
                // need to grow tileFont, there isn't sufficient space to draw legend
                tileFont = adjustFont(tileFont, (int) maxTop.getWidth() + 4);
                Rectangle2D maxCharBounds = tileFont.getMaxCharBounds(DEFAULT_RENDER_CONTEXT);
                charWidth = (int) (maxCharBounds.getWidth() / size);
                charHeight = (int) (maxCharBounds.getHeight() / size);
            } else if (maxTop.getWidth() < size * charWidth - 4) {
                // grow legend
                legendFont = adjustFont(legendFont, widthStr, size * charWidth - 4);
                maxTop = getStringBounds(legendFont, widthStr);
            }

            Rectangle2D maxLeft = getStringBounds(legendFont, Integer.toString(board.getHeight()));

            offsetX = (int) Math.ceil(maxLeft.getWidth());
            offsetY = (int) Math.ceil(maxTop.getHeight());
        }

        tileWidth = size * charWidth;
        tileHeight = size * charHeight;
    }

    /**
     * Create an image with default font scaling strategy
     */
    public BufferedImage initAndCreateImage(boolean legend, BoardStyle style, int size,
                                            Board board, int playerX, int playerY, Direction playerDir) {
        init(board, size, legend);
        return createImage(legend, style, size, board, playerX, playerY, playerDir);
    }

    /**
     * Create an image with your parameters
     */
    public BufferedImage createImage(boolean legend, BoardStyle style, int size,
                                            Board board, int playerX, int playerY, Direction playerDir) {
        BufferedImage img = new BufferedImage(
                offsetX + board.getWidth() * tileWidth,
                offsetY + board.getHeight() * tileHeight,
                BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = img.createGraphics();
        try {
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            drawBoard(g2d, style, size, board, playerX, playerY, playerDir);

            if (legend) {
                drawLegend(g2d, board.getWidth(), board.getHeight());
            }
        } finally {
            g2d.dispose();
        }

        return img;
    }

    public void drawBoard(Graphics2D g2d, BoardStyle style, int size, Board board, int playerX, int playerY, Direction playerDir) {
        g2d.setFont(tileFont);
        g2d.translate(offsetX, offsetY);
        style.draw(g2d, size, charWidth, charHeight, board, playerX, playerY, playerDir);
        g2d.translate(-offsetX, -offsetY);
    }

    public void drawLegend(Graphics2D g2d, int width, int height) {
        g2d.setColor(legendColor);
        g2d.setFont(legendFont);

        Rectangle bounds = new Rectangle();
        bounds.setSize(tileWidth, offsetY);
        bounds.translate(offsetX, 0);
        for (int x = 1; x <= width; x++) {
            String str = Integer.toString(x);

            drawStringCentered(g2d, str, bounds);
            bounds.translate(tileWidth, 0);
        }

        bounds.setSize(offsetX, tileHeight);
        bounds.setLocation(0, offsetY);
        for (int y = 1; y <= height; y++) {
            String str = Integer.toString(y);

            drawStringCentered(g2d, str, bounds);
            bounds.translate(0, tileHeight);
        }
    }
}
