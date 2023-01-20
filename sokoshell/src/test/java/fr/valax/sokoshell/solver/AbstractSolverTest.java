package fr.valax.sokoshell.solver;

import fr.valax.sokoshell.TestUtils;
import fr.valax.sokoshell.graphics.style.MapStyle;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

public class AbstractSolverTest {

    @Test
    void deadPositionsDetectionTest() {
        Level level = TestUtils.getLevel(Path.of("levels8xv/Aruba10.8xv"), 46 - 1);
        MapStyle style = TestUtils.getStyle(Path.of("isekai/isekai.style"));

        Map map = level.getMap();
        map.removeStateCrates(level.getInitialState());
        //mR.setShowDeadTiles(true);
        style.print(level);
        System.out.println("Computing dead positions...");

        map.computeFloors();
        map.computeDeadTiles();
        style.print(map, level.getPlayerX(), level.getPlayerY());

        final int[] count = {0};

        map.forEachNotWall((t) -> {
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
            Map map = level.getMap();
            State init = level.getInitialState();

            map.computeFloors();
            map.removeStateCrates(init);
            map.computeDeadTiles();
            map.addStateCrates(init);

            System.out.println(solver.checkFreezeDeadlock(map, level.getInitialState()));

        }
    }

    @Test
    void freezeDeadlockTest2() {
        Level level = TestUtils.getLevel(Path.of("levels8xv/Aruba10.8xv"), 46 - 1);
        MapStyle style = TestUtils.getStyle(Path.of("isekai/isekai.style"));
        //mr.setShowDeadTiles(true);

        BasicBruteforceSolver solver = BasicBruteforceSolver.newBFSSolver();

        Map map = level.getMap();
        State init = level.getInitialState();

        map.computeFloors();
        map.removeStateCrates(init);
        map.computeDeadTiles();

        State myState = new State(275, new int[] {91, 122, 184, 182, 181, 180, 198, 178, 108, 176, 177, 199, 146, 147, 148, 215, 237, 127, 221, 230, 231, 232, 233, 234, 216, 236, 238, 239, 240, 241, 242, 243, 244, 268, 269, 270, 271, 272, 253, 251, 254, 202, 280, 260, 261, 281, 282}, null);

        map.addStateCrates(myState);

        style.print(map, myState.playerPos() % map.getWidth(), myState.playerPos() / map.getWidth());
        System.out.println(solver.checkFreezeDeadlock(map, myState));
    }

    @Test
    void freezeDeadlockTest3() {
        Level level = TestUtils.getLevel(Path.of("levels8xv/Original.8xv"), 48 - 1);
        MapStyle style = TestUtils.getStyle(Path.of("isekai/isekai.style"));
        //mr.setShowDeadTiles(true);

        BasicBruteforceSolver solver = BasicBruteforceSolver.newBFSSolver();

        Map map = level.getMap();
        State init = level.getInitialState();

        map.computeFloors();
        map.removeStateCrates(init);
        map.computeDeadTiles();

        State myState = new State(33, new int[] {16, 18, 20, 22, 17, 32, 21, 29, 31, 43, 48, 122, 30, 61, 68, 83, 73, 74, 127, 150, 162, 139, 140, 172, 176, 151, 152, 158, 163, 177, 165, 166, 171, 179}, null);
        map.addStateCrates(myState);

        style.print(map, myState.playerPos() % map.getWidth(), myState.playerPos() / map.getWidth());
        System.out.println(solver.checkFreezeDeadlock(map, myState));
    }
}
