package fr.valax.sokoshell.solver.heuristic;

import fr.poulpogaz.json.JsonException;
import fr.valax.sokoshell.graphics.style.MapRenderer;
import fr.valax.sokoshell.graphics.style.MapStyleReader;
import fr.valax.sokoshell.readers.PackReaders;
import fr.valax.sokoshell.solver.Level;
import fr.valax.sokoshell.solver.Map;
import fr.valax.sokoshell.solver.Pack;
import fr.valax.sokoshell.solver.State;
import fr.valax.sokoshell.utils.PerformanceMeasurer;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GreedyHeuristicTest {

    @Test
    void distancesTest() throws java.io.IOException, JsonException {

        //Pack pack = PackReaders.read(Path.of("../levels/TIPEex.8xv"), false);
        Pack pack = PackReaders.read(Path.of("../levels/levels8xv/Original.8xv"), false);

        Level level = pack.getLevel(89);
        Map map = level.getMap();
        State s = level.getInitialState();

        MapRenderer mR = new MapRenderer();
        mR.setStyle(new MapStyleReader().read(Path.of("../styles/isekai/isekai.style")));

        map.initForSolver();
        map.addStateCrates(s);
        mR.sysPrint(map, level.getPlayerX(), level.getPlayerY());


        GreedyHeuristic h;
        PerformanceMeasurer pm = new PerformanceMeasurer();
        int score = 0;

        final int MES_COUNT = 1000;
        h = new GreedyHeuristic(map);
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
