package fr.valax.sokoshell.solver;

import fr.poulpogaz.json.JsonException;
import fr.valax.sokoshell.readers.PackReaders;
import fr.valax.sokoshell.readers.SOKReader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class TestUtils {

    public static Level getLevel(Path path) {
        return getLevel(path, 0);
    }

    public static Level getLevel(Path path, int index) {
        try {
            Pack p = PackReaders.read(path, false);

            return p.getLevel(index);
        } catch (IOException | JsonException e) {
            throw new RuntimeException(e);
        }
    }

    public static Level getSOKLevel(String str) {
        return getSOKLevel(str, 0);
    }

    public static Level getSOKLevel(String str, int index) {
        SOKReader reader = new SOKReader();
        try {
            Pack p = reader.read(new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8)));

            return p.getLevel(index);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
