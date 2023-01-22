package fr.valax.sokoshell.graphics;

import org.jline.utils.AttributedStyle;

import static org.jline.utils.AttributedStyle.*;

/**
 * A color either indexed or 24 bit.
 */
public class Color {

    private static final java.awt.Color BLACK_BRIGHT = java.awt.Color.BLACK.brighter();
    private static final java.awt.Color RED_BRIGHT = java.awt.Color.RED.brighter();
    private static final java.awt.Color GREEN_BRIGHT = java.awt.Color.GREEN.brighter();
    private static final java.awt.Color YELLOW_BRIGHT = java.awt.Color.YELLOW.brighter();
    private static final java.awt.Color BLUE_BRIGHT = java.awt.Color.BLUE.brighter();
    private static final java.awt.Color MAGENTA_BRIGHT = java.awt.Color.MAGENTA.brighter();
    private static final java.awt.Color CYAN_BRIGHT = java.awt.Color.CYAN.brighter();
    private static final java.awt.Color WHITE_BRIGHT = java.awt.Color.WHITE.brighter();

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

    /**
     * Convert this color to the color object of AWT.
     * If this color is indexed then color are transformed as follows:
     * <pre>
     *      BLACK            -> java.awt.Color.BLACK
     *      RED              -> java.awt.Color.RED
     *      GREEN            -> java.awt.Color.GREEN
     *      YELLOW           -> java.awt.Color.YELLOW
     *      BLUE             -> java.awt.Color.BLUE
     *      MAGENTA          -> java.awt.Color.MAGENTA
     *      CYAN             -> java.awt.Color.CYAN
     *      WHITE            -> java.awt.Color.WHITE
     *      BLACK + BRIGHT   -> java.awt.Color.BLACK_BRIGHT
     *      RED + BRIGHT     -> java.awt.Color.RED_BRIGHT
     *      GREEN + BRIGHT   -> java.awt.Color.GREEN_BRIGHT
     *      YELLOW + BRIGHT  -> java.awt.Color.YELLOW_BRIGHT
     *      BLUE + BRIGHT    -> java.awt.Color.BLUE_BRIGHT
     *      MAGENTA + BRIGHT -> java.awt.Color.MAGENTA_BRIGHT
     *      CYAN + BRIGHT    -> java.awt.Color.CYAN_BRIGHT
     *      WHITE + BRIGHT   -> java.awt.Color.WHITE_BRIGHT
     * </pre>
     *
     * @return convert this color to Color of AWT
     */
    public java.awt.Color toAWT() {
        if (isIndexedColor()) {
            return switch (index) {
                case BLACK   -> java.awt.Color.BLACK;
                case RED     -> java.awt.Color.RED;
                case GREEN   -> java.awt.Color.GREEN;
                case YELLOW  -> java.awt.Color.YELLOW;
                case BLUE    -> java.awt.Color.BLUE;
                case MAGENTA -> java.awt.Color.MAGENTA;
                case CYAN    -> java.awt.Color.CYAN;
                case WHITE   -> java.awt.Color.WHITE;
                case BLACK + BRIGHT   -> BLACK_BRIGHT;
                case RED + BRIGHT     -> RED_BRIGHT;
                case GREEN + BRIGHT   -> GREEN_BRIGHT;
                case YELLOW + BRIGHT  -> YELLOW_BRIGHT;
                case BLUE + BRIGHT    -> BLUE_BRIGHT;
                case MAGENTA + BRIGHT -> MAGENTA_BRIGHT;
                case CYAN + BRIGHT    -> CYAN_BRIGHT;
                case WHITE + BRIGHT   -> WHITE_BRIGHT;
                default -> throw new IllegalStateException();
            };
        } else {
            return new java.awt.Color(red, green, blue);
        }
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
}