package fr.valax.sokoshell.solver.board.mark;

public class FixedSizeMarkSystem implements MarkSystem {

    protected final FMark[] marks;
    protected int mark;

    public FixedSizeMarkSystem(int capacity) {
        marks = new FMark[capacity];
        for (int i = 0; i < capacity; i++) {
            marks[i] = new FMark();
        }
    }

    public void mark(int i) {
        marks[i].mark();
    }

    public boolean isMarked(int i) {
        return marks[i].isMarked();
    }

    @Override
    public Mark newMark() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void unmarkAll() {
        mark++;

        if (mark == 0) {
            reset();
        }
    }

    @Override
    public void reset() {
        mark = 0;

        for (FMark mark : marks) {
            mark.unmark();
        }
    }

    @Override
    public int getMark() {
        return mark;
    }

    private class FMark implements Mark {

        private int mark = 0;

        @Override
        public void mark() {
            mark = FixedSizeMarkSystem.this.mark;
        }

        @Override
        public void unmark() {
            mark = FixedSizeMarkSystem.this.mark - 1;
        }

        @Override
        public boolean isMarked() {
            return mark == FixedSizeMarkSystem.this.mark;
        }

        @Override
        public MarkSystem getMarkSystem() {
            return FixedSizeMarkSystem.this;
        }
    }
}
