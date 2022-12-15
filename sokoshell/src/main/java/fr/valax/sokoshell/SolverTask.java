package fr.valax.sokoshell;

import fr.valax.sokoshell.solver.*;
import fr.valax.sokoshell.utils.Utils;

import java.nio.file.Path;
import java.util.Map;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static fr.valax.sokoshell.utils.Utils.SCHEDULED_EXECUTOR;
import static fr.valax.sokoshell.utils.Utils.SOKOSHELL_EXECUTOR;

/**
 * A solver task is used to solve a collection of level in another thread.
 * If the solver implements {@link Trackable}, a {@link Tracker} is added
 * to the solver.
 */
public class SolverTask {

    private static final AtomicInteger index = new AtomicInteger(1);

    protected final Solver solver;

    protected Tracker tracker;
    protected ScheduledFuture<?> trackerFuture;

    private final List<SolverParameter> params;
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

    private List<SolverReport> solverReports;

    public SolverTask(Solver solver, List<SolverParameter> params, List<Level> levels, String pack, String level) {
        this.solver = solver;
        this.params = params;
        this.levels = Objects.requireNonNull(levels);
        this.pack = pack;
        this.level = level;

        listeners = new ArrayList<>();

        requestedAt = System.currentTimeMillis();
        taskIndex = index.getAndIncrement();
    }

    /**
     * Starts the task asynchronously
     */
    public void start() {
        start(true);
    }

    /**
     * Starts the task, it will run asynchronously or not depending on the parameter
     *
     * @param asynchronously should the task run asynchronously?
     */
    public void start(boolean asynchronously) {
        boolean solve = false;
        synchronized (this) {
            if (taskStatus == TaskStatus.PENDING) {
                changeStatus(TaskStatus.RUNNING);

                if (solver instanceof Trackable t) {
                    tracker = getTracker();
                    t.setTacker(tracker);

                    trackerFuture = SCHEDULED_EXECUTOR.scheduleWithFixedDelay(
                            () -> tracker.updateStatistics(t),
                            1000, 1000, TimeUnit.MILLISECONDS);
                } else {
                    trackerFuture = null;
                }

                if (asynchronously) {
                    SOKOSHELL_EXECUTOR.submit(this::solve);
                } else {
                    solve = true;
                }
            }
        }

        if (solve) { // outside synchronized block to allow stop to work
            solve();
        }
    }

    protected Tracker getTracker() {
        /*Object object = params.get(Tracker.TRACKER_PARAM);

        if (object instanceof Tracker tracker) {
            return tracker;
        } else {*/
            return new BasicTracker();
        //}
    }

    protected void solve() {
        startedAt = System.currentTimeMillis();

        try {
            List<SolverReport> solverReports = new ArrayList<>();

            boolean noChange = false;

            for (currentLevel = 0; currentLevel < levels.size(); currentLevel++) {
                Level level = levels.get(currentLevel);
                SolverParameters parameters = new SolverParameters(solver.getSolverType(), level, params);

                SolverReport solverReport = solver.solve(parameters);
                level.addSolverReport(solverReport);
                solverReports.add(solverReport);

                if (taskStatus != TaskStatus.RUNNING) {
                    noChange = true;
                    break;
                }
            }
            this.solverReports = solverReports;

            if (!noChange) {
                changeStatus(TaskStatus.FINISHED);
            }
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

    /**
     * Stop this tasks. If it's running, the solver is stopped and the status change to {@link TaskStatus#STOPPED}.
     * IF it's pending, the status change to {@link TaskStatus#CANCELED}. Otherwise, it does nothing
     */
    public synchronized void stop() {
        if (taskStatus == TaskStatus.RUNNING) {
            solver.stop();

            if (trackerFuture != null) {
                trackerFuture.cancel(false);
            }

            // wait until solver properly finish his task.
            while (solver.isRunning()) {
                Thread.onSpinWait();

                if (Thread.interrupted()) {
                    throw new RuntimeException("It seems the solver couldn't be cancelled properly.");
                }
            }

            changeStatus(TaskStatus.STOPPED);
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

    /**
     * Adds a listener to this task
     *
     * @param listener the listener to add
     */
    public void addListener(TaskListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    /**
     * Removes a listener
     *
     * @param listener the listener to remove
     */
    public void removeListener(TaskListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    /**
     * Returns all listeners attached to this task
     *
     * @return all listeners attached to this task
     */
    public TaskListener[] getListeners() {
        synchronized (listeners) {
            return listeners.toArray(new TaskListener[0]);
        }
    }

    /**
     * Returns the solver used by this task
     *
     * @return the solver used by this task
     */
    public Solver getSolver() {
        return solver;
    }

    /**
     * The index of this task. It is independent of the {@link TaskList}.
     *
     * @return The index of this task
     */
    public int getTaskIndex() {
        return taskIndex;
    }

    /**
     * Returns which packs the user requested to solve in a form of a {@link fr.valax.sokoshell.utils.GlobIterator}
     *
     * @return which packs the user requested to solve in a form of a glob
     * @see fr.valax.sokoshell.utils.GlobIterator
     */
    public String getPack() {
        return pack;
    }

    /**
     * Returns which levels the user requested to solve in a form of a {@link fr.valax.interval.Set}
     *
     * @return which pack the user requested to solve in a form of a {@link fr.valax.interval.Set}
     * @see fr.valax.interval.Set
     */
    public String getLevel() {
        return level;
    }

    /**
     * Returns the time in millis at that the user requested this task
     *
     * @return the time in millis at that the user requested this task
     */
    public long getRequestedAt() {
        return requestedAt;
    }

    /**
     * Returns the time in millis at the task was started i.e. when his status changed to {@link TaskStatus#RUNNING}
     *
     * @return the time in millis at the task was started or -1
     */
    public long getStartedAt() {
        return startedAt;
    }

    /**
     * Returns the time in millis at the task finished i.e. when his status changed to {@link TaskStatus#STOPPED},
     * {@link TaskStatus#ERROR} or {@link TaskStatus#FINISHED}
     *
     * @return the time in millis at the task finished or -1
     */
    public long getFinishedAt() {
        return finishedAt;
    }

    /**
     * Returns The status of this task
     *
     * @return The status of this task
     */
    public TaskStatus getTaskStatus() {
        return taskStatus;
    }

    /**
     * Returns the index of the level that is currently being solved
     *
     * @return the index of the level that is currently being solved
     */
    public int getCurrentLevel() {
        return currentLevel;
    }

    /**
     * Returns an unmodifiable list of all levels that will be solved
     *
     * @return an unmodifiable list of all levels that will be solved
     */
    public List<Level> getLevels() {
        return Collections.unmodifiableList(levels);
    }

    /**
     * Returns the number of level to solve
     *
     * @return the number of level to solve
     */
    public int size() {
        return levels.size();
    }

    /**
     * Returns all {@link SolverReport} that were produced at the end of task
     *
     * @return all {@link SolverReport} that were produced at the end of task or {@code null}
     */
    public List<SolverReport> getSolutions() {
        if (solverReports == null) {
            return null;
        } else {
            return Collections.unmodifiableList(solverReports);
        }
    }
}
