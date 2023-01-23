package fr.valax.sokoshell.solver.board.mark;

import java.util.ArrayList;
import java.util.List;

/**
 * A heavyweight mark system contains a pointer to every mark associated with this system
 */
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
