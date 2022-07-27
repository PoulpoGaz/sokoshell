package fr.valax.sokoshell.graphics;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

public class AnsiTile extends TileStyle {

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

    private final AttributedString[] ansi;

    public AnsiTile(int size, AttributedString[] ansi) {
        super(size);
        this.ansi = ansi;

        if (ansi.length != size) {
            throw new IllegalArgumentException();
        }

        for (AttributedString str : ansi) {
            if (str == null || str.columnLength() != size) {
                throw new IllegalArgumentException("Invalid length: " + str + ". length=" + str.columnLength());
            }
        }
    }

    @Override
    public TileStyle merge(TileStyle foreground) {
        if (foreground instanceof ImageTile fg) {
            return mergeImage(fg);
        } else if (foreground instanceof AnsiTile fg) {
            return mergeAnsi(fg);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private TileStyle mergeImage(ImageTile fg) {
        AttributedString[] strings = new AttributedString[size];
        AttributedStringBuilder asb = new AttributedStringBuilder();

        for (int y = 0; y < size; y++) {
            asb.setLength(0);

            for (int x = 0; x < size; x++) {
                AttributedStyle style = ansi[y].styleAt(x);
                char c = ansi[y].charAt(x);

                int rgba = fg.getImage().getRGB(x, y);
                int alpha = (rgba >> 24) & 0xFF;
                int red   = (rgba >> 16) & 0xFF;
                int green = (rgba >>  8) & 0xFF;
                int blue  =  rgba        & 0xFF;

                if (alpha == 0) {
                    asb.styled(style, c + "");
                } else if (alpha < 255) {

                    Color fgColor = blendForeground(style.getStyle(), alpha / 255d, red, green, blue);
                    Color bgColor = blendBackground(style.getStyle(), alpha / 255d, red, green, blue);

                    style = fgColor.setFG(style);
                    style = bgColor.setBG(style);
                    asb.styled(style, c + "");
                } else { // completely erase background
                    asb.styled(AttributedStyle.DEFAULT.background(red, green, blue), " ");
                }
            }

            strings[y] = asb.toAttributedString();
        }

        return new AnsiTile(size, strings);
    }

    private TileStyle mergeAnsi(AnsiTile fg) {
        AttributedString[] strings = new AttributedString[size];
        AttributedStringBuilder asb = new AttributedStringBuilder();

        for (int y = 0; y < size; y++) {
            asb.setLength(0);

            for (int x = 0; x < size; x++) {
                AttributedStyle bgStyle = ansi[y].styleAt(x);
                char bgChar = ansi[y].charAt(x);

                AttributedStyle fgStyle = fg.ansi[y].styleAt(x);
                char fgChar = fg.ansi[y].charAt(x);

                char c;
                if (Character.isWhitespace(fgChar)) {
                    c = bgChar;
                } else {
                    c = fgChar;
                }

                Color bg = Color.background(bgStyle);

                AttributedStyle style;
                if (bg != null) {
                    style = bg.setBG(fgStyle);
                } else {
                    style = fgStyle;
                }

                asb.styled(style, c + "");
            }

            strings[y] = asb.toAttributedString();
        }

        return new AnsiTile(size, strings);
    }

    private Color blendForeground(long style, double alpha, int red, int green, int blue) {
        long fg = (style & F_FOREGROUND) != 0 ? style & (FG_COLOR | F_FOREGROUND) : 0;

        if (fg > 0) {
            if ((fg & F_FOREGROUND_RGB) != 0) {
                int r = (int) (fg >> (FG_COLOR_EXP + 16)) & 0xFF;
                int g = (int) (fg >> (FG_COLOR_EXP + 8)) & 0xFF;
                int b = (int) (fg >> FG_COLOR_EXP) & 0xFF;

                return new Color(blend(alpha, r, red), blend(alpha, g, green), blend(alpha, b, blue));
            } else if ((fg & F_FOREGROUND_IND) != 0) {
                int index = (int) (fg >> FG_COLOR_EXP) & 0xFF;

                if (alpha < 0.5) {
                    return new Color(index);
                } // otherwise use rgb
             }
        }
        return new Color(red, green, blue);
    }

    private Color blendBackground(long style, double alpha, int red, int green, int blue) {
        long bg = (style & F_BACKGROUND) != 0 ? style & (BG_COLOR | F_BACKGROUND) : 0;

        if (bg > 0) {
            if ((bg & F_BACKGROUND_RGB) != 0) {
                int r = (int) (bg >> (BG_COLOR_EXP + 16)) & 0xFF;
                int g = (int) (bg >> (BG_COLOR_EXP + 8)) & 0xFF;
                int b = (int) (bg >> BG_COLOR_EXP) & 0xFF;

                return new Color(blend(alpha, r, red), blend(alpha, g, green), blend(alpha, b, blue));
            } else if ((bg & F_BACKGROUND_IND) != 0) {
                int index = (int) (bg >> BG_COLOR_EXP) & 0xFF;

                if (alpha < 0.5) {
                    return new Color(index);
                } // otherwise use rgb
            }
        }
        return new Color(red, green, blue);
    }

    private int blend(double alpha, int background, int foreground) {
        return (int) ((1 - alpha) * background + alpha * foreground);
    }

    @Override
    public void draw(Graphics g, int x, int y) {
        for (int i = 0; i < ansi.length; i++) {
            g.getSurface().draw(ansi[i], x, y + i);
        }
    }

    @Override
    public AttributedString[] getAsString() {
        return ansi;
    }
}
