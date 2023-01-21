package fr.valax.sokoshell.solver.pathfinder;

import fr.valax.sokoshell.TestUtils;
import fr.valax.sokoshell.solver.Level;
import fr.valax.sokoshell.solver.Map;
import fr.valax.sokoshell.solver.TileInfo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PlayerAStarTest {

    @Test
    void simple() {
        Level level = TestUtils.getLevel("""
                ########
                #@     #
                #    # #
                #  ### #
                #  #   #
                #    # #
                ########
                """);

        Map map = level.getMap();
        PlayerAStar aStar = new PlayerAStar(map);

        Node node = aStar.findPath(map.getAt(1, 1), map.getAt(6, 5), null, null);
        assertTrue(node.getPlayer().isAt(6, 5));
        node = node.getParent();
        assertTrue(node.getPlayer().isAt(6, 4));
        node = node.getParent();
        assertTrue(node.getPlayer().isAt(6, 3));
        node = node.getParent();
        assertTrue(node.getPlayer().isAt(6, 2));
        node = node.getParent();
        assertTrue(node.getPlayer().isAt(6, 1));
        node = node.getParent();
        assertTrue(node.getPlayer().isAt(5, 1));
        node = node.getParent();
        assertTrue(node.getPlayer().isAt(4, 1));
        node = node.getParent();
        assertTrue(node.getPlayer().isAt(3, 1));
        node = node.getParent();
        assertTrue(node.getPlayer().isAt(2, 1));
        node = node.getParent();
        assertTrue(node.getPlayer().isAt(1, 1));
        assertNull(node.getParent());
    }

    @Test
    void complex() {
        Level level = TestUtils.getLevel(LABYRINTH);

        Map map = level.getMap();
        PlayerAStar aStar = new PlayerAStar(map);

        Node node = aStar.findPath(map.getAt(1, 1), map.getAt(49, 49), null, null);
        assertNotNull(node);
        assertTrue(node.getPlayer().isAt(49, 49));

        int x = 49;
        int y = 49;
        while (node.getParent() != null) {
            TileInfo old = node.getParent().getPlayer();

            assertTrue(((old.getX() - x == 0 && Math.abs(old.getY() - y) == 1) // player can only move on the y-axis
                    || (old.getY() - y == 0 && Math.abs(old.getX() - x) == 1)) && // or the x-axis
                    !old.isSolid()); // player can't be in a wall

            x = old.getX();
            y = old.getY();

            node = node.getParent();
        }

        assertTrue(node.getPlayer().isAt(1, 1));
    }


    private static final String LABYRINTH = """
            ###################################################
            #@          # #   #   #             #   #   # #   #
            # # # ### ### # # # ##### # # # ##### # # ### # # #
            # # # #       # #     #   # # #       # #     # # #
            # ########### ### ########### # ##### # ##### # ###
            #   #   # # # # #       #     # #     #       # # #
            # ##### # # ### ####### # ### ####### ##### # # # #
            #   #     # #     # #   #   #   # # # # # # #     #
            # # # ##### # ### # ### ### ##### # # # # # ### ###
            # #   #   #   #   #       #         # #   # # # # #
            # ### ### ####### ##### ### ############# # # ### #
            # #     #                 #   #       #         # #
            # ### ### # # ######### ### ### ### ##### ####### #
            # #   # # # #     #         # # # #   # #   #   # #
            ##### # # ##### # ### # ##### ### # # # # ### ### #
            #           # # # #   #         #   #   #         #
            # ### # ##### # # ##### # ####### # ### # ### #####
            #   # # # #     #   # # #   #     # #     # #     #
            ### ##### ########### ### ### ####### # # # ### # #
            # #   # #         #   #   #   #   #   # #   # # # #
            # # # # ### ######### ######### ### ####### # ### #
            #   # # #         # #   # #       #     #   #     #
            # # # # # # ####### # ### ##### ##### ##### ### # #
            # # # #   #         #     #         #   #   #   # #
            # ##### ##### ### ####### ##### ### ### ### #######
            #   #   # #     # # #     #     # # #   # #   #   #
            # ####### # # # # # ##### # ### # ##### # ##### # #
            # # # # #   # # # #   #     # #     # # #   # # # #
            # # # # # # ### ### # # ### # ### ### # # ### ### #
            #   #     # #     # #   #   #     # # #   #       #
            ##### # # # ######### # ######### # # # ##### #####
            # #   # # #       #   #   #               #       #
            # ### ### # # ##### # # # ####### # ######### # ###
            # #     # # # # # # # # # #   #   #   #       # # #
            # # ### ### # # # ######### # ### # # # ######### #
            # #   #   # #     #       # #     # # #           #
            # ### ### ####### # # ### # ##### # # # # ####### #
            # #   #   #     #   #   # #   # # # # # # #     # #
            # ### ####### ##### ########### # ### ### # ### # #
            #   # #       #     #   # # #       # #   #   #   #
            # # # # ##### ### # # ### # ### # ########### # # #
            # # # #   # #   # # #     # #   #     #   #   # # #
            # ### ##### # # ### # ##### # ####### ### ### # # #
            #   # #   #   # #       # #   #   #     # #   # # #
            ### # # # # # ### ##### # ##### ### ### # ### #####
            #       # # #   #     #               # #         #
            ### ### # ### # # ##### # ####### # ### # #########
            #   #   # #   # # # #   # # #     # #           # #
            ### # # # ##### # # # ### # # # ### # ### ##### # #
            #   # # # #       #   #   #   #   # # #   #       #
            ###################################################
            """;
}
