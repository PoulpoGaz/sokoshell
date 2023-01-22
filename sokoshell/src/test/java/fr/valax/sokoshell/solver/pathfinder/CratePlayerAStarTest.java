package fr.valax.sokoshell.solver.pathfinder;

import fr.valax.sokoshell.TestUtils;
import fr.valax.sokoshell.solver.Board;
import fr.valax.sokoshell.solver.Direction;
import fr.valax.sokoshell.solver.Level;
import fr.valax.sokoshell.solver.Board;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static fr.valax.sokoshell.solver.Direction.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

        Board board = level.getMap();
        CratePlayerAStar aStar = new CratePlayerAStar(board);
        Node end = aStar.findPathAndComputeMoves(board.getAt(1, 1), board.getAt(1, 1), board.getAt(2, 3), board.getAt(4, 4));

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

        Board board = level.getMap();
        CratePlayerAStar aStar = new CratePlayerAStar(board);
        Node end = aStar.findPathAndComputeMoves(board.getAt(1, 1), board.getAt(17, 1), board.getAt(16, 2), board.getAt(1, 6));

        // 9998 dijkstra
        // 133 A*
        PathfinderUtils.check(1, 1, 16, 2, 17, 1, 1, 6, end, solution);
    }

    @Test
    void originalAndExtra() {
        Level level = TestUtils.getLevel(Path.of("levels8xv/Original.8xv"), 0);
        Board board = level.getMap();
        CratePlayerAStar aStar = new CratePlayerAStar(board);

        assertTrue(aStar.hasPath(board.getAt(4, 4), board.getAt(4, 4), board.getAt(5, 7), board.getAt(17, 8)));
        assertFalse(aStar.hasPath(board.getAt(4, 4), board.getAt(4, 4), board.getAt(2, 7), board.getAt(17, 8)));
        assertTrue(aStar.hasPath(board.getAt(4, 4), board.getAt(5, 5), board.getAt(5, 7), board.getAt(17, 8)));
        assertFalse(aStar.hasPath(board.getAt(4, 4), board.getAt(5, 3), board.getAt(5, 7), board.getAt(17, 8)));
    }
}
