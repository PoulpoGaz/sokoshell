package fr.valax.sokoshell.solver;

import fr.poulpogaz.json.JsonException;
import fr.valax.sokoshell.readers.PackReaders;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

public class MapTest {

    @Test
    void topLeftReachablePositionTest() throws JsonException, IOException {
        Pack pack = PackReaders.read(Path.of("levels8xv/Original.8xv"), false);

        Level level = pack.getLevel(0);
        Map map = level.getMap();

        int player = level.getPlayerY() * map.getWidth() + level.getPlayerX();
        map.findReachableCases(player, false);

        Assertions.assertEquals(8, map.getTopLeftReachablePositionX());
        Assertions.assertEquals(4, map.getTopLeftReachablePositionY());

        int i = map.topLeftReachablePosition(5, 7, 5, 6);

        Assertions.assertEquals(3, i % map.getWidth());
        Assertions.assertEquals(4, i / map.getWidth());
    }
}
