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

    private final List<SolverReport> solverReports;

    public Level(Map map, int playerPos, int index) {
        this.map = map;
        this.playerPos = playerPos;
        this.index = index;

        solverReports = new ArrayList<>();
    }

    public void writeSolutions(JsonPrettyWriter jpw) throws JsonException, IOException {
        for (SolverReport solution : solverReports) {
            jpw.beginObject();
            solution.writeSolution(jpw);
            jpw.endObject();
        }
    }

    /**
     * Returns a copy of the map
     *
     * @return a copy of the map
     */
    public Map getMap() {
        return new Map(map);
    }

    /**
     * Returns the width of this level
     *
     * @return the width of this level
     */
    public int getWidth() {
        return map.getWidth();
    }

    /**
     * Returns the height of this level
     *
     * @return the height of this level
     */
    public int getHeight() {
        return map.getHeight();
    }

    /**
     * Returns the player position on the x-axis at the beginning
     *
     * @return the player position on the x-axis at the beginning
     */
    public int getPlayerX() {
        return playerPos % getWidth();
    }

    /**
     * Returns the player position on the y-axis at the beginning
     *
     * @return the player position on the y-axis at the beginning
     */
    public int getPlayerY() {
        return playerPos / getWidth();
    }

    /**
     * Returns the initial state i.e. a state representing the level at the beginning
     *
     * @return the initial state
     */
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

    /**
     * Returns the last solver report that is a solution
     * @return the last solver report that is a solution
     */
    public SolverReport getLastSolution() {
        if (solverReports.isEmpty()) {
            return null;
        }

        for (int i = solverReports.size() - 1; i >= 0; i--) {
            SolverReport r = solverReports.get(i);

            if (r.isSolved()) {
                return r;
            }
        }

        return null;
    }

    /**
     * Returns the solver report at the specified position
     *
     * @param index index of the report to return
     * @return the solver report at the specified position
     */
    public SolverReport getSolverReport(int index) {
        if (index < 0 || index >= solverReports.size()) {
            return null;
        } else {
            return solverReports.get(index);
        }
    }

    /**
     * Returns all solver reports
     *
     * @return all solver reports
     */
    public List<SolverReport> getSolverReports() {
        return solverReports;
    }

    /**
     * Add a solver report to this level
     *
     * @param solverReport the report to add
     * @throws IllegalArgumentException if the report isn't for this level
     */
    public void addSolverReport(SolverReport solverReport) {
        if (solverReport.getParameters().getLevel() != this) {
            throw new IllegalArgumentException("Attempting to add a report to the wrong level");
        }
        solverReports.add(solverReport);
    }

    /**
     * Returns if an attempt to solve this level was done. It doesn't mean that this level has a solution
     *
     * @return {@code true} if an attempt to solve this level was done.
     */
    public boolean hasReport() {
        return solverReports.size() > 0;
    }

    /**
     * Returns {@code true} if this level has a solution
     *
     * @return {@code true} if this level has a solution
     */
    public boolean hasSolution() {
        for (SolverReport r : solverReports) {
            if (r.isSolved()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns the index of this level in the pack
     *
     * @return the index of this level in the pack
     */
    public int getIndex() {
        return index;
    }

    /**
     * Returns the pack in which this level is
     *
     * @return the pack in which this level is
     */
    public Pack getPack() {
        return pack;
    }


    /**
     * A builder of {@link Level}
     */
    public static class Builder {

        private int playerX = -1;
        private int playerY = -1;

        private Tile[][] map = new Tile[0][0];
        private int width;
        private int height;
        private int index;

        /**
         * Builds and returns a {@link Level}
         *
         * @return the new {@link Level}
         * @throws BuilderException if the player is outside the map
         * r the player is on a solid tile
         */
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

        /**
         * Returns the player position on the x-axis
         *
         * @return the player position on the x-axis
         */
        public int getPlayerX() {
            return playerX;
        }

        /**
         * Returns the player position on the y-axis
         *
         * @return the player position on the y-axis
         */
        public int getPlayerY() {
            return playerY;
        }

        /**
         * Set the player position to (x, y)
         *
         * @param x player position on the x-axis
         * @param y player position on the y-axis
         */
        public void setPlayerPos(int x, int y) {
            this.playerX = x;
            this.playerY = y;
        }

        /**
         * Set the player position on the x-axis to x
         *
         * @param playerX the new player position on the x-axis
         */
        public void setPlayerX(int playerX) {
            this.playerX = playerX;
        }

        /**
         * Set the player position on the y-axis to x
         *
         * @param playerY the new player position on the y-axis
         */
        public void setPlayerY(int playerY) {
            this.playerY = playerY;
        }

        private void resizeIfNeeded(int minWidth, int minHeight) {
            setSize(Math.max(minWidth, width),
                    Math.max(minHeight, height));
        }

        /**
         * Resize this level to (newWidth, newHeight). If dimensions are higher than the old one,
         * new tiles are filled with WALL. For other, tiles are the same.
         *
         * @param newWidth the new width of the level
         * @param newHeight the new width of the level
         */
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

        /**
         * Returns the width of the level
         *
         * @return the width of the level
         */
        public int getWidth() {
            return width;
        }

        /**
         * Sets the width of the level
         *
         * @param width the new width of the level
         * @see #setSize(int, int)
         */
        public void setWidth(int width) {
            setSize(width, height);
        }

        /**
         * Returns the height of the level
         *
         * @return the height of the level
         */
        public int getHeight() {
            return height;
        }

        /**
         * Sets the height of the level
         *
         * @param height the new height of the level
         * @see #setSize(int, int)
         */
        public void setHeight(int height) {
            setSize(width, height);
        }

        /**
         * Set at (x, y) the tile. If (x, y) is outside the level, the level is resized
         *
         * @param tile the new tile
         * @param x x position
         * @param y y position
         */
        public void set(Tile tile, int x, int y) {
            resizeIfNeeded(x, y);
            map[y][x] = tile;
        }

        /**
         * Returns the tile at (x, y)
         * @param x x position of the tile
         * @param y y position of the tile
         * @return the tile at (x, y)
         */
        public Tile get(int x, int y) {
            if (x < 0 || x >= width || y < 0 || y >= height) {
                return null;
            }

            return map[y][x];
        }

        /**
         * Returns the index of the level
         * @return the index of the level
         */
        public int getIndex() {
            return index;
        }

        /**
         * Sets the index of the level
         * @param index the new index of the level
         */
        public void setIndex(int index) {
            this.index = index;
        }
    }
}
