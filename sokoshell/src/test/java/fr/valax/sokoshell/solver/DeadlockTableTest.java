package fr.valax.sokoshell.solver;

import fr.valax.sokoshell.TestUtils;
import fr.valax.sokoshell.graphics.style.BasicStyle;
import fr.valax.sokoshell.solver.board.Board;
import fr.valax.sokoshell.solver.board.Direction;
import fr.valax.sokoshell.solver.board.MutableBoard;
import fr.valax.sokoshell.solver.board.tiles.Tile;
import fr.valax.sokoshell.solver.board.tiles.TileInfo;
import fr.valax.sokoshell.utils.PerformanceMeasurer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DeadlockTableTest {

    private static int numNodes = 0;
    private static int numDeadlocks = 0;

    @Test
    void test() {
        State.initZobristValues(100);
        DeadlockTable table = DeadlockTable.generate2(4, -1);
        //print(table, 4);
        try {
            DeadlockTable.write(table, Path.of("4x4.table"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Level l = TestUtils.getLevel("""
                #####
                #$#$#
                #$$$#
                # @ #
                #####
                """);

        // System.out.println(table.isDeadlock(l, l.getPlayerX(), l.getPlayerY()));
    }

    @Test
    void test2() throws IOException {
        DeadlockTable table = DeadlockTable.read(Path.of("../4x4.table"));

        Level l = TestUtils.getLevel("""
                ########
                # $ $  #
                # $$$  #
                #  @   #
                #      #
                ########
                """);

        System.out.println(table.isDeadlock(l.getAt(3, 2), Direction.UP));
    }

    @Test
    void write() throws IOException {
        State.initZobristValues(100);
        DeadlockTable table = DeadlockTable.generate(3);
        print(table, 3);

        Path tablePath = Path.of("3x3.table");
        DeadlockTable.write(table, tablePath);
        DeadlockTable table2 = DeadlockTable.read(tablePath);
        print(table2, 3);

        equals(table, table2, 3);
    }

    @Test
    void multithreadedVsMonoThread() {
        State.initZobristValues(100);

        DeadlockTable table = DeadlockTable.generate2(3, -1);
        DeadlockTable table2 = DeadlockTable.generate(3);

        equals(table, table2, 3);
        print(table, 3);
        print(table2, 3);

        Level l = TestUtils.getLevel("""
                ######
                # $  #
                # $  #
                ##@# #
                #    #
                ######
                """);

        System.out.println(table.isDeadlock(l.getAt(2, 3), Direction.UP));
    }

    private Tile toTile(int i) {
        if (i == 0) {
            return Tile.FLOOR;
        } else if (i == 1) {
            return Tile.CRATE;
        } else {
            return Tile.WALL;
        }
    }

    @Test
    void orderTest() {
        int[][] order = DeadlockTable.createOrder(3);

        for (int i = 0; i < order.length; i++) {
            System.out.printf("(%d; %d)%n", order[i][0], order[i][1]);
        }
    }

    @Test
    void countNotDetected() throws IOException {
        State.initZobristValues(100);
        DeadlockTable table = DeadlockTable.read(Path.of("../4x4.table"));
        System.out.println(DeadlockTable.countNotDetectedDeadlock(table, 4));
    }

    private void equals(DeadlockTable a, DeadlockTable b, int size) {
        int playerX = size / 2;
        int playerY = size - 1;

        Board board = new MutableBoard(size, size);

        TileInfo player = board.getAt(playerX, playerY);
        board.setAt(playerX, playerY - 1, Tile.CRATE);

        int[][] possibilities = generatePossibilities(size * size - 2);
        int[][] order = DeadlockTable.createOrder(size);

        for (int i = 0; i < possibilities.length; i++) {
            for (int j = 0; j < order.length; j++) {
                board.setAt(playerX + order[j][0], playerY + order[j][1], toTile(possibilities[i][j]));
            }

            boolean a1 = a.isDeadlock(player, Direction.UP);
            boolean b1 = b.isDeadlock(player, Direction.UP);
            assertEquals(a1, b1, () -> {
                return "\n" + BasicStyle.XSB_STYLE.drawToString(board, playerX, playerY).toAnsi() + "\n" + a1 + " != " + b1;
            });
        }
    }

    private int[][] generatePossibilities(int n) {
        int[][] array = new int[(int) Math.pow(3, n)][n];

        int i = 0;
        int[] possibilities = new int[n];
        while (i < array.length) {
            array[i] = possibilities.clone();

            int j = n - 1;
            while (j >= 0 && possibilities[j] == 2) {
                possibilities[j] = 0;
                j--;
            }

            if (j >= 0) {
                possibilities[j]++;
            }

            i++;
        }

        return array;
    }

    private void print(DeadlockTable table, int size) {
        numNodes = 0;
        numDeadlocks = 0;

        print(new MutableBoard(size, size), table);
        System.out.println(numDeadlocks + "/" + numNodes);
    }

    private void print(Board board, DeadlockTable table) {
        if (table == null) {
            return;
        }

        int playerX = board.getWidth() / 2;
        int playerY = board.getHeight() - 1;

        numNodes++;
        //BasicStyle.XSB_STYLE.print(board, playerX, playerY);
        if (table == DeadlockTable.NOT_DEADLOCK) {
            //System.out.println("Not a deadlock");
        } else if (table == DeadlockTable.DEADLOCK) {
            //System.out.println("Deadlock");
            numDeadlocks++;
        } else {
            // System.out.printf("Putting FLOOR at (%d; %d)%n", table.x, table.y);
            print(board, table.floorChild);

            // System.out.printf("Putting WALL at (%d; %d)%n", table.x, table.y);
            board.setAt(playerX + table.x, playerY + table.y, Tile.WALL);
            print(board, table.wallChild);

            // System.out.printf("Putting CRATE at (%d; %d)%n", table.x, table.y);
            board.setAt(playerX + table.x, playerY + table.y, Tile.CRATE);
            print(board, table.crateChild);

            board.setAt(playerX + table.x, playerY + table.y, Tile.FLOOR);
        }
    }
}
