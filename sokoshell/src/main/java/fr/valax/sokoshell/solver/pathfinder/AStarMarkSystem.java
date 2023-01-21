package fr.valax.sokoshell.solver.pathfinder;

import fr.valax.sokoshell.solver.mark.Mark;
import fr.valax.sokoshell.solver.mark.MarkSystem;

/**
 * A mark is visited, if it is equal to the global mark.
 * A mark is marked, if it is equal to the global mark minus one.
 */
public class AStarMarkSystem implements MarkSystem {

    private int mark = 0;
    private final AStarMark[] marks;

    public AStarMarkSystem(int capacity) {
        marks = new AStarMark[capacity];

        for (int i = 0; i < capacity; i++) {
            marks[i] = new AStarMark();
        }
    }

    @Override
    public Mark newMark() {
        throw new UnsupportedOperationException();
    }

    /**
     * Unmark and <strong>un-visit</strong> all mark
     */
    @Override
    public void unmarkAll() {
        mark += 2;
    }

    public void mark(int i) {
        marks[i].mark();
    }

    public void setVisited(int i) {
        marks[i].setVisited();
    }

    public boolean isMarked(int i) {
        return marks[i].isMarked();
    }

    public boolean isVisited(int i) {
        return marks[i].isVisited();
    }

    @Override
    public void reset() {
        mark = 0;

        for (AStarMark mark : marks) {
            mark.unmark();
        }
    }

    @Override
    public int getMark() {
        return 0;
    }

    private class AStarMark implements Mark {

        private int mark = AStarMarkSystem.this.mark - 2;

        @Override
        public void mark() {
            mark = AStarMarkSystem.this.mark - 1;
        }

        public void markVisited() {
            mark = AStarMarkSystem.this.mark - 1;
        }

        public void setVisited() {
            mark = AStarMarkSystem.this.mark;
        }

        @Override
        public void unmark() {
            mark = AStarMarkSystem.this.mark - 2;
        }

        @Override
        public boolean isMarked() {
            return mark == AStarMarkSystem.this.mark - 1;
        }

        public boolean isVisited() {
            return mark == AStarMarkSystem.this.mark;
        }

        @Override
        public MarkSystem getMarkSystem() {
            return AStarMarkSystem.this;
        }
    }
}
