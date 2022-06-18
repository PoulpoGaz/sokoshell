package fr.valax.sokoshell.solver;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static fr.valax.sokoshell.utils.Utils.SCHEDULED_EXECUTOR;
import static fr.valax.sokoshell.utils.Utils.SOKOSHELL_EXECUTOR;

/**
 * A solver task is used to solve a sokoban in another thread.
 * If the solver implements {@link Trackable}, a {@link Tracker} is added
 * to the solver.
 */
public class SolverTask {

    private final Solver solver;
    private final SolverParameters parameters;

    private Tracker tracker;
    private CompletableFuture<Solution> solverFuture;
    private ScheduledFuture<?> trackerFuture;

    public SolverTask(Solver solver, SolverParameters parameters) {
        this.solver = solver;
        this.parameters = parameters;
    }

    public void start() {

        if (solver instanceof Trackable t) {
            Object object = parameters.get(Tracker.TRACKER_PARAM);

            if (object instanceof Tracker tracker) {
                this.tracker = tracker;
            } else {
                tracker = new MyTracker();
            }
            t.setTacker(tracker);

            trackerFuture = SCHEDULED_EXECUTOR.scheduleWithFixedDelay(
                    () -> tracker.updateStatistics(t),
                    0, 1, TimeUnit.SECONDS);
        } else {
            trackerFuture = null;
        }

        solverFuture = CompletableFuture.supplyAsync(this::solve, SOKOSHELL_EXECUTOR);
    }

    public void stop() {
        if (solverFuture != null) {
            solver.stop();
        }

        if (trackerFuture != null) {
            trackerFuture.cancel(false);
        }
    }

    public CompletableFuture<Void> onEnd(Consumer<Solution> consumer) {
        return solverFuture.thenAccept(consumer);
    }

    private Solution solve() {
        try {
            return solver.solve(parameters);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        } finally {
            if (trackerFuture != null) {
                trackerFuture.cancel(false);
            }
        }
    }

    public CompletableFuture<Solution> getSolverFuture() {
        return solverFuture;
    }

    public Solver getSolver() {
        return solver;
    }

    public SolverParameters getParameters() {
        return parameters;
    }

    private static class MyTracker implements Tracker {

        private final List<Integer> statePerSecond = new ArrayList<>();
        private final List<Integer> queueSizePerSecond = new ArrayList<>();

        @Override
        public void updateStatistics(Trackable trackable) {
            int state = trackable.nStateExplored();
            int queue = trackable.currentQueueSize();

            // check if research has started
            if (state < 0 || queue < 0) {
                return;
            }

            statePerSecond.add(state);
            queueSizePerSecond.add(queue);
        }

        @Override
        public SolverStatistics getStatistics(Trackable trackable) {
            SolverStatistics statistics = new SolverStatistics();
            statistics.setTimeStarted(trackable.timeStarted());
            statistics.setTimeEnded(trackable.timeEnded());
            statistics.setNodeExploredByTimeUnit(statePerSecond);
            statistics.setQueueSizeByTimeUnit(queueSizePerSecond);
            statistics.setTimeUnit(1);

            statistics.add(trackable.nStateExplored(), trackable.currentQueueSize());

            return statistics;
        }
    }
}
