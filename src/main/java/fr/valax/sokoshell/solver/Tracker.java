package fr.valax.sokoshell.solver;

/**
 * A tracker is an object used by {@link Trackable} to get solver statistics
 */
public interface Tracker {

    String TRACKER_PARAM = "tracker";

    void updateStatistics(Trackable trackable);

    /**
     * It is called once at the end of research.
     * @return the statistics
     */
    SolverStatistics getStatistics(Trackable trackable);
}
