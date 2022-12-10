package fr.valax.sokoshell;

/**
 * All different status a task can have
 */
public enum TaskStatus {

    /**
     * The task is pending
     */
    PENDING,

    /**
     * The task is running
     */
    RUNNING,

    /**
     * The task was canceled i.e. it was in the pending queue and the user
     * requested to stop it
     */
    CANCELED,

    /**
     * The task was running, and it was stopped by the user
     */
    STOPPED,

    /**
     * The task ended with an error
     */
    ERROR,

    /**
     * The task ended without an error. This doesn't mean a solution was found
     */
    FINISHED
}
