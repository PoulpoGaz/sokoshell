package fr.valax.sokoshell.graphics;

import fr.valax.sokoshell.loader.PackReaders;
import fr.valax.sokoshell.solver.Level;
import fr.valax.sokoshell.solver.Pack;
import fr.valax.sokoshell.solver.Tile;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

public class MapStyleReaderTest {

    @Test
    void test() throws IOException {
        MapStyleReader reader = new MapStyleReader();
        MapStyle style = reader.read(Path.of("styles/default/style"));

        MapRenderer renderer = new MapRenderer();
        renderer.setStyle(style);

        Pack pack = PackReaders.read(Path.of("levels/Original.8xv"));
        Level level = pack.levels().get(0);

        loop:
        for (int y = 0; y < level.getHeight(); y++) {
            for (int x = 0; x < level.getWidth(); x++) {
                if (level.getMap().getAt(x, y).isTarget()) {
                    level.getMap().setAt(x, y, Tile.CRATE_ON_TARGET);
                    break loop;
                }
            }
        }

        Surface surface = new Surface();
        Graphics g = new Graphics(surface);

        for (int size : style.availableSizes()) {
            if (size > 8) {
                continue;
            }

            surface.resize(level.getWidth() * size, level.getHeight() * size);

            renderer.draw(g, 0, 0, size, level.getMap(), level.getPlayerX(), level.getPlayerY(), null);
            surface.drawBuffer();
        }
    }
}
