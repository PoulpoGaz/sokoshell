package fr.valax.sokoshell;

import java.util.*;

public class TaskList {

    private final List<SolverTask> finished;
    private SolverTask runningTask;
    private final LinkedList<SolverTask> pending;

    public TaskList() {
        finished = new ArrayList<>();
        runningTask = null;
        pending = new LinkedList<>();
    }

    public synchronized void offerTask(SolverTask task) {
        offerTask(task, pending.size());
    }

    /**
     *
     * @param task
     * @param index 0 to add to the top, nPendingTask() to add to the end
     *              out of bounds index will add to the end
     */
    public synchronized void offerTask(SolverTask task, int index) {
        if (task.getTaskStatus() == TaskStatus.PENDING) {
            if (runningTask == null) {
                runningTask = task;
                task.start();
            } else if (index >= pending.size() || index < 0) {
                pending.offer(task);
            } else {
                pending.add(index, task);
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
        while (!pending.isEmpty()) {
            SolverTask p = pending.getLast();
            p.stop();
        }

        if (runningTask != null) {
            runningTask.stop();
        }
    }

    public synchronized void move(SolverTask task, int position) {
        move(task, position, false);
    }

    public synchronized void swap(SolverTask task, int position) {
        move(task, position, true);
    }

    private synchronized void move(SolverTask task, int position, boolean swap) {
        if (!pending.contains(task)) {
            throw new IllegalArgumentException("Task isn't pending or not owned by this TaskList");
        }

        if (position < 0) {
            position = 0;
        } else if (position >= pending.size()) {
            position = pending.size() - 1;
        }

        int i = pending.indexOf(task);

        if (i != position) {
            if (swap) {
                pending.set(i, pending.get(position));
                pending.set(position, task);
            } else {
                pending.remove(i);
                pending.add(i, task);
            }
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

    public int nTask() {
        return finished.size() + (runningTask == null ? 0 : 1) + pending.size();
    }

    public int nPendingTask() {
        return pending.size();
    }
}
