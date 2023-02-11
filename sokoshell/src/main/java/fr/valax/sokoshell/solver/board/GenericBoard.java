package fr.valax.sokoshell.solver.board;

import fr.valax.sokoshell.solver.Corral;
import fr.valax.sokoshell.solver.CorralDetector;
import fr.valax.sokoshell.solver.State;
import fr.valax.sokoshell.solver.board.tiles.Tile;
import fr.valax.sokoshell.solver.board.tiles.TileInfo;

import java.util.function.Consumer;

/**
 * A {@code package-private} class meant to be use as a base class for {@link Board} implementations.
 * It defines all read-only methods, as well as a way to store the tiles. It is essentially a 2D-array of
 * {@link TileInfo}, the indices being the y and x coordinates (i.e. {@code content[y][x]} is the tile at (x;y)).
 *
 * @see Board
 * @see TileInfo
 */
public abstract class GenericBoard implements Board {

    protected final int width;

    protected final int height;

    protected TileInfo[][] content;

    public GenericBoard(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @SuppressWarnings("CopyConstructorMissesField")
    public GenericBoard(Board other) {
        this(other.getWidth(), other.getHeight());
    }

    @Override
    public int getWidth() { return width; }

    @Override
    public int getHeight() { return height; }

    @Override
    public int getY(int index) { return index / width; }

    @Override
    public int getX(int index) { return index % width; }

    @Override
    public int getIndex(int x, int y) { return y * width + x; }

    @Override
    public TileInfo getAt(int index) {
        return content[getY(index)][getX(index)];
    }

    @Override
    public TileInfo getAt(int x, int y) {
        return content[y][x];
    }


    // SETTERS: throw UnsupportedOperationException as this object is immutable //
    @Override
    public void forEach(Consumer<TileInfo> consumer) {
        throw new UnsupportedOperationException("Board is immutable");
    }

    @Override
    public void setAt(int index, Tile tile) {
        throw new UnsupportedOperationException("Board is immutable");
    }

    @Override
    public void setAt(int x, int y, Tile tile) {
        throw new UnsupportedOperationException("Board is immutable");
    }

    @Override
    public void addStateCrates(State state) {
        throw new UnsupportedOperationException("Board is immutable");
    }

    @Override
    public void removeStateCrates(State state) {
        throw new UnsupportedOperationException("Board is immutable");
    }

    @Override
    public void safeAddStateCrates(State state) {
        throw new UnsupportedOperationException("Board is immutable");
    }

    @Override
    public void safeRemoveStateCrates(State state) {
        throw new UnsupportedOperationException("Board is immutable");
    }

    // Solver-used methods: throw UnsupportedOperationException as this object is (for now) not to be used by solvers //

    @Override
    public void initForSolver() {
        throw new UnsupportedOperationException("Board is not intended for solvers");
    }

    @Override
    public void computeFloors() {
        throw new UnsupportedOperationException("Board is not intended for solvers");
    }

    @Override
    public void forEachNotWall(Consumer<TileInfo> consumer) {
        throw new UnsupportedOperationException("Board is not intended for solvers");
    }

    @Override
    public void addStateCratesAndAnalyse(State state) {
        throw new UnsupportedOperationException("Board is not intended for solvers");
    }

    @Override
    public void removeStateCratesAndReset(State state) {
        throw new UnsupportedOperationException("Board is not intended for solvers");
    }

    @Override
    public void computeDeadTiles() {
        throw new UnsupportedOperationException("Board is not intended for solvers");
    }

    @Override
    public void findTunnels() {
        throw new UnsupportedOperationException("Board is not intended for solvers");
    }

    @Override
    public void findRooms() {
        throw new UnsupportedOperationException("Board is not intended for solvers");
    }

    @Override
    public void tryComputePackingOrder() {
        throw new UnsupportedOperationException("Board is not intended for solvers");
    }

    @Override
    public void findReachableCases(int playerPos) {
        throw new UnsupportedOperationException("Board is not intended for solvers");
    }

    @Override
    public int topLeftReachablePosition(int crateToMoveX, int crateToMoveY, int destX, int destY) {
        throw new UnsupportedOperationException("Board is not intended for solvers");
    }

    @Override
    public Corral getCorral(TileInfo tile) {
        return null;
    }

    @Override
    public CorralDetector getCorralDetector() {
        return null;
    }
}
