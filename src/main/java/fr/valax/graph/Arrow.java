package fr.valax.graph;

import java.awt.*;

public abstract class Arrow {

    protected int width;
    protected int height;

    public Arrow(int width, int height) {
        this.width = width;
        this.height = height;

        if (width <= 0 || height <= 0) {
            throw new IllegalStateException();
        }
    }

    public int getWidth() {
        return width;
    }

    public abstract void drawXAxis(Graphics2D g2d, Color color, int lineWidth);

    public abstract void drawYAxis(Graphics2D g2d, Color color, int lineWidth);

    public void setWidth(int width) {
        if (width > 0) {
            this.width = width;
        }
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        if (height > 0) {
            this.height = height;
        }
    }
}