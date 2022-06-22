package fr.valax.sokoshell.solver;

import fr.poulpogaz.json.IJsonWriter;
import fr.poulpogaz.json.JsonException;
import fr.poulpogaz.json.JsonPrettyWriter;
import fr.valax.graph.Label;
import fr.valax.graph.ScatterPlot;
import fr.valax.graph.Series;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
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

    public void writeSolution(JsonPrettyWriter jpw) throws JsonException, IOException {
        jpw.field("status", status.name());
        jpw.key("parameters");
        parameters.append(jpw);

        if (states != null) {
            jpw.key("solution").beginArray();

            for (State s : states) {
                jpw.beginObject();

                jpw.setInline(JsonPrettyWriter.Inline.ALL);
                jpw.field("player", s.playerPos());

                jpw.key("crates").beginArray();

                for (int crate : s.cratesIndices()) {
                    jpw.value(crate);
                }

                jpw.endArray();

                jpw.endObject();
                jpw.setInline(JsonPrettyWriter.Inline.NONE);
            }

            jpw.endArray();
        }

        jpw.key("statistics");
        statistics.writeStatistics(jpw);
    }

    public BufferedImage createGraph() {
        /*Series state = createSeries("State explored(t)", statistics.getStateExplored());
        state.setColor(new Color(0, 175, 244));

        Series queue = createSeries("Queue size(t)", statistics.getQueueSize());
        queue.setColor(new Color(59, 165, 93));*/

        ScatterPlot plot = new ScatterPlot();
        //plot.addSeries(state);
        //plot.addSeries(queue);
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
