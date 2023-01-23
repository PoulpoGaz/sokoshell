package fr.valax.sokoshell.solver.heuristic;

import fr.valax.sokoshell.TestUtils;
import fr.valax.sokoshell.graphics.style.BoardStyle;
import fr.valax.sokoshell.solver.Level;
import fr.valax.sokoshell.solver.Pack;
import fr.valax.sokoshell.solver.board.MutableBoard;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Arrays;

public class SimpleHeuristicTest {

    @Test
    void distancesTest() {
        Pack pack = TestUtils.getPack(Path.of("TIPEex.8xv"));
        BoardStyle style = TestUtils.getStyle(Path.of("isekai/isekai.style"));

        Level level = pack.getLevel(0);
        MutableBoard board = new MutableBoard(level.getBoard());

        board.initForSolver();
        style.print(board, level.getPlayerX(), level.getPlayerY());

        for (int y = 0; y < board.getHeight(); y++) {
            for (int x = 0; x < board.getWidth(); x++) {
                System.out.printf("(%d,%d) : %s%n", x, y, Arrays.toString(board.getAt(x, y).getTargets()));
            }
        }

        SimpleHeuristic h = new SimpleHeuristic(board);
        System.out.println(h);
    }
}
