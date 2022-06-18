package fr.valax.sokoshell.solver;

/**
 * A tracker is an object used by {@link Trackable} to get solver statistics
 */
public interface Tracker {

    /**
     * It is called once at the end of research.
     * @return the statistics
     */
    SolverStatistics getStatistics();
}
