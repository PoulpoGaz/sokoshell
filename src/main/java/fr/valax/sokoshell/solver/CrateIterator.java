package fr.valax.sokoshell.solver;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class CrateIterator implements Iterator<Integer> {

    private Map map;

    /**
     * The number of element we are skipping
     */
    private int skip = 0;

    /**
     * The crates we are iterating over
     */
    private int[] crates;

    private int index = 0;

    public CrateIterator() {

    }

    @Override
    public boolean hasNext() {
        return nextIndex();
    }

    @Override
    public Integer next() {
        if (!nextIndex()) {
            throw new NoSuchElementException();
        }

        return crates[index++];
    }

    /**
     * Moves to next element that is not marked and return true if it exists.
     *
     * @return true if there is another element
     */
    private boolean nextIndex() {

        if (skip > 0) {
            for (; index < crates.length; index++) {
                if (!map.getAt(crates[index]).isMarked()) {
                    break;
                }
            }
        }

        return index < crates.length;
    }

    public boolean hasPrevious() {
        return previousIndex();
    }

    public Integer previous() {
        if (!previousIndex()) {
            throw new NoSuchElementException();
        }

        return crates[index--];
    }

    /**
     * Moves to previous element that is not marked and return true if it exists.
     *
     * @return true if there is another element
     */
    private boolean previousIndex() {

        if (skip > 0) {
            for (; index >= 0; index--) {
                if (!map.getAt(crates[index]).isMarked()) {
                    break;
                }
            }
        }

        return index >= 0;
    }

    public void skipIndex(int index) {
        skipCrate(map.getAt(index));
    }

    /**
     *
     * @param crate mark the specified crate as skipped
     */
    public void skipCrate(int crate) {
        for (int i = 0; i < crates.length; i++) {
            if (crates[i] == crate) {
                skipIndex(i);
                return;
            }
        }
    }

    /**
     * For performance, this function doesn't check if the tile is in the array
     * @param tile a crate that is in the crates array
     */
    public void skipCrate(TileInfo tile) {
        if (skip <= 0) {
            for (int crate : crates) {
                map.getAt(crate).unmark();
            }

            skip = 0;
        }

        tile.mark();
        skip++;
    }

    public void unSkipIndex(int index) {
        map.getAt(index).unmark();
        skip--;
    }

    public void unSkipCrate(int crate) {
        for (int i = 0; i < crates.length; i++) {
            if (crates[i] == crate) {
                unSkipIndex(i);
                return;
            }
        }
    }

    /**
     * Setting crate reset the object. The skip field is set
     * to false. No crate will be skipped
     * @param crates the crates to iterate over
     */
    public void setCrates(int[] crates) {
        reset();
        this.crates = crates;
    }

    public void reset() {
        if (skip > 0 && this.crates != null) {
            for (int crate : this.crates) {
                map.getAt(crate).unmark();
            }
        }

        skip = 0;
        index = 0;
    }

    public Map getMap() {
        return map;
    }

    public void setMap(Map map) {
        reset();
        this.map = map;
    }
}
