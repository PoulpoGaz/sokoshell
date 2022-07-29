package fr.valax.sokoshell.readers;

import fr.poulpogaz.json.JsonException;
import fr.valax.sokoshell.solver.Pack;
import fr.valax.sokoshell.utils.Utils;

import java.io.IOException;
import java.nio.file.Path;

public class PackReaders {

    public static final Reader ISK_READER = new IsekaiReader();
    public static final Reader SLC_READER = new SLCReader();
    public static final Reader SOK_READER = new SOKReader();

    public static Pack read(Path path) throws IOException, JsonException {
        return read(path, true);
    }

    public static Pack read(Path path, boolean loadSolution) throws IOException, JsonException {
        return switch (Utils.getExtension(path)) {
            case "8xv" -> ISK_READER.read(path, loadSolution);
            case "slc" -> SLC_READER.read(path, loadSolution);
            default -> SOK_READER.read(path, loadSolution);
        };
    }
}
