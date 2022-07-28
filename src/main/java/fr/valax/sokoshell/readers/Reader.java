package fr.valax.sokoshell.readers;

import fr.poulpogaz.json.JsonException;
import fr.valax.sokoshell.solver.Pack;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public interface Reader {

    default Pack read(Path path) throws IOException, JsonException {
        return read(path, true);
    }

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

    Pack read(InputStream is) throws IOException, JsonException;
}
