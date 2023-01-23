package fr.valax.sokoshell.solver.pathfinder;

import fr.valax.sokoshell.TestUtils;
import fr.valax.sokoshell.solver.Level;
import fr.valax.sokoshell.solver.board.Direction;
import static fr.valax.sokoshell.solver.board.Direction.*;

import fr.valax.sokoshell.solver.board.MutableBoard;
import fr.valax.sokoshell.solver.board.tiles.MutableTileInfo;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class PlayerAStarTest {

    @Test
    void simple() {
        Direction[] solution = new Direction[] {
            RIGHT, RIGHT, RIGHT, RIGHT, RIGHT,
            DOWN, DOWN, DOWN, DOWN
        };

        Level level = TestUtils.getLevel("""
                ########
                #@     #
                #    # #
                #  ### #
                #  #   #
                #    # #
                ########
                """);

        MutableBoard board = new MutableBoard(level.getBoard());
        PlayerAStar aStar = new PlayerAStar(board);

        // 21 nodes A*
        // 23 nodes dijkstra
        Node node = aStar.findPathAndComputeMoves(board.getAt(1, 1), board.getAt(6, 5), null, null);
        PathfinderUtils.check(1, 1, -1, -1, 6, 5, -1, -1, node, solution);
    }

    @Test
    void complex() {
        Level level = TestUtils.getLevel(LABYRINTH);

        MutableBoard board = new MutableBoard(level.getBoard());
        PlayerAStar aStar = new PlayerAStar(board);

        // 181 nodes A*
        // 274 nodes dijkstra
        Node node = aStar.findPathAndComputeMoves(board.getAt(1, 1), board.getAt(28, 14), null, null);
        assertNotNull(node);
        assertTrue(node.getPlayer().isAt(28, 14));

        int x = 28;
        int y = 14;
        while (node.getParent() != null) {
            MutableTileInfo old = node.getParent().getPlayer();

            assertTrue(((old.getX() - x == 0 && Math.abs(old.getY() - y) == 1) // player can only move on the y-axis
                    || (old.getY() - y == 0 && Math.abs(old.getX() - x) == 1)) && // or the x-axis
                    !old.isSolid()); // player can't be in a wall

            x = old.getX();
            y = old.getY();

            node = node.getParent();
        }

        assertTrue(node.getPlayer().isAt(1, 1));
    }


    @Test
    void originalAndExtraTest() {
        Level level = TestUtils.getLevel(Path.of("levels8xv/Original.8xv"), 0);

        MutableBoard board = new MutableBoard(level.getBoard());
        PlayerAStar aStar = new PlayerAStar(board);

        assertTrue(aStar.hasPath(board.getAt(11, 8), board.getAt(17, 8), null, null));
        assertFalse(aStar.hasPath(board.getAt(11, 8), board.getAt(5, 2), null, null));
    }


    private static final String LABYRINTH = """
            ##############################
            #@   #                       #
            #  # ######################  #
            #  # #    #    #    #        #
            #  #    #    #    #    #     #
            #  #  #    #    #    #       #
            #  #     #    #    #    #    #
            #  #   #    #    #    #      #
            #  #      #    #    #    #   #
            #  #    #    #    #    #     #
            #  #       #    #    #       #
            #  #     #    #    #    #    #
            #  #        #    #    #      #
            #  ######################### #
            #                          # #
            ##############################
            """;
}
