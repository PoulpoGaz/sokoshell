package fr.valax.sokoshell.graphics.style;

import fr.valax.sokoshell.graphics.Graphics;
import fr.valax.sokoshell.graphics.Surface;
import fr.valax.sokoshell.solver.Direction;
import fr.valax.sokoshell.solver.Level;
import fr.valax.sokoshell.TestUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

public class MapStyleReaderTest {

    @Test
    void test() throws IOException {
        Level level = TestUtils.getLevel(Path.of("levels8xv/Original.8xv"));

        MapRenderer mr = new MapRenderer();
        MapStyle style = new MapStyleReader().read(Path.of("../styles/warehouse/warehouse.style"));
        mr.setStyle(style);

        Surface s = new Surface();
        s.resize(level.getWidth(), level.getHeight());
        Graphics g = new Graphics(s);

        for (int i = 1; i <= 16; i++) {
            if (style.isSupported(i)) {
                System.out.println("Size: " + i);
                s.clear();
                s.resize(level.getWidth() * i, level.getHeight() * i);
                mr.draw(g, 0, 0, i, level.getMap(), level.getPlayerX(), level.getPlayerY(), Direction.DOWN);
                s.print();
            }
        }
    }
}
