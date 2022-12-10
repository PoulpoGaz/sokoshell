package fr.valax.sokoshell;

/**
 * A task listener listen to every task status modification of a task
 */
public interface TaskListener {

    /**
     * Called when the status of the specified task changed from 'oldStatus' to 'newStatus'
     *
     * @param task the task that has changed status
     * @param oldStatus old task status
     * @param newStatus new task status
     */
    void statusChanged(SolverTask task, TaskStatus oldStatus, TaskStatus newStatus);
}
