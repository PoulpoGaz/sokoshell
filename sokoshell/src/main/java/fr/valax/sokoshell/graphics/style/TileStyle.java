package fr.valax.sokoshell.graphics.style;

import fr.valax.sokoshell.graphics.Graphics;
import org.jline.utils.AttributedString;

public abstract class TileStyle {

    protected final int size;

    public TileStyle(int size) {
        this.size = size;
    }

    public abstract TileStyle merge(TileStyle foreground);

    public abstract void draw(Graphics g, int x, int y);

    public abstract AttributedString[] getAsString();

    public int getSize() {
        return size;
    }
}
