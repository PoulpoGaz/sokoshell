package fr.valax.sokoshell.graphics;

import fr.valax.sokoshell.solver.Direction;
import fr.valax.sokoshell.solver.Tile;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.jline.utils.AttributedStyle.*;

public class MapStyle {

    private final Map<Tile, AttributedString> styles = new HashMap<>();
    private final Map<Tile, AttributedString> player = new HashMap<>();

    /**
     * Floor, then wall, crate, crate on target, target, player left, right, down, up
     * Each "lines" corresponds to a size
     */
    private BufferedImage tileset;
    private int maxSize;
    private int[] sizes;

    public MapStyle() {
        try {
            load(Path.of("src/main/resources/mapstyle.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setDefault() {
        styles.put(Tile.FLOOR, new AttributedString(" ", DEFAULT.background(GREEN)));
        styles.put(Tile.WALL, new AttributedString(" ", DEFAULT.background(100, 100, 100)));
        styles.put(Tile.TARGET, new AttributedString(" ", DEFAULT.background(RED)));
        styles.put(Tile.CRATE, new AttributedString(" ", DEFAULT.background(96, 61, 23))); // brown
        styles.put(Tile.CRATE_ON_TARGET, new AttributedString(" ", DEFAULT.background(YELLOW)));

        player.put(Tile.FLOOR, new AttributedString("o", DEFAULT.background(GREEN)));
        player.put(Tile.TARGET, new AttributedString("o", DEFAULT.background(RED)));

        tileset = null;
        sizes = null;
    }

    public AttributedString getStyle(Tile tile, boolean player) {
        if (player) {
            return this.player.get(tile);
        } else {
            return styles.get(tile);
        }
    }

    public boolean hasTileset() {
        return tileset != null;
    }

    public int[] availableSizes() {
        if (sizes == null) {
            return null;
        } else {
            return Arrays.copyOf(sizes, sizes.length);
        }
    }

    public boolean hasSize(int size) {
        for (int s : sizes) {
            if (s == size) {
                return true;
            }
        }

        return false;
    }

    public int findBestSize(int size) {
        int i = findBestSizeIndex(size);

        if (i < 0) {
            return -1;
        } else {
            return sizes[i];
        }
    }

    public void load(Path path) throws IOException {
        boolean loaded = false;

        try {
            setDefault();

            InputStream is = Files.newInputStream(path);

            Properties properties = new Properties();
            properties.load(is);

            is.close();

            sizes = parseSizes(properties.getProperty("sizes"));

            if (sizes.length > 0) {
                String tileset = properties.getProperty("tileset");

                if (tileset == null) {
                    throw new IOException("null tileset");
                }

                Path p = Path.of(tileset);

                if (!p.isAbsolute()) {
                    p = path.getParent().resolve(p);
                }

                this.tileset = ImageIO.read(p.toFile());

                maxSize = sizes[0];
                for (int i = 1; i < sizes.length; i++) {
                    maxSize = Math.max(maxSize, sizes[i]);
                }
            }

            set(styles, Tile.FLOOR, properties.getProperty("floor"));
            set(styles, Tile.WALL, properties.getProperty("wall"));
            set(styles, Tile.TARGET, properties.getProperty("target"));
            set(styles, Tile.CRATE, properties.getProperty("crate"));
            set(styles, Tile.CRATE_ON_TARGET, properties.getProperty("crate_on_target"));
            set(player, Tile.FLOOR, properties.getProperty("player"));
            set(player, Tile.TARGET, properties.getProperty("player_on_target"));

            loaded = true;
        } finally {
            if (!loaded) {
                setDefault();
            }
        }
    }

    private int[] parseSizes(String property) throws IOException {
        if (property == null) {
            return new int[0];
        }

        String[] p =property.split(" ?, ?");

        if (p.length == 0) {
            throw new IOException("Bad sizes");
        }

        int[] sizes = new int[p.length];

        for (int i = 0; i < p.length; i++) {
            String str = p[i];

            try {
                sizes[i] = Integer.parseInt(str);
            } catch (NumberFormatException e) {
                throw new IOException(e);
            }

            if (sizes[i] < 0) {
                throw new IOException("negative size");
            }
        }

        return sizes;
    }

    private void set(Map<Tile, AttributedString> map, Tile tile, String str) throws IOException {
        if (str != null) {
            AttributedString attrStr = AttributedString.fromAnsi(str);

            if (attrStr.length() != 1) {
                throw new IOException("Invalid character");
            }

            map.put(tile, attrStr);
        }
    }

    public void draw(AttributedStringBuilder builder, Tile tile, boolean player) {
        builder.append(getStyle(tile, player));
    }

    public void draw(Graphics g, int x, int y, int size,
                     Tile tile, Direction playerDir) {
        if (tile == null) {
            return;
        }

        int bestSize = findBestSizeIndex(size);

        if (bestSize < 0) {

            AttributedString str = getStyle(tile, playerDir != null);

            for (int y2 = y; y2 < y + size; y2++) {
                for (int x2 = x; x2 < x + size; x2++) {
                    g.getSurface().set(str, x2, y2);
                }
            }
        } else {

            int tileIndex = getTileIndex(tile);
            int playerIndex = getPlayerIndex(playerDir);

            BufferedImage tileImage = getImage(tileIndex, bestSize, size);
            g.drawImage(tileImage, x, y);

            if (playerIndex >= 0) {
                BufferedImage player = getImage(playerIndex, bestSize, size);
                g.drawImage(player, x, y);
            }
        }
    }

    /**
     * Returns the index of the best size.
     * It returns the index of the value closest to size and lower than size
     */
    private int findBestSizeIndex(int size) {
        if (sizes == null) {
            return -1;
        }

        int bestI = -1;

        for (int i = 0; i < sizes.length; i++) {

            if (sizes[i] == size) {
                return i;
            } else if (sizes[i] < size) {
                if (bestI < 0) {
                    bestI = i;
                } else if (sizes[i] > sizes[bestI]) {
                    bestI = i;
                }
            }

        }

        return bestI;
    }

    private int getTileIndex(Tile tile) {
        return switch (tile) {
            case FLOOR -> 0;
            case WALL -> 1;
            case CRATE -> 2;
            case CRATE_ON_TARGET -> 3;
            case TARGET -> 4;
        };
    }

    private int getPlayerIndex(Direction playerDir) {
        if (playerDir == null) {
            return -1;
        } else {
            return switch (playerDir) {
                case LEFT -> 5;
                case RIGHT -> 6;
                case DOWN -> 7;
                case UP -> 8;
            };
        }
    }

    private BufferedImage getImage(int index, int sizeIndex, int resized) {
        int size = sizes[sizeIndex];

        int y = 0;
        for (int i = 0; i < sizeIndex; i++) {
            y += sizes[i];
        }


        BufferedImage image = tileset.getSubimage(
                maxSize * index, y,
                size, size);

        if (size != resized) {
            BufferedImage scaled = new BufferedImage(resized, resized, BufferedImage.TYPE_INT_ARGB);

            Graphics2D g2D = scaled.createGraphics();
            g2D.drawImage(image.getScaledInstance(resized, resized, BufferedImage.SCALE_DEFAULT), 0, 0, null);

            g2D.dispose();

            return scaled;
        } else {
            return image;
        }
    }
}
