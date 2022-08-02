package fr.valax.sokoshell;

public interface TaskListener {

    void statusChanged(SolverTask task, TaskStatus oldStatus, TaskStatus newStatus);
}
