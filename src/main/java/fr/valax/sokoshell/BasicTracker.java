package fr.valax.sokoshell;

import fr.valax.sokoshell.solver.SolverStatistics;
import fr.valax.sokoshell.solver.Trackable;
import fr.valax.sokoshell.solver.Tracker;

import java.util.ArrayList;
import java.util.List;

public class BasicTracker implements Tracker {

    private final List<SolverStatistics.InstantStatistic> stats = new ArrayList<>();

    @Override
    public void updateStatistics(Trackable trackable) {
        long time = System.currentTimeMillis();

        int state = trackable.nStateExplored();
        int queue = trackable.currentQueueSize();

        // check if research has started
        if (state < 0 || queue < 0) {
            return;
        }

        stats.add(new SolverStatistics.InstantStatistic(time, state, queue));
    }

    @Override
    public void reset() {
        stats.clear();
    }

    @Override
    public SolverStatistics getStatistics(Trackable trackable) {
        long end = trackable.timeEnded();
        stats.add(new SolverStatistics.InstantStatistic(end, trackable.nStateExplored(), trackable.currentQueueSize()));

        SolverStatistics statistics = new SolverStatistics();
        statistics.setTimeStarted(trackable.timeStarted());
        statistics.setTimeEnded(trackable.timeEnded());
        statistics.setStatistics(stats);

        return statistics;
    }
}