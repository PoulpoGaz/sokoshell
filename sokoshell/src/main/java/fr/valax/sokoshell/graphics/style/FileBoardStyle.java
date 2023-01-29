package fr.valax.sokoshell.graphics.style;

import fr.valax.sokoshell.graphics.Graphics;
import fr.valax.sokoshell.graphics.GraphicsUtils;
import fr.valax.sokoshell.solver.board.Board;
import fr.valax.sokoshell.solver.board.Direction;
import fr.valax.sokoshell.solver.board.tiles.TileInfo;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.*;

import static fr.valax.sokoshell.graphics.GraphicsUtils.*;

/**
 * A board style loaded from a file
 */
public class FileBoardStyle extends BoardStyle {

    private final int[] availableSizes;
    private final Map<String, Sampler[]> samplers = new HashMap<>();

    protected FileBoardStyle(BoardStyleReader reader) throws IOException {
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
        size = availableSizes[index];

        Sampler tileSampler = getSampler(tile, index);
        Sampler playerSampler = getSampler(playerDir, index);

        StyledCharacter out = new StyledCharacter();

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                fetch(x, y, out, tileSampler, playerSampler);
                g.getSurface().draw(out.getChar(), out.getStyle(), drawX + x, drawY + y);
            }
        }
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
    public BufferedImage createImage(Board board, int playerX, int playerY, Direction playerDir) {
        int sizeIndex = availableSizes.length - 1;
        int size = availableSizes[sizeIndex];

        if (isImageOnly(sizeIndex)) {
            CreateImageHelper helper = new CreateImageHelper();
            helper.charWidth = 1;
            helper.charHeight = 1;
            helper.tileWidth = size;
            helper.tileHeight = size;

            return helper.createImage(false, this, size, board, playerX, playerY, playerDir);
        } else {
            return new CreateImageHelper()
                    .initAndCreateImage(false, this, size, board, playerX, playerY, playerDir);
        }
    }

    @Override
    public BufferedImage createImageWithLegend(Board board, int playerX, int playerY, Direction playerDir) {
        int sizeIndex = availableSizes.length - 1;
        int size = availableSizes[sizeIndex];

        if (isImageOnly(sizeIndex)) {
            return createImageWithLegendOnlyImage(size, board, playerX, playerY, playerDir);
        } else {
            return new CreateImageHelper()
                    .initAndCreateImage(true, this, size, board, playerX, playerY, playerDir);
        }
    }

    private BufferedImage createImageWithLegendOnlyImage(int size, Board board, int playerX, int playerY, Direction playerDir) {
        CreateImageHelper helper = new CreateImageHelper();

        String widthStr = Integer.toString(board.getWidth());
        String heightStr = Integer.toString(board.getHeight());

        // get size of legend rectangle
        Rectangle2D maxTop = getStringBounds(widthStr);

        Font legendFont = DEFAULT_FONT;
        int scaleFactor = (int) Math.ceil((maxTop.getWidth() + 4d) / size);
        if (scaleFactor > 1) {
            // need to increase scaleFactor, there isn't sufficient space to draw legend

            legendFont = adjustFont(legendFont, widthStr, Math.max((size - 1) * scaleFactor, 1));
            maxTop = getStringBounds(legendFont, widthStr);
        }

        helper.legendFont = legendFont;
        helper.offsetX = (int) Math.ceil(getStringBounds(legendFont, heightStr).getWidth());
        helper.offsetY = (int) Math.ceil(maxTop.getHeight());

        helper.charWidth = scaleFactor;
        helper.charHeight = scaleFactor;
        helper.tileWidth = scaleFactor * size;
        helper.tileHeight = scaleFactor * size;

        return helper.createImage(true, this, size, board, playerX, playerY, playerDir);
    }

    private boolean isImageOnly(int sizeIndex) {
        boolean image = true;
        for (Sampler[] samplers : this.samplers.values()) {
            if (samplers[sizeIndex] instanceof AnsiSampler) {
                image = false;
                break;
            }
        }

        return image;
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
