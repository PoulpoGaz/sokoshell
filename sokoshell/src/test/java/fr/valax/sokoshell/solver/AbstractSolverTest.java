package fr.valax.sokoshell.solver;

import fr.poulpogaz.json.JsonException;
import fr.valax.sokoshell.graphics.style.MapRenderer;
import fr.valax.sokoshell.graphics.style.MapStyleReader;
import fr.valax.sokoshell.readers.PackReaders;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

public class AbstractSolverTest {

    @Test
    void deadPositionsDetectionTest() throws IOException, JsonException {
        Pack pack = PackReaders.read(Path.of("../levels/levels8xv/Aruba10.8xv"), false);

        Level level = pack.getLevel(46 - 1);
        Board board = level.getMap();
        board.removeStateCrates(level.getInitialState());
        MapRenderer mR = new MapRenderer();
        mR.setStyle(new MapStyleReader().read(Path.of("../styles/isekai/isekai.style")));
        mR.setShowDeadTiles(true);
        mR.sysPrint(level);
        System.out.println("Computing dead positions...");

        board.computeFloors();
        board.computeDeadTiles();
        mR.sysPrint(board, level.getPlayerX(), level.getPlayerY());

        final int[] count = {0};

        board.forEachNotWall((t) -> {
            if (t.isDeadTile()) {
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
        Assertions.assertNotEquals(0, pack.nLevels());


        BasicBruteforceSolver solver = BasicBruteforceSolver.newBFSSolver();

        for (Level level : pack.levels()) {
            Board board = level.getMap();
            State init = level.getInitialState();

            board.computeFloors();
            board.removeStateCrates(init);
            board.computeDeadTiles();
            board.addStateCrates(init);

            System.out.println(solver.checkFreezeDeadlock(board, level.getInitialState()));

        }
    }

    @Test
    void freezeDeadlockTest2() throws Exception {
        Pack pack = PackReaders.read(Path.of("../levels/levels8xv/Aruba10.8xv"), false);

        Assertions.assertNotNull(pack);
        Assertions.assertNotNull(pack.levels());
        Assertions.assertNotEquals(0, pack.nLevels());

        MapRenderer mr = new MapRenderer();
        mr.setStyle(new MapStyleReader().read(Path.of("../styles/isekai/isekai.style")));
        mr.setShowDeadTiles(true);

        BasicBruteforceSolver solver = BasicBruteforceSolver.newBFSSolver();

        Level level = pack.getLevel(46 - 1);
        Board board = level.getMap();
        State init = level.getInitialState();

        board.computeFloors();
        board.removeStateCrates(init);
        board.computeDeadTiles();

        State myState = new State(275, new int[] {91, 122, 184, 182, 181, 180, 198, 178, 108, 176, 177, 199, 146, 147, 148, 215, 237, 127, 221, 230, 231, 232, 233, 234, 216, 236, 238, 239, 240, 241, 242, 243, 244, 268, 269, 270, 271, 272, 253, 251, 254, 202, 280, 260, 261, 281, 282}, null);

        board.addStateCrates(myState);

        mr.sysPrint(board, myState.playerPos() % board.getWidth(), myState.playerPos() / board.getWidth());
        System.out.println(solver.checkFreezeDeadlock(board, myState));
    }

    @Test
    void freezeDeadlockTest3() throws JsonException, IOException {
        Pack pack = PackReaders.read(Path.of("../levels/levels8xv/Original.8xv"), false);

        Assertions.assertNotNull(pack);
        Assertions.assertNotNull(pack.levels());
        Assertions.assertNotEquals(0, pack.nLevels());

        MapRenderer mr = new MapRenderer();
        mr.setStyle(new MapStyleReader().read(Path.of("../styles/isekai/isekai.style")));
        mr.setShowDeadTiles(true);

        BasicBruteforceSolver solver = BasicBruteforceSolver.newBFSSolver();

        Level level = pack.getLevel(48 - 1);
        Board board = level.getMap();
        State init = level.getInitialState();

        board.computeFloors();
        board.removeStateCrates(init);
        board.computeDeadTiles();

        State myState = new State(33, new int[] {16, 18, 20, 22, 17, 32, 21, 29, 31, 43, 48, 122, 30, 61, 68, 83, 73, 74, 127, 150, 162, 139, 140, 172, 176, 151, 152, 158, 163, 177, 165, 166, 171, 179}, null);
        board.addStateCrates(myState);

        mr.sysPrint(board, myState.playerPos() % board.getWidth(), myState.playerPos() / board.getWidth());
        System.out.println(solver.checkFreezeDeadlock(board, myState));
    }
}
