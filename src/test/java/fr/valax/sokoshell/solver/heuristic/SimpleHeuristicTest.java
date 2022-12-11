package fr.valax.sokoshell.solver.heuristic;

import fr.poulpogaz.json.JsonException;
import fr.valax.sokoshell.graphics.MapRenderer;
import fr.valax.sokoshell.graphics.MapStyleReader;
import fr.valax.sokoshell.readers.PackReaders;
import fr.valax.sokoshell.solver.Level;
import fr.valax.sokoshell.solver.Map;
import fr.valax.sokoshell.solver.Pack;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Arrays;

public class SimpleHeuristicTest {

    @Test
    void distancesTest() throws java.io.IOException, JsonException {

        Pack pack = PackReaders.read(Path.of("TIPEex.8xv"), false);

        Level level = pack.getLevel(0);
        Map map = level.getMap();
        //map.removeStateCrates(level.getInitialState());
        MapRenderer mR = new MapRenderer();
        mR.setStyle(new MapStyleReader().read(Path.of("styles/isekai/isekai.style")));
        //mR.setShowDeadTiles(true);
        //mR.sysPrint(level);

        map.computeFloors();
        map.computeDeadTiles();
        mR.sysPrint(map, level.getPlayerX(), level.getPlayerY());


        SimpleHeuristic h = new SimpleHeuristic(map);
        System.out.println(h);
    }
}
