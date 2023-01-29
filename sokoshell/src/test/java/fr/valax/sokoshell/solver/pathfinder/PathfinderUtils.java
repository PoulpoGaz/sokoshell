package fr.valax.sokoshell.solver.pathfinder;

import fr.valax.sokoshell.solver.board.Direction;
import fr.valax.sokoshell.solver.board.tiles.TileInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class PathfinderUtils {

    public static void check(int playerX, int playerY, int crateX, int crateY,
                             int playerDestX, int playerDestY, int crateDestX, int crateDestY,
                             Node end, Direction... solution) {
        assertNotNull(end);

        List<Node> nodes = new ArrayList<>();
        Node temp = end;
        while (temp != null) {
            nodes.add(temp);
            temp = temp.getParent();
        }
        Collections.reverse(nodes);
        assertEquals(solution.length + 1, nodes.size());

        boolean crate = crateX >= 0 && crateY >= 0;
        for (int i = 0; i < nodes.size() - 1; i++) {
            Node node = nodes.get(i);

            TileInfo p = node.getPlayer();
            TileInfo c = node.getCrate();

            String errorMessage = "At %d. Player expected to be at (%d; %d) but was at (%d; %d). "
                    .formatted(i - 1, playerX, playerY, p.getX(), p.getY());

            assertEquals(p.getX(), playerX, errorMessage);
            assertEquals(p.getY(), playerY, errorMessage);

            if (crate) {
                errorMessage = "At %d. Crate expected to be at (%d; %d) but was at (%d; %d)"
                        .formatted(i - 1, crateX, crateY, c.getX(), c.getY());

                assertEquals(c.getX(), crateX, errorMessage);
                assertEquals(c.getY(), crateY, errorMessage);
            }

            playerX += solution[i].dirX();
            playerY += solution[i].dirY();

            if (crate && playerX == crateX && playerY == crateY) {
                crateX += solution[i].dirX();
                crateY += solution[i].dirY();
            }
        }

        assertEquals(playerDestX, playerX);
        assertEquals(playerDestY, playerY);

        if (crate) {
            assertEquals(crateDestX, crateX);
            assertEquals(crateDestY, crateY);
        }
    }
}
