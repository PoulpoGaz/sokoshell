package fr.valax.sokoshell.solver;

/**
 * A solver that implements this interface allows
 * other objects to get information about the current
 * research.
 *
 * Methods are by default non-synchronized and <strong>should not</strong>
 * modify the state of the solver.
 * Implementations are free to violate the first term of the contract
 * <strong>(not the second)</strong>, but they must say it.
 */
public interface Trackable extends Solver {

    int nStateExplored();

    int currentQueueSize();

    long timeStarted();

    long timeEnded();

    void setTacker(Tracker tracker);

    Tracker getTracker();
}
