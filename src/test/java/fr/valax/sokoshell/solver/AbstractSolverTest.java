package fr.valax.sokoshell.solver;

import fr.valax.sokoshell.graphics.MapRenderer;
import fr.valax.sokoshell.graphics.MapStyle;
import fr.valax.sokoshell.loader.PackReaders;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

public class AbstractSolverTest {

    @Test
    void test() {
        Pack pack;
        try {
            pack = PackReaders.read(Path.of("levels/Microba0.8xv")); // "levels/Original.8xv"
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to read pack");
            return;
        }

        Level level = pack.levels().get(0);
        Map map = level.getMap();
        map.removeStateCrates(level.getInitialState());
        MapRenderer mR = new MapRenderer();
        mR.setStyle(new MapStyle());
        mR.sysPrint(level);
        System.out.println("Computing dead locks...");
        BasicBrutalSolver solver = BasicBrutalSolver.newBFSSolver();
        boolean[][] dp = solver.computeDeadPositions(level.getMap());
        int count = 0;
        for (int y = 0; y < dp.length; y++) {
            for (int x = 0; x < dp[y].length; x++) {
                if (dp[y][x] && map.getAt(x, y) != Tile.WALL) {
                    System.out.printf("Dead lock at (%d;%d)%n", x, y);
                    count++;
                }
            }
        }
        System.out.println(count + " dead positions found.");

    }
}
