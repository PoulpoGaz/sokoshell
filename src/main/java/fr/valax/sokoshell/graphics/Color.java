package fr.valax.sokoshell.graphics;

import org.jline.utils.AttributedStyle;

public class Color {

    private static final long F_FOREGROUND_IND  = 0x00000100;
    private static final long F_FOREGROUND_RGB  = 0x00000200;
    private static final long F_FOREGROUND      = F_FOREGROUND_IND | F_FOREGROUND_RGB;
    private static final long F_BACKGROUND_IND  = 0x00000400;
    private static final long F_BACKGROUND_RGB  = 0x00000800;
    private static final long F_BACKGROUND      = F_BACKGROUND_IND | F_BACKGROUND_RGB;

    private static final int FG_COLOR_EXP    = 15;
    private static final int BG_COLOR_EXP    = 39;
    private static final long FG_COLOR        = 0xFFFFFFL << FG_COLOR_EXP;
    private static final long BG_COLOR        = 0xFFFFFFL << BG_COLOR_EXP;

    public static Color foreground(AttributedStyle style) {
        long fg = (style.getStyle() & F_FOREGROUND) != 0 ? style.getStyle() & (FG_COLOR | F_FOREGROUND) : 0;

        if (fg > 0) {
            if ((fg & F_FOREGROUND_RGB) != 0) {
                int r = (int) (fg >> (FG_COLOR_EXP + 16)) & 0xFF;
                int g = (int) (fg >> (FG_COLOR_EXP + 8)) & 0xFF;
                int b = (int) (fg >> FG_COLOR_EXP) & 0xFF;

                return new Color(r, g, b);
            } else if ((fg & F_FOREGROUND_IND) != 0) {
                int index = (int) (fg >> FG_COLOR_EXP) & 0xFF;

                return new Color(index);
            }
        }

        return null;
    }

    public static Color background(AttributedStyle style) {
        long fg = (style.getStyle() & F_BACKGROUND) != 0 ? style.getStyle() & (BG_COLOR | F_BACKGROUND) : 0;

        if (fg > 0) {
            if ((fg & F_BACKGROUND_RGB) != 0) {
                int r = (int) (fg >> (BG_COLOR_EXP + 16)) & 0xFF;
                int g = (int) (fg >> (BG_COLOR_EXP + 8)) & 0xFF;
                int b = (int) (fg >> BG_COLOR_EXP) & 0xFF;

                return new Color(r, g, b);
            } else if ((fg & F_BACKGROUND_IND) != 0) {
                int index = (int) (fg >> BG_COLOR_EXP) & 0xFF;

                return new Color(index);
            }
        }

        return null;
    }

    public static java.awt.Color awtForeground(AttributedStyle style) {
        Color foreground = foreground(style);

        if (foreground == null) {
            return null;
        } else {
            return foreground.asAWTColor();
        }
    }

    public static java.awt.Color awtBackground(AttributedStyle style) {
        Color background = background(style);

        if (background == null) {
            return null;
        } else {
            return background.asAWTColor();
        }
    }

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