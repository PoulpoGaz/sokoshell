package fr.valax.sokoshell.solver;

import fr.valax.sokoshell.TestUtils;
import fr.valax.sokoshell.graphics.style.BoardStyle;
import fr.valax.sokoshell.solver.board.Board;
import fr.valax.sokoshell.solver.board.MutableBoard;
import fr.valax.sokoshell.solver.board.Room;
import fr.valax.sokoshell.solver.board.Tunnel;
import fr.valax.sokoshell.solver.board.tiles.TileInfo;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class BoardTest {

    @Test
    void topLeftReachablePositionTest() {
        Level level = TestUtils.getLevel(Path.of("../levels/levels8xv/Original.8xv"));
        Board board = new MutableBoard(level);

        int i = board.topLeftReachablePosition(board.getAt(5, 7), board.getAt(5, 6));

        assertEquals(3, i % board.getWidth());
        assertEquals(4, i / board.getWidth());
    }

    // ***************
    // * TUNNEL TEST *
    // ***************

    @Test
    void findTunnelTest() {
        Set<TTunnel> tunnelsSet = new HashSet<>();
        tunnelsSet.add(new TTunnel(4, 4, 3, 5,    5, 4, 3, 6,    true,  false));
        tunnelsSet.add(new TTunnel(5, 5, 5, 6,    5, 4, 5, 7,    false, false));
        tunnelsSet.add(new TTunnel(8, 4, 8, 6,    7, 4, 8, 7,    true,  false));
        tunnelsSet.add(new TTunnel(4, 7, 4, 7,    3, 7, 5, 7,    false, false));
        tunnelsSet.add(new TTunnel(6, 7, 7, 7,    5, 7, 8, 7,    false, false));
        tunnelsSet.add(new TTunnel(5, 8, 9, 8,    5, 7, 9, 7,    true,  false));
        tunnelsSet.add(new TTunnel(10, 7, 10, 7,  9, 7, 11, 7,   false, true));
        tunnelsSet.add(new TTunnel(11, 8, 11, 8,  11, 7, -1, -1, false, true));
        tunnelsSet.add(new TTunnel(12, 7, 13, 7,  11, 7, 14, 7,  false, true));

        Level level = TestUtils.getLevel(Path.of("levels8xv/Original.8xv"));
        Board board = new MutableBoard(level);
        State.initZobristValues(board.getWidth() * board.getHeight());
        board.removeStateCrates(level.getInitialState());
        board.computeFloors();
        board.findTunnels();

        List<Tunnel> tunnels = board.getTunnels();

        for (Tunnel t : tunnels) {
            TileInfo s = t.getStart();
            TileInfo e = t.getEnd();
            TileInfo so = t.getStartOut();
            TileInfo eo = t.getEndOut();

            TTunnel arr1 = new TTunnel(s.getX(), s.getY(), e.getX(), e.getY());
            arr1.setOnlyPlayer(t.isPlayerOnlyTunnel());
            arr1.setOneWay(t.isOneway());
            TTunnel arr2 = new TTunnel(e.getX(), e.getY(), s.getX(), s.getY());
            arr2.setOnlyPlayer(t.isPlayerOnlyTunnel());
            arr2.setOneWay(t.isOneway());

            if (so != null) {
                arr1.setStartOutX(so.getX());
                arr1.setStartOutY(so.getY());
                arr2.setEndOutX(so.getX());
                arr2.setEndOutY(so.getY());
            } else {
                arr1.setStartOutX(-1);
                arr1.setStartOutY(-1);
                arr2.setEndOutX(-1);
                arr2.setEndOutY(-1);
            }

            if (eo != null) {
                arr1.setEndOutX(eo.getX());
                arr1.setEndOutY(eo.getY());
                arr2.setStartOutX(eo.getX());
                arr2.setStartOutY(eo.getY());
            } else {
                arr1.setEndOutX(-1);
                arr1.setEndOutY(-1);
                arr2.setStartOutX(-1);
                arr2.setStartOutY(-1);
            }

            System.out.println(arr1);

            if (tunnelsSet.contains(arr1)) {
                tunnelsSet.remove(arr1);
            } else if (tunnelsSet.contains(arr2)) {
                tunnelsSet.remove(arr2);
            } else {
                throw new AssertionFailedError("No tunnel of the form: " + arr1);
            }
        }

        assertEquals(0, tunnelsSet.size());
    }


    @Test
    void tunnelExitTest1() {
        String mapStr = """
                #############
                #@ #######  #
                #           #
                #############
                """;

        Level level = TestUtils.getLevel(mapStr);
        Board board = new MutableBoard(level);
        board.initForSolver();

        List<Tunnel> tunnels = board.getTunnels();
        assertEquals(1, tunnels.size());

        Tunnel t = tunnels.get(0);
        assertFalse(t.isPlayerOnlyTunnel());

        TileInfo start;
        TileInfo startOut;
        TileInfo end;
        TileInfo endOut;

        if (t.getStart().isAt(3, 2)) {
            start = t.getStart();
            startOut = t.getStartOut();
            end = t.getEnd();
            endOut = t.getEndOut();
        } else {
            start = t.getEnd();
            startOut = t.getEndOut();
            end = t.getStart();
            endOut = t.getStartOut();
        }

        assertTrue(start.isAt(3, 2));
        assertNotNull(startOut);
        assertTrue(startOut.isAt(2, 2));

        assertTrue(end.isAt(9, 2));
        assertNotNull(endOut);
        assertTrue(endOut.isAt(10, 2));

        for (int x = 3; x < 10; x++) {
            TileInfo tile = board.getAt(x, 2);
            assertEquals(t, tile.getTunnel());
            assertNotNull(tile.getTunnelExit());

            Tunnel.Exit exit = tile.getTunnelExit();
            assertEquals(startOut, exit.getLeftExit());
            assertEquals(endOut, exit.getRightExit());
            assertNull(exit.getUpExit());
            assertNull(exit.getDownExit());
        }
    }

    @Test
    void tunnelExitTest2() {
        String mapStr = """
                ######
                #    #
                # ## #
                # #  #
                #   @#
                ######
                """;

        Level level = TestUtils.getLevel(mapStr);
        Board board = new MutableBoard(level);
        board.initForSolver();

        List<Tunnel> tunnels = board.getTunnels();
        assertEquals(1, tunnels.size());

        Tunnel t = tunnels.get(0);
        assertTrue(t.isPlayerOnlyTunnel());

        TileInfo start;
        TileInfo startOut;
        TileInfo end;
        TileInfo endOut;

        if (t.getStart().isAt(4, 2)) {
            start = t.getStart();
            startOut = t.getStartOut();
            end = t.getEnd();
            endOut = t.getEndOut();
        } else {
            start = t.getEnd();
            startOut = t.getEndOut();
            end = t.getStart();
            endOut = t.getStartOut();
        }

        assertTrue(start.isAt(4, 2));
        assertNotNull(startOut);
        assertTrue(startOut.isAt(4, 3));

        assertTrue(end.isAt(2, 4));
        assertNotNull(endOut);
        assertTrue(endOut.isAt(3, 4));

        assertTrue(equals(new Tunnel.Exit(null, null, null, startOut), board.getAt(4, 2).getTunnelExit()));
        assertNull(board.getAt(4, 1).getTunnelExit());
        assertNull(board.getAt(3, 1).getTunnelExit());
        assertNull(board.getAt(2, 1).getTunnelExit());
        assertNull(board.getAt(1, 1).getTunnelExit());
        assertNull(board.getAt(1, 2).getTunnelExit());
        assertNull(board.getAt(1, 3).getTunnelExit());
        assertNull(board.getAt(1, 4).getTunnelExit());
        assertTrue(equals(new Tunnel.Exit(null, null, endOut, null), board.getAt(2, 4).getTunnelExit()));

    }


    private boolean equals(Tunnel.Exit a, Tunnel.Exit b) {
        if (!Objects.equals(a.getLeftExit(), b.getLeftExit())) return false;
        if (!Objects.equals(a.getUpExit(), b.getUpExit())) return false;
        if (!Objects.equals(a.getRightExit(), b.getRightExit())) return false;
        return Objects.equals(a.getDownExit(), b.getDownExit());
    }


    private static class TTunnel {

        private int startX;
        private int startY;
        private int endX;
        private int endY;
        private int startOutX;
        private int startOutY;
        private int endOutX;
        private int endOutY;
        private boolean onlyPlayer;
        private boolean oneWay;

        public TTunnel(int startX, int startY, int endX, int endY) {
            this.startX = startX;
            this.startY = startY;
            this.endX = endX;
            this.endY = endY;
        }

        private TTunnel(int startX, int startY, int endX, int endY, int startOutX, int startOutY, int endOutX, int endOutY, boolean onlyPlayer, boolean oneWay) {
            this.startX = startX;
            this.startY = startY;
            this.endX = endX;
            this.endY = endY;
            this.startOutX = startOutX;
            this.startOutY = startOutY;
            this.endOutX = endOutX;
            this.endOutY = endOutY;
            this.onlyPlayer = onlyPlayer;
            this.oneWay = oneWay;
        }

        public int startX() {
            return startX;
        }

        public void setStartX(int startX) {
            this.startX = startX;
        }

        public int startY() {
            return startY;
        }

        public void setStartY(int startY) {
            this.startY = startY;
        }

        public int endX() {
            return endX;
        }

        public void setEndX(int endX) {
            this.endX = endX;
        }

        public int endY() {
            return endY;
        }

        public void setEndY(int endY) {
            this.endY = endY;
        }

        public int startOutX() {
            return startOutX;
        }

        public void setStartOutX(int startOutX) {
            this.startOutX = startOutX;
        }

        public int startOutY() {
            return startOutY;
        }

        public void setStartOutY(int startOutY) {
            this.startOutY = startOutY;
        }

        public int endOutX() {
            return endOutX;
        }

        public void setEndOutX(int endOutX) {
            this.endOutX = endOutX;
        }

        public int endOutY() {
            return endOutY;
        }

        public void setEndOutY(int endOutY) {
            this.endOutY = endOutY;
        }

        public boolean onlyPlayer() {
            return onlyPlayer;
        }

        public void setOnlyPlayer(boolean onlyPlayer) {
            this.onlyPlayer = onlyPlayer;
        }

        public boolean oneWay() {
            return oneWay;
        }

        public void setOneWay(boolean oneWay) {
            this.oneWay = oneWay;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TTunnel tTunnel)) return false;

            if (startX != tTunnel.startX) return false;
            if (startY != tTunnel.startY) return false;
            if (endX != tTunnel.endX) return false;
            if (endY != tTunnel.endY) return false;
            if (startOutX != tTunnel.startOutX) return false;
            if (startOutY != tTunnel.startOutY) return false;
            if (endOutX != tTunnel.endOutX) return false;
            if (endOutY != tTunnel.endOutY) return false;
            if (onlyPlayer != tTunnel.onlyPlayer) return false;
            return oneWay == tTunnel.oneWay;
        }

        @Override
        public int hashCode() {
            int result = startX;
            result = 31 * result + startY;
            result = 31 * result + endX;
            result = 31 * result + endY;
            result = 31 * result + startOutX;
            result = 31 * result + startOutY;
            result = 31 * result + endOutX;
            result = 31 * result + endOutY;
            result = 31 * result + (onlyPlayer ? 1 : 0);
            result = 31 * result + (oneWay ? 1 : 0);
            return result;
        }

        @Override
        public String toString() {
            return "(%d; %d) - (%d; %d) --> (%d; %d) - (%d; %d). only player? %s. one way? %s"
                    .formatted(startOutX, startOutY, startX, startY, endX, endY, endOutX, endOutY, onlyPlayer, oneWay);
        }
    }




    // **********************
    // * PACKING ORDER TEST *
    // **********************

    @Test
    void packingOrderTest1() {
        Level level = TestUtils.getLevel(Path.of("levels8xv/Original.8xv"));
        Board board = new MutableBoard(level);
        board.removeStateCrates(level.getInitialState());
        board.initForSolver();

        Room room = null;
        for (Room r : board.getRooms()) {
            if (r.isGoalRoom()) {
                assertNull(room);
                room = r;
            }
        }

        assertNotNull(room);
        assertNotNull(room.getPackingOrder());
        for (TileInfo t : room.getPackingOrder()) {
            System.out.println(t.getX() + " - " + t.getY());
        }
    }

    @Test
    void packingOrderTest2() {
        Level level = TestUtils.getLevel(Path.of("levels8xv/Original.8xv"), 10);
        Board board = new MutableBoard(level);
        board.removeStateCrates(level.getInitialState());
        board.initForSolver();

        for (Room r : board.getRooms()) {
            if (r.isGoalRoom()) {
                System.out.println("-");
                for (TileInfo t : r.getPackingOrder()) {
                    System.out.println(t.getX() + " - " + t.getY());
                }
            }
        }
    }

    @Test
    void tileToTargetsDistancesTest() {
        Level level = TestUtils.getLevel(Path.of("TIPEex.8xv"), 0);
        BoardStyle style = TestUtils.getStyle(Path.of("isekai/isekai.style"));

        Board board = new MutableBoard(level);

        board.removeStateCrates(level.getInitialState());
        board.initForSolver();
        style.print(board, level.getPlayerX(), level.getPlayerY());

        for (int y = 0; y < board.getHeight(); y++) {
            for (int x = 0; x < board.getWidth(); x++) {
                //System.out.printf("(%d,%d) : %s%n", x, y, Arrays.toString(map.getAt(x, y).getTargets()));
                System.out.printf("%d", board.getAt(x, y).getNearestTarget().distance());
            }
            System.out.println();
        }
    }
}
