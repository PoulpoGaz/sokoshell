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

        Level level = pack.getLevel(3);
        Map map = level.getMap();
        State s = level.getInitialState();

        MapRenderer mR = new MapRenderer();
        mR.setStyle(new MapStyleReader().read(Path.of("../styles/isekai/isekai.style")));

        map.initForSolver();
        map.addStateCrates(s);
        //mR.sysPrint(map, level.getPlayerX(), level.getPlayerY());

        /*for (int i : s.cratesIndices()) {
            System.out.println(Arrays.toString(map.getAt(i).getTargets()));
        }*/

        GreedyHeuristic h;
        PerformanceMeasurer pm = new PerformanceMeasurer();
        int scoreNew = 0, scoreOld = 0;
        long start, end;



        h = new GreedyHeuristic(map);
        start = System.currentTimeMillis();
        scoreOld = h.compute(level.getInitialState());
        end = System.currentTimeMillis();
        System.out.printf("old: %d%n", end - start);

        h = new GreedyHeuristic(map);
        start = System.currentTimeMillis();
        scoreNew = h.compute_old(level.getInitialState());
        end = System.currentTimeMillis();
        System.out.printf("new: %d%n", end - start);

        /*final int MES_COUNT = 100;

        h = new GreedyHeuristic(map);
        pm.start("trash");
        score2 = h.compute(level.getInitialState());
        pm.end("trash");

        h = new GreedyHeuristic(map);
        for (int k  =0; k < MES_COUNT; k++) {
            if (k < MES_COUNT / 2) {
                pm.start("old");
                score1 = h.compute_old(level.getInitialState());
                pm.end("old");
                pm.start("new");
                score2 = h.compute(level.getInitialState());
                pm.end("new");
            } else {
                pm.start("new");
                score2 = h.compute(level.getInitialState());
                pm.end("new");
                pm.start("old");
                score1 = h.compute_old(level.getInitialState());
                pm.end("old");
            }
        }*/


        System.out.println(pm);
        System.out.printf("Scores: %d %d%n", scoreNew, scoreOld);
        /*List<GreedyHeuristic.CrateToTarget> ctt = h.getCrateToTargetsList();
        System.out.println(h);*/
        //assertEquals(88, score1);
        assertEquals(scoreNew, scoreOld);
        /*for (int i = 1; i < ctt.size(); i++) {
            assertTrue(ctt.get(i - 1).compareTo(ctt.get(i)) <= 0);
        }*/
        //System.out.println(map.getTargetCount());



        /*System.out.println(pm);
        System.out.printf("Score: %d%n", score);
        ctt = h.getCrateToTargetsList();
        System.out.println(h);
        assertEquals(88, score);*/
        /*for (int i = 1; i < ctt.size(); i++) {
            assertTrue(ctt.get(i - 1).compareTo(ctt.get(i)) <= 0);
        }*/
        //System.out.println(map.getTargetCount());

    }
}
