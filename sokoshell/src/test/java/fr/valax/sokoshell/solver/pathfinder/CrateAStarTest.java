package fr.valax.sokoshell.solver.pathfinder;

import fr.valax.sokoshell.TestUtils;
import fr.valax.sokoshell.solver.Direction;
import fr.valax.sokoshell.solver.Level;
import fr.valax.sokoshell.solver.Map;
import org.junit.jupiter.api.Test;

import static fr.valax.sokoshell.solver.Direction.*;

public class CrateAStarTest {

    @Test
    void simple() {
        Direction[] solution = new Direction[] {
                DOWN, DOWN, DOWN, RIGHT, UP, LEFT, UP, RIGHT, RIGHT, RIGHT,
                UP, RIGHT, DOWN, DOWN, UP, RIGHT, RIGHT, RIGHT, DOWN, DOWN,
                LEFT, LEFT, LEFT
        };

        Level level = TestUtils.getLevel("""
                ##########
                #@       #
                #        #
                # $## ## #
                #  #.    #
                ##########
                """);

        Map map = level.getMap();
        CrateAStar aStar = new CrateAStar(map);
        Node end = aStar.findPath(map.getAt(1, 1), null, map.getAt(2, 3), map.getAt(4, 4));

        // 482 dijkstra
        // 269 A*
        PathfinderUtils.check(1, 1, 2, 3, 5, 4, 4, 4, end, solution);
    }

    @Test
    void multipleCrates() {
        Direction[] solution = new Direction[] {
                DOWN, DOWN, DOWN, RIGHT, UP, LEFT, UP, RIGHT, RIGHT, RIGHT,
                UP, RIGHT, DOWN, DOWN, UP, RIGHT, RIGHT, RIGHT, DOWN, DOWN,
                LEFT, LEFT, LEFT
        };

        Level level = TestUtils.getLevel("""
                ##########
                #@       #
                #        #
                # $$$ $$ #
                #  $.    #
                ##########
                """);

        Map map = level.getMap();
        CrateAStar aStar = new CrateAStar(map);
        Node end = aStar.findPath(map.getAt(1, 1), null, map.getAt(2, 3), map.getAt(4, 4));

        PathfinderUtils.check(1, 1, 2, 3, 5, 4, 4, 4, end, solution);
    }

    @Test
    void simple2() {
        Direction[] solution = new Direction[] {
                RIGHT, RIGHT, RIGHT, RIGHT, RIGHT, RIGHT, RIGHT, RIGHT, RIGHT, RIGHT, RIGHT, RIGHT, RIGHT, RIGHT, RIGHT,
                DOWN, DOWN, DOWN, DOWN, RIGHT, DOWN, LEFT, LEFT, LEFT, LEFT, LEFT, LEFT, LEFT, LEFT, LEFT, LEFT, LEFT,
                LEFT, LEFT, LEFT, LEFT
        };

        Level level = TestUtils.getLevel("""
                ###################
                #@                #
                #               $ #
                #                 #
                #                 #
                #                 #
                #                 #
                ###################
                """);

        Map map = level.getMap();
        CrateAStar aStar = new CrateAStar(map);
        Node end = aStar.findPath(map.getAt(1, 1), null, map.getAt(16, 2), map.getAt(1, 6));

        // 6997 dijkstra
        // 52 A*
        PathfinderUtils.check(1, 1, 16, 2, 2, 6, 1, 6, end, solution);
    }
}
