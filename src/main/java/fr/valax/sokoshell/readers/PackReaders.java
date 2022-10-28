package fr.valax.sokoshell.readers;

import fr.poulpogaz.json.JsonException;
import fr.valax.sokoshell.solver.Pack;
import fr.valax.sokoshell.utils.Utils;

import java.io.IOException;
import java.nio.file.Path;

/**
 * A utility class to read a pack from a file. It automatically chooses the appropriate {@link Reader}
 */
public class PackReaders {

    public static final Reader ISK_READER = new IsekaiReader();
    public static final Reader SLC_READER = new SLCReader();
    public static final Reader SOK_READER = new SOKReader();

    /**
     * Reads a {@link Pack} from the specified path. If the extension of the path is .8xv, it will
     * use the {@link IsekaiReader}, if it's .slc, the {@link SLCReader} will be used, otherwise
     * it's the {@link SOKReader}.
     * If there is next to the file a solution
     * file then the solution file will be read. A solution file is file with the same name
     * but ends with ".solution.json.gz"
     *
     * @param path location of the file
     * @return the pack read
     * @throws IOException if an I/O error occurs
     * @throws JsonException if there is a problem in the solution file
     */
    public static Pack read(Path path) throws IOException, JsonException {
        return read(path, true);
    }

    /**
     * Reads a {@link Pack} from the specified path. If the extension of the path is .8xv, it will
     * use the {@link IsekaiReader}, if it's .slc, the {@link SLCReader} will be used, otherwise
     * it's the {@link SOKReader}.
     * If {@code loadSolution} is true and there is next to the file a solution file then the
     * solution file will be read. A solution file is file with the same name but ends with ".solution.json.gz"
     *
     * @param path location of the file
     * @param loadSolution load solution file if it exists
     * @return the pack read
     * @throws IOException if an I/O error occurs
     * @throws JsonException if there is a problem in the solution file
     */
    public static Pack read(Path path, boolean loadSolution) throws IOException, JsonException {
        return switch (Utils.getExtension(path)) {
            case "8xv" -> ISK_READER.read(path, loadSolution);
            case "slc" -> SLC_READER.read(path, loadSolution);
            default -> SOK_READER.read(path, loadSolution);
        };
    }
}
