package fr.valax.sokoshell.solver;

/**
 * A solver that implements this interface allows
 * other objects to get information about the current
 * research.
 * <br>
 * Methods are by default non-synchronized and <strong>should not</strong>
 * modify the state of the solver.
 * Implementations are free to violate the first term of the contract
 * <strong>(not the second)</strong>, but they must indicate it.
 */
public interface Trackable extends Solver {

    /**
     * @return the number of state explored or -1
     */
    int nStateExplored();

    /**
     * Returns the size of the queue. The queue contains all
     * states that will be processed in the future. It may return
     * {@code -1} when the Solver doesn't have a queue, or it is
     * impossible to get this information .
     * @return the size of the queue or -1
     */
    int currentQueueSize();

    /**
     * @return the time in milliseconds at which the solver was started
     */
    long timeStarted();

    /**
     * @return the time in milliseconds at which the solver finished the research or was stopped
     */
    long timeEnded();

    /**
     * @return the state the solver is processing. It may return null
     */
    State currentState();

    /**
     * Set the {@link Tracker} that is tracking this trackable
     * @param tracker the tracker
     */
    void setTacker(Tracker tracker);

    /**
     * @return the tracker that is tracking this trackable
     */
    Tracker getTracker();
}
