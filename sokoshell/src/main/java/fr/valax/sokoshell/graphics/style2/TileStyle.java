package fr.valax.sokoshell.graphics.style2;

import fr.valax.sokoshell.graphics.Graphics;
import fr.valax.sokoshell.solver.Map;

public abstract class TileStyle {

    public static final int STYLE_REPEAT = 0;
    public static final int STYLE_CLAMP_TO_EDGE = 1;
    public static final int STYLE_CLAMP_TO_BORDER = 1;
    public static final int STYLE_STRETCH = 2;

    public abstract void draw(Graphics g, Map map, int x, int y, int size);
}
