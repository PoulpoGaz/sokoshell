package fr.valax.sokoshell.solver.board.mark;

/**
 * Contains the basic for all mark system
 */
public abstract class AbstractMarkSystem implements MarkSystem {

    /**
     * A mark is marked if it's value is equals to this field
     */
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
