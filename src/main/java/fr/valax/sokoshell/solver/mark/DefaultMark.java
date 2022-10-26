package fr.valax.sokoshell.solver.mark;

public class DefaultMark implements Mark {

    private final MarkSystem markSystem;
    private int mark;

    public DefaultMark(MarkSystem markSystem) {
        this.markSystem = markSystem;
        unmark();
    }

    @Override
    public void mark() {
        mark = markSystem.getMark();
    }

    @Override
    public void unmark() {
        mark = markSystem.getMark() - 1;
    }

    @Override
    public boolean isMarked() {
        return mark == markSystem.getMark();
    }

    @Override
    public MarkSystem getMarkSystem() {
        return markSystem;
    }
}
