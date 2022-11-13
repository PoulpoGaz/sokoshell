package fr.valax.sokoshell;

import fr.valax.sokoshell.graphics.AnsiTile;
import fr.valax.sokoshell.graphics.ImageTile;
import fr.valax.sokoshell.graphics.MapStyle;
import fr.valax.sokoshell.graphics.TileStyle;
import fr.valax.sokoshell.solver.Direction;
import fr.valax.sokoshell.solver.Level;
import fr.valax.sokoshell.solver.Map;

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

    public BufferedImage asImage(Map map, int playerX, int playerY, Direction playerDir, int size) {
        playerDir = playerDir == null ? Direction.DOWN : playerDir;
        size = mapStyle.findBestSize(size);

        int imgWidth = size * map.getWidth();
        int imgHeight = size * map.getHeight();

        BufferedImage image = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        try {
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, imgWidth, imgHeight);

            int xDraw;
            int yDraw = 0;
            for (int y = 0; y < map.getHeight(); y++) {

                xDraw = 0;
                for (int x = 0; x < map.getWidth(); x++) {
                    TileStyle style;
                    if (x == playerX && y == playerY) {
                        style = mapStyle.get(size, map.getAt(x, y).getTile(), playerDir);
                    } else {
                        style = mapStyle.get(size, map.getAt(x, y).getTile(), null);
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
