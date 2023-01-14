package fr.valax.sokoshell.graphics.style;

import fr.valax.sokoshell.SokoShell;
import fr.valax.sokoshell.graphics.Graphics;
import fr.valax.sokoshell.solver.Direction;
import fr.valax.sokoshell.solver.TileInfo;

import static org.jline.utils.AttributedStyle.*;

public class XSBStyle extends MapStyle {

    public XSBStyle() {
        super("xsb", SokoShell.NAME, SokoShell.VERSION);
    }

    @Override
    public void draw(Graphics g, TileInfo tile, Direction playerDir, int drawX, int drawY, int size) {
        g.setStyle(DEFAULT);
        
        if (playerDir != null) {
            if (tile.isTarget()) {
                g.setChar('+');
            } else {
                g.setChar('@');
            }
        } else {
            switch (tile.getTile()) {
                case FLOOR -> g.setChar(' ');
                case WALL -> g.setChar('#');
                case TARGET -> g.setChar('.');
                case CRATE_ON_TARGET -> g.setChar('*');
                case CRATE -> g.setChar('$');
            }
        }

        g.fillRectangle(drawX, drawY, size, size);
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
