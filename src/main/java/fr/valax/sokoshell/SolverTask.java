package fr.valax.sokoshell;

import fr.valax.sokoshell.solver.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    protected final Solver solver;

    protected Tracker tracker;
    protected CompletableFuture<List<Solution>> solverFuture;
    protected ScheduledFuture<?> trackerFuture;

    private final Map<String, Object> params;
    private final List<Level> levels;

    private long requestedAt;
    private long startedAt;
    private long endedAt;


    public SolverTask(Solver solver, Map<String, Object> params, List<Level> levels) {
        this.solver = solver;
        this.params = params;
        this.levels = Objects.requireNonNull(levels);

        requestedAt = System.currentTimeMillis();
    }

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

    protected Tracker getTracker() {
        Object object = params.get(Tracker.TRACKER_PARAM);

        if (object instanceof Tracker tracker) {
            return tracker;
        } else {
            return new BasicTracker();
        }
    }

    protected List<Solution> solve() {
        startedAt = System.currentTimeMillis();

        try {
            List<Solution> solutions = new ArrayList<>(levels.size());

            for (Level level : levels) {
                SokoShellHelper.INSTANCE.tryPrintln("Solving level n°" + (level.getIndex() + 1), 5, TimeUnit.MILLISECONDS);

                SolverParameters parameters = new SolverParameters(solver.getSolverType(), level, params);

                Solution solution = solver.solve(parameters);

                solutions.add(solution);
                if (solution.isStopped()) {
                    break;
                }
            }

            return solutions;
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        } finally {
            if (trackerFuture != null) {
                trackerFuture.cancel(false);
            }

            endedAt = System.currentTimeMillis();
        }
    }

    public void stop() {
        if (solverFuture != null) {
            solver.stop();
        }

        if (trackerFuture != null) {
            trackerFuture.cancel(false);
        }
    }

    public CompletableFuture<Void> onEnd(Consumer<List<Solution>> consumer) {
        return solverFuture.thenAccept(consumer);
    }

    public long getRequestedAt() {
        return requestedAt;
    }

    public long getStartedAt() {
        return startedAt;
    }

    public long getEndedAt() {
        return endedAt;
    }
}
