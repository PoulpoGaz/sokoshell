package fr.valax.sokoshell.solver;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class PathfinderTest {

    private static final Pathfinder PATHFINDER = new Pathfinder();

    @Test
    void playerTest() {
        Level level = TestUtils.getLevel(Path.of("../levels/levels8xv/Original.8xv"), 0);

        Map map = level.getMap();

        boolean path = PATHFINDER.getPlayerAStar()
                .hasPath(map.getAt(11, 8), map.getAt(17, 8), null, null);
        assertTrue(path);

        path = PATHFINDER.getPlayerAStar()
                .hasPath(map.getAt(11, 8), map.getAt(5, 2), null, null);
        assertFalse(path);
    }

    @Test
    void playerTest2() {
        String levelStr = """
                ######
                #   @#
                ######
                """;

        Level level = TestUtils.getSOKLevel(levelStr);
        Map map = level.getMap();
        TileInfo start = map.getAt(0, 0);

        assertThrows(IllegalArgumentException.class, () -> PATHFINDER.hasPath(start, start, null, null));
        assertThrows(IllegalArgumentException.class, () -> PATHFINDER.hasPath(start, null, null, null));
        assertThrows(NullPointerException.class, () -> PATHFINDER.hasPath(null, start, null, null));
        assertThrows(NullPointerException.class, () -> PATHFINDER.hasPath(null, null, null, null));
    }

    @Test
    void crateTest() {
        Level level = TestUtils.getLevel(Path.of("../levels/levels8xv/Original.8xv"), 0);

        Map map = level.getMap();

        boolean path = PATHFINDER.getCrateAStar()
                .hasPath(map.getAt(4, 4), null, map.getAt(5, 7), map.getAt(17, 8));
        assertTrue(path);

        path = PATHFINDER.getCrateAStar()
                .hasPath(map.getAt(4, 4), null, map.getAt(2, 7), map.getAt(17, 8));
        assertFalse(path);
    }

    @Test
    void crateTest2() {
        String levelStr = """
                ######
                #$  @#
                ######
                """;

        Level level = TestUtils.getSOKLevel(levelStr);
        Map map = level.getMap();

        assertThrows(IllegalArgumentException.class, () -> {
            PATHFINDER.hasPath(map.getAt(2, 1), null, map.getAt(1, 1), map.getAt(0, 0));
        });

        assertThrows(IllegalArgumentException.class, () -> {
            PATHFINDER.hasPath(map.getAt(2, 1), null, map.getAt(0, 0), map.getAt(1, 1));
        });

        assertThrows(IllegalArgumentException.class, () -> {
            PATHFINDER.hasPath(map.getAt(2, 1), null, map.getAt(2, 1), map.getAt(2, 1));
        });

        assertThrows(NullPointerException.class, () -> {
            PATHFINDER.hasPath(map.getAt(2, 1), null, map.getAt(2, 1), null);
        });

        assertThrows(NullPointerException.class, () -> {
            PATHFINDER.hasPath(map.getAt(2, 1), null, null, map.getAt(1, 1));
        });
    }

    @Test
    void fullTest() {
        Level level = TestUtils.getLevel(Path.of("../levels/levels8xv/Original.8xv"), 0);

        Map map = level.getMap();

        boolean path = PATHFINDER.getCratePlayerAStar()
                .hasPath(map.getAt(4, 4), map.getAt(4, 4), map.getAt(5, 7), map.getAt(17, 8));
        assertTrue(path);

        path = PATHFINDER.getCratePlayerAStar()
                .hasPath(map.getAt(4, 4), map.getAt(4, 4), map.getAt(2, 7), map.getAt(17, 8));
        assertFalse(path);

        path = PATHFINDER.getCratePlayerAStar()
                .hasPath(map.getAt(4, 4), map.getAt(5, 5), map.getAt(5, 7), map.getAt(17, 8));
        assertTrue(path);

        path = PATHFINDER.getCratePlayerAStar()
                .hasPath(map.getAt(4, 4), map.getAt(5, 3), map.getAt(5, 7), map.getAt(17, 8));
        assertFalse(path);
    }

    @Test
    void fullTest2() {
        String levelStr = """
                ######
                #$  @#
                ######
                """;

        Level level = TestUtils.getSOKLevel(levelStr);
        Map map = level.getMap();

        assertThrows(IllegalArgumentException.class, () -> {
            PATHFINDER.hasPath(map.getAt(2, 1), map.getAt(0, 0), map.getAt(1, 1), map.getAt(2, 1));
        });
    }

    @Test
    void test() {
        Level level = TestUtils.getLevel(Path.of("../levels/levels8xv/Original.8xv"), 0);

        Map map = level.getMap();

        map.getAt(6, 4).addCrate();
        map.getAt(7, 4).removeCrate();

        Pathfinder.Node node = PATHFINDER.getCratePlayerAStar()
                .findPath(map.getAt(7, 4), map.getAt(5, 1), map.getAt(7, 3), map.getAt(7, 2));
        assertTrue(node.player().isAt(5, 1));
        assertTrue(node.crate().isAt(7, 2));
    }
}
