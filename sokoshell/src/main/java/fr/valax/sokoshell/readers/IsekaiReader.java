package fr.valax.sokoshell.readers;

import fr.valax.sokoshell.solver.Level;
import fr.valax.sokoshell.solver.Pack;
import fr.valax.sokoshell.solver.board.tiles.Tile;
import static fr.valax.sokoshell.solver.board.tiles.Tile.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads a pack from the
 * <a href="https://github.com/PoulpoGaz/Isekai/blob/master/isekai-commons/src/main/java/fr/poulpogaz/isekai/commons/pack/PackIO.java#L38">
 *     Isekai file format
 * </a>
 */
public class IsekaiReader implements Reader {

    // Constants relative to the .8xv format
    // see https://github.com/mateoconlechuga/convbin/blob/master/src/convert.c#L132

    private static final byte CHECKSUM_LEN = 2;
    private static final byte DATA = 0x4a;

    private static final byte[] PACK_MARKER = new byte[] {(byte) 0xFE, (byte) 0xDC, (byte) 0xBA};

    private static final Tile[] INTERNAL_ORDER = new Tile[] {
            FLOOR, WALL, CRATE, CRATE_ON_TARGET, TARGET
    };

    @Override
    public Pack read(InputStream is) throws IOException {
        byte[] fileBytes = is.readAllBytes();

        byte[] data = extract(fileBytes);

        if (data == null) {
            throw new IOException("Not 8xv format");
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(data);

        bais.readNBytes(PACK_MARKER.length);

        // pack info
        String name = readString(bais);
        String author = readString(bais);

        bais.readNBytes(9); // skip theme (8) and valid (1)

        List<Level> levels = readLevels(bais);

        return new Pack(name, author, levels);
    }

    private static String readString(InputStream is) throws IOException {
        StringBuilder builder = new StringBuilder();

        while (true) {
            int next = is.read();

            if (next == -1) {
                throw new IOException("EOF");
            }

            if (next == '\0') {
                return builder.toString();
            }

            builder.append((char) next);
        }
    }

    private static List<Level> readLevels(InputStream is) throws IOException {
        int nLevels = (is.read() & 0xFF) | ((is.read() << 8) & 0xFF00);

        is.readNBytes(nLevels * 2); // skip offsets, they are useless here

        List<Level> levels = new ArrayList<>();
        for (int i = 0; i < nLevels; i++) {
            Level.Builder builder = new Level.Builder();
            builder.setIndex(i);

            builder.setPlayerPos(is.read(), is.read());
            builder.setSize(is.read(), is.read());

            boolean compressed = is.read() == 1;

            if (compressed) {
                fillLevelCompressed(builder, is);
            } else {
                fillLevel(builder, is);
            }

            levels.add(builder.build());
        }

        return levels;
    }

    private static void fillLevel(Level.Builder level, InputStream is) throws IOException {
        int width = level.getWidth();
        int height = level.getHeight();

        Tile[] values = INTERNAL_ORDER;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int i = is.read();

                if (i == -1) {
                    throw new IOException("EOF");
                } else if (i >= values.length) {
                    throw new IOException("Invalid tile index: " + i);
                }

                level.set(values[i], x, y);
            }
        }
    }

    private static void fillLevelCompressed(Level.Builder level, InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // uncompress
        int count = -1;
        int i;
        while ((i = is.read()) != 0xFF) {
            if (i == -1) {
                throw new IOException("EOF");
            }

            if (count > 0) {
                for (int j = 0; j < count; j++) {
                    baos.write(i);
                }

                count = -1;
            } else {
                count = i + 1;
            }
        }

        // setup data
        byte[] data = baos.toByteArray();

        int width = level.getWidth();
        int height = level.getHeight();

        Tile[] values = INTERNAL_ORDER;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                i = y * width + x;

                if (data[i] < 0 || data[i] >= values.length) {
                    continue;
                }

                level.set(values[data[i]], x, y);
            }
        }
    }

    public static byte[] extract(byte[] in) {
        int length = in.length - DATA - CHECKSUM_LEN;

        if (length < 0) {
            return null;
        } else {
            byte[] output = new byte[length];

            System.arraycopy(in, DATA, output, 0, output.length);

            return output;
        }
    }
}
