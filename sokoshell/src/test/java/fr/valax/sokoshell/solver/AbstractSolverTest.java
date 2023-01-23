package fr.valax.sokoshell.solver;

import fr.valax.sokoshell.TestUtils;
import fr.valax.sokoshell.graphics.style.BoardStyle;
import fr.valax.sokoshell.solver.board.MutableBoard;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

public class AbstractSolverTest {

    @Test
    void deadPositionsDetectionTest() {
        Level level = TestUtils.getLevel(Path.of("levels8xv/Aruba10.8xv"), 46 - 1);
        BoardStyle style = TestUtils.getStyle(Path.of("isekai/isekai.style"));

        MutableBoard board = new MutableBoard(level.getBoard());
        board.removeStateCrates(level.getInitialState());
        //mR.setShowDeadTiles(true);
        style.print(level);
        System.out.println("Computing dead positions...");

        board.computeFloors();
        board.computeDeadTiles();
        style.print(board, level.getPlayerX(), level.getPlayerY());

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
    void freezeDeadlockTest() {
        Pack pack = TestUtils.getPack("""
                ######
                #@   #
                #    #
                #$  .#
                ######
                                
                ######
                #@   #
                # $$ #
                # $$ #
                #....#
                ######
                                
                #####
                #*. #
                #*$@#
                #*  #
                #####
                """);

        Assertions.assertNotNull(pack);
        Assertions.assertNotNull(pack.levels());
        Assertions.assertNotEquals(0, pack.nLevels());


        BasicBruteforceSolver solver = BasicBruteforceSolver.newBFSSolver();

        for (Level level : pack.levels()) {
            MutableBoard board = new MutableBoard(level.getBoard());
            State init = level.getInitialState();

            board.computeFloors();
            board.removeStateCrates(init);
            board.computeDeadTiles();
            board.addStateCrates(init);

            System.out.println(solver.checkFreezeDeadlock(board, level.getInitialState()));

        }
    }

    @Test
    void freezeDeadlockTest2() {
        Level level = TestUtils.getLevel(Path.of("levels8xv/Aruba10.8xv"), 46 - 1);
        BoardStyle style = TestUtils.getStyle(Path.of("isekai/isekai.style"));
        //mr.setShowDeadTiles(true);

        BasicBruteforceSolver solver = BasicBruteforceSolver.newBFSSolver();

        MutableBoard board = new MutableBoard(level.getBoard());
        State init = level.getInitialState();

        board.computeFloors();
        board.removeStateCrates(init);
        board.computeDeadTiles();

        State myState = new State(275, new int[] {91, 122, 184, 182, 181, 180, 198, 178, 108, 176, 177, 199, 146, 147, 148, 215, 237, 127, 221, 230, 231, 232, 233, 234, 216, 236, 238, 239, 240, 241, 242, 243, 244, 268, 269, 270, 271, 272, 253, 251, 254, 202, 280, 260, 261, 281, 282}, null);

        board.addStateCrates(myState);

        style.print(board, myState.playerPos() % board.getWidth(), myState.playerPos() / board.getWidth());
        System.out.println(solver.checkFreezeDeadlock(board, myState));
    }

    @Test
    void freezeDeadlockTest3() {
        Level level = TestUtils.getLevel(Path.of("levels8xv/Original.8xv"), 48 - 1);
        BoardStyle style = TestUtils.getStyle(Path.of("isekai/isekai.style"));
        //mr.setShowDeadTiles(true);

        BasicBruteforceSolver solver = BasicBruteforceSolver.newBFSSolver();

        MutableBoard board = new MutableBoard(level.getBoard());
        State init = level.getInitialState();

        board.computeFloors();
        board.removeStateCrates(init);
        board.computeDeadTiles();

        State myState = new State(33, new int[] {16, 18, 20, 22, 17, 32, 21, 29, 31, 43, 48, 122, 30, 61, 68, 83, 73, 74, 127, 150, 162, 139, 140, 172, 176, 151, 152, 158, 163, 177, 165, 166, 171, 179}, null);
        board.addStateCrates(myState);

        style.print(board, myState.playerPos() % board.getWidth(), myState.playerPos() / board.getWidth());
        System.out.println(solver.checkFreezeDeadlock(board, myState));
    }
}
