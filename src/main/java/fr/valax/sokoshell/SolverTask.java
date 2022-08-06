package fr.valax.sokoshell;

import fr.valax.sokoshell.solver.*;
import fr.valax.sokoshell.utils.Utils;

import java.nio.file.Path;
import java.util.Map;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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
    protected ScheduledFuture<?> trackerFuture;

    private final Map<String, Object> params;
    private final List<Level> levels;
    private int currentLevel = -1;

    private final List<TaskListener> listeners;

    private final String pack;
    private final String level;

    private final int taskIndex;
    private final long requestedAt;
    private long startedAt = -1;
    private long finishedAt = -1;
    private volatile TaskStatus taskStatus = TaskStatus.PENDING;

    private List<Solution> solutions;

    public SolverTask(Solver solver, Map<String, Object> params, List<Level> levels, String pack, String level) {
        this.solver = solver;
        this.params = params;
        this.levels = Objects.requireNonNull(levels);
        this.pack = pack;
        this.level = level;

        listeners = new ArrayList<>();

        requestedAt = System.currentTimeMillis();
        taskIndex = index++;
    }

    public synchronized void start() {
        if (taskStatus == TaskStatus.PENDING) {
            changeStatus(TaskStatus.RUNNING);

            if (solver instanceof Trackable t) {
                tracker = getTracker();
                t.setTacker(tracker);

                trackerFuture = SCHEDULED_EXECUTOR.scheduleWithFixedDelay(
                        () -> tracker.updateStatistics(t),
                        5, 1000, TimeUnit.MILLISECONDS);
            } else {
                trackerFuture = null;
            }

            SOKOSHELL_EXECUTOR.submit(this::solve);
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

    protected void solve() {
        startedAt = System.currentTimeMillis();

        try {
            List<Solution> solutions = new ArrayList<>();

            boolean noChange = false;

            for (currentLevel = 0; currentLevel < levels.size(); currentLevel++) {
                Level level = levels.get(currentLevel);
                SolverParameters parameters = new SolverParameters(solver.getSolverType(), level, params);

                Solution solution = solver.solve(parameters);
                level.addSolution(solution);
                solutions.add(solution);

                if (taskStatus != TaskStatus.RUNNING) {
                    noChange = true;
                    break;
                }
            }

            if (!noChange) {
                changeStatus(TaskStatus.FINISHED);
            }

            this.solutions = solutions;
        } catch (Throwable e) {
            Utils.append(e, Path.of("errors"));
            e.printStackTrace();
            changeStatus(TaskStatus.ERROR);
        } finally {
            if (trackerFuture != null) {
                trackerFuture.cancel(false);
            }

            finishedAt = System.currentTimeMillis();
        }
    }

    public synchronized void stop() {
        if (taskStatus == TaskStatus.RUNNING) {
            changeStatus(TaskStatus.STOPPED);

            solver.stop();

            if (trackerFuture != null) {
                trackerFuture.cancel(false);
            }
        } else if (taskStatus == TaskStatus.PENDING) {
            changeStatus(TaskStatus.CANCELED);
        }
    }

    private synchronized void changeStatus(TaskStatus newStatus) {
        if (taskStatus == newStatus) {
            return;
        }

        TaskStatus old = this.taskStatus;
        taskStatus = newStatus;

        for (TaskListener listener : listeners) {
            listener.statusChanged(this, old, newStatus);
        }
    }

    public void addListener(TaskListener listener) {
        listeners.add(listener);
    }

    public void removeListener(TaskListener listener) {
        listeners.remove(listener);
    }

    public TaskListener[] getListeners() {
        return listeners.toArray(new TaskListener[0]);
    }

    public Solver getSolver() {
        return solver;
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

    public int getCurrentLevel() {
        return currentLevel;
    }

    public List<Level> getLevels() {
        return Collections.unmodifiableList(levels);
    }

    public int size() {
        return levels.size();
    }

    public List<Solution> getSolutions() {
        if (solutions == null) {
            return null;
        } else {
            return Collections.unmodifiableList(solutions);
        }
    }
}
