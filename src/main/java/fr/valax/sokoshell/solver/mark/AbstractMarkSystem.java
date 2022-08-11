package fr.valax.sokoshell.solver.mark;

public abstract class AbstractMarkSystem implements MarkSystem {

    protected int mark;

    @Override
    public Mark newMark() {
        return new DefaultMark(this);
    }

    @Override
    public void unmarkAll() {
        mark++;

        if (mark == 0) {
            reset();
        }
    }

    @Override
    public abstract void reset();

    @Override
    public int getMark() {
        return mark;
    }
}
