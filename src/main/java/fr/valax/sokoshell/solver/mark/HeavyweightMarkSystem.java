package fr.valax.sokoshell.solver.mark;

import java.util.ArrayList;
import java.util.List;

public class HeavyweightMarkSystem extends AbstractMarkSystem {

    protected final List<Mark> marks;

    public HeavyweightMarkSystem() {
        marks = new ArrayList<>();
    }

    @Override
    public Mark newMark() {
        Mark m = super.newMark();
        marks.add(m);

        return m;
    }

    @Override
    public void reset() {
        mark = 0;

        for (Mark m : marks) {
            m.unmark();
        }
    }
}
