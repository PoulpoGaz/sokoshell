package fr.valax.sokoshell.solver;

import fr.poulpogaz.json.JsonException;
import fr.poulpogaz.json.utils.Pair;
import fr.valax.sokoshell.readers.PackReaders;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MapTest {

    @Test
    void topLeftReachablePositionTest() throws JsonException, IOException {
        Pack pack = PackReaders.read(Path.of("levels8xv/Original.8xv"), false);

        Level level = pack.getLevel(0);
        Map map = level.getMap();

        int i = map.topLeftReachablePosition(5, 7, 5, 6);

        assertEquals(3, i % map.getWidth());
        assertEquals(4, i / map.getWidth());
    }

    @Test
    void findTunnelTest() throws JsonException, IOException {
        Set<TTunnel> tunnelsSet = new HashSet<>();
        tunnelsSet.add(new TTunnel(4, 4, 3, 5,    5, 4, 3, 6,    true));
        tunnelsSet.add(new TTunnel(5, 5, 5, 6,    5, 4, 5, 7,    false));
        tunnelsSet.add(new TTunnel(8, 4, 8, 6,    7, 4, 8, 7,    true));
        tunnelsSet.add(new TTunnel(4, 7, 4, 7,    3, 7, 5, 7,    false));
        tunnelsSet.add(new TTunnel(6, 7, 7, 7,    5, 7, 8, 7,    false));
        tunnelsSet.add(new TTunnel(5, 8, 9, 8,    5, 7, 9, 7,    true));
        tunnelsSet.add(new TTunnel(10, 7, 10, 7,  9, 7, 11, 7,   false));
        tunnelsSet.add(new TTunnel(11, 8, 11, 8,  11, 7, -1, -1, false));
        tunnelsSet.add(new TTunnel(12, 7, 13, 7,  11, 7, 14, 7,  false));

        Pack pack = PackReaders.read(Path.of("levels8xv/Original.8xv"), false);

        Level level = pack.getLevel(0);
        Map map = level.getMap();
        map.removeStateCrates(level.getInitialState());
        map.computeFloors();
        map.findTunnels();

        List<Tunnel> tunnels = map.getTunnels();

        for (Tunnel t : tunnels) {
            TileInfo s = t.getStart();
            TileInfo e = t.getEnd();
            TileInfo so = t.getStartOut();
            TileInfo eo = t.getEndOut();

            TTunnel arr1 = new TTunnel(s.getX(), s.getY(), e.getX(), e.getY());
            arr1.setOnlyPlayer(t.isPlayerOnlyTunnel());
            TTunnel arr2 = new TTunnel(e.getX(), e.getY(), s.getX(), s.getY());
            arr2.setOnlyPlayer(t.isPlayerOnlyTunnel());

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

        public TTunnel(int startX, int startY, int endX, int endY) {
            this.startX = startX;
            this.startY = startY;
            this.endX = endX;
            this.endY = endY;
        }

        private TTunnel(int startX, int startY, int endX, int endY, int startOutX, int startOutY, int endOutX, int endOutY, boolean onlyPlayer) {
            this.startX = startX;
            this.startY = startY;
            this.endX = endX;
            this.endY = endY;
            this.startOutX = startOutX;
            this.startOutY = startOutY;
            this.endOutX = endOutX;
            this.endOutY = endOutY;
            this.onlyPlayer = onlyPlayer;
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
            return onlyPlayer == tTunnel.onlyPlayer;
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
            return result;
        }

        @Override
        public String toString() {
            return "(%d; %d) - (%d; %d) --> (%d; %d) - (%d; %d). only player? %s"
                    .formatted(startOutX, startOutY, startX, startY, endX, endY, endOutX, endOutY, onlyPlayer);
        }
    }
}
