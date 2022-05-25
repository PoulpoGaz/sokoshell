package fr.valax.sokoshell.loader;

import fr.valax.sokoshell.solver.Pack;

import java.io.IOException;
import java.nio.file.Path;

public interface Reader {

    Pack read(Path path) throws IOException;
}
