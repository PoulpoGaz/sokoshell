package fr.valax.sokoshell.solver;

import fr.valax.interval.IntWrapper;

import java.util.function.Consumer;

public class Map {

    public static final int MINIMUM_WIDTH = 5;
    public static final int MINIMUM_HEIGHT = 5;

    private final TileInfo[][] content;
    private final int width;
    private final int height;

    private int topLeftReachablePositionX = -1;
    private int topLeftReachablePositionY = -1;

    public Map(Tile[][] content, int width, int height) {
        this.width = width;
        this.height = height;

        this.content = new TileInfo[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                this.content[y][x] = new TileInfo(this, content[y][x], x, y);
            }
        }

        State.initZobristValues(width * height);
    }

    public Map(Map other) {
        this.width = other.width;
        this.height = other.height;
        this.content = new TileInfo[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                this.content[y][x] = new TileInfo(this, other.content[y][x]);
            }
        }
    }

    /**
     * Puts the crates of the given state in the content array.
     * @param state The state with the crates
     */
    public void addStateCrates(State state) {
        for (int i : state.cratesIndices()) {
            getAt(i).addCrate();
        }
    }

    /**
     * Removes the crates of the given state from the content array.
     * @param state The state with the crates
     */
    public void removeStateCrates(State state) {
        for (int i : state.cratesIndices()) {
            getAt(i).removeCrate();
        }
    }


    public void forEach(Consumer<TileInfo> consumer) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                consumer.accept(content[y][x]);
            }
        }
    }

    public void set(Map map) {
        forEach((tile) -> {
            TileInfo other = map.getAt(tile.getX(), tile.getY());
            tile.set(other);
        });
    }


    // ************
    // * ANALYSIS *
    // ************


    /**
     * Reset reachable flag
     */
    public void resetDynamicInformation() {
        forEach(TileInfo::resetDynamicInformation);
    }


    // * STATIC *

    /**
     * Detects the dead positions of a level. Dead positions are cases that make the level unsolvable
     * when a crate is put on them.
     * After this function has been called, to check if a given crate at (x,y) is a dead position,
     * you can use {@link TileInfo#isDeadTile()} to check in constant time.
     * The map <strong>MUST</strong> have <strong>NO CRATES</strong> for this function to work.
     */
    public void computeDeadTiles() {
        // reset
        forEach((tile) -> tile.setDeadTile(true));

        // loop
        forEach((tile) -> {
            if (!tile.isDeadTile()) {
                return;
            }

            if (tile.anyCrate()) {
                tile.setDeadTile(true);
                return;
            }

            if (!tile.isTarget()) {
                return;
            }

            findNonDeadCases(tile, null);
        });
    }
    /**
     * Discovers all the reachable cases from (x, y) to find dead positions, as described
     * <a href="www.sokobano.de/wiki/index.php?title=How_to_detect_deadlocks#Detecting_simple_deadlocks">here</a>
     */
    private void findNonDeadCases(TileInfo tile, Direction lastDir) {
        tile.setDeadTile(false);
        for (Direction d : Direction.values()) {
            if (d == lastDir) { // do not go backwards
                continue;
            }

            final int nextX = tile.getX() + d.dirX();
            final int nextY = tile.getY() + d.dirY();
            final int nextNextX = nextX + d.dirX();
            final int nextNextY = nextY + d.dirY();

            if (getAt(nextX, nextY).isDeadTile()    // avoids to check already processed cases
                    && isTileEmpty(nextX, nextY)
                    && isTileEmpty(nextNextX, nextNextY)) {
                findNonDeadCases(getAt(nextX, nextY), Direction.opposite(d));
            }
        }
    }

    // * DYNAMIC *

    /**
     * Find reachable tiles
     * @param playerPos
     * @param reset
     */
    protected void findReachableCases(int playerPos, boolean reset) {
        if (reset) {
            forEach((t) -> t.setReachable(false));
        }

        topLeftReachablePositionX = width;
        topLeftReachablePositionY = height;

        findReachableCases_aux(getAt(playerPos));
    }

    private void findReachableCases_aux(TileInfo tile) {
        tile.setReachable(true);
        for (Direction d : Direction.values()) {
            TileInfo adjacent = tile.adjacent(d);

            // the second part of the condition avoids to check already processed cases
            if (!adjacent.isSolid() && !adjacent.isReachable()) {
                if (adjacent.getY() < topLeftReachablePositionY || (adjacent.getY() == topLeftReachablePositionY && adjacent.getX() < topLeftReachablePositionX)) {
                    topLeftReachablePositionX = adjacent.getX();
                    topLeftReachablePositionY = adjacent.getY();
                }

                findReachableCases_aux(adjacent);
            }
        }
    }

    /**
     *
     * @return the new top left reachable position after pushing the crate
     */
    protected int topLeftReachablePosition(int crateToMoveX, int crateToMoveY, int destX, int destY) {
        if (topLeftReachablePositionX < 0 || topLeftReachablePositionY < 0) {
            throw new IllegalStateException();
        }

        getAt(crateToMoveX, crateToMoveY).removeCrate();
        getAt(destX, destY).addCrate();


        IntWrapper topX = new IntWrapper(topLeftReachablePositionX);
        IntWrapper topY = new IntWrapper(topLeftReachablePositionY);

        topLeftReachablePosition_aux(getAt(crateToMoveX, crateToMoveY), topX, topY);
        forEach(TileInfo::unmark);

        // undo
        getAt(crateToMoveX, crateToMoveY).addCrate();
        getAt(destX, destY).removeCrate();

        return topY.get() * width + topX.get();
    }

    private void topLeftReachablePosition_aux(TileInfo tile, IntWrapper topX, IntWrapper topY) {
        if (tile.getY() < topY.get() || (tile.getY() == topY.get() && tile.getX() < topX.get())) {
            topX.set(tile.getX());
            topY.set(tile.getY());
        }

        tile.mark();
        for (Direction d : Direction.values()) {
            TileInfo adjacent = tile.adjacent(d);

            // the second part of the condition avoids to check already processed cases
            if (!adjacent.isSolid() && !adjacent.isMarked() && !adjacent.isReachable()) {
                topLeftReachablePosition_aux(adjacent, topX, topY);
            }
        }
    }


    // *********************
    // * GETTERS / SETTERS *
    // *********************


    public int getWidth() { return width; }

    public int getHeight() { return height; }

    public int getX(int index) { return index % width; }

    public int getY(int index) { return index / width; }

    public TileInfo getAt(int index) {
        return content[getY(index)][getX(index)];
    }

    public TileInfo getAt(int x, int y) {
        return content[y][x];
    }

    public TileInfo safeGetAt(int x, int y) {
        if (caseExists(x, y)) {
            return getAt(x, y);
        } else {
            return null;
        }
    }

    public TileInfo safeGetAt(int x, int y, Direction dir) {
        int x2 = x + dir.dirX();
        int y2 = y + dir.dirY();

        return safeGetAt(x2, y2);
    }

    public void setAt(int index, Tile tile) { content[getY(index)][getX(index)].setTile(tile); }

    public void setAt(int x, int y, Tile tile) {
        content[y][x].setTile(tile);
    }

    /**
     * Tells whether the case at (x,y) exists or not (i.e. if the case is in the map)
     * @param x x-coordinate
     * @param y y-coordinate
     * @return true if the case exists, false otherwise
     */
    public boolean caseExists(int x, int y) {
        return (0 <= x && x < width) && (0 <= y && y < height);
    }

    /**
     * Same than caseExists(x, y) but with an index
     * @param index index of the case
     * @return true if the case exists, false otherwise
     */
    public boolean caseExists(int index) {
        return caseExists(getX(index), getY(index));
    }

    /**
     * Tells whether the tile at the given coordinates is empty or not.
     * @param x x coordinate of the case
     * @param y y coordinate of the case
     * @return true if empty, false otherwise
     */
    public boolean isTileEmpty(int x, int y) {
        TileInfo t = getAt(x, y);
        return !t.isSolid();
    }

    /**
     * Checks if the map is solved (i.e. all the crates are on a target)
     * /!\ The crates MUST have been put on the map for this function to work as expected.
     * @return true if the map is completed, false otherwise
     */
    public boolean isCompletedWith(State s) {
        for (int i : s.cratesIndices()) {
            if (!getAt(i).isCrateOnTarget()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the map is completed (i.e. all the crates are on a target)
     * @return true if completed, false otherwise
     */
    public boolean isCompleted() {
        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++) {
                if (getAt(x, y).isCrate()) {
                    return false;
                }
            }
        }
        return true;
    }

    public int getTopLeftReachablePositionX() {
        return topLeftReachablePositionX;
    }

    public int getTopLeftReachablePositionY() {
        return topLeftReachablePositionY;
    }
}
