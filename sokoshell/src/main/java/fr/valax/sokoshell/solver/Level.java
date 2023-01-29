package fr.valax.sokoshell.solver;

import fr.poulpogaz.json.JsonException;
import fr.poulpogaz.json.JsonPrettyWriter;
import fr.valax.sokoshell.solver.board.Board;
import fr.valax.sokoshell.solver.board.Direction;
import fr.valax.sokoshell.solver.board.ImmutableBoard;
import fr.valax.sokoshell.solver.board.tiles.Tile;
import fr.valax.sokoshell.utils.BuilderException;

import java.io.IOException;
import java.util.*;

/**
 * @author darth-mole
 * @author PoulpoGaz
 */
public class Level extends ImmutableBoard {

    // package private
    Pack pack;
    private final int playerPos;
    private final int index;

    private final List<SolverReport> solverReports;

    public Level(Tile[][] tiles, int width, int height, int playerPos, int index) {
        super(tiles, width, height);
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
                if (getAt(x, y).anyCrate()) {
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
     * Returns the last report
     *
     * @return the last report
     */
    public SolverReport getLastReport() {
        if (solverReports.isEmpty()) {
            return null;
        } else {
            return solverReports.get(solverReports.size() - 1);
        }
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
     * Returns the number of solver report
     *
     * @return the number of solver report
     */
    public int numberOfSolverReport() {
        return solverReports.size();
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

        private Tile[][] board = new Tile[0][0];
        private int width;
        private int height;
        private int index;

        /**
         * Builds and returns a {@link Level}
         *
         * @return the new {@link Level}
         * @throws BuilderException if the player is outside the board
         * r the player is on a solid tile
         */
        public Level build() {
            if (board == null) {
                throw new BuilderException("Board is null");
            }

            if (playerX < 0 || playerX >= width) {
                throw new BuilderException("Player x out of bounds");
            }

            if (playerY < 0 || playerY >= height) {
                throw new BuilderException("Player y out of bounds");
            }

            if (board[playerY][playerX].isSolid()) {
                throw new BuilderException("Player is on a solid tile");
            }

            formatLevel();

            return new Level(board, width, height, playerY * width + playerX, index);
        }

        /**
         * Format the level for the solver. Some levels aren't surrounded by wall
         * or have rooms that are inaccessible. This method removes these rooms
         * and add wall if necessary.
         */
        private void formatLevel() {
            Set<Integer> visited = new HashSet<>();

            int i = 0;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (board[y][x] != Tile.WALL && !visited.contains(i)) {
                        addWallIfNecessary(x, y, visited);
                    }

                    i++;
                }
            }

            surroundByWallIfNecessary();
        }

        private void addWallIfNecessary(int x, int y, Set<Integer> visited) {
            boolean needWall = true;

            Set<Integer> localVisited = new HashSet<>();
            Stack<Integer> toVisit = new Stack<>();
            toVisit.add(y * width + x);
            localVisited.add(toVisit.peek());

            while (!toVisit.isEmpty()) {
                int i = toVisit.pop();

                int x2 = i % width;
                int y2 = i / width;

                if (x2 == playerX && y2 == playerY) {
                    needWall = false;
                }

                for (Direction d : Direction.VALUES) {
                    int x3 = x2 + d.dirX();
                    int y3 = y2 + d.dirY();

                    if (x3 < 0 || x3 >= width || y3 < 0 || y3 >= height) {
                        continue;
                    }

                    int i3 = y3 * width + x3;

                    if (board[y3][x3] != Tile.WALL && localVisited.add(i3)) {
                        visited.add(i3);
                        toVisit.push(i3);
                    }
                }
            }

            if (needWall) {
                for (Integer i : localVisited) {
                    int x2 = i % width;
                    int y2 = i / width;

                    board[y2][x2] = Tile.WALL;
                }
            }
        }

        private void surroundByWallIfNecessary() {
            int left = 0;
            int right = 0;
            int top = 0;
            int bottom = 0;

            for (int y = 0; y < height; y++) {
                if (board[y][0] != Tile.WALL) {
                    left = 1;
                }
                if (board[y][width - 1] != Tile.WALL) {
                    right = 1;
                }
            }

            for (int x = 0; x < width; x++) {
                if (board[0][x] != Tile.WALL) {
                    top = 1;
                }
                if (board[height - 1][x] != Tile.WALL) {
                    bottom = 1;
                }
            }

            if (left == 0 && right == 0 && top == 0 && bottom == 0) {
                return;
            }

            Tile[][] newTiles = new Tile[height + top + bottom][width + right + left];

            for (int y = 0; y < height + top + bottom; y++) {
                for (int x = 0; x < width + right + left; x++) {
                    if (x >= left && y >= top && x < width + left && y < height + top) {
                        newTiles[y][x] = board[y - top][x - left];
                    } else {
                        newTiles[y][x] = Tile.WALL;
                    }
                }
            }

            board = newTiles;
            width += right + left;
            height += top + bottom;
            playerX += left;
            playerY += top;
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

            Tile[][] newBoard = new Tile[newHeight][newWidth];

            int yMax = Math.min(newHeight, height);
            int xMax = Math.min(newWidth, width);
            for (int y = 0; y < yMax; y++) {
                System.arraycopy(board[y], 0, newBoard[y], 0, xMax);

                for (int x = xMax; x < newWidth; x++) {
                    newBoard[y][x] = Tile.WALL;
                }
            }

            board = newBoard;

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
            board[y][x] = tile;
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

            return board[y][x];
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
