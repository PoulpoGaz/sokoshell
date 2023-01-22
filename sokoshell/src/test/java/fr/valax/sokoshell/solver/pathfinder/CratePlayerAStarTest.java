package fr.valax.sokoshell.solver.pathfinder;

import fr.valax.sokoshell.TestUtils;
import fr.valax.sokoshell.solver.Direction;
import fr.valax.sokoshell.solver.Level;
import fr.valax.sokoshell.solver.Map;
import org.junit.jupiter.api.Test;

import static fr.valax.sokoshell.solver.Direction.*;
import static fr.valax.sokoshell.solver.Direction.LEFT;

public class CratePlayerAStarTest {

    @Test
    void simple() {
        Direction[] solution = new Direction[] {
                DOWN, DOWN, DOWN, RIGHT, UP, LEFT, UP, RIGHT, RIGHT, RIGHT, // 9
                UP, RIGHT, DOWN, DOWN, UP, RIGHT, RIGHT, RIGHT, DOWN, DOWN, // 19
                LEFT, LEFT, LEFT, // 22
                UP, UP, LEFT, LEFT, UP, LEFT, LEFT
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
        CratePlayerAStar aStar = new CratePlayerAStar(map);
        Node end = aStar.findPath(map.getAt(1, 1), map.getAt(1, 1), map.getAt(2, 3), map.getAt(4, 4));

        // 516 dijkstra
        // 336 A*
        PathfinderUtils.check(1, 1, 2, 3, 1, 1, 4, 4, end, solution);
    }

    @Test
    void simple2() {
        Direction[] solution = new Direction[] {
                RIGHT, RIGHT, RIGHT, RIGHT, RIGHT, RIGHT, RIGHT, RIGHT, RIGHT, RIGHT, RIGHT, RIGHT, RIGHT, RIGHT, RIGHT,
                DOWN, DOWN, DOWN, DOWN, RIGHT, DOWN, LEFT, LEFT, LEFT, LEFT, LEFT, LEFT, LEFT, LEFT, LEFT, LEFT, LEFT,
                LEFT, LEFT, LEFT, LEFT, // 35
                UP, UP, UP, UP, UP, RIGHT, RIGHT, RIGHT, RIGHT, RIGHT, RIGHT, RIGHT, RIGHT, RIGHT, RIGHT,
                RIGHT, RIGHT, RIGHT, RIGHT, RIGHT
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
        CratePlayerAStar aStar = new CratePlayerAStar(map);
        Node end = aStar.findPath(map.getAt(1, 1), map.getAt(17, 1), map.getAt(16, 2), map.getAt(1, 6));

        // 9998 dijkstra
        // 133 A*
        PathfinderUtils.check(1, 1, 16, 2, 17, 1, 1, 6, end, solution);
    }
}
