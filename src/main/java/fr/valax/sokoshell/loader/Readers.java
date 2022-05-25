package fr.valax.sokoshell.loader;

import fr.valax.sokoshell.solver.Pack;

import java.io.IOException;
import java.nio.file.Path;

public class Readers {

    public static Pack read(Path path) throws IOException {
        Reader reader;
        if (path.getFileName().toString().endsWith(".8xv")) {
            reader = new IsekaiReader();
        } else {
            reader = new SOKReader();
        }

        return reader.read(path);
    }
}
