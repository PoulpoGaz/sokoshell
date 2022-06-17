package fr.valax.sokoshell.solver;

import fr.valax.sokoshell.graphics.MapRenderer;
import fr.valax.sokoshell.graphics.MapStyle;
import fr.valax.sokoshell.loader.PackReaders;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

public class AbstractSolverTest {

    @Test
    void deadPositionsDetectionTest() {
        Pack pack;
        try {
            pack = PackReaders.read(Path.of("levels/Original.8xv"));
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
        mR.toggleDeadPositionShow();
        mR.sysPrint(level);
        System.out.println("Computing dead positions...");
        BasicBrutalSolver solver = BasicBrutalSolver.newBFSSolver();
        boolean[][] dp = solver.computeDeadPositions(level.getMap());
        mR.sysPrint(level);

        int count = 0;
        for (int y = 0; y < dp.length; y++) {
            for (int x = 0; x < dp[y].length; x++) {
                if (dp[y][x] && map.getAt(x, y) != Tile.WALL) {
                    System.out.printf("Dead position at (%d;%d)%n", x, y);
                    count++;
                }
            }
        }
        System.out.println(count + " dead positions found.");

    }
}
