package fr.valax.sokoshell.solver.heuristic;

import fr.valax.sokoshell.TestUtils;
import fr.valax.sokoshell.graphics.style.MapStyle;
import fr.valax.sokoshell.solver.Level;
import fr.valax.sokoshell.solver.Map;
import fr.valax.sokoshell.solver.Pack;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Arrays;

public class SimpleHeuristicTest {

    @Test
    void distancesTest() {
        Pack pack = TestUtils.getPack(Path.of("TIPEex.8xv"));
        MapStyle style = TestUtils.getStyle(Path.of("isekai/isekai.style"));

        Level level = pack.getLevel(0);
        Map map = level.getMap();

        map.initForSolver();
        style.print(map, level.getPlayerX(), level.getPlayerY());

        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                System.out.printf("(%d,%d) : %s%n", x, y, Arrays.toString(map.getAt(x, y).getTargets()));
            }
        }

        SimpleHeuristic h = new SimpleHeuristic(map);
        System.out.println(h);
    }
}
