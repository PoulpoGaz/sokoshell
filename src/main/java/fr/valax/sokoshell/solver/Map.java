package fr.valax.sokoshell.solver;

import fr.valax.interval.IntWrapper;
import fr.valax.sokoshell.solver.mark.AbstractMarkSystem;
import fr.valax.sokoshell.solver.mark.MarkSystem;

import java.util.function.Consumer;

/**
 * Represents the Sokoban map.<br />
 * This is essentially a 2D-array of {@link TileInfo}, the indices being the y and x coordinates
 * (i.e. {@code content[y][x]} is the tile at (x;y)).<br />
 * This class also implements static and dynamic analysis of the Sokoban map:
 * <ul>
 *     <li>Static</li>
 *     <ul>
 *         <li>Dead positions: cases that make the level unsolvable when a crate is pushed on them</li>
 *     </ul>
 *     <li>Dynamic</li>
 *     <ul>
 *         <li>Reachable cases: cases that the player can reach according to his position</li>
 *     </ul>
 * </ul>
 *
 * @author darth-mole
 * @author PoulpoGaz
 */
public class Map {

    public static final int MINIMUM_WIDTH = 5;
    public static final int MINIMUM_HEIGHT = 5;

    private final TileInfo[][] content;
    private final int width;
    private final int height;

    private final MarkSystem markSystem = newMarkSystem(TileInfo::unmark);
    private final MarkSystem reachableMarkSystem = newMarkSystem((t) -> t.setReachable(false));

    /**
     * Must be used only for {@link #topLeftReachablePosition(int, int, int, int)}
     */
    private final IntWrapper topX = new IntWrapper();
    private final IntWrapper topY = new IntWrapper();

    /**
     * Creates a Map with the specified width, height and tiles
     *
     * @param content a rectangular matrix of size width * height. The first index is for the rows
     *                and the second for the columns
     * @param width map width
     * @param height map height
     */
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

    /**
     * Creates a copy of other
     *
     * @param other the map to copy
     */
    public Map(Map other) {
        this.width = other.width;
        this.height = other.height;
        this.content = new TileInfo[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                this.content[y][x] = other.content[y][x].copyTo(this);
            }
        }
    }

    /**
     * Puts the crates of the given state in the content array.
     *
     * @param state The state with the crates
     */
    public void addStateCrates(State state) {
        for (int i : state.cratesIndices()) {
            getAt(i).addCrate();
        }
    }

    /**
     * Puts the crates of the given state in the content array.
     * If a crate is outside the map, it doesn't throw an {@link IndexOutOfBoundsException}
     *
     * @param state The state with the crates
     */
    public void safeAddStateCrates(State state) {
        for (int i : state.cratesIndices()) {
            TileInfo info = safeGetAt(i);

            if (info != null) {
                info.addCrate();
            }
        }
    }

    /**
     * Removes the crates of the given state from the content array.
     *
     * @param state The state with the crates
     */
    public void removeStateCrates(State state) {
        for (int i : state.cratesIndices()) {
            getAt(i).removeCrate();
        }
    }

    /**
     * Removes the crates of the given state from the content array.
     * If a crate is outside the map, it doesn't throw an {@link IndexOutOfBoundsException}
     *
     * @param state The state with the crates
     */
    public void safeRemoveStateCrates(State state) {
        for (int i : state.cratesIndices()) {
            TileInfo info = safeGetAt(i);

            if (info != null) {
                info.removeCrate();
            }
        }
    }


    /**
     * Apply the consumer on every tile info
     *
     * @param consumer the consumer to apply
     */
    public void forEach(Consumer<TileInfo> consumer) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                consumer.accept(content[y][x]);
            }
        }
    }

    /**
     * Copy the other map in this map. Mark systems are not copied
     *
     * @param map the map to copy
     */
    public void set(Map map) {
        forEach((tile) -> {
            TileInfo other = map.getAt(tile.getX(), tile.getY());
            tile.set(other);
        });
    }


    // ************
    // * ANALYSIS *
    // ************

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
        forEach(tile -> tile.setDeadTile(true));

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
        for (Direction d : Direction.VALUES) {
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
                findNonDeadCases(getAt(nextX, nextY), d.negate());
            }
        }
    }

    // * DYNAMIC *

    /**
     * Find reachable tiles
     * @param playerPos
     */
    protected void findReachableCases(int playerPos) {
        reachableMarkSystem.unmarkAll();
        findReachableCases_aux(getAt(playerPos));
    }

    private void findReachableCases_aux(TileInfo tile) {
        tile.setReachable(true);
        for (Direction d : Direction.VALUES) {
            TileInfo adjacent = tile.adjacent(d);

            // the second part of the condition avoids to check already processed cases
            if (!adjacent.isSolid() && !adjacent.isReachable()) {
                findReachableCases_aux(adjacent);
            }
        }
    }

    /**
     * This method compute the top left reachable position of the player of pushing a crate
     * at (crateToMoveX, crateToMoveY) to (destX, destY). It is used to calculate the position
     * of the player in a {@link State}.
     * This is also an example of use of {@link MarkSystem}
     *
     * @return the top left reachable position after pushing the crate
     * @see MarkSystem
     * @see fr.valax.sokoshell.solver.mark.Mark
     */
    protected int topLeftReachablePosition(int crateToMoveX, int crateToMoveY, int destX, int destY) {
        // temporary move the crate
        getAt(crateToMoveX, crateToMoveY).removeCrate();
        getAt(destX, destY).addCrate();

        topX.set(width);
        topY.set(height);

        markSystem.unmarkAll();
        topLeftReachablePosition_aux(getAt(crateToMoveX, crateToMoveY));

        // undo
        getAt(crateToMoveX, crateToMoveY).addCrate();
        getAt(destX, destY).removeCrate();

        return topY.get() * width + topX.get();
    }

    private void topLeftReachablePosition_aux(TileInfo tile) {
        if (tile.getY() < topY.get() || (tile.getY() == topY.get() && tile.getX() < topX.get())) {
            topX.set(tile.getX());
            topY.set(tile.getY());
        }

        tile.mark();
        for (Direction d : Direction.VALUES) {
            TileInfo adjacent = tile.adjacent(d);

            if (!adjacent.isSolid() && !adjacent.isMarked()) {
                topLeftReachablePosition_aux(adjacent);
            }
        }
    }


    // *********************
    // * GETTERS / SETTERS *
    // *********************


    /**
     * Returns the width of the map
     *
     * @return the width of the map
     */
    public int getWidth() { return width; }

    /**
     * Returns the height of the map
     *
     * @return the height of the map
     */
    public int getHeight() { return height; }

    /**
     * Convert an index to a position on the x-axis
     *
     * @param index the index to convert
     * @return the converted position
     */
    public int getX(int index) { return index % width; }

    /**
     * Convert an index to a position on the y-axis
     *
     * @param index the index to convert
     * @return the converted position
     */
    public int getY(int index) { return index / width; }

    /**
     * Returns the {@link TileInfo} at the specific index
     *
     * @param index the index of the {@link TileInfo}
     * @return the TileInfo at the specific index
     * @throws IndexOutOfBoundsException if the index lead to a position outside the map
     * @see #getX(int)
     * @see #getY(int)
     * @see #safeGetAt(int)
     */
    public TileInfo getAt(int index) {
        return content[getY(index)][getX(index)];
    }

    /**
     * Returns the {@link TileInfo} at the specific index
     *
     * @param index the index of the {@link TileInfo}
     * @return the TileInfo at the specific index or {@code null}
     * if the index represent a position outside the map
     * @see #getX(int)
     * @see #getY(int)
     */
    public TileInfo safeGetAt(int index) {
        int x = getX(index);
        int y = getY(index);

        if (caseExists(x, y)) {
            return getAt(x, y);
        } else {
            return null;
        }
    }

    /**
     * Returns the {@link TileInfo} at the specific position
     *
     * @param x x the of the tile
     * @param y y the of the tile
     * @return the TileInfo at the specific coordinate
     * @throws IndexOutOfBoundsException if the position is outside the map
     * @see #safeGetAt(int, int)
     */
    public TileInfo getAt(int x, int y) {
        return content[y][x];
    }

    /**
     * Returns the {@link TileInfo} at the specific position
     *
     * @param x x the of the tile
     * @param y y the of the tile
     * @return the TileInfo at the specific index or {@code null}
     * if the index represent a position outside the map
     * @see #getX(int)
     * @see #getY(int)
     */
    public TileInfo safeGetAt(int x, int y) {
        if (caseExists(x, y)) {
            return getAt(x, y);
        } else {
            return null;
        }
    }

    /**
     * Returns the tile next to the tile at (x, y) according to dir
     */
    public TileInfo safeGetAt(int x, int y, Direction dir) {
        int x2 = x + dir.dirX();
        int y2 = y + dir.dirY();

        return safeGetAt(x2, y2);
    }

    /**
     * Set at tile at the specified index. The index will be converted to
     * cartesian coordinate with {@link #getX(int)} and {@link  #getY(int)}
     *
     * @param index index in the map
     * @param tile the new tile
     * @throws IndexOutOfBoundsException if the index lead to a position outside the map
     */
    public void setAt(int index, Tile tile) { content[getY(index)][getX(index)].setTile(tile); }

    /**
     * Set at tile at (x, y)
     *
     * @param x x position in the map
     * @param y y position in the map
     * @throws IndexOutOfBoundsException if the position is outside the map
     */
    public void setAt(int x, int y, Tile tile) {
        content[y][x].setTile(tile);
    }

    /**
     * Tells whether the case at (x,y) exists or not (i.e. if the case is in the map)
     * 
     * @param x x-coordinate
     * @param y y-coordinate
     * @return {@code true} if the case exists, {@code false} otherwise
     */
    public boolean caseExists(int x, int y) {
        return (0 <= x && x < width) && (0 <= y && y < height);
    }

    /**
     * Same than caseExists(x, y) but with an index
     * 
     * @param index index of the case
     * @return {@code true} if the case exists, {@code false} otherwise
     * @see #caseExists(int, int) 
     */
    public boolean caseExists(int index) {
        return caseExists(getX(index), getY(index));
    }

    /**
     * Tells whether the tile at the given coordinates is empty or not.
     * 
     * @param x x coordinate of the case
     * @param y y coordinate of the case
     * @return {@code true} if empty, {@code false} otherwise
     */
    public boolean isTileEmpty(int x, int y) {
        TileInfo t = getAt(x, y);
        return !t.isSolid();
    }

    /**
     * Checks if the map is solved (i.e. all the crates are on a target).<br />
     * <strong>The crates MUST have been put on the map for this function to work as expected.</strong>
     * 
     * @return {@code true} if the map is completed, false otherwise
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
     * 
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

    /**
     * Returns a {@link MarkSystem} that can be used to avoid checking twice  a tile
     *
     * @return a mark system
     * @see MarkSystem
     */
    public MarkSystem getMarkSystem() {
        return markSystem;
    }

    /**
     * Returns the {@link MarkSystem} used by the {@link #findReachableCases(int)} algorithm
     *
     * @return the reachable mark system
     * @see MarkSystem
     */
    public MarkSystem getReachableMarkSystem() {
        return reachableMarkSystem;
    }

    /**
     * Creates a {@link MarkSystem} that apply the specified reset consumer to every
     * {@link TileInfo} that are in this {@link Map}.
     *
     * @param reset the reset function
     * @return a new MarkSystem
     * @see MarkSystem
     * @see fr.valax.sokoshell.solver.mark.Mark
     */
    private MarkSystem newMarkSystem(Consumer<TileInfo> reset) {
        return new AbstractMarkSystem() {
            @Override
            public void reset() {
                mark = 0;
                forEach(reset);
            }
        };
    }
}
