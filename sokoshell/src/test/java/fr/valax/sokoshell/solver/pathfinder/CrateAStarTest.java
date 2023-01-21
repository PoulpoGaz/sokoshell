package fr.valax.sokoshell.solver.pathfinder;

import fr.valax.sokoshell.TestUtils;
import fr.valax.sokoshell.solver.Direction;
import fr.valax.sokoshell.solver.Level;
import fr.valax.sokoshell.solver.Map;
import fr.valax.sokoshell.solver.TileInfo;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static fr.valax.sokoshell.solver.Direction.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CrateAStarTest {

    @Test
    void simple() {
        Direction[] solution = new Direction[] {
                DOWN, DOWN, DOWN, RIGHT, UP, LEFT, UP, RIGHT, RIGHT, RIGHT,
                UP, RIGHT, DOWN, DOWN, UP, RIGHT, RIGHT, RIGHT, DOWN, DOWN,
                LEFT, LEFT, LEFT
        };

        Level level = TestUtils.getLevel("""
                ##########
                #@       #
                #        #
                # $## ## #
                #  #.    #
                ##########
                """);

        Map map = level.getMap();
        CrateAStar aStar = new CrateAStar(map);
        Node end = aStar.findPath(map.getAt(1, 1), null, map.getAt(2, 3), map.getAt(4, 4));

        List<Node> nodes = new ArrayList<>();
        Node temp = end;
        while (temp != null) {
            nodes.add(temp);
            temp = temp.getParent();
        }
        Collections.reverse(nodes);

        // assertEquals(nodes.size(), solution.length);

        int playerX = 1;
        int playerY = 1;
        int crateX = 2;
        int crateY = 3;

        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);

            TileInfo p = node.getPlayer();
            TileInfo c = node.getCrate();

            String errorMessage = "At %d. Player expected to be at (%d; %d) but was at (%d; %d). Crate expected to be at (%d; %d) but was at (%d; %d)"
                    .formatted(i, playerX, playerY, p.getX(), p.getY(), crateX, crateY, c.getX(), c.getY());

            assertEquals(p.getX(), playerX, errorMessage);
            assertEquals(p.getY(), playerY, errorMessage);
            assertEquals(c.getX(), crateX, errorMessage);
            assertEquals(c.getY(), crateY, errorMessage);

            playerX += solution[i].dirX();
            playerY += solution[i].dirY();

            if (playerX == crateX && playerY == crateY) {
                crateX += solution[i].dirX();
                crateY += solution[i].dirY();
            }
        }
    }
}
