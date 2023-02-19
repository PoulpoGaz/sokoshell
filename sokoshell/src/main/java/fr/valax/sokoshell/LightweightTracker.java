package fr.valax.sokoshell;

import fr.valax.sokoshell.solver.ISolverStatistics;
import fr.valax.sokoshell.solver.Trackable;
import fr.valax.sokoshell.solver.Tracker;
import fr.valax.sokoshell.utils.PrettyTable;

import java.io.PrintStream;

/**
 * A tracker that produce lightweight results
 */
public class LightweightTracker implements Tracker {

    // see BasicTracker$SolverStatistics#averageQueueSize()
    private double area;
    private long lastQueueSize = -1;
    private long lastTime = -1;

    @Override
    public void updateStatistics(Trackable trackable) {
        if (lastTime < 0) {
            lastTime = System.currentTimeMillis();
            lastQueueSize = trackable.currentQueueSize();
        } else {
            long currentQueueSize = trackable.currentQueueSize();
            long currentTime = System.currentTimeMillis();

            if (lastTime - currentTime >= 1) {
                area += (currentTime - lastTime) * (currentQueueSize + lastQueueSize) / 2d;
            }

            lastQueueSize = currentQueueSize;
            lastTime = currentTime;
        }
    }

    @Override
    public void reset() {
        lastQueueSize = -1;
        lastTime = -1;
        area = 0;
    }

    @Override
    public ISolverStatistics getStatistics(Trackable trackable) {
        updateStatistics(trackable);

        long runTime = trackable.timeEnded() - trackable.timeStarted();
        long nodePerSeconds = 1000L * trackable.nStateExplored() / runTime;

        return new Statistics(trackable.timeStarted(), trackable.timeEnded(),
                trackable.nStateExplored(), nodePerSeconds, (int) (area / runTime),
                trackable.lowerBound());
    }

    protected record Statistics(long timeStarted, long timeEnded,
                                int totalStateExplored, long nodeExploredPerSeconds,
                                int averageQueueSize, int lowerBound) implements ISolverStatistics {

        @Override
        public PrettyTable printStatistics(PrintStream out, PrintStream err) {
            ISolverStatistics.super.printStatistics(out, err);

            out.printf("Total state explored: %d%n", totalStateExplored);
            out.printf("Average queue size: %d%n", averageQueueSize());
            out.printf("Node explored per seconds: %d%n",  nodeExploredPerSeconds());

            return null;
        }
    }
}
