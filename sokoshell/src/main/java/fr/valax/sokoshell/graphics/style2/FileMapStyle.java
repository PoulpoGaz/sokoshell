package fr.valax.sokoshell.graphics.style2;

import fr.valax.sokoshell.graphics.Graphics;
import fr.valax.sokoshell.graphics.Surface;
import fr.valax.sokoshell.graphics.style.Color;
import fr.valax.sokoshell.solver.Direction;
import fr.valax.sokoshell.solver.TileInfo;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Objects;

import static fr.valax.sokoshell.graphics.GraphicsUtils.*;

/**
 * A map style loaded from a file
 */
public class FileMapStyle extends MapStyle {

    private int[] availableSizes;
    private java.util.Map<String, Sampler[]> samplers = new HashMap<>();

    public FileMapStyle(String name, String author, String version) {
        super(name, author, version);
    }

    @Override
    public void draw(Graphics g, TileInfo tile, Direction playerDir, int drawX, int drawY, int size) {
        int index = findBestSizeIndex(size);

        Sampler tileSampler = getSampler(tile, index);
        Sampler playerSampler = getSampler(playerDir, index);

        drawSamplers(g.getSurface(), drawX, drawY, availableSizes[index], tileSampler, playerSampler);
    }

    protected void drawSamplers(Surface s, int drawX, int drawY, int size, Sampler... samplers) {
        StyledCharacter out = new StyledCharacter();

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {

                boolean merge = false;
                for (Sampler sampler : samplers) {
                    if (sampler != null) {
                        sampler.fetch(x, y, out, merge);
                        merge = true;
                    }
                }

                s.draw(out.getChar(), out.getStyle(), drawX + x, drawY + y);
            }
        }
    }

    protected Sampler getSampler(TileInfo tile, int sizeIndex) {
        return samplers.get(tile.getTile().name())[sizeIndex];
    }

    protected Sampler getSampler(Direction dir, int sizeIndex) {
        if (dir == null) {
            return null;
        } else {
            return samplers.get(dir.name())[sizeIndex];
        }
    }

    @Override
    public int findBestSize(int size) {
        int i = findBestSizeIndex(size);

        if (i < 0) {
            return -1;
        } else {
            return availableSizes[i];
        }
    }

    private int findBestSizeIndex(int size) {
        int bestI = -1;

        for (int i = 0; i < availableSizes.length; i++) {

            if (availableSizes[i] == size) {
                return i;
            } else if (availableSizes[i] < size) {
                if (bestI < 0) {
                    bestI = i;
                } else if (availableSizes[i] > availableSizes[bestI]) {
                    bestI = i;
                }
            }

        }

        return bestI;
    }

    @Override
    public boolean isSupported(int size) {
        for (int availableSize : availableSizes) {
            if (availableSize == size) {
                return true;
            }
        }

        return false;
    }

    protected interface Sampler {

        void fetch(int x, int y, StyledCharacter out, boolean merge);
    }


    protected static class ImageSampler implements Sampler {

        private final BufferedImage image;

        public ImageSampler(BufferedImage image) {
            this.image = Objects.requireNonNull(image);

            if (image.getWidth() != image.getHeight()) {
                throw new IllegalArgumentException("Not a square");
            }
        }

        @Override
        public void fetch(int x, int y, StyledCharacter out, boolean merge) {
            if (merge) {
                out.mergeRGB(image.getRGB(x, y));
            } else {
                out.setRGB(image.getRGB(x, y));
            }
        }
    }


    protected static class AnsiSampler implements Sampler {

        private final AttributedString[] ansi;

        public AnsiSampler(int size, AttributedString[] ansi) {
            this.ansi = ansi;

            if (ansi.length != size) {
                throw new IllegalArgumentException();
            }

            for (AttributedString str : ansi) {
                if (str == null || str.columnLength() != size) {
                    throw new IllegalArgumentException("Invalid length: " + str + ".");
                } else if (str.columnLength() != size) {
                    throw new IllegalArgumentException("Invalid length: " + str + ". length=" + str.columnLength());
                }
            }
        }

        @Override
        public void fetch(int x, int y, StyledCharacter out, boolean merge) {
            char c = ansi[y].charAt(x);
            AttributedStyle style = ansi[y].styleAt(x);

            if (merge) {
                out.mergeAnsi(c, style);
            } else {
                out.setAnsi(c, style);
            }
        }
    }


    protected static class StyledCharacter {

        private char c;
        private AttributedStyle style;

        private int rgb = java.awt.Color.BLACK.getRGB();
        private boolean color = true;

        public void setRGB(int rgb) {
            this.rgb = rgb & 0xFF000000; // remove alpha
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
            return c;
        }

        public AttributedStyle getStyle() {
            return style;
        }
    }
}
