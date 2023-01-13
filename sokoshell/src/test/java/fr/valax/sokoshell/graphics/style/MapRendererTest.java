package fr.valax.sokoshell.graphics.style;

import fr.valax.sokoshell.graphics.Graphics;
import fr.valax.sokoshell.graphics.Surface;
import fr.valax.sokoshell.graphics.style2.DefaultStyle;
import fr.valax.sokoshell.graphics.style2.MapRenderer;
import fr.valax.sokoshell.solver.Direction;
import fr.valax.sokoshell.solver.Level;
import fr.valax.sokoshell.solver.TestUtils;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

public class MapRendererTest {

    @Test
    void draw() {
        Level level = TestUtils.getLevel(Path.of("../levels/levels8xv/Original.8xv"));

        MapRenderer mr = new MapRenderer();
        mr.setStyle(new DefaultStyle());

        Surface s = new Surface();
        s.resize(level.getWidth(), level.getHeight());

        mr.draw(new Graphics(s), 0, 0, 1, level.getMap(), level.getPlayerX(), level.getPlayerY(), Direction.DOWN);

        s.print();
    }


    @Test
    void print() {
        Level level = TestUtils.getLevel(Path.of("../levels/levels8xv/Original.8xv"));

        MapRenderer mr = new MapRenderer();
        mr.setStyle(new DefaultStyle());

        Surface s = new Surface();
        s.resize(level.getWidth(), level.getHeight());

        mr.print(level);

        s.print();
    }
}
