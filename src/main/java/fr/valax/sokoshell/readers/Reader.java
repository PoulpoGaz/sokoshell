package fr.valax.sokoshell.readers;

import fr.poulpogaz.json.JsonException;
import fr.valax.sokoshell.solver.Pack;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A reader is used to read a pack from a file or an {@link InputStream}
 *
 * @see Pack
 */
public interface Reader {

    /**
     * Reads a {@link Pack} from the specified path. If there is next to the file a solution
     * file then the solution file will be read. A solution file is file with the same name
     * but ends with ".solution.json.gz"
     *
     * @param path location of the file
     * @return the pack read
     * @throws IOException if an I/O error occurs
     * @throws JsonException if there is a problem in the solution file
     */
    default Pack read(Path path) throws IOException, JsonException {
        return read(path, true);
    }

    /**
     * Reads a {@link Pack} from the specified path. If {@code loadSolution} is true
     * and there is next to the file a solution file then the solution file will be read
     * A solution file is file with the same name but ends with ".solution.json.gz"
     *
     * @param path location of the file
     * @param loadSolution load solution file if it exists
     * @return the pack read
     * @throws IOException if an I/O error occurs
     * @throws JsonException if there is a problem in the solution file
     */
    default Pack read(Path path, boolean loadSolution) throws IOException, JsonException {
        try (InputStream is = Files.newInputStream(path)) {
            Pack p = read(is);
            p.setSourcePath(path);

            if (loadSolution) {
                Path solutionPath = Path.of(path + ".solutions.json.gz");
                p.readSolutions(solutionPath);
            }

            return p;
        }
    }

    /**
     * Reads a {@link Pack} from the specified input stream
     *
     * @param is the input stream that contains the pack
     * @return the pack read
     * @throws IOException if an I/O error occurs
     */
    Pack read(InputStream is) throws IOException;
}
