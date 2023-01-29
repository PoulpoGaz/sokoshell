package fr.valax.sokoshell.solver.pathfinder;

import fr.valax.sokoshell.TestUtils;
import fr.valax.sokoshell.solver.Level;
import fr.valax.sokoshell.solver.board.Board;
import fr.valax.sokoshell.solver.board.Direction;
import fr.valax.sokoshell.solver.board.MutableBoard;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static fr.valax.sokoshell.solver.board.Direction.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

        Board board = new MutableBoard(level);
        CrateAStar aStar = new CrateAStar(board);
        Node end = aStar.findPathAndComputeMoves(board.getAt(1, 1), null, board.getAt(2, 3), board.getAt(4, 4));

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

        Board board = new MutableBoard(level);
        CrateAStar aStar = new CrateAStar(board);
        Node end = aStar.findPathAndComputeMoves(board.getAt(1, 1), null, board.getAt(2, 3), board.getAt(4, 4));

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

        Board board = new MutableBoard(level);
        CrateAStar aStar = new CrateAStar(board);
        Node end = aStar.findPathAndComputeMoves(board.getAt(1, 1), null, board.getAt(16, 2), board.getAt(1, 6));

        // 6997 dijkstra
        // 52 A*
        PathfinderUtils.check(1, 1, 16, 2, 2, 6, 1, 6, end, solution);
    }

    @Test
    void originalAndExtraTest() {
        Level level = TestUtils.getLevel(Path.of("levels8xv/Original.8xv"), 0);

        Board board = new MutableBoard(level);
        CrateAStar aStar = new CrateAStar(board);
        assertTrue(aStar.hasPath(board.getAt(4, 4), null, board.getAt(5, 7), board.getAt(17, 8)));
        assertFalse(aStar.hasPath(board.getAt(4, 4), null, board.getAt(2, 7), board.getAt(17, 8)));
    }
}
