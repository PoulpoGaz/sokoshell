package fr.valax.sokoshell.solver;

import java.util.ArrayList;
import java.util.List;

/**
 * An object that contains various statistics about a solution, including
 * time start and end, number of node explored and queue size every time unit
 */
public class SolverStatistics {

    private List<Integer> stateExplored;
    private List<Integer> queueSize;
    private int timeUnit; // in sec

    private long timeStarted; // in millis
    private long timeEnded; // in millis

    public SolverStatistics() {
        stateExplored = new ArrayList<>();
        queueSize = new ArrayList<>();
        timeUnit = -1;
    }

    public void add(int nodeExplored, int queueSize) {
        stateExplored.add(nodeExplored);
        this.queueSize.add(queueSize);
    }

    public List<Integer> getStateExplored() {
        return stateExplored;
    }

    public List<Integer> getQueueSize() {
        return queueSize;
    }

    public void setStateExplored(List<Integer> stateExplored) {
        this.stateExplored = stateExplored;
    }

    public void setQueueSize(List<Integer> queueSize) {
        this.queueSize = queueSize;
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
