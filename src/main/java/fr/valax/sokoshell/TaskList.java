package fr.valax.sokoshell;

import fr.valax.sokoshell.solver.Level;
import fr.valax.sokoshell.solver.Solution;

import java.util.*;

public class TaskList {

    private final List<SolverTask> finished;
    private SolverTask runningTask;
    private final Queue<SolverTask> pending;

    public TaskList() {
        finished = new ArrayList<>();
        runningTask = null;
        pending = new ArrayDeque<>();
    }

    public synchronized void offerTask(SolverTask task) {
        if (task.getTaskStatus() == TaskStatus.PENDING) {
            if (runningTask == null) {
                runningTask = task;
                task.start();
            } else {
                pending.offer(task);
            }

            task.addListener(this::onStatusChanged);
        } else if (task.getTaskStatus() == TaskStatus.RUNNING) {
            if (runningTask != null) {
                throw new IllegalStateException("A task is already running");
            } else {
                runningTask = task;
            }

            task.addListener(this::onStatusChanged);
        } else {
            finished.add(task);
        }
    }

    private synchronized void onStatusChanged(SolverTask task, TaskStatus old, TaskStatus newStatus) {
        if (newStatus != TaskStatus.PENDING && newStatus != TaskStatus.RUNNING) {
            List<Solution> solutions = task.getSolutions();

            if (solutions != null) {
                for (Solution solution : solutions) {
                    Level level = solution.getParameters().getLevel();
                    level.addSolution(solution);
                }
            }
            finished.add(task);

            if (runningTask == task) {
                runningTask = pending.poll();

                if (runningTask != null) {
                    runningTask.start();
                } else {
                    // TODO:
                    // System.out.println("Finished all tasks! See results with 'list solution --task-index INDEX'");
                }
            } else {
                pending.remove(task);
            }
        }
    }

    public synchronized SolverTask getTask(int index) {
        for (SolverTask finished : finished) {
            if (finished.getTaskIndex() == index) {
                return finished;
            }
        }

        if (runningTask != null && runningTask.getTaskIndex() == index) {
            return runningTask;
        }

        for (SolverTask finished : pending) {
            if (finished.getTaskIndex() == index) {
                return finished;
            }
        }

        return null;
    }

    public synchronized void stopAll() {
        for (SolverTask p : pending) {
            p.stop();
        }

        if (runningTask != null) {
            runningTask.stop();
        }
    }

    public boolean isRunning() {
        return runningTask != null;
    }

    public synchronized List<SolverTask> getTasks() {
        List<SolverTask> tasks = new ArrayList<>(finished.size() + pending.size() + (runningTask == null ? 0 : 1));
        tasks.addAll(finished);
        if (runningTask != null) {
            tasks.add(runningTask);
        }
        tasks.addAll(pending);

        return tasks;
    }

    public List<SolverTask> getFinished() {
        return Collections.unmodifiableList(finished);
    }

    public SolverTask getRunningTask() {
        return runningTask;
    }

    public Collection<SolverTask> getPendingTask() {
        return Collections.unmodifiableCollection(pending);
    }
}
