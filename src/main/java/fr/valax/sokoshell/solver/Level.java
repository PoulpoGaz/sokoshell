package fr.valax.sokoshell.solver;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author darth-mole
 * @author PoulpoGaz
 */
public class Level {

    private final Map map;
    private final int playerPos;

    private Solution solution;

    public Level(Map map, int playerPos) {
        this.map = map;
        this.playerPos = playerPos;
    }

    public Map getMap() {
        return map;
    }

    public int getWidth() {
        return map.getWidth();
    }

    public int getHeight() {
        return map.getHeight();
    }

    public int getPlayerX() {
        return playerPos % getWidth();
    }

    public int getPlayerY() {
        return playerPos / getWidth();
    }

    public State getInitialState() {
        List<Integer> cratesIndices = new ArrayList<>();

        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++) {
                if (map.getAt(x, y).isCrate()) {
                    cratesIndices.add(y * getWidth() + x);
                }
            }
        }

        int[] cratesIndicesArray = new int[cratesIndices.size()];
        for (int i = 0; i < cratesIndices.size(); i++) {
            cratesIndicesArray[i] = cratesIndices.get(i);
        }

        return new State(playerPos, cratesIndicesArray, null);
    }

    public Solution getSolution() {
        return solution;
    }

    public void setSolution(Solution solution) {
        this.solution = solution;
    }

    public static class Builder {

        private int playerPos;

        private Tile[][] map = new Tile[0][0];
        private int width;
        private int height;

        public Level build() {
            Objects.requireNonNull(map);

            Map m = new Map(map, width, height);

            return new Level(m, playerPos);
        }

        public int getPlayerPos() {
            return playerPos;
        }

        public void setPlayerPos(int x, int y) {
            this.playerPos = y * width + x;
        }

        private void resizeIfNeeded(int minWidth, int minHeight) {
            setSize(Math.max(minWidth, width),
                    Math.max(minHeight, height));
        }

        public void setSize(int newWidth, int newHeight) {
            if (newWidth == width && newHeight == height) {
                return;
            }

            Tile[][] newMap = new Tile[newHeight][newWidth];

            int yMax = Math.min(newHeight, height);
            int xMax = Math.min(newWidth, width);
            for (int y = 0; y < yMax; y++) {
                System.arraycopy(map[y], 0, newMap[y], 0, xMax);

                for (int x = xMax; x < newWidth; x++) {
                    newMap[y][x] = Tile.WALL;
                }
            }

            map = newMap;

            width = newWidth;
            height = newHeight;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            setSize(width, height);
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            setSize(width, height);
        }

        public void set(Tile tile, int x, int y) {
            resizeIfNeeded(x, y);
            map[y][x] = tile;
        }

        public Tile get(int x, int y) {
            if (x < 0 || x >= width || y < 0 || y >= height) {
                return null;
            }

            return map[y][x];
        }
    }
}
