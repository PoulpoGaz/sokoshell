package fr.valax.sokoshell;

import fr.valax.sokoshell.graphics.style.MapRenderer;
import fr.valax.sokoshell.graphics.style.MapStyle;
import fr.valax.sokoshell.solver.Direction;
import fr.valax.sokoshell.solver.Level;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class PNGExporterTest {

    @Test
    void test() {
        Level level = TestUtils.getLevel(Path.of("levels8xv/Original.8xv"));
        MapStyle style = TestUtils.getStyle(Path.of("warehouse/warehouse.style"));

        try {
            MapRenderer mr = new MapRenderer();
            mr.setStyle(style);

            ImageIO.write(mr.createImage(16, level.getMap(), level.getPlayerX(), level.getPlayerY(), Direction.DOWN), "png", new File("out.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
