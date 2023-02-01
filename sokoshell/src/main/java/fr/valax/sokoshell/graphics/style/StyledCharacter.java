package fr.valax.sokoshell.graphics.style;

import fr.valax.sokoshell.graphics.Color;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

import static fr.valax.sokoshell.graphics.GraphicsUtils.*;

/**
 * A style character is a character with a {@link AttributedStyle}
 * or an empty space with a color.
 */
public class StyledCharacter {

    private char c;
    private AttributedStyle style;

    private int rgb;
    private boolean color;

    public StyledCharacter() {
        rgb = java.awt.Color.BLACK.getRGB();
        color = true;
    }

    public StyledCharacter(char c, AttributedStyle style) {
        this.c = c;
        this.style = style;
        color = false;
    }

    public StyledCharacter(int rgb) {
        this.rgb = rgb;
        color = true;
    }

    public void appendTo(AttributedStringBuilder asb) {
        if (color) {
            asb.style(AttributedStyle.DEFAULT.backgroundRgb(rgb)).append(' ');
        } else {
            asb.style(style).append(c);
        }
    }


    /**
     * Sets this character as an empty space with a background color rgb
     * @param rgb background color
     */
    public void setRGB(int rgb) {
        this.rgb = 0xFF000000 | rgb; // remove alpha
        color = true;
    }

    /**
     * Sets this character as c with style {@code style}
     * @param c character
     * @param style style
     */
    public void setAnsi(char c, AttributedStyle style) {
        this.c = c;
        this.style = style;
        color = false;
    }

    /**
     * Merge this character with an empty space with background color argb.
     * If argb is completely transparent (alpha = 0) then this character isn't modified.
     * If argb is completely opaque (alpha = 255) then this character is set an empty
     * space with background color argb.
     * Otherwise, it is as if a mask is drawn on the character: background color
     * and foreground color are blended with argb
     *
     * @param argb background color
     */
    public void mergeRGB(int argb) {
        int alpha = getAlpha(argb);

        if (alpha == 0) {
            return;
        }

        if (alpha == 255) {
            setRGB(argb);
        } else {
            int r = getRed(argb);
            int g = getGreen(argb);
            int b = getBlue(argb);
            double a = alpha / 255d;

            if (color) {
                int red = lerp(a, getRed(this.rgb), r);
                int green = lerp(a, getGreen(this.rgb), g);
                int blue = lerp(a, getBlue(this.rgb), b);

                this.rgb = toRGB(red, green, blue);
            } else {
                blendForeground(a, r, g, b);
                blendBackground(a, r, g, b);
            }
        }
    }

    /**
     * Merge this character with a character with style fgStyle.
     *
     * @param fgChar foreground char
     * @param fgStyle foreground style
     */
    public void mergeAnsi(char fgChar, AttributedStyle fgStyle) {
        if (color) {
            mergeAnsi(' ', AttributedStyle.DEFAULT.backgroundRgb(rgb), fgChar, fgStyle);
        } else {
            mergeAnsi(this.c, this.style, fgChar, fgStyle);
        }
    }

    private void mergeAnsi(char bgChar, AttributedStyle bgStyle, char fgChar, AttributedStyle fgStyle) {
        if (Character.isWhitespace(bgChar)) {
            Color bgBgColor = background(bgStyle);
            Color fgBgColor = background(fgStyle);

            if (bgBgColor == null || fgBgColor != null) {
                this.style = fgStyle;
            } else {
                this.style = bgBgColor.setBG(fgStyle);
            }

            this.c = fgChar;
        } else if (Character.isWhitespace(fgChar)) {
            Color fgFgColor = background(fgStyle);

            if (fgFgColor == null) {
                this.style = bgStyle;
                this.c = bgChar;
            } else {
                this.style = fgFgColor.setFG(bgStyle);
                this.c = fgChar;
            }
        } else { // none of them is whitespace
            Color bgBgColor = background(bgStyle);

            if (bgBgColor == null) {
                this.style = fgStyle;
            } else {
                this.style = bgBgColor.setBG(fgStyle);
            }

            this.c = fgChar;
        }

        color = false;
    }

    private void blendForeground(double alpha, int red, int green, int blue) {
        long styleLong = this.style.getStyle();
        long fg = (styleLong & F_FOREGROUND) != 0 ? styleLong & (FG_COLOR | F_FOREGROUND) : 0;

        if (fg > 0) {
            if ((fg & F_FOREGROUND_RGB) != 0) {
                int r = (int) (fg >> (FG_COLOR_EXP + 16)) & 0xFF;
                int g = (int) (fg >> (FG_COLOR_EXP + 8)) & 0xFF;
                int b = (int) (fg >> FG_COLOR_EXP) & 0xFF;

                style = style.foreground(lerp(alpha, r, red), lerp(alpha, g, green), lerp(alpha, b, blue));
                return;
            } else if ((fg & F_FOREGROUND_IND) != 0) {
                int index = (int) (fg >> FG_COLOR_EXP) & 0xFF;

                if (alpha < 0.5) {
                    style = style.foreground(index);
                    return;
                } // otherwise use rgb
            }
        }

        style = style.foreground(red, green, blue);
    }

    private void blendBackground(double alpha, int red, int green, int blue) {
        long styleLong = this.style.getStyle();
        long bg = (styleLong & F_BACKGROUND) != 0 ? styleLong & (BG_COLOR | F_BACKGROUND) : 0;

        if (bg > 0) {
            if ((bg & F_BACKGROUND_RGB) != 0) {
                int r = (int) (bg >> (BG_COLOR_EXP + 16)) & 0xFF;
                int g = (int) (bg >> (BG_COLOR_EXP + 8)) & 0xFF;
                int b = (int) (bg >> BG_COLOR_EXP) & 0xFF;

                style = style.background(lerp(alpha, r, red), lerp(alpha, g, green), lerp(alpha, b, blue));
                return;
            } else if ((bg & F_BACKGROUND_IND) != 0) {
                int index = (int) (bg >> BG_COLOR_EXP) & 0xFF;

                if (alpha < 0.5) {
                    style = style.background(index);
                    return;
                } // otherwise use rgb
            }
        }

        style = style.background(red, green, blue);
    }

    public char getChar() {
        if (color) {
            return ' ';
        } else {
            return c;
        }
    }

    public AttributedStyle getStyle() {
        if (color) {
            return AttributedStyle.DEFAULT.backgroundRgb(rgb);
        } else {
            return style;
        }
    }
}