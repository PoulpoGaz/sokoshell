package fr.valax.sokoshell.solver;

import fr.valax.sokoshell.TestUtils;
import fr.valax.sokoshell.graphics.style.BasicStyle;
import fr.valax.sokoshell.solver.board.MutableBoard;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

public class CorralTest {

    @Test
    void corralTest() {
        Level level = TestUtils.getLevel(Path.of("levels8xv/Original.8xv"), 3);
        MutableBoard board = new MutableBoard(level);

        CorralDetector corralDetector = board.getCorralDetector();

        int playerX = 1;
        int playerY = 3;
        int c1      = board.getIndex(3, 7);
        int c2      = board.getIndex(3, 6);
        int c3      = board.getIndex(4, 6);
        int player  = board.getIndex(playerX, playerY);

        board.removeStateCrates(level.getInitialState());
        board.computeFloors();
        board.computeDeadTiles();
        board.getAt(c1).addCrate();
        board.getAt(c2).addCrate();
        board.getAt(c3).addCrate();

        BasicStyle.XSB_STYLE.print(board, playerX, playerY);

        State state = new State(player, new int[] {c1, c2, c3}, 0, null);
        corralDetector.findCorral(board, playerX, playerY);
        corralDetector.findPICorral(board, state.cratesIndices());

        for (Corral c : corralDetector.getCorrals()) {
            System.out.printf("%d - %d. deadlock? %s%n", c.getTopX(), c.getTopY(), c.isDeadlock(state));
        }
    }

    @Test
    void corralTest2() {
        Level level = TestUtils.getLevel(Path.of("levels8xv/Original.8xv"), 2);
        MutableBoard board = new MutableBoard(level);

        CorralDetector corralDetector = board.getCorralDetector();

        int[] crates = new int[] {
                board.getIndex(1, 6),
                board.getIndex(2, 6),
                board.getIndex(2, 7),
                board.getIndex(2, 8),
                board.getIndex(1, 8),

                board.getIndex(10, 3),
                board.getIndex(12, 4),
                board.getIndex(11, 7),
                board.getIndex(13, 5),
                board.getIndex(13, 7),
                board.getIndex(14, 6),
        };

        int playerX = 9;
        int playerY = 1;
        int player  = board.getIndex(playerX, playerY);

        board.removeStateCrates(level.getInitialState());
        board.computeFloors();
        board.computeDeadTiles();

        State state = new State(player, crates, 0, null);
        board.addStateCrates(state);

        BasicStyle.XSB_STYLE.print(board, playerX, playerY);

        corralDetector.findCorral(board, playerX, playerY);
        corralDetector.findPICorral(board, state.cratesIndices());

        for (Corral c : corralDetector.getCorrals()) {
            System.out.printf("%d - %d. deadlock? %s%n", c.getTopX(), c.getTopY(), c.isDeadlock(state));
        }
    }

    @Test
    void multiCorral() {
        Level level = TestUtils.getLevel(Path.of("levels8xv/Original.8xv"), 4);
        MutableBoard board = new MutableBoard(level);

        CorralDetector corralDetector = board.getCorralDetector();

        int[] crates = new int[] {
                board.getIndex(9, 5),
                board.getIndex(10, 3),
                board.getIndex(11, 2),
                board.getIndex(13, 3),
                board.getIndex(1, 5)
        };

        int playerX = 14;
        int playerY = 2;
        int player  = board.getIndex(playerX, playerY);

        board.removeStateCrates(level.getInitialState());
        board.computeFloors();
        board.computeDeadTiles();

        State state = new State(player, crates, 0, null);
        board.addStateCrates(state);

        BasicStyle.XSB_STYLE.print(board, playerX, playerY);

        corralDetector.findCorral(board, playerX, playerY);
        corralDetector.findPICorral(board, state.cratesIndices());

        for (Corral c : corralDetector.getCorrals()) {
            System.out.printf("%d - %d. deadlock? %s%n", c.getTopX(), c.getTopY(), c.isDeadlock(state));
        }
    }

    @Test
    void corralTest3() {
        Level level = TestUtils.getLevel(Path.of("levels8xv/Original.8xv"), 3);
        MutableBoard board = new MutableBoard(level);
        CorralDetector corralDetector = board.getCorralDetector();

        int[] crates = new int[] {
                board.getIndex(1, 3),
                board.getIndex(6, 10),
                board.getIndex(7, 10),
                board.getIndex(8, 11)
        };

        int playerX = 2;
        int playerY = 8;
        int player  = board.getIndex(playerX, playerY);

        board.removeStateCrates(level.getInitialState());
        board.computeFloors();
        board.computeDeadTiles();

        State state = new State(player, crates, 0, null);
        board.addStateCrates(state);

        BasicStyle.XSB_STYLE.print(board, playerX, playerY);

        corralDetector.findCorral(board, playerX, playerY);
        corralDetector.findPICorral(board, state.cratesIndices());

        for (Corral c : corralDetector.getCorrals()) {
            System.out.printf("%d - %d. pi-corral? %s deadlock? %s%n", c.getTopX(), c.getTopY(), c.isPICorral(), c.isDeadlock(state));
        }
    }
}
