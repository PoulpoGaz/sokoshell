package fr.valax.sokoshell.solver;

import fr.valax.sokoshell.graphics.MapRenderer;
import fr.valax.sokoshell.loader.PackReaders;
import org.junit.jupiter.api.Assertions;
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
        //mR.setStyle(new MapStyle());
        //mR.toggleDeadPositionShow(); // TODO: redo that
        mR.sysPrint(level);
        System.out.println("Computing dead positions...");

        map.computeDeadTiles();
        mR.sysPrint(level);

        final int[] count = {0};

        map.forEach((t) -> {
            if (t.isDeadTile() && !t.isWall()) {
                System.out.printf("Dead position at (%d;%d)%n", t.getX(), t.getY());
                count[0] = count[0] + 1;
            }
        });

        System.out.println(count[0] + " dead positions found.");
    }

    @Test
    void freezeDeadlockTest() throws Exception {
        Pack pack = PackReaders.SOK_READER
                .read(AbstractSolverTest.class.getResourceAsStream("/freezeDeadlockTest.sok"));

        Assertions.assertNotNull(pack);
        Assertions.assertNotNull(pack.levels());
        Assertions.assertNotEquals(0, pack.levels().size());


        BasicBrutalSolver solver = BasicBrutalSolver.newBFSSolver();

        for (Level level : pack.levels()) {
            Map map = level.getMap();
            State init = level.getInitialState();

            solver.it.setMap(map);
            map.removeStateCrates(init);
            map.computeDeadTiles();
            map.addStateCrates(init);

            System.out.println(solver.checkFreezeDeadlock(map, level.getInitialState()));

        }


    }
}
