package fr.valax.sokoshell;

import fr.valax.sokoshell.graphics.style.AnsiTile;
import fr.valax.sokoshell.graphics.style.ImageTile;
import fr.valax.sokoshell.graphics.style.MapStyle;
import fr.valax.sokoshell.graphics.style.TileStyle;
import fr.valax.sokoshell.solver.Board;
import fr.valax.sokoshell.solver.Direction;
import fr.valax.sokoshell.solver.Level;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Doesn't support ansi tile...
 */
public class PNGExporter {

    private MapStyle mapStyle;

    public PNGExporter() {

    }

    public BufferedImage asImage(Level level, int size) {
        return asImage(level.getMap(), level.getPlayerX(), level.getPlayerY(), Direction.DOWN, size);
    }

    public BufferedImage asImage(Board board, int playerX, int playerY, Direction playerDir, int size) {
        playerDir = playerDir == null ? Direction.DOWN : playerDir;
        size = mapStyle.findBestSize(size);

        int imgWidth = size * board.getWidth();
        int imgHeight = size * board.getHeight();

        BufferedImage image = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        try {
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, imgWidth, imgHeight);

            int xDraw;
            int yDraw = 0;
            for (int y = 0; y < board.getHeight(); y++) {

                xDraw = 0;
                for (int x = 0; x < board.getWidth(); x++) {
                    TileStyle style;
                    if (x == playerX && y == playerY) {
                        style = mapStyle.get(size, board.getAt(x, y).getTile(), playerDir);
                    } else {
                        style = mapStyle.get(size, board.getAt(x, y).getTile(), null);
                    }

                    if (style instanceof ImageTile imgTile) {
                        g2d.drawImage(imgTile.getImage(), xDraw, yDraw, size, size, null);
                    } else if (style instanceof AnsiTile) {
                        throw new UnsupportedOperationException("AnsiTile not supported");
                    }

                    xDraw += size;
                }

                yDraw += size;
            }
        } finally {
            g2d.dispose();
        }

        return image;
    }

    public MapStyle getMapStyle() {
        return mapStyle;
    }

    public void setMapStyle(MapStyle mapStyle) {
        this.mapStyle = mapStyle;
    }
}
