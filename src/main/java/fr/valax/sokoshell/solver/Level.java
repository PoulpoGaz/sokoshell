package fr.valax.sokoshell.solver;

import fr.valax.sokoshell.utils.BuilderException;

import java.util.Objects;

/**
 * @author darth-mole
 * @author PoulpoGaz
 */
public class Level {

    private Map map;
    private int playerPos;

    public Level(Map map, int playerPos) {
        this.map = map;
        this.playerPos = playerPos;
    }


    public Map getMap() {
        return map;
    }


    public static class Builder {

        private int playerPos;

        private Tile[][] map = new Tile[0][0];
        private int width;
        private int height;

        public Level build() {
            Objects.requireNonNull(map);

            if (playerPos < 0 || playerPos > map.length) {
                throw new BuilderException("Player isn't in the map");
            }

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
