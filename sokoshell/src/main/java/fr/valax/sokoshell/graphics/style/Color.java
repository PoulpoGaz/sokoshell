package fr.valax.sokoshell.graphics.style;

import org.jline.utils.AttributedStyle;

public class Color {

    private final int index;
    private final int red;
    private final int green;
    private final int blue;

    public Color(int index) {
        this.red = -1;
        this.green = -1;
        this.blue = -1;
        this.index = index;
    }

    public Color(int red, int green, int blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.index = -1;
    }

    public AttributedStyle setFG(AttributedStyle style) {
        if (index >= 0) {
            return style.foreground(index);
        } else {
            return style.foreground(red, green, blue);
        }
    }

    public AttributedStyle setBG(AttributedStyle style) {
        if (index >= 0) {
            return style.background(index);
        } else {
            return style.background(red, green, blue);
        }
    }

    public boolean isIndexedColor() {
        return index >= 0;
    }

    public boolean isRGBColor() {
        return !isIndexedColor();
    }

    public int getIndex() {
        return index;
    }

    public int getRed() {
        return red;
    }

    public int getGreen() {
        return green;
    }

    public int getBlue() {
        return blue;
    }

    public java.awt.Color asAWTColor() {
        return new java.awt.Color(red, green, blue);
    }
}