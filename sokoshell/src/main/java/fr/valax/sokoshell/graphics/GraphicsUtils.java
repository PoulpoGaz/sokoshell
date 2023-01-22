package fr.valax.sokoshell.graphics;

import fr.valax.sokoshell.graphics.style.StyledCharacter;
import org.jline.utils.AttributedCharSequence;
import org.jline.utils.AttributedStyle;
import org.jline.utils.WCWidth;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;

/**
 * Utility class for drawing and styles
 */
public class GraphicsUtils {

    public static final long F_FOREGROUND_IND  = 0x00000100;
    public static final long F_FOREGROUND_RGB  = 0x00000200;
    public static final long F_FOREGROUND      = F_FOREGROUND_IND | F_FOREGROUND_RGB;
    public static final int FG_COLOR_EXP       = 15;
    public static final long FG_COLOR          = 0xFFFFFFL << FG_COLOR_EXP;

    public static final long F_BACKGROUND_IND  = 0x00000400;
    public static final long F_BACKGROUND_RGB  = 0x00000800;
    public static final long F_BACKGROUND      = F_BACKGROUND_IND | F_BACKGROUND_RGB;
    public static final int BG_COLOR_EXP       = 39;
    public static final long BG_COLOR          = 0xFFFFFFL << BG_COLOR_EXP;

    /**
     * The default font used by style when creating an image
     */
    public static final Font DEFAULT_FONT = new Font("Monospaced", Font.PLAIN, 12);

    /**
     * The maximal width of a char in the {@linkplain #DEFAULT_FONT default font}
     */
    public static final int CHAR_WIDTH;

    /**
     * The maximal height of a char in the {@linkplain #DEFAULT_FONT default font}
     */
    public static final int CHAR_HEIGHT;

    static {
        Rectangle2D max = DEFAULT_FONT.getMaxCharBounds(new FontRenderContext(null, true, true));
        CHAR_WIDTH = (int) Math.ceil(max.getWidth());
        CHAR_HEIGHT = (int) Math.ceil(max.getHeight());
    }

    /**
     * @param string a string
     * @return the length of the string in column
     * @see WCWidth
     */
    public static int columnLength(AttributedCharSequence string) {
        return columnLength(string, 0, string.length());
    }

    /**
     * @param string a string
     * @param start start index inclusive
     * @param end end index inclusive
     * @return the length of the string in column from start to end
     * @see WCWidth
     */
    public static int columnLength(AttributedCharSequence string, int start, int end) {
        int cols = 0;
        int len = end - start;
        for (int cur = 0; cur < len; ) {
            int cp = string.codePointAt(cur);
            if (!string.isHidden(cur)) {
                cols += WCWidth.wcwidth(cp);
            }

            cur += Character.charCount(cp);
        }
        return cols;
    }

    /**
     *
     * @param t parameter t
     * @param a parameter a
     * @param b parameter b
     * @return computes the linear interpolation between a and b with parameter t
     */
    public static int lerp(double t, int a, int b) {
        return (int) ((1 - t) * a + t * b);
    }

    /**
     * @param rgb 32-bit color formatted as ARGB
     * @return the alpha component of rgb between 0 and 255
     */
    public static int getAlpha(int rgb) {
        return (rgb >> 24) & 0xFF;
    }

    /**
     * @param rgb 32-bit color formatted as ARGB
     * @return the red component of rgb between 0 and 255
     */
    public static int getRed(int rgb) {
        return (rgb >> 16) & 0xFF;
    }

    /**
     * @param rgb 32-bit color formatted as ARGB
     * @return the green component of rgb between 0 and 255
     */
    public static int getGreen(int rgb) {
        return (rgb >> 8) & 0xFF;
    }

    /**
     * @param rgb 32-bit color formatted as ARGB
     * @return the blue component of rgb between 0 and 255
     */
    public static int getBlue(int rgb) {
        return (rgb >> 0) & 0xFF;
    }

    /**
     *
     * @param red red component, between 0 and 255
     * @param green green component, between 0 and 255
     * @param blue green component, between 0 and 255
     * @return convert to 24 bits color formatted as RGB (8 bits per component)
     */
    public static int toRGB(int red, int green, int blue) {
        return (red & 0xFF) << 16 | (green & 0xFF) << 8 | (blue & 0xFF);
    }

    /**
     * @param style style to extract the foreground color
     * @return the foreground color in the specified style or {@code null} if not defined
     */
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

    /**
     * @param style style to extract the background color
     * @return the background color in the specified style or {@code null} if not defined
     */
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

    /**
     * Draw a character in the rectangle (x, y, charWidth, charHeight).
     * charWidth and charHeight are assumed to be maximal size of a character
     * for the font used by Graphics2D. The character is drawn at
     * {@code y + char height - font's max descent} and at x.<br>
     * A rectangle is filled with the background color of the character,
     * otherwise if not defined the defaultBackground. If no foreground
     * color is defined, the character is drawn with defaultForeground.
     *
     * @param g2d graphics to draw with
     * @param c the character to draw
     * @param x x position
     * @param y y position
     * @param charWidth maximal width of a character
     * @param charHeight maximal height of a character
     * @param defaultBackground default background
     * @param defaultForeground default foreground
     */
    public static void draw(Graphics2D g2d, StyledCharacter c,
                            int x, int y, int charWidth, int charHeight,
                            java.awt.Color defaultBackground, java.awt.Color defaultForeground) {
        Color background = background(c.getStyle());
        if (background != null) {
            g2d.setColor(background.toAWT());
        } else {
            g2d.setColor(defaultBackground);
        }

        g2d.fillRect(x, y, charWidth, charHeight);


        Color foreground = foreground(c.getStyle());
        if (foreground != null) {
            g2d.setColor(foreground.toAWT());
        } else {
            g2d.setColor(defaultForeground);
        }

        FontMetrics fm = g2d.getFontMetrics();
        int yDraw = y + (charHeight - fm.getMaxDescent());

        g2d.drawString(String.valueOf(c.getChar()), x, yDraw);
    }
}
