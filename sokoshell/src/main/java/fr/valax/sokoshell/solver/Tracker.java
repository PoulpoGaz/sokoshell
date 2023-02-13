package fr.valax.sokoshell.solver;

import fr.valax.sokoshell.DefaultTracker;

/**
 * A tracker is an object that watch a {@link Trackable} and gather solver statistics
 * @see DefaultTracker
 * @see Trackable
 */
public interface Tracker {

    /**
     * The name of the parameter
     * @see SolverParameters
     */
    String TRACKER_PARAM = "tracker";

    /**
     * Get data from a {@link Trackable}
     * @param trackable a trackable from which we get data
     * @see Trackable
     */
    void updateStatistics(Trackable trackable);

    /**
     * Clear all previously gathered statistics
     */
    void reset();

    /**
     * Build a {@link ISolverStatistics} object. It uses the Trackable to get the last data.
     * It is called once at the end of research.
     * @param trackable a trackable from which we get data
     * @return solver statistics
     * @see ISolverStatistics
     */
    ISolverStatistics getStatistics(Trackable trackable);
}
