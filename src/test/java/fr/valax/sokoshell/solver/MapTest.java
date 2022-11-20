package fr.valax.sokoshell.solver;

import fr.poulpogaz.json.JsonException;
import fr.poulpogaz.json.utils.Pair;
import fr.valax.sokoshell.readers.PackReaders;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MapTest {

    @Test
    void topLeftReachablePositionTest() throws JsonException, IOException {
        Pack pack = PackReaders.read(Path.of("levels8xv/Original.8xv"), false);

        Level level = pack.getLevel(0);
        Map map = level.getMap();

        int i = map.topLeftReachablePosition(5, 7, 5, 6);

        assertEquals(3, i % map.getWidth());
        assertEquals(4, i / map.getWidth());
    }

    @Test
    void findTunnelTest() throws JsonException, IOException {
        Set<TTunnel> tunnelsSet = new HashSet<>();
        tunnelsSet.add(new TTunnel(4, 4, 3, 5));
        tunnelsSet.add(new TTunnel(5, 5, 5, 6));
        tunnelsSet.add(new TTunnel(8, 4, 8, 6));
        tunnelsSet.add(new TTunnel(4, 7, 4, 7));
        //tunnelsSet.add(new TTunnel(6, 7, 7, 7));
        tunnelsSet.add(new TTunnel(5, 8, 9, 8));
        //tunnelsSet.add(new TTunnel(10, 7, 10, 7));
        tunnelsSet.add(new TTunnel(11, 8, 11, 8));
        tunnelsSet.add(new TTunnel(6, 7, 13, 7));

        Pack pack = PackReaders.read(Path.of("levels8xv/Original.8xv"), false);

        Level level = pack.getLevel(0);
        Map map = level.getMap();
        map.removeStateCrates(level.getInitialState());
        map.computeFloors();
        map.findTunnels();

        List<Tunnel> tunnels = map.getTunnels();

        for (Tunnel t : tunnels) {
            TileInfo start = t.getStart();
            TileInfo end = t.getEnd();

            TTunnel arr1 = new TTunnel(start.getX(), start.getY(), end.getX(), end.getY());
            TTunnel arr2 = new TTunnel(end.getX(), end.getY(), start.getX(), start.getY());

            if (tunnelsSet.contains(arr1)) {
                tunnelsSet.remove(arr1);
            } else if (tunnelsSet.contains(arr2)) {
                tunnelsSet.remove(arr2);
            } else {
                throw new AssertionFailedError("No tunnel of the form: (%d; %d) -> (%d; %d)"
                        .formatted(start.getX(), start.getY(), end.getX(), end.getY()));
            }
        }

        assertEquals(0, tunnelsSet.size());
    }

    private record TTunnel(int startX, int startY, int endX, int endY) {}
}
