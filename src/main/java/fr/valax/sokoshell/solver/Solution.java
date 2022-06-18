package fr.valax.sokoshell.solver;

import java.util.List;

public class Solution {

    public static final int SOLVED = 1;
    public static final int NO_SOLUTION = 2;
    public static final int STOPPED = 3;
    public static final int PAUSED = 4;

    private final SolverType type;
    private final SolverParameters parameters;
    private final SolverStatistics statistics;

    private final List<State> states;

    private final int status;

    public Solution(SolverType type,
                    SolverParameters parameters,
                    SolverStatistics statistics,
                    List<State> states,
                    int status) {
        this.type = type;
        this.parameters = parameters;
        this.statistics = statistics;
        this.states = states;
        this.status = status;
    }

    public SolverType getType() {
        return type;
    }

    public SolverParameters getParameters() {
        return parameters;
    }

    public SolverStatistics getStatistics() {
        return statistics;
    }

    public List<State> getStates() {
        return states;
    }

    public boolean isSolved() {
        return status == SOLVED;
    }

    public boolean hasNoSolution() {
        return status == NO_SOLUTION;
    }

    public boolean isStopped() {
        return status == STOPPED;
    }

    public boolean isPaused() {
        return status == PAUSED;
    }

    public int getStatus() {
        return status;
    }
}
