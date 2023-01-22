package fr.valax.sokoshell;

import fr.poulpogaz.json.JsonException;
import fr.valax.sokoshell.graphics.style.BoardStyle;
import fr.valax.sokoshell.graphics.style.BoardStyleReader;
import fr.valax.sokoshell.readers.PackReaders;
import fr.valax.sokoshell.readers.XSBReader;
import fr.valax.sokoshell.solver.Level;
import fr.valax.sokoshell.solver.Pack;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Load and cache packs and styles
 */
public class TestUtils {

    private static final XSBReader xsbReader = new XSBReader();
    private static final BoardStyleReader boardStyleReader = new BoardStyleReader();

    private static final Map<String, Pack> CACHE = new HashMap<>();
    private static final Map<String, BoardStyle> STYLE_CACHE = new HashMap<>();

    public static Pack getPack(Path path) {
        String key = "path:" + path.toString();
        Pack pack = CACHE.get(key);

        if (pack == null) {
            try {
                pack = PackReaders.read(Path.of("../levels/").resolve(path), false);
            } catch (IOException | JsonException e) {
                throw new RuntimeException(e);
            }

            CACHE.put(key, pack);
        }

        return pack;
    }

    public static Level getLevel(Path path, int index) {
        return getPack(path).getLevel(index);
    }

    public static Level getLevel(Path path) {
        return getLevel(path, 0);
    }



    public static Pack getPack(String xsb) {
        String key = "xsb:" + xsb;
        Pack pack = CACHE.get(key);

        if (pack == null) {
            try {
                pack = xsbReader.read(new ByteArrayInputStream(xsb.getBytes(StandardCharsets.UTF_8)));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            CACHE.put(key, pack);
        }

        return pack;
    }

    public static Level getLevel(String xsb, int index) {
        return getPack(xsb).getLevel(index);
    }

    public static Level getLevel(String xsb) {
        return getLevel(xsb, 0);
    }


    public static BoardStyle getStyle(Path path) {
        BoardStyle s = STYLE_CACHE.get(path.toString());

        if (s == null) {
            try {
                s = boardStyleReader.read(Path.of("../styles/").resolve(path));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            STYLE_CACHE.put(path.toString(), s);
        }

        return s;
    }
}
