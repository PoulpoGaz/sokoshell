package fr.valax.sokoshell.graphics;

import org.jline.utils.AttributedStyle;

import java.awt.*;
import java.util.Objects;

public class BasicBorder implements Border {

    public static final Skin LIGHT = new Skin('─', '│', '─', '│', '┌', '┐', '└', '┘');
    public static final Skin HEAVY = new Skin('━', '┃', '━', '┃', '┏', '┓', '┗', '┛');

    protected boolean top;
    protected boolean left;
    protected boolean bottom;
    protected boolean right;

    protected Skin skin;

    public BasicBorder() {
        this(true, true, true, true, LIGHT);
    }

    public BasicBorder(boolean top, boolean left, boolean bottom, boolean right) {
        this(top, left, bottom, right, LIGHT);
    }

    public BasicBorder(boolean top, boolean left, boolean bottom, boolean right, Skin skin) {
        this.top = top;
        this.left = left;
        this.bottom = bottom;
        this.right = right;
        this.skin = Objects.requireNonNull(skin);
    }

    @Override
    public void drawBorder(fr.valax.sokoshell.graphics.Component c, Graphics g, int x, int y, int width, int height) {
        g.setStyle(AttributedStyle.DEFAULT);

        int x2 = x + width - 1;
        int y2 = y + height - 1;

        if (top) {
            g.setChar(skin.top());
            g.drawLine(x + 1, y, x2 - 1, y);
        }

        if (bottom) {
            g.setChar(skin.bottom());
            g.drawLine(x + 1, y2, x2 - 1, y2);
        }

        if (left) {
            g.setChar(skin.left());
            g.drawLine(x, y + 1, x, y2 - 1);
        }

        if (right) {
            g.setChar(skin.right());
            g.drawLine(x2, y + 1, x2, y2 - 1);
        }

        Surface s = g.getSurface();
        drawCorner(s,  x,  y,    top,  left,    skin.top(),  skin.left(),     skin.topLeft());
        drawCorner(s, x2,  y,    top, right,    skin.top(), skin.right(),    skin.topRight());
        drawCorner(s, x2, y2, bottom, right, skin.bottom(),  skin.left(), skin.bottomRight());
        drawCorner(s,  x, y2, bottom,  left, skin.bottom(), skin.right(),  skin.bottomLeft());
    }

    /**
     * if a then draw corner1
     * if b then draw corner2
     * if a && b then draw corner3
     */
    private void drawCorner(Surface s, int x, int y, boolean a, boolean b, char corner1, char corner2, char corner3) {
        if (a && b) {
            s.draw(corner3, AttributedStyle.DEFAULT, x, y);
        } else if (a) {
            s.draw(corner1, AttributedStyle.DEFAULT, x, y);
        } else if (b) {
            s.draw(corner2, AttributedStyle.DEFAULT, x, y);
        }
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(
                top ? 1 : 0,
                left ? 1 : 0,
                bottom ? 1 : 0,
                right ? 1 : 0);
    }

    public record Skin(char top,
                       char left,
                       char bottom,
                       char right,
                       char topLeft,
                       char topRight,
                       char bottomLeft,
                       char bottomRight) {

    }
}
