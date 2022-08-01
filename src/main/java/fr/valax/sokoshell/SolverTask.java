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

    private static int index = 1;

    protected final Solver solver;

    protected Tracker tracker;
    protected CompletableFuture<List<Solution>> solverFuture;
    protected ScheduledFuture<?> trackerFuture;

    private final Map<String, Object> params;
    private final List<Level> levels;

    private final String pack;
    private final String level;

    private final int taskIndex;
    private final long requestedAt;
    private long startedAt = -1;
    private long finishedAt = -1;
    private volatile TaskStatus taskStatus = TaskStatus.PENDING;

    private List<Solution> solutions;

    private List<Consumer<List<Solution>>> onEnd;

    public SolverTask(Solver solver, Map<String, Object> params, List<Level> levels, String pack, String level) {
        this.solver = solver;
        this.params = params;
        this.levels = Objects.requireNonNull(levels);
        this.pack = pack;
        this.level = level;

        requestedAt = System.currentTimeMillis();
        taskIndex = index++;
    }

    public void start() {
        if (taskStatus == TaskStatus.PENDING) {
            taskStatus = TaskStatus.RUNNING;

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

            if (onEnd != null) {
                for (Consumer<List<Solution>> onEnd : this.onEnd) {
                    onEnd(onEnd);
                }
            }
        }
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
                //SokoShellHelper.INSTANCE.tryPrintln("Solving level n°" + (level.getIndex() + 1), 5, TimeUnit.MILLISECONDS);

                SolverParameters parameters = new SolverParameters(solver.getSolverType(), level, params);

                Solution solution = solver.solve(parameters);

                solutions.add(solution);
                if (solution.isStopped()) {
                    break;
                }
            }

            if (taskStatus == TaskStatus.RUNNING) {
                taskStatus = TaskStatus.FINISHED;
            }

            this.solutions = solutions;

            return solutions;
        } catch (Throwable e) {
            e.printStackTrace();
            taskStatus = TaskStatus.ERROR;
            return null;
        } finally {
            if (trackerFuture != null) {
                trackerFuture.cancel(false);
            }

            finishedAt = System.currentTimeMillis();
        }
    }

    public void stop() {
        if (taskStatus == TaskStatus.RUNNING) {
            taskStatus = TaskStatus.STOPPED;

            solver.stop();

            if (trackerFuture != null) {
                trackerFuture.cancel(false);
            }
        } else {
            taskStatus = TaskStatus.CANCELED;
        }
    }

    public CompletableFuture<Void> onEnd(Consumer<List<Solution>> consumer) {
        if (taskStatus == TaskStatus.PENDING) {
            if (onEnd == null) {
                onEnd = new ArrayList<>();
            }
            onEnd.add(consumer);

            return null;
        } else if (taskStatus == TaskStatus.RUNNING) {
            return solverFuture.thenAccept(consumer);
        } else {
            return CompletableFuture.runAsync(() -> consumer.accept(solutions));
        }
    }

    /**
     * Cancel a pending task
     */
    public void cancel() {
        if (taskStatus == TaskStatus.PENDING) {
            taskStatus = TaskStatus.CANCELED;

            if (onEnd != null) {
                for (Consumer<List<Solution>> onEnd : this.onEnd) {
                    onEnd(onEnd);
                }
            }
        }
    }

    public int getTaskIndex() {
        return taskIndex;
    }

    public String getPack() {
        return pack;
    }

    public String getLevel() {
        return level;
    }

    public long getRequestedAt() {
        return requestedAt;
    }

    public long getStartedAt() {
        return startedAt;
    }

    public long getFinishedAt() {
        return finishedAt;
    }

    public TaskStatus getTaskStatus() {
        return taskStatus;
    }

    public List<Solution> getSolutions() {
        return solutions;
    }
}
