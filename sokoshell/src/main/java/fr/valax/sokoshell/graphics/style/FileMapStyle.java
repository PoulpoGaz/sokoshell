package fr.valax.sokoshell.graphics.style;

import fr.valax.sokoshell.graphics.Graphics;
import fr.valax.sokoshell.graphics.Surface;
import fr.valax.sokoshell.solver.Direction;
import fr.valax.sokoshell.solver.TileInfo;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;

/**
 * A map style loaded from a file
 */
public class FileMapStyle extends MapStyle {

    private final int[] availableSizes;
    private final Map<String, Sampler[]> samplers = new HashMap<>();

    public FileMapStyle(MapStyleReader reader) throws IOException {
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
