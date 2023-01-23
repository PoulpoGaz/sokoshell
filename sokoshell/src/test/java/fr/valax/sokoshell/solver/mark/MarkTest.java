package fr.valax.sokoshell.solver.mark;

import fr.valax.sokoshell.solver.board.mark.HeavyweightMarkSystem;
import fr.valax.sokoshell.solver.board.mark.Mark;
import fr.valax.sokoshell.solver.board.mark.MarkSystem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MarkTest {

    @Test
    void markTest() {
        MarkSystem system = new HeavyweightMarkSystem();

        Mark mark1 = system.newMark();
        Mark mark2 = system.newMark();
        Mark mark3 = system.newMark();

        Assertions.assertFalse(mark1.isMarked());
        Assertions.assertFalse(mark2.isMarked());
        Assertions.assertFalse(mark3.isMarked());
        Assertions.assertEquals(system, mark1.getMarkSystem());
        Assertions.assertEquals(system, mark2.getMarkSystem());
        Assertions.assertEquals(system, mark3.getMarkSystem());

        mark1.mark();
        Assertions.assertTrue(mark1.isMarked());
        Assertions.assertFalse(mark2.isMarked());
        Assertions.assertFalse(mark3.isMarked());

        system.unmarkAll();
        Assertions.assertFalse(mark1.isMarked());
        Assertions.assertFalse(mark2.isMarked());
        Assertions.assertFalse(mark3.isMarked());

        mark1.mark();
        mark2.mark();

        Assertions.assertTrue(mark1.isMarked());
        Assertions.assertTrue(mark2.isMarked());
        Assertions.assertFalse(mark3.isMarked());

        system.reset();
        Assertions.assertFalse(mark1.isMarked());
        Assertions.assertFalse(mark2.isMarked());
        Assertions.assertFalse(mark3.isMarked());
    }
}
