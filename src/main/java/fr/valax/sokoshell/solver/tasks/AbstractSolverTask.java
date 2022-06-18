package fr.valax.sokoshell.solver.tasks;

import fr.valax.sokoshell.solver.Solver;
import fr.valax.sokoshell.solver.Trackable;
import fr.valax.sokoshell.solver.Tracker;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static fr.valax.sokoshell.utils.Utils.SCHEDULED_EXECUTOR;
import static fr.valax.sokoshell.utils.Utils.SOKOSHELL_EXECUTOR;

public abstract class AbstractSolverTask<T> implements ISolverTask<T> {

    protected final Solver solver;

    protected Tracker tracker;
    protected CompletableFuture<T> solverFuture;
    protected ScheduledFuture<?> trackerFuture;

    public AbstractSolverTask(Solver solver) {
        this.solver = solver;
    }

    protected abstract Tracker getTracker();

    @Override
    public void start() {
        if (solver instanceof Trackable t) {
            tracker = getTracker();
            t.setTacker(tracker);

            trackerFuture = SCHEDULED_EXECUTOR.scheduleWithFixedDelay(
                    () -> tracker.updateStatistics(t),
                    5, 1000, TimeUnit.MILLISECONDS);
        } else {
            trackerFuture = null;
        }

        solverFuture = CompletableFuture.supplyAsync(this::solve, SOKOSHELL_EXECUTOR);
    }

    protected abstract T solve();

    @Override
    public void stop() {
        if (solverFuture != null) {
            solverFuture.cancel(false);
        }

        if (trackerFuture != null) {
            trackerFuture.cancel(false);
        }
    }

    @Override
    public CompletableFuture<Void> onEnd(Consumer<T> consumer) {
        return solverFuture.thenAccept(consumer);
    }
}
