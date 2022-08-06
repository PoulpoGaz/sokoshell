package fr.valax.sokoshell.solver;

import fr.poulpogaz.json.JsonException;
import fr.valax.sokoshell.graphics.MapRenderer;
import fr.valax.sokoshell.graphics.MapStyleReader;
import fr.valax.sokoshell.readers.PackReaders;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

public class AbstractSolverTest {

    @Test
    void deadPositionsDetectionTest() throws IOException, JsonException {
        Pack pack = PackReaders.read(Path.of("levels8xv/Aruba10.8xv"), false);

        Level level = pack.levels().get(46 - 1);
        Map map = level.getMap();
        map.removeStateCrates(level.getInitialState());
        MapRenderer mR = new MapRenderer();
        mR.setStyle(new MapStyleReader().read(Path.of("styles/isekai/isekai.style")));
        mR.setShowDeadTiles(true);
        mR.sysPrint(level);
        System.out.println("Computing dead positions...");

        map.computeDeadTiles();
        mR.sysPrint(map, level.getPlayerX(), level.getPlayerY());

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

    @Test
    void freezeDeadlockTest2() throws Exception {
        Pack pack = PackReaders.read(Path.of("levels8xv/Aruba10.8xv"), false);

        Assertions.assertNotNull(pack);
        Assertions.assertNotNull(pack.levels());
        Assertions.assertNotEquals(0, pack.levels().size());


        BasicBrutalSolver solver = BasicBrutalSolver.newBFSSolver();

        Level level = pack.levels().get(46 - 1);
        Map map = level.getMap();
        State init = level.getInitialState();

        solver.it.setMap(map);
        map.removeStateCrates(init);
        map.computeDeadTiles();

        State myState = new State(275, new int[] {91, 122, 184, 182, 181, 180, 198, 178, 108, 176, 177, 199, 146, 147, 148, 215, 237, 127, 221, 230, 231, 232, 233, 234, 216, 236, 238, 239, 240, 241, 242, 243, 244, 268, 269, 270, 271, 272, 253, 251, 254, 202, 280, 260, 261, 281, 282}, null);

        map.addStateCrates(myState);

        MapRenderer mr = new MapRenderer();
        mr.setStyle(new MapStyleReader().read(Path.of("styles/isekai/isekai.style")));
        mr.setShowDeadTiles(true);
        mr.sysPrint(map, myState.playerPos() % map.getWidth(), myState.playerPos() / map.getHeight());
        System.out.println(solver.checkFreezeDeadlock(map, myState));
    }
}
