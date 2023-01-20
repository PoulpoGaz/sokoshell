package fr.valax.sokoshell.graphics.style;

import fr.valax.sokoshell.graphics.Graphics;
import fr.valax.sokoshell.graphics.GraphicsUtils;
import fr.valax.sokoshell.graphics.Surface;
import fr.valax.sokoshell.solver.Direction;
import fr.valax.sokoshell.solver.Tile;
import fr.valax.sokoshell.solver.TileInfo;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;

import java.awt.*;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * A map style loaded from a file
 */
public class FileMapStyle extends MapStyle {

    private final int[] availableSizes;
    private final Map<String, Sampler[]> samplers = new HashMap<>();

    protected FileMapStyle(MapStyleReader reader) throws IOException {
        super(reader.name, reader.author, reader.version);

        int[] availableSizes = null;

        for (Map.Entry<String, List<Sampler>> s : reader.samplers.entrySet()) {
            Sampler[] samplerArray = s.getValue().toArray(new Sampler[0]);
            Arrays.sort(samplerArray, Comparator.comparingInt(Sampler::getSize));

            if (availableSizes == null) {
                availableSizes = new int[samplerArray.length];

                for (int i = 0; i < samplerArray.length; i++) {
                    availableSizes[i] = samplerArray[i].getSize();

                    if (i > 0 && availableSizes[i - 1] == availableSizes[i]) {
                        throw new IOException("Duplicate sampler for " + s.getKey());
                    }
                }
            } else {
                if (samplerArray.length != availableSizes.length) {
                    throw new IOException("Invalid number of sampler for " + s.getKey());
                }

                for (int i = 0; i < samplerArray.length; i++) {
                    if (availableSizes[i] != samplerArray[i].getSize()) {
                        throw new IOException("Invalid sampler size for " + s.getKey());
                    }
                }
            }

            samplers.put(s.getKey(), samplerArray);
        }

        this.availableSizes = Objects.requireNonNull(availableSizes);
    }

    @Override
    public void draw(Graphics g, TileInfo tile, Direction playerDir, int drawX, int drawY, int size) {
        int index = findBestSizeIndex(size);

        Sampler tileSampler = getSampler(tile, index);
        Sampler playerSampler = getSampler(playerDir, index);

        drawSamplers(g.getSurface(), drawX, drawY, availableSizes[index], tileSampler, playerSampler);
    }

    @Override
    public void draw(Graphics2D g2d,
                     TileInfo tile, Direction playerDir,
                     int drawX, int drawY, int size, int charWidth, int charHeight) {
        int index = findBestSizeIndex(size);

        Sampler tileSampler = getSampler(tile, index);
        Sampler playerSampler = getSampler(playerDir, index);

        StyledCharacter out = new StyledCharacter();

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                fetch(x, y, out, tileSampler, playerSampler);
                GraphicsUtils.draw(g2d, out, drawX + x * charWidth, drawY + y * charHeight, charWidth, charHeight, Color.BLACK, Color.WHITE);
            }
        }
    }

    @Override
    public BufferedImage createImage(fr.valax.sokoshell.solver.Map map, int playerX, int playerY, Direction playerDir) {
        int sizeIndex = availableSizes.length - 1;
        int size = availableSizes[sizeIndex];

        boolean image = true;
        for (Sampler[] samplers : this.samplers.values()) {
            if (samplers[sizeIndex] instanceof AnsiSampler) {
                image = false;
                break;
            }
        }

        if (image) {
            BufferedImage img = new BufferedImage(map.getWidth() * size, map.getHeight() * size, BufferedImage.TYPE_INT_ARGB);

            Graphics2D g2d = img.createGraphics();
            try {
                for (int y = 0; y < map.getHeight(); y++) {
                    for (int x = 0; x < map.getWidth(); x++) {
                        TileInfo tile = map.getAt(x, y);

                        ImageSampler tileSampler = (ImageSampler) getSampler(tile, sizeIndex);
                        g2d.drawImage(tileSampler.image, x * size, y * size, null);

                        if (x == playerX && y == playerY) {
                            ImageSampler playerSampler = (ImageSampler) getSampler(playerDir, sizeIndex);
                            g2d.drawImage(playerSampler.image, x * size, y * size, null);
                        }
                    }
                }

            } finally {
                g2d.dispose();
            }

            return img;
        } else {
            BufferedImage img = new BufferedImage(
                    map.getWidth() * GraphicsUtils.CHAR_WIDTH * size,
                    map.getHeight() * GraphicsUtils.CHAR_HEIGHT * size,
                    BufferedImage.TYPE_INT_ARGB);

            Graphics2D g2d = img.createGraphics();
            try {
                g2d.setFont(GraphicsUtils.DEFAULT_FONT);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                for (int y = 0; y < map.getHeight(); y++) {
                    for (int x = 0; x < map.getWidth(); x++) {
                        TileInfo tile = map.getAt(x, y);

                        int drawX = x * size * GraphicsUtils.CHAR_WIDTH;
                        int drawY = y * size * GraphicsUtils.CHAR_HEIGHT;
                        if (playerX == x && playerY == y) {
                            draw(g2d, tile, playerDir, drawX, drawY, size, GraphicsUtils.CHAR_WIDTH, GraphicsUtils.CHAR_HEIGHT);
                        } else {
                            draw(g2d, tile, null, drawX, drawY, size, GraphicsUtils.CHAR_WIDTH, GraphicsUtils.CHAR_HEIGHT);
                        }
                    }
                }

            } finally {
                g2d.dispose();
            }

            return img;
        }
    }

    protected void drawSamplers(Surface s, int drawX, int drawY, int size, Sampler... samplers) {
        StyledCharacter out = new StyledCharacter();

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                fetch(x, y, out, samplers);
                s.draw(out.getChar(), out.getStyle(), drawX + x, drawY + y);
            }
        }
    }

    private void fetch(int x, int y, StyledCharacter out, Sampler... samplers) {
        boolean merge = false;
        for (Sampler sampler : samplers) {
            if (sampler != null) {
                sampler.fetch(x, y, out, merge);
                merge = true;
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
        int minI = 0; // included
        int maxI = availableSizes.length; // excluded

        while (maxI - minI > 1) {
            int i = (maxI + minI) / 2;

            if (availableSizes[i] == size) {
                return i;
            } else if (availableSizes[i] > size) {
                maxI = i;
            } else {
                if (i + 1 >= availableSizes.length || availableSizes[i + 1] > size) {
                    return i;
                }

                minI = i;
            }
        }

        return minI;
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

        int getSize();
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

        @Override
        public int getSize() {
            return image.getWidth();
        }

        public BufferedImage getImage() {
            return image;
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

        @Override
        public int getSize() {
            return ansi.length;
        }
    }
}
