package fr.valax.sokoshell.solver.heuristic;

import fr.poulpogaz.json.JsonException;
import fr.valax.sokoshell.graphics.style.MapRenderer;
import fr.valax.sokoshell.graphics.style.MapStyleReader;
import fr.valax.sokoshell.readers.PackReaders;
import fr.valax.sokoshell.solver.Level;
import fr.valax.sokoshell.solver.Map;
import fr.valax.sokoshell.solver.Pack;
import fr.valax.sokoshell.solver.State;
import org.junit.jupiter.api.Test;

import javax.swing.text.html.HTMLDocument;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GreedyHeuristicTest {

    @Test
    void distancesTest() throws java.io.IOException, JsonException {

        //Pack pack = PackReaders.read(Path.of("../levels/TIPEex.8xv"), false);
        Pack pack = PackReaders.read(Path.of("../levels/levels8xv/Original.8xv"), false);

        Level level = pack.getLevel(5);
        Map map = level.getMap();
        State s = level.getInitialState();

        MapRenderer mR = new MapRenderer();
        mR.setStyle(new MapStyleReader().read(Path.of("../styles/isekai/isekai.style")));

        map.initForSolver();
        map.addStateCrates(s);
        mR.sysPrint(map, level.getPlayerX(), level.getPlayerY());

        /*for (int i : s.cratesIndices()) {
            System.out.println(Arrays.toString(map.getAt(i).getTargets()));
        }*/

        GreedyHeuristic h = new GreedyHeuristic(map);
        h.compute(level.getInitialState());
        List<GreedyHeuristic.CrateToTarget> ctt = h.getCrateToTargets();
        assertEquals(map.getTargetCount() * map.getTargetCount(), ctt.size());
        for (int i = 1; i < ctt.size(); i++) {
            assertTrue(ctt.get(i - 1).compareTo(ctt.get(i)) <= 0);
        }
        System.out.println(h);
    }
}
