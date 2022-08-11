package fr.valax.sokoshell.solver.mark;

/**
 * @see MarkSystem
 * @author PoulpoGaz
 */
public interface Mark {

    /**
     * Marks the object. After this method is called, {@link #isMarked()}
     * will return {@code true}
     */
    void mark();

    /**
     * Un-marks the object. After this method is called, {@link #isMarked()}
     * will return {@code false}
     */
    void unmark();

    /**
     * Mark or not the object. After this method is called, {@link #isMarked()}
     * will return {@code marked}
     */
    void setMarked(boolean marked);

    /**
     * @return true is the object is marked
     */
    boolean isMarked();

    /**
     * @return the {@link MarkSystem} associated with this mark
     */
    MarkSystem getMarkSystem();
}
