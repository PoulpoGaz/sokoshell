package fr.valax.sokoshell.graphics;

import fr.valax.sokoshell.graphics.style.Color;
import fr.valax.sokoshell.graphics.style.StyledCharacter;
import org.jline.utils.AttributedCharSequence;
import org.jline.utils.AttributedStyle;
import org.jline.utils.WCWidth;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;

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

    public static final Font DEFAULT_FONT = new Font("Monospaced", Font.PLAIN, 12);
    public static final int CHAR_WIDTH;
    public static final int CHAR_HEIGHT;

    static {
        Rectangle2D max = DEFAULT_FONT.getMaxCharBounds(new FontRenderContext(null, true, true));
        CHAR_WIDTH = (int) Math.ceil(max.getWidth());
        CHAR_HEIGHT = (int) Math.ceil(max.getHeight());

        System.out.println(DEFAULT_FONT.getStringBounds("o", new FontRenderContext(null, true, true)));
        System.out.println(CHAR_WIDTH + " - " + CHAR_HEIGHT);
    }

    public static int columnLength(AttributedCharSequence string) {
        return columnLength(string, 0, string.length());
    }

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

    public static int blend(double alpha, int background, int foreground) {
        return (int) ((1 - alpha) * background + alpha * foreground);
    }

    public static int getAlpha(int rgb) {
        return (rgb >> 24) & 0xFF;
    }

    public static int getRed(int rgb) {
        return (rgb >> 16) & 0xFF;
    }

    public static int getGreen(int rgb) {
        return (rgb >> 8) & 0xFF;
    }

    public static int getBlue(int rgb) {
        return (rgb >> 0) & 0xFF;
    }

    public static int toRGB(int red, int green, int blue) {
        return (red & 0xFF) << 16 | (green & 0xFF) << 8 | (blue & 0xFF);
    }

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
