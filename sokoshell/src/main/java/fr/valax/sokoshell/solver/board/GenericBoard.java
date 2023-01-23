package fr.valax.sokoshell.solver.board;

import fr.valax.sokoshell.solver.State;
import fr.valax.sokoshell.solver.board.mark.MarkSystem;
import fr.valax.sokoshell.solver.board.tiles.GenericTileInfo;
import fr.valax.sokoshell.solver.board.tiles.TileInfo;

/**
 * Base class for {@link IBoard} implementations. Defines all read-only methods.
 * @param <T> The type of {@link T} to store.
 */
public abstract class GenericBoard<T extends GenericTileInfo<T, ?>> implements IBoard<T> {

    protected final int width;

    protected final int height;

    protected T[][] content;

    public GenericBoard(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @SuppressWarnings("CopyConstructorMissesField")
    public GenericBoard(IBoard<?> other) {
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
    public T getAt(int index) {
        return content[getY(index)][getX(index)];
    }

    @Override
    public T safeGetAt(int index) {
        int x = getX(index);
        int y = getY(index);

        if (caseExists(x, y)) {
            return getAt(x, y);
        } else {
            return null;
        }
    }

    @Override
    public T getAt(int x, int y) {
        return content[y][x];
    }

    @Override
    public T safeGetAt(int x, int y) {
        if (caseExists(x, y)) {
            return getAt(x, y);
        } else {
            return null;
        }
    }

    @Override
    public boolean caseExists(int x, int y) {
        return (0 <= x && x < width) && (0 <= y && y < height);
    }

    @Override
    public boolean caseExists(int index) {
        return caseExists(getX(index), getY(index));
    }

    @Override
    public boolean isTileEmpty(int x, int y) {
        T t = getAt(x, y);
        return !t.isSolid();
    }

    @Override
    public boolean isCompletedWith(State s) {
        for (int i : s.cratesIndices()) {
            if (!getAt(i).isCrateOnTarget()) {
                return false;
            }
        }
        return true;
    }

    @Override
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
}
