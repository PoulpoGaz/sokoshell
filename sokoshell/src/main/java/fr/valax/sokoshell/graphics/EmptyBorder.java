package fr.valax.sokoshell.graphics;

import java.awt.*;

public class EmptyBorder implements Border {

    protected int top;
    protected int left;
    protected int bottom;
    protected int right;

    public EmptyBorder() {
        top = left = bottom = right = 0;
    }

    public EmptyBorder(int top, int left, int bottom, int right) {
        this.top = top;
        this.left = left;
        this.bottom = bottom;
        this.right = right;
    }

    @Override
    public void drawBorder(Component c, Graphics g, int x, int y, int width, int height) {

    }

    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(top, left, bottom, right);
    }
}
