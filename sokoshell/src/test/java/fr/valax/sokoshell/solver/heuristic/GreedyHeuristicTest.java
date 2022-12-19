package fr.valax.sokoshell.solver.heuristic;

import fr.poulpogaz.json.JsonException;
import fr.valax.sokoshell.graphics.style.MapRenderer;
import fr.valax.sokoshell.graphics.style.MapStyleReader;
import fr.valax.sokoshell.readers.PackReaders;
import fr.valax.sokoshell.solver.Level;
import fr.valax.sokoshell.solver.Map;
import fr.valax.sokoshell.solver.Pack;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

public class GreedyHeuristicTest {

    @Test
    void distancesTest() throws java.io.IOException, JsonException {

        Pack pack = PackReaders.read(Path.of("../levels/TIPEex.8xv"), false);

        Level level = pack.getLevel(0);
        Map map = level.getMap();

        MapRenderer mR = new MapRenderer();
        mR.setStyle(new MapStyleReader().read(Path.of("../styles/isekai/isekai.style")));

        map.computeFloors();
        map.computeDeadTiles();
        //mR.sysPrint(map, level.getPlayerX(), level.getPlayerY());

        GreedyHeuristic h = new GreedyHeuristic(map);
        System.out.println(h);
    }
}
