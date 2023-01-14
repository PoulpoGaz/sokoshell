package fr.valax.sokoshell.solver;

import fr.valax.sokoshell.TestUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LevelTest {

    @Test
    void formatTest() {
        String level = """
                  #
                 # #
                 # #
                #@ #
                 ##
                """;

        Level l = TestUtils.getLevel(level);
        Map map = l.getMap();

        assertEquals(Tile.WALL, map.getAt(0, 0).getTile());
        assertEquals(Tile.WALL, map.getAt(1, 0).getTile());
        assertEquals(Tile.WALL, map.getAt(2, 0).getTile());
        assertEquals(Tile.WALL, map.getAt(3, 0).getTile());

        assertEquals(Tile.WALL,  map.getAt(0, 1).getTile());
        assertEquals(Tile.WALL,  map.getAt(1, 1).getTile());
        assertEquals(Tile.FLOOR, map.getAt(2, 1).getTile());
        assertEquals(Tile.WALL,  map.getAt(3, 1).getTile());

        assertEquals(Tile.WALL,  map.getAt(0, 2).getTile());
        assertEquals(Tile.WALL,  map.getAt(1, 2).getTile());
        assertEquals(Tile.FLOOR, map.getAt(2, 2).getTile());
        assertEquals(Tile.WALL,  map.getAt(3, 2).getTile());

        assertEquals(Tile.WALL,  map.getAt(0, 3).getTile());
        assertEquals(Tile.FLOOR, map.getAt(1, 3).getTile());
        assertEquals(Tile.FLOOR, map.getAt(2, 3).getTile());
        assertEquals(Tile.WALL,  map.getAt(3, 3).getTile());

        assertEquals(Tile.WALL,  map.getAt(0, 4).getTile());
        assertEquals(Tile.WALL,  map.getAt(1, 4).getTile());
        assertEquals(Tile.WALL,  map.getAt(2, 4).getTile());
        assertEquals(Tile.WALL,  map.getAt(3, 4).getTile());
    }

    @Test
    void formatTest2() {
        String level = """
                  #
                 # ###
                 # # #
                #@ ###
                 ##
                """;

        Level l = TestUtils.getLevel(level);
        Map map = l.getMap();

        assertEquals(Tile.WALL, map.getAt(0, 0).getTile());
        assertEquals(Tile.WALL, map.getAt(1, 0).getTile());
        assertEquals(Tile.WALL, map.getAt(2, 0).getTile());
        assertEquals(Tile.WALL, map.getAt(3, 0).getTile());
        assertEquals(Tile.WALL, map.getAt(4, 0).getTile());
        assertEquals(Tile.WALL, map.getAt(5, 0).getTile());

        assertEquals(Tile.WALL,  map.getAt(0, 1).getTile());
        assertEquals(Tile.WALL,  map.getAt(1, 1).getTile());
        assertEquals(Tile.FLOOR, map.getAt(2, 1).getTile());
        assertEquals(Tile.WALL,  map.getAt(3, 1).getTile());
        assertEquals(Tile.WALL, map.getAt(4, 1).getTile());
        assertEquals(Tile.WALL, map.getAt(5, 1).getTile());

        assertEquals(Tile.WALL,  map.getAt(0, 2).getTile());
        assertEquals(Tile.WALL,  map.getAt(1, 2).getTile());
        assertEquals(Tile.FLOOR, map.getAt(2, 2).getTile());
        assertEquals(Tile.WALL,  map.getAt(3, 2).getTile());
        assertEquals(Tile.WALL, map.getAt(4, 2).getTile());
        assertEquals(Tile.WALL, map.getAt(5, 2).getTile());

        assertEquals(Tile.WALL,  map.getAt(0, 3).getTile());
        assertEquals(Tile.FLOOR, map.getAt(1, 3).getTile());
        assertEquals(Tile.FLOOR, map.getAt(2, 3).getTile());
        assertEquals(Tile.WALL,  map.getAt(3, 3).getTile());
        assertEquals(Tile.WALL, map.getAt(4, 3).getTile());
        assertEquals(Tile.WALL, map.getAt(5, 3).getTile());

        assertEquals(Tile.WALL,  map.getAt(0, 4).getTile());
        assertEquals(Tile.WALL,  map.getAt(1, 4).getTile());
        assertEquals(Tile.WALL,  map.getAt(2, 4).getTile());
        assertEquals(Tile.WALL,  map.getAt(3, 4).getTile());
        assertEquals(Tile.WALL, map.getAt(4, 4).getTile());
        assertEquals(Tile.WALL, map.getAt(5, 4).getTile());
    }

    @Test
    void formatTest3() {
        String level = """
                 #
                #   #
                #   #
                @ ###
                ##
                """;

        Level l = TestUtils.getLevel(level);
        Map map = l.getMap();

        assertEquals(7, map.getWidth());
        assertEquals(6, map.getHeight());
        assertEquals(1, l.getPlayerX());
        assertEquals(4, l.getPlayerY());

        assertEquals(Tile.WALL, map.getAt(0, 0).getTile());
        assertEquals(Tile.WALL, map.getAt(1, 0).getTile());
        assertEquals(Tile.WALL, map.getAt(2, 0).getTile());
        assertEquals(Tile.WALL, map.getAt(3, 0).getTile());
        assertEquals(Tile.WALL, map.getAt(4, 0).getTile());
        assertEquals(Tile.WALL, map.getAt(5, 0).getTile());
        assertEquals(Tile.WALL, map.getAt(6, 0).getTile());

        assertEquals(Tile.WALL,  map.getAt(0, 1).getTile());
        assertEquals(Tile.WALL,  map.getAt(1, 1).getTile());
        assertEquals(Tile.WALL,  map.getAt(2, 1).getTile());
        assertEquals(Tile.FLOOR, map.getAt(3, 1).getTile());
        assertEquals(Tile.FLOOR, map.getAt(4, 1).getTile());
        assertEquals(Tile.FLOOR, map.getAt(5, 1).getTile());
        assertEquals(Tile.WALL,  map.getAt(6, 1).getTile());

        assertEquals(Tile.WALL,  map.getAt(0, 2).getTile());
        assertEquals(Tile.WALL,  map.getAt(1, 2).getTile());
        assertEquals(Tile.FLOOR, map.getAt(2, 2).getTile());
        assertEquals(Tile.FLOOR, map.getAt(3, 2).getTile());
        assertEquals(Tile.FLOOR, map.getAt(4, 2).getTile());
        assertEquals(Tile.WALL,  map.getAt(5, 2).getTile());
        assertEquals(Tile.WALL,  map.getAt(6, 2).getTile());

        assertEquals(Tile.WALL,  map.getAt(0, 3).getTile());
        assertEquals(Tile.WALL,  map.getAt(1, 3).getTile());
        assertEquals(Tile.FLOOR, map.getAt(2, 3).getTile());
        assertEquals(Tile.FLOOR, map.getAt(3, 3).getTile());
        assertEquals(Tile.FLOOR, map.getAt(4, 3).getTile());
        assertEquals(Tile.WALL,  map.getAt(5, 3).getTile());
        assertEquals(Tile.WALL,  map.getAt(6, 3).getTile());

        assertEquals(Tile.WALL,  map.getAt(0, 4).getTile());
        assertEquals(Tile.FLOOR, map.getAt(1, 4).getTile());
        assertEquals(Tile.FLOOR, map.getAt(2, 4).getTile());
        assertEquals(Tile.WALL,  map.getAt(3, 4).getTile());
        assertEquals(Tile.WALL,  map.getAt(4, 4).getTile());
        assertEquals(Tile.WALL,  map.getAt(5, 4).getTile());
        assertEquals(Tile.WALL,  map.getAt(6, 4).getTile());

        assertEquals(Tile.WALL, map.getAt(0, 5).getTile());
        assertEquals(Tile.WALL, map.getAt(1, 5).getTile());
        assertEquals(Tile.WALL, map.getAt(2, 5).getTile());
        assertEquals(Tile.WALL, map.getAt(3, 5).getTile());
        assertEquals(Tile.WALL, map.getAt(4, 5).getTile());
        assertEquals(Tile.WALL, map.getAt(5, 5).getTile());
        assertEquals(Tile.WALL, map.getAt(6, 5).getTile());
    }
}
