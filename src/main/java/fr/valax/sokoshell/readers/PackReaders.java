package fr.valax.sokoshell.readers;

import fr.poulpogaz.json.JsonException;
import fr.valax.sokoshell.solver.Pack;

import java.io.IOException;
import java.nio.file.Path;

public class PackReaders {

    public static final Reader ISK_READER = new IsekaiReader();
    public static final Reader SOK_READER = new SOKReader();

    public static Pack read(Path path) throws IOException, JsonException {
        return read(path, true);
    }

    public static Pack read(Path path, boolean loadSolution) throws IOException, JsonException {
        if (path.getFileName().toString().endsWith(".8xv")) {
            return ISK_READER.read(path, loadSolution);
        } else {
            return SOK_READER.read(path, loadSolution);
        }
    }
}
