package fr.valax.sokoshell.solver.tasks;

import fr.valax.sokoshell.solver.SolverStatistics;
import fr.valax.sokoshell.solver.Trackable;
import fr.valax.sokoshell.solver.Tracker;

import java.util.ArrayList;
import java.util.List;

public class BasicTracker implements Tracker {

    private final List<Integer> state = new ArrayList<>();
    private final List<Integer> queueSize = new ArrayList<>();

    @Override
    public void updateStatistics(Trackable trackable) {
        int state = trackable.nStateExplored();
        int queue = trackable.currentQueueSize();

        // check if research has started
        if (state < 0 || queue < 0) {
            return;
        }

        this.state.add(state);
        queueSize.add(queue);
    }

    @Override
    public void reset() {
        state.clear();
        queueSize.clear();
    }

    @Override
    public SolverStatistics getStatistics(Trackable trackable) {
        SolverStatistics statistics = new SolverStatistics();
        statistics.setTimeStarted(trackable.timeStarted());
        statistics.setTimeEnded(trackable.timeEnded());
        statistics.setStateExplored(state);
        statistics.setQueueSize(queueSize);
        statistics.setTimeUnit(1);

        statistics.add(trackable.nStateExplored(), trackable.currentQueueSize());

        return statistics;
    }
}