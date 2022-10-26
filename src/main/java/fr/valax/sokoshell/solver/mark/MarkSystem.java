package fr.valax.sokoshell.solver.mark;

/**
 * <p>
 *     A MarkSystem is used by dfs/bfs/others algorithm to avoid checking twice an object.
 *     With a MarkSystem, you don't need to unmark all visited objects
 *     {@link Mark} associated with this system can be created using {@link #newMark()}.
 * </p>
 * <h2>How it works</h2>
 * <p>
 *     A mark have a value, the same for a MarkSystem. A mark is marked if it value is equals
 *     to the value of the MarkSystem. So, to unmark all mark, you just have to increase
 *     the MarkSystem's value.
 * </p>
 * @see Mark
 * @author PoulpoGaz
 */
public interface MarkSystem {

    /**
     * Create a new mark associated with this MarkSystem.
     * The mark is by default unmarked
     * @return a new mark
     */
    Mark newMark();

    /**
     * Unmark all marks
     */
    void unmarkAll();

    /**
     * Set the 'selected' mark to 0 and unmark all Mark
     */
    void reset();

    /**
     * @return the selected mark.
     */
    int getMark();
}
