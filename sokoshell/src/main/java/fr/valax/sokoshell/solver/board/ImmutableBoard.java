package fr.valax.sokoshell.solver.board;

import fr.valax.sokoshell.solver.State;
import fr.valax.sokoshell.solver.board.mark.MarkSystem;
import fr.valax.sokoshell.solver.board.tiles.ImmutableTileInfo;
import fr.valax.sokoshell.solver.board.tiles.Tile;
import fr.valax.sokoshell.solver.board.tiles.TileInfo;

import java.util.List;
import java.util.function.Consumer;

public class ImmutableBoard extends GenericBoard {

    public ImmutableBoard(Tile[][] content, int width, int height) {
        super(width, height);

        this.content = new ImmutableTileInfo[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                this.content[y][x] = new ImmutableTileInfo(this, content[y][x], x, y);
            }
        }
    }

    public ImmutableBoard(Board other) {
        super(other.getWidth(), other.getHeight());

        this.content = new ImmutableTileInfo[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                this.content[y][x] = new ImmutableTileInfo(other.getAt(x, y));
            }
        }
    }

    // GETTERS //

    @Override
    public int getTargetCount() {
        return 0;
    }

    @Override
    public List<Tunnel> getTunnels() {
        return null;
    }

    @Override
    public List<Room> getRooms() {
        return null;
    }

    @Override
    public boolean isGoalRoomLevel() {
        return false;
    }

    @Override
    public MarkSystem getMarkSystem() {
        return null;
    }

    @Override
    public MarkSystem getReachableMarkSystem() {
        return null;
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
}
