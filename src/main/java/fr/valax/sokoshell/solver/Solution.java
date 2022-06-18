package fr.valax.sokoshell.solver;

import fr.valax.graph.Label;
import fr.valax.graph.ScatterPlot;
import fr.valax.graph.Series;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

public class Solution {

    private final SolverType type;
    private final SolverParameters parameters;
    private final SolverStatistics statistics;

    private final List<State> states;

    private final SolverStatus status;

    public Solution(SolverType type,
                    SolverParameters parameters,
                    SolverStatistics statistics,
                    List<State> states,
                    SolverStatus status) {
        this.type = type;
        this.parameters = parameters;
        this.statistics = statistics;
        this.states = states;
        this.status = status;
    }

    public BufferedImage createGraph() {
        Series state = createSeries("State explored/s", statistics.getStateExploredByTimeUnit());
        state.setColor(new Color(0, 175, 244));

        Series queue = createSeries("Queue size/s", statistics.getQueueSizeByTimeUnit());
        queue.setColor(new Color(59, 165, 93));

        ScatterPlot plot = new ScatterPlot();
        plot.addSeries(state);
        plot.addSeries(queue);
        plot.setShowLegend(true);
        plot.setTitle(new Label("Statistics"));

        return plot.createAtOrigin(1000, 500);
    }

    private Series createSeries(String name, List<Integer> values) {
        Series series = new Series();
        series.setName(name);

        for (int i = 0; i < values.size(); i++) {
            int v = values.get(i);

            series.addPoint(new Point(i, v));
        }

        return series;
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
        return status == SolverStatus.SOLUTION_FOUND;
    }

    public boolean hasNoSolution() {
        return status == SolverStatus.NO_SOLUTION;
    }

    public boolean isStopped() {
        return status == SolverStatus.STOPPED;
    }

    public boolean isPaused() {
        return status == SolverStatus.PAUSED;
    }

    public SolverStatus getStatus() {
        return status;
    }
}
