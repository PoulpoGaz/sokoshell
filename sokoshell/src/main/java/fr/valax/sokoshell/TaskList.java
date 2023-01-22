package fr.valax.sokoshell;

import java.util.*;

/**
 * A task list manages a queue of {@link SolverTask}. It holds finished, pending tasks
 * and the running tasks. It is responsible for automatically starting a pending task if the
 * running task has finished
 */
public class TaskList {

    private final List<SolverTask> finished;
    private SolverTask runningTask;
    private final LinkedList<SolverTask> pending;

    public TaskList() {
        finished = new ArrayList<>();
        runningTask = null;
        pending = new LinkedList<>();
    }

    /**
     * Adds the specified task to the end of the queue
     *
     * @param task the task to add
     */
    public synchronized void offerTask(SolverTask task) {
        offerTask(task, pending.size());
    }

    /**
     * Adds a task at the specified index.
     *
     * @param task the task to add
     * @param index 0 to add to the top, nPendingTask() to add to the end
     *              Out of range index will add the task to the end
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
                }
            } else {
                pending.remove(task);
            }
        }
    }

    /**
     * Returns the task which has the specified index
     *
     * @param index task index
     * @return the task which has the specified index
     */
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

    /**
     * Cancel all pending tasks and stop the running task
     */
    public synchronized void stopAll() {
        while (!pending.isEmpty()) {
            SolverTask p = pending.getLast();
            p.stop();
        }

        if (runningTask != null) {
            runningTask.stop();
        }
    }

    /**
     * Move at task at the index position in the pending queue. Example:
     * <pre>
     *     pending tasks: #1 #2 #3 #4
     *     move(#2, 3) will result in
     *     pending tasks: #1 #3 #4 #2
     * </pre>
     *
     * @param task the task to move
     * @param position the new position of the task
     * @throws IllegalArgumentException if the task isn't pending or not owned by the TaskList
     */
    public synchronized void move(SolverTask task, int position) {
        move(task, position, false);
    }

    /**
     * Swap the specified task with the task at the index position in the pending queue. Example:
     * <pre>
     *     pending tasks: #1 #2 #3 #4
     *     swap(#2, 3) will result in
     *     pending tasks: #1 #4 #3 #2
     * </pre>
     *
     * @param task the task to move
     * @param position the new position of the task
     * @throws IllegalArgumentException if the task isn't pending or not owned by the TaskList
     */
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

    /**
     * Returns {@code true} if a task is running
     *
     * @return {@code true} if a task is running
     */
    public boolean isRunning() {
        return runningTask != null;
    }

    /**
     * Returns a list of all tasks that are in this TaskList, this includes finished, running and pending task.
     * Modification of the returned list has no effect over the TaskList
     *
     * @return a list of all tasks that are in this TaskList, this includes finished, running and pending task
     */
    public synchronized List<SolverTask> getTasks() {
        List<SolverTask> tasks = new ArrayList<>(finished.size() + pending.size() + (runningTask == null ? 0 : 1));
        tasks.addAll(finished);
        if (runningTask != null) {
            tasks.add(runningTask);
        }
        tasks.addAll(pending);

        return tasks;
    }

    /**
     * Returns an unmodifiable list of all finished task
     *
     * @return an unmodifiable list of all finished task
     */
    public List<SolverTask> getFinished() {
        return Collections.unmodifiableList(finished);
    }

    /**
     * Returns the running task
     *
     * @return the running task
     */
    public SolverTask getRunningTask() {
        return runningTask;
    }

    /**
     * Returns an unmodifiable collection of all pending tasks
     *
     * @return an unmodifiable collection of all pending tasks
     */
    public Collection<SolverTask> getPendingTask() {
        return Collections.unmodifiableCollection(pending);
    }

    /**
     * Returns the number of task that are in this TaskList. This includes finished, pending and running tasks.
     *
     * @return the number of task that are in this TaskList.
     */
    public int nTask() {
        return finished.size() + (runningTask == null ? 0 : 1) + pending.size();
    }

    /**
     * Returns the number of pending tasks
     *
     * @return the number of pending tasks
     */
    public int nPendingTask() {
        return pending.size();
    }
}
