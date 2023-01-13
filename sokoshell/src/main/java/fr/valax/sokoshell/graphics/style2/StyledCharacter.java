package fr.valax.sokoshell.graphics.style2;

import fr.valax.sokoshell.graphics.style.Color;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

import static fr.valax.sokoshell.graphics.GraphicsUtils.*;

public class StyledCharacter {

    private char c;
    private AttributedStyle style;

    private int rgb = java.awt.Color.BLACK.getRGB();
    private boolean color = true;


    public void appendTo(AttributedStringBuilder asb) {
        if (color) {
            asb.style(AttributedStyle.DEFAULT.backgroundRgb(rgb)).append(' ');
        } else {
            asb.style(style).append(c);
        }
    }


    public void setRGB(int rgb) {
        this.rgb = 0xFF000000 | rgb; // remove alpha
        color = true;
    }

    public void setAnsi(char c, AttributedStyle style) {
        this.c = c;
        this.style = style;
        color = false;
    }

    public void mergeRGB(int rgb) {
        int alpha = (rgb >> 24) & 0xFF;

        if (alpha == 0) {
            return;
        }

        if (alpha == 255) {
            setRGB(rgb);
        } else {
            int r = getRed(rgb);
            int g = getGreen(rgb);
            int b = getBlue(rgb);
            double a = alpha / 255d;

            if (color) {
                int red = blend(a, getRed(this.rgb), r);
                int green = blend(a, getGreen(this.rgb), g);
                int blue = blend(a, getBlue(this.rgb), b);

                this.rgb = toRGB(red, green, blue);
            } else {
                blendForeground(a, r, g, b);
                blendBackground(a, r, g, b);
            }
        }
    }

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
            Color fgFgColor = Color.background(fgStyle);

            if (fgFgColor == null) {
                this.style = bgStyle;
                this.c = bgChar;
            } else {
                this.style = fgFgColor.setFG(bgStyle);
                this.c = fgChar;
            }
        } else { // none of them is whitespace
            Color bgBgColor = Color.background(bgStyle);

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

                style.foreground(blend(alpha, r, red), blend(alpha, g, green), blend(alpha, b, blue));
                return;
            } else if ((fg & F_FOREGROUND_IND) != 0) {
                int index = (int) (fg >> FG_COLOR_EXP) & 0xFF;

                if (alpha < 0.5) {
                    style.foreground(index);
                    return;
                } // otherwise use rgb
            }
        }

        style.foreground(red, green, blue);
    }

    private void blendBackground(double alpha, int red, int green, int blue) {
        long styleLong = this.style.getStyle();
        long bg = (styleLong & F_BACKGROUND) != 0 ? styleLong & (BG_COLOR | F_BACKGROUND) : 0;

        if (bg > 0) {
            if ((bg & F_BACKGROUND_RGB) != 0) {
                int r = (int) (bg >> (BG_COLOR_EXP + 16)) & 0xFF;
                int g = (int) (bg >> (BG_COLOR_EXP + 8)) & 0xFF;
                int b = (int) (bg >> BG_COLOR_EXP) & 0xFF;

                style.foreground(blend(alpha, r, red), blend(alpha, g, green), blend(alpha, b, blue));
                return;
            } else if ((bg & F_BACKGROUND_IND) != 0) {
                int index = (int) (bg >> BG_COLOR_EXP) & 0xFF;

                if (alpha < 0.5) {
                    style.foreground(index);
                    return;
                } // otherwise use rgb
            }
        }

        style.foreground(red, green, blue);
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