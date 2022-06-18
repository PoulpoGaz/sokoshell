package fr.valax.sokoshell.solver;

import java.util.ArrayList;
import java.util.List;

/**
 * An object that contains various statistics about a solution, including
 * time start and end, number of node explored and queue size by time unit
 */
public class SolverStatistics {

    private final List<Integer> nodeExploredByTimeUnit;
    private final List<Integer> queueSizeByTimeUnit;
    private int timeUnit; // in sec

    private long timeStarted; // in millis
    private long timeEnded; // in millis

    public SolverStatistics() {
        nodeExploredByTimeUnit = new ArrayList<>();
        queueSizeByTimeUnit = new ArrayList<>();
        timeUnit = -1;
    }

    public void add(int nodeExplored, int queueSize) {
        nodeExploredByTimeUnit.add(nodeExplored);
        queueSizeByTimeUnit.add(queueSize);
    }

    public List<Integer> getNodeExploredByTimeUnit() {
        return nodeExploredByTimeUnit;
    }

    public List<Integer> getQueueSizeByTimeUnit() {
        return queueSizeByTimeUnit;
    }

    public int getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(int timeUnit) {
        this.timeUnit = timeUnit;
    }

    public long getTimeStarted() {
        return timeStarted;
    }

    public void setTimeStarted(long timeStarted) {
        this.timeStarted = timeStarted;
    }

    public long getTimeEnded() {
        return timeEnded;
    }

    public void setTimeEnded(long timeEnded) {
        this.timeEnded = timeEnded;
    }
}
