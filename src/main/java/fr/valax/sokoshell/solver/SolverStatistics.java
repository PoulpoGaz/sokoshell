package fr.valax.sokoshell.solver;

import java.util.ArrayList;
import java.util.List;

/**
 * An object that contains various statistics about a solution, including
 * time start and end, number of node explored and queue size by time unit
 */
public class SolverStatistics {

    private List<Integer> stateExploredByTimeUnit;
    private List<Integer> queueSizeByTimeUnit;
    private int timeUnit; // in sec

    private long timeStarted; // in millis
    private long timeEnded; // in millis

    public SolverStatistics() {
        stateExploredByTimeUnit = new ArrayList<>();
        queueSizeByTimeUnit = new ArrayList<>();
        timeUnit = -1;
    }

    public void add(int nodeExplored, int queueSize) {
        stateExploredByTimeUnit.add(nodeExplored);
        queueSizeByTimeUnit.add(queueSize);
    }

    public List<Integer> getStateExploredByTimeUnit() {
        return stateExploredByTimeUnit;
    }

    public List<Integer> getQueueSizeByTimeUnit() {
        return queueSizeByTimeUnit;
    }

    public void setStateExploredByTimeUnit(List<Integer> stateExploredByTimeUnit) {
        this.stateExploredByTimeUnit = stateExploredByTimeUnit;
    }

    public void setQueueSizeByTimeUnit(List<Integer> queueSizeByTimeUnit) {
        this.queueSizeByTimeUnit = queueSizeByTimeUnit;
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
