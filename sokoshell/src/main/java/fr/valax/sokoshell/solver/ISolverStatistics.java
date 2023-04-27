package fr.valax.sokoshell.solver;

import fr.valax.sokoshell.utils.PrettyTable;
import fr.valax.sokoshell.utils.Utils;

import java.io.PrintStream;
import java.io.Serializable;

/**
 * An object that contains various statistics about a solution, including
 * time start and end, number of node explored and queue size at a specific instant
 */
public interface ISolverStatistics extends Serializable {

    /**
     * Returns the time in millis when the solver was started
     *
     * @return the time in millis when the solver was started
     */
    long timeStarted();

    /**
     * Returns the time in millis when the solver stopped running
     *
     * @return the time in millis when the solver stopped running
     */
    long timeEnded();

    /**
     * Returns the time used by the solver to solve a level
     *
     * @return the run time in millis
     */
    default long runTime() {
        return timeEnded() - timeStarted();
    }

    /**
     * Returns the total number of state explored by the solver.
     * If the solver doesn't use State or the {@link Tracker}
     * doesn't compute this property, implementations can return
     * a negative number
     *
     * @return total number of state explored
     */
    int totalStateExplored();

    /**
     * @return number of state explored per seconds or -1
     */
    long stateExploredPerSeconds();

    /**
     * @return average queue size or -1
     */
    int averageQueueSize();

    /**
     * @return lower bound or -1
     */
    int lowerBound();

    /**
     * Print statistics to out.
     *
     * @param out standard output stream
     * @param err error output stream
     * @return an optional table containing statistics
     */
    default PrettyTable printStatistics(PrintStream out, PrintStream err) {
        out.printf("Started at %s. Finished at %s. Run time: %s%n",
                Utils.formatDate(timeStarted()),
                Utils.formatDate(timeEnded()),
                Utils.prettyDate(runTime()));

        return null;
    }

    /**
     * Basic implementation of {@link ISolverStatistics} then just
     * save time started and time ended
     */
    record Basic(long timeStarted, long timeEnded) implements ISolverStatistics {

        @Override
        public int totalStateExplored() {
            return -1;
        }

        @Override
        public long stateExploredPerSeconds() {
            return -1;
        }

        @Override
        public int averageQueueSize() {
            return -1;
        }

        @Override
        public int lowerBound() {
            return -1;
        }
    }
}
