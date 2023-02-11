package fr.valax.sokoshell.solver;

import fr.valax.sokoshell.TestUtils;
import fr.valax.sokoshell.solver.board.MutableBoard;
import fr.valax.sokoshell.solver.board.tiles.Tile;
import fr.valax.sokoshell.solver.board.tiles.TileInfo;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

public class CorralDetectorTest {

    @Test
    void test() {
        Level level = TestUtils.getLevel(Path.of("levels8xv/Original.8xv"), 0);
        MutableBoard board = new MutableBoard(level);

        CorralDetector corralDetector = new CorralDetector(board.getWidth() * board.getHeight());

        corralDetector.findCorral(board, level.getPlayerX(), level.getPlayerY());
        corralDetector.findPICorral(board, level.getInitialState().cratesIndices());
        print(corralDetector.getCorrals());

        board.setAt(5, 7, Tile.FLOOR);
        board.setAt(5, 8, Tile.CRATE);
        corralDetector.findCorral(board, level.getPlayerX(), level.getPlayerY());
        corralDetector.findPICorral(board, level.getInitialState().cratesIndices());
        print(corralDetector.getCorrals());
    }

    @Test
    void level2Test() {
        Level level = TestUtils.getLevel(Path.of("levels8xv/Original.8xv"), 1);
        MutableBoard board = new MutableBoard(level);

        CorralDetector corralDetector = new CorralDetector(board.getWidth() * board.getHeight());
        corralDetector.findCorral(board, level.getPlayerX(), level.getPlayerY());

        print(corralDetector.getCorrals());
    }

    @Test
    void level2Test2() {
        Level level = TestUtils.getLevel(Path.of("levels8xv/Original.8xv"), 1);
        MutableBoard board = new MutableBoard(level);

        board.removeStateCrates(level.getInitialState());
        board.getAt(7, 7).addCrate();;

        CorralDetector corralDetector = new CorralDetector(board.getWidth() * board.getHeight());
        corralDetector.findCorral(board, 6, 7);

        print(corralDetector.getCorrals());
    }


    @Test
    void piCorralTest1() {
        Level level = TestUtils.getLevel(Path.of("TIPEex.8xv"), 8);
        MutableBoard board = new MutableBoard(level);

        CorralDetector corralDetector = new CorralDetector(board.getWidth() * board.getHeight());

        corralDetector.findCorral(board, level.getPlayerX(), level.getPlayerY());
        corralDetector.findPICorral(board, level.getInitialState().cratesIndices());
        print(corralDetector.getCorrals());
    }

    @Test
    void piCorralTest2() {
        Level level = TestUtils.getLevel(Path.of("TIPEex.8xv"), 9);
        MutableBoard board = new MutableBoard(level);

        CorralDetector corralDetector = new CorralDetector(board.getWidth() * board.getHeight());

        corralDetector.findCorral(board, level.getPlayerX(), level.getPlayerY());
        corralDetector.findPICorral(board, level.getInitialState().cratesIndices());
        print(corralDetector.getCorrals());
    }

    @Test
    void piCorralTest3() {
        Level level = TestUtils.getLevel(Path.of("TIPEex.8xv"), 10);
        MutableBoard board = new MutableBoard(level);

        CorralDetector corralDetector = new CorralDetector(board.getWidth() * board.getHeight());

        corralDetector.findCorral(board, level.getPlayerX(), level.getPlayerY());
        corralDetector.findPICorral(board, level.getInitialState().cratesIndices());
        print(corralDetector.getCorrals());
    }


    private static void print(Set<Corral> corrals) {
        System.out.println("~~~~~~~~~~~~~~~~~~~~");
        System.out.printf("Number of corrals: %d%n", corrals.size());
        for (Corral c : corrals) {
            System.out.printf("~~~~ Corral n°%d ~~~~%n", c.id);
            System.out.println("Contains player? " + c.containsPlayer());
            System.out.println("PI Corral? " + c.isPICorral());
            System.out.printf("(top x, top y) = (%d; %d)%n", c.topX + 1, c.topY + 1);
            printList("Barrier", c.barrier);
            printList("Crates", c.crates);
        }
        System.out.println();
    }

    private static void printList(String name, List<TileInfo> list) {
        System.out.printf("%s: [", name);

        for (int i = 0; i < list.size() - 1; i++) {
            TileInfo tile = list.get(i);
            System.out.printf("{%s (%d; %d)}, ", tile.getTile(), (tile.getX() + 1), (tile.getY() + 1));
        }

        if (list.size() > 1) {
            TileInfo tile = list.get(list.size() - 1);
            System.out.printf("{%s (%d; %d)}", tile.getTile(), (tile.getX() + 1), (tile.getY() + 1));
        }
        System.out.println(']');
    }
}
