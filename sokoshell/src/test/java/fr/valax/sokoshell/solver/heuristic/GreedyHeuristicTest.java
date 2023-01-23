package fr.valax.sokoshell.solver.heuristic;

import fr.valax.sokoshell.TestUtils;
import fr.valax.sokoshell.graphics.style.BoardStyle;
import fr.valax.sokoshell.solver.Level;
import fr.valax.sokoshell.solver.Pack;
import fr.valax.sokoshell.solver.State;
import fr.valax.sokoshell.solver.board.MutableBoard;
import fr.valax.sokoshell.utils.PerformanceMeasurer;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

public class GreedyHeuristicTest {

    @Test
    void distancesTest() {

        //Pack pack = PackReaders.read(Path.of("../levels/TIPEex.8xv"), false);
        Pack pack = TestUtils.getPack(Path.of("levels8xv/Original.8xv"));
        BoardStyle style = TestUtils.getStyle(Path.of("isekai/isekai.style"));

        Level level = pack.getLevel(89);
        MutableBoard board = new MutableBoard(level.getBoard());
        State s = level.getInitialState();


        board.initForSolver();
        board.addStateCrates(s);
        style.print(board, level.getPlayerX(), level.getPlayerY());


        GreedyHeuristic h;
        PerformanceMeasurer pm = new PerformanceMeasurer();
        int score = 0;

        final int MES_COUNT = 1000;
        h = new GreedyHeuristic(board);
        for (int k = 0; k < MES_COUNT; k++) {
            pm.start("new");
            score = h.compute(level.getInitialState());
            pm.end("new");
        }

        System.out.println("Score: " + score);
        System.out.println(pm);
    }

    private final Runtime rt = Runtime.getRuntime();
    private long prevTotal = 0;
    private long prevFree = 0;

    GreedyHeuristicTest() {
        prevFree = rt.freeMemory();
    }

    private void memUsageStats() {

        long total = rt.totalMemory();
        long free = rt.freeMemory();
        long used = total - free;
        long prevUsed = (prevTotal - prevFree);
        System.out.println(
                        "Total: " + total +
                        ", Used: " + used +
                        ", ∆Used: " + (used - prevUsed) +
                        ", Free: " + free +
                        ", ∆Free: " + (free - prevFree));
        prevTotal = total;
        prevFree = free;
    }
}
