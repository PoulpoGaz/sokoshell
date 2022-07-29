package fr.valax.sokoshell.solver;

import fr.poulpogaz.json.JsonException;
import fr.poulpogaz.json.JsonPrettyWriter;
import fr.valax.sokoshell.utils.BuilderException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author darth-mole
 * @author PoulpoGaz
 */
public class Level {

    // package private
    Pack pack;
    private final Map map;
    private final int playerPos;
    private final int index;

    private final List<Solution> solutions;

    public Level(Map map, int playerPos, int index) {
        this.map = map;
        this.playerPos = playerPos;
        this.index = index;

        solutions = new ArrayList<>();
    }

    public void writeSolutions(JsonPrettyWriter jpw) throws JsonException, IOException {
        for (Solution solution : solutions) {

            if (solution.isSolved() || solution.hasNoSolution() || solution.getStatus() == SolverStatus.TIMEOUT) {
                jpw.beginObject();
                solution.writeSolution(jpw);
                jpw.endObject();
            }
        }
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
                if (map.getAt(x, y).anyCrate()) {
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

    public Solution getLastSolution() {
        if (solutions.isEmpty()) {
            return null;
        }

        return solutions.get(solutions.size() - 1);
    }

    public Solution getSolution(int index) {
        if (index < 0 || index >= solutions.size()) {
            return null;
        } else {
            return solutions.get(index);
        }
    }

    public List<Solution> getSolutions() {
        return solutions;
    }

    public void addSolution(Solution solution) {
        solutions.add(solution);
    }

    public boolean hasSolution() {
        return solutions.size() > 0;
    }

    public int getIndex() {
        return index;
    }

    public Pack getPack() {
        return pack;
    }

    public static class Builder {

        private int playerX = -1;
        private int playerY = -1;

        private Tile[][] map = new Tile[0][0];
        private int width;
        private int height;
        private int index;

        public Level build() {
            if (map == null) {
                throw new BuilderException("Map is null");
            }

            if (playerX < 0 || playerX >= width) {
                throw new BuilderException("Player x out of bounds");
            }

            if (playerY < 0 || playerY >= height) {
                throw new BuilderException("Player y out of bounds");
            }

            if (map[playerY][playerX].isSolid()) {
                throw new BuilderException("Player is on a solid tile");
            }

            Map m = new Map(map, width, height);

            return new Level(m, playerY * width + playerX, index);
        }

        public int getPlayerX() {
            return playerX;
        }

        public int getPlayerY() {
            return playerY;
        }

        public void setPlayerPos(int x, int y) {
            this.playerX = x;
            this.playerY = y;
        }

        public void setPlayerX(int playerX) {
            this.playerX = playerX;
        }

        public void setPlayerY(int playerY) {
            this.playerY = playerY;
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

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }
    }
}
