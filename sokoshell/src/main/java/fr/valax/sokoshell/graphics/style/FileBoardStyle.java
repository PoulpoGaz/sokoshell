package fr.valax.sokoshell.graphics.style;

import fr.valax.sokoshell.graphics.Graphics;
import fr.valax.sokoshell.graphics.GraphicsUtils;
import fr.valax.sokoshell.solver.board.Board;
import fr.valax.sokoshell.solver.board.Direction;
import fr.valax.sokoshell.solver.board.tiles.Tile;
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

    public static final String NO_DIRECTION = "no_direction";
    public static final String DEAD_TILE = "dead_tile";
    public static final String TUNNEL    = "tunnel";
    public static final String ROOM      = "room";

    private final int[] availableSizes;

    // samplers by size then name
    private final Map<Integer, Map<String, Sampler>> samplersBySize = new HashMap<>();

    protected FileBoardStyle(BoardStyleReader reader) throws IOException {
        super(reader.name, reader.author, reader.version);

        for (Map.Entry<String, List<Sampler>> entry : reader.samplers.entrySet()) {
            for (Sampler sampler : entry.getValue()) {
                Map<String, Sampler> map = samplersBySize.computeIfAbsent(sampler.getSize(), k -> new HashMap<>());

                map.put(entry.getKey(), sampler);
            }
        }

        checkSamplers();

        this.availableSizes = samplersBySize.keySet().stream().mapToInt(i -> i).sorted().toArray();
    }

    private void checkSamplers() throws IOException {
        for (Map.Entry<Integer, Map<String, Sampler>> entry : samplersBySize.entrySet()) {
            Map<String, Sampler> samplers = entry.getValue();

            for (Tile tile : Tile.values()) {
                if (!samplers.containsKey(tile.name())) {
                    throw new IOException("Missing sampler " + tile.name() + " for size " + entry.getKey());
                }
            }

            for (Direction dir : Direction.values()) {
                if (!samplers.containsKey(dir.name())) {
                    throw new IOException("Missing sampler " + dir.name() + " for size " + entry.getKey());
                }
            }
        }
    }

    @Override
    public void draw(Graphics g, TileInfo tile, boolean player, Direction playerDir, int drawX, int drawY, int size) {
        int index = findBestSizeIndex(size);
        size = availableSizes[index];

        List<Sampler> samplers = getSamplersFor(tile, player, playerDir, size);
        draw(g, samplers, drawX, drawY, size);
    }

    @Override
    public void draw(Graphics g, Tile tile, boolean player, Direction playerDir, int drawX, int drawY, int size) {
        int index = findBestSizeIndex(size);
        size = availableSizes[index];

        List<Sampler> samplers = getSamplersFor(tile, player, playerDir, size);
        draw(g, samplers, drawX, drawY, size);
    }

    private void draw(Graphics g, List<Sampler> samplers, int drawX, int drawY, int size) {
        StyledCharacter out = new StyledCharacter();

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                fetch(x, y, out, samplers);
                g.getSurface().draw(out.getChar(), out.getStyle(), drawX + x, drawY + y);
            }
        }
    }


    @Override
    public void draw(Graphics2D g2d,
                     TileInfo tile, boolean player, Direction playerDir,
                     int drawX, int drawY, int size, int charWidth, int charHeight) {
        int index = findBestSizeIndex(size);
        size = availableSizes[index];

        List<Sampler> samplers = getSamplersFor(tile, player, playerDir, size);
        draw(g2d, samplers, drawX, drawY, size, charWidth, charHeight);
    }

    @Override
    public void draw(Graphics2D g2d, Tile tile, boolean player, Direction playerDir, int drawX, int drawY, int size, int charWidth, int charHeight) {
        int index = findBestSizeIndex(size);
        size = availableSizes[index];

        List<Sampler> samplers = getSamplersFor(tile, player, playerDir, size);
        draw(g2d, samplers, drawX, drawY, size, charWidth, charHeight);
    }

    private void draw(Graphics2D g2d, List<Sampler> samplers, int drawX, int drawY, int size, int charWidth, int charHeight) {
        StyledCharacter out = new StyledCharacter();

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                fetch(x, y, out, samplers);
                GraphicsUtils.draw(g2d, out, drawX + x * charWidth, drawY + y * charHeight, charWidth, charHeight, Color.BLACK, Color.WHITE);
            }
        }
    }

    private void fetch(int x, int y, StyledCharacter out, List<Sampler> samplers) {
        boolean merge = false;
        for (Sampler sampler : samplers) {
            if (sampler != null) {
                sampler.fetch(x, y, out, merge);
                merge = true;
            }
        }
    }

    protected List<Sampler> getSamplersFor(TileInfo tile, boolean player, Direction playerDir, int size) {
        Map<String, Sampler> allSamplers = samplersBySize.get(size);

        List<Sampler> samplers = new ArrayList<>();
        addDefaultSamplers(allSamplers, samplers, tile.getTile(), player, playerDir);
        if (tile.isDeadTile() && drawDeadTiles) {
            samplers.add(allSamplers.get(DEAD_TILE));
        }
        if (tile.isInATunnel() && drawTunnels) {
            samplers.add(allSamplers.get(TUNNEL));
        }
        if (tile.isInARoom() && drawRooms) {
            samplers.add(allSamplers.get(ROOM));
        }

        return samplers;
    }

    protected List<Sampler> getSamplersFor(Tile tile, boolean player, Direction playerDir, int size) {
        Map<String, Sampler> allSamplers = samplersBySize.get(size);

        List<Sampler> samplers = new ArrayList<>();
        addDefaultSamplers(allSamplers, samplers, tile, player, playerDir);

        return samplers;
    }

    private void addDefaultSamplers(Map<String, Sampler> allSamplers, List<Sampler> dest,
                                    Tile tile, boolean player, Direction playerDir) {
        dest.add(allSamplers.get(tile.name()));

        if (player) {
            if (playerDir == null) {
                Sampler sampler = allSamplers.get(NO_DIRECTION);
                if (sampler == null) {
                    sampler = allSamplers.get(Direction.DOWN.name());
                }
                dest.add(sampler);
            } else {
                dest.add(allSamplers.get(playerDir.name()));
            }
        }
    }

    @Override
    public BufferedImage createImage(Board board, int playerX, int playerY, Direction playerDir) {
        int sizeIndex = availableSizes.length - 1;
        int size = availableSizes[sizeIndex];

        if (isImageOnly(size)) {
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

        if (isImageOnly(size)) {
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

    public boolean isImageOnly(int size) {
        Map<String, Sampler> samplers = samplersBySize.get(size);

        if (samplers == null) {
            return false;
        }

        boolean image = true;
        for (Sampler sampler : samplers.values()) {
            if (sampler instanceof AnsiSampler) {
                image = false;
                break;
            }
        }

        return image;
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

    protected static class MaskSampler implements Sampler {

        private final int rgba;
        private final int size;

        public MaskSampler(int rgba, int size) {
            this.rgba = rgba;
            this.size = size;
        }

        @Override
        public void fetch(int x, int y, StyledCharacter out, boolean merge) {
            if (merge) {
                out.mergeRGB(rgba);
            } else {
                out.setRGB(rgba);
            }
        }

        @Override
        public int getSize() {
            return size;
        }
    }

    protected static class AnsiSampler implements Sampler {

        private final AttributedString[] ansi;

        public AnsiSampler(AttributedString[] ansi) {
            this.ansi = ansi;
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
