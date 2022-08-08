package fr.valax.sokoshell;

import fr.valax.sokoshell.solver.SolverStatistics;
import fr.valax.sokoshell.solver.Trackable;
import fr.valax.sokoshell.solver.Tracker;

import java.util.ArrayList;
import java.util.List;

public class BasicTracker implements Tracker {

    private final List<SolverStatistics.InstantStatistic> stats = new ArrayList<>();

    private boolean end = false;

    @Override
    public synchronized void updateStatistics(Trackable trackable) {
        if (end) {
            return;
        }

        long time = System.currentTimeMillis();

        int state = trackable.nStateExplored();
        int queue = trackable.currentQueueSize();

        stats.add(new SolverStatistics.InstantStatistic(time, state, queue));
    }

    @Override
    public synchronized void reset() {
        end = false;
        stats.clear();
    }

    @Override
    public synchronized SolverStatistics getStatistics(Trackable trackable) {
        this.end = true;

        long end = trackable.timeEnded();
        add(end, trackable.nStateExplored(), trackable.currentQueueSize());

        stats.add(0, new SolverStatistics.InstantStatistic(trackable.timeStarted(), 0, 0));

        SolverStatistics statistics = new SolverStatistics();
        statistics.setTimeStarted(trackable.timeStarted());
        statistics.setTimeEnded(trackable.timeEnded());
        statistics.setStatistics(stats);

        return statistics;
    }

    private void add(long time, int state, int queue) {
        if (!stats.isEmpty()) {
            SolverStatistics.InstantStatistic last = stats.get(stats.size() - 1);

            if (last.nodeExplored() == state && last.queueSize() == queue && Math.abs(last.time() - time) <= 5) {
                return;
            }
        }

        stats.add(new SolverStatistics.InstantStatistic(time, state, queue));
    }
}