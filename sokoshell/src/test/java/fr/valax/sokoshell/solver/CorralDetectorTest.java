package fr.valax.sokoshell.solver;

import fr.valax.sokoshell.TestUtils;
import fr.valax.sokoshell.solver.board.MutableBoard;
import fr.valax.sokoshell.solver.board.tiles.Tile;
import fr.valax.sokoshell.solver.board.tiles.TileInfo;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class CorralDetectorTest {

    @Test
    void test() {
        Level level = TestUtils.getLevel(Path.of("levels8xv/Original.8xv"), 0);
        MutableBoard board = new MutableBoard(level);

        CorralDetector corralDetector = new CorralDetector(board.getWidth() * board.getHeight());
        corralDetector.findCorral(board, level.getPlayerX(), level.getPlayerY());

        Set<Corral> corrals = new HashSet<>();
        for (int y = 0; y < board.getHeight(); y++) {
            for (int x = 0; x < board.getWidth(); x++) {
                if (!board.getAt(x, y).isSolid()) {
                    corrals.add(corralDetector.findCorral(board.getAt(x, y)));
                }
            }
        }

        System.out.println(corrals.size());
        for (Corral c : corrals) {
            System.out.println("------------------------------------------");
            System.out.println("Reachable? " + c.containsPlayer());
            System.out.print("Tiles: [");
            for (TileInfo tile : c.tiles) {
                System.out.printf("{%s (%d; %d)}, ", tile.getTile(), (tile.getX() + 1),  (tile.getY() + 1));
            }
            System.out.println("]");

            System.out.print("Crates: [");
            for (TileInfo crate : c.crates) {
                System.out.printf("{%s (%d; %d)}, ", crate.getTile(), (crate.getX() + 1),  (crate.getY() + 1));
            }
            System.out.println("]");
        }

        board.setAt(5, 7, Tile.FLOOR);
        board.setAt(5, 8, Tile.CRATE);

        corralDetector.findCorral(board, level.getPlayerX(), level.getPlayerY());

        corrals.clear();
        for (int y = 0; y < board.getHeight(); y++) {
            for (int x = 0; x < board.getWidth(); x++) {
                if (!board.getAt(x, y).isSolid()) {
                    corrals.add(corralDetector.findCorral(board.getAt(x, y)));
                }
            }
        }

        System.out.println(corrals.size());
        for (Corral c : corrals) {
            System.out.println("------------------------------------------");
            System.out.println("Reachable? " + c.containsPlayer());
            System.out.print("Tiles: [");
            for (TileInfo tile : c.tiles) {
                System.out.printf("{%s (%d; %d)}, ", tile.getTile(), (tile.getX() + 1),  (tile.getY() + 1));
            }
            System.out.println("]");

            System.out.print("Crates: [");
            for (TileInfo crate : c.crates) {
                System.out.printf("{%s (%d; %d)}, ", crate.getTile(), (crate.getX() + 1),  (crate.getY() + 1));
            }
            System.out.println("]");
        }
    }

    @Test
    void level2Test() {
        Level level = TestUtils.getLevel(Path.of("levels8xv/Original.8xv"), 1);
        MutableBoard board = new MutableBoard(level);

        CorralDetector corralDetector = new CorralDetector(board.getWidth() * board.getHeight());
        corralDetector.findCorral(board, level.getPlayerX(), level.getPlayerY());

        Set<Corral> corrals = new HashSet<>();
        for (int y = 0; y < board.getHeight(); y++) {
            for (int x = 0; x < board.getWidth(); x++) {
                if (!board.getAt(x, y).isSolid()) {
                    corrals.add(corralDetector.findCorral(board.getAt(x, y)));
                }
            }
        }

        System.out.println(corrals.size());
        for (Corral c : corrals) {
            System.out.println("------------------------------------------");
            System.out.println("Reachable? " + c.containsPlayer());
            System.out.print("Tiles: [");
            for (TileInfo tile : c.tiles) {
                System.out.printf("{%s (%d; %d)}, ", tile.getTile(), (tile.getX() + 1), (tile.getY() + 1));
            }
            System.out.println("]");

            System.out.print("Crates: [");
            for (TileInfo crate : c.crates) {
                System.out.printf("{%s (%d; %d)}, ", crate.getTile(), (crate.getX() + 1), (crate.getY() + 1));
            }
            System.out.println("]");
        }
    }

    @Test
    void level2Test2() {
        Level level = TestUtils.getLevel(Path.of("levels8xv/Original.8xv"), 1);
        MutableBoard board = new MutableBoard(level);

        board.removeStateCrates(level.getInitialState());
        board.getAt(7, 7).addCrate();;

        CorralDetector corralDetector = new CorralDetector(board.getWidth() * board.getHeight());
        corralDetector.findCorral(board, 6, 7);

        Set<Corral> corrals = new HashSet<>();
        for (int y = 0; y < board.getHeight(); y++) {
            for (int x = 0; x < board.getWidth(); x++) {
                if (!board.getAt(x, y).isSolid()) {
                    corrals.add(corralDetector.findCorral(board.getAt(x, y)));
                }
            }
        }

        System.out.println(corrals.size());
        for (Corral c : corrals) {
            System.out.println("------------------------------------------");
            System.out.println("Reachable? " + c.containsPlayer());
            System.out.print("Tiles: [");
            for (TileInfo tile : c.tiles) {
                System.out.printf("{%s (%d; %d)}, ", tile.getTile(), (tile.getX() + 1), (tile.getY() + 1));
            }
            System.out.println("]");

            System.out.print("Crates: [");
            for (TileInfo crate : c.crates) {
                System.out.printf("{%s (%d; %d)}, ", crate.getTile(), (crate.getX() + 1), (crate.getY() + 1));
            }
            System.out.println("]");
        }
    }
}
