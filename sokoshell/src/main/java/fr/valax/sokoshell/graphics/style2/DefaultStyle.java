package fr.valax.sokoshell.graphics.style2;

import fr.valax.sokoshell.SokoShell;
import fr.valax.sokoshell.graphics.Graphics;
import fr.valax.sokoshell.solver.Direction;
import fr.valax.sokoshell.solver.TileInfo;

import static org.jline.utils.AttributedStyle.*;

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
            switch (tile.getTile()) {
                case TARGET -> g.setStyle(DEFAULT.background(RED));
                case CRATE -> g.setStyle(DEFAULT.background(YELLOW));
                case WALL -> g.setStyle(DEFAULT.background(WHITE));
                case FLOOR -> g.setStyle(DEFAULT.background(GREEN));
                case CRATE_ON_TARGET -> g.setStyle(DEFAULT.background(CYAN));
            }
        }

        g.drawRectangle(drawX, drawY, size, size);
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
