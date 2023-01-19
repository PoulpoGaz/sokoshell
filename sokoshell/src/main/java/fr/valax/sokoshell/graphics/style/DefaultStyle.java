package fr.valax.sokoshell.graphics.style;

import fr.valax.sokoshell.SokoShell;
import fr.valax.sokoshell.graphics.Graphics;
import fr.valax.sokoshell.solver.Direction;
import fr.valax.sokoshell.solver.Map;
import fr.valax.sokoshell.solver.TileInfo;

import java.awt.*;
import java.awt.image.BufferedImage;

import static org.jline.utils.AttributedStyle.*;

@Deprecated
public class DefaultStyle extends MapStyle {

    public DefaultStyle() {
        super("default", SokoShell.NAME, SokoShell.VERSION);
    }

    @Override
    public void draw(Graphics g, TileInfo tile, Direction playerDir, int drawX, int drawY, int size) {
        if (playerDir != null) {
            g.setChar('o');

            if (tile.isTarget()) {
                g.setStyle(DEFAULT.background(GREEN));
            } else {
                g.setStyle(DEFAULT.background(RED));
            }
        } else {
            g.setChar(' ');
        }

        g.fillRectangle(drawX, drawY, size, size);
    }

    @Override
    public void draw(Graphics2D g2d, TileInfo tile, Direction playerDir, int drawX, int drawY, int size, int charWidth, int charHeight) {

    }

    @Override
    public BufferedImage createImage(Map map, int playerX, int playerY, Direction playerDir) {
        return null;
    }

    @Override
    public int findBestSize(int size) {
        return 1;
    }

    @Override
    public boolean isSupported(int size) {
        return true;
    }
}
