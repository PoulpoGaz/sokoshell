package fr.valax.sokoshell.loader;

import fr.valax.sokoshell.solver.Pack;

import java.io.IOException;
import java.nio.file.Path;

public class PackReaders {

    public static final Reader ISK_READER = new IsekaiReader();
    public static final Reader SOK_READER = new SOKReader();

    public static Pack read(Path path) throws IOException {
        if (path.getFileName().toString().endsWith(".8xv")) {
            return ISK_READER.read(path);
        } else {
            return SOK_READER.read(path);
        }
    }
}
