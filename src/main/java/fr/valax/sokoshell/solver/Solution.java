package fr.valax.sokoshell.solver;

import fr.poulpogaz.json.IJsonReader;
import fr.poulpogaz.json.JsonException;
import fr.poulpogaz.json.JsonPrettyWriter;
import fr.poulpogaz.json.JsonReader;
import fr.valax.graph.Label;
import fr.valax.graph.ScatterPlot;
import fr.valax.graph.Series;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Queue;
import java.util.*;
import java.util.stream.Collectors;

public class Solution {

    private final SolverParameters parameters;
    private final SolverStatistics statistics;

    private final List<State> states;

    private final SolverStatus status;

    private final List<Move> fullSolution;

    public Solution(SolverParameters parameters,
                    SolverStatistics statistics,
                    List<State> states,
                    SolverStatus status) {
        this.parameters = parameters;
        this.statistics = statistics;
        this.states = states;
        this.status = status;

        if (states != null && status == SolverStatus.SOLUTION_FOUND) {
            fullSolution = createFullSolution();
        } else {
            fullSolution = null;
        }
    }


    private List<Move> createFullSolution() {
        Map map = parameters.getLevel().getMap();

        List<Move> path = new ArrayList<>();

        for (int i = 0; i < states.size() - 1; i++) {
            State current = states.get(i);

            if (i != 0) {
                map.addStateCrates(current);
            }

            int playerX = map.getX(current.playerPos());
            int playerY = map.getY(current.playerPos());

            State next = states.get(i + 1);
            Direction dir = getDirection(map, current, next);

            int destX = next.playerPos() % map.getWidth() - dir.dirX();
            int destY = next.playerPos() / map.getWidth() - dir.dirY();

            if (playerX != destX || playerY != destY) {
                path.addAll(findPath(map, playerX, playerY, destX, destY));
            }

            path.add(new Move(dir, true));

            map.removeStateCrates(current);
        }

        // reset
        map.addStateCrates(states.get(0));

        return path;
    }

    /**
     * Doesn't move any crates
     */
    private List<Move> findPath(Map map, int fromX, int fromY, int destX, int destY) {
        Set<Node> visited = new HashSet<>();
        Queue<Node> queue = new ArrayDeque<>();
        queue.offer(new Node(null, fromX, fromY, null));
        visited.add(queue.peek());

        Node solution = null;
        while (!queue.isEmpty() && solution == null) {
            Node node = queue.poll();

            for (Direction direction : Direction.values()) {
                int newX = node.playerX + direction.dirX();
                int newY = node.playerY + direction.dirY();

                if (map.getAt(newX, newY).isSolid()) {
                    continue;
                }

                Node child = new Node(node, newX, newY, direction);
                if (newX == destX && newY == destY) {
                    solution = child;
                    break;
                }

                if (visited.add(child)) {
                    queue.offer(child);
                }
            }
        }

        if (solution == null) {
            throw new IllegalStateException("Can't find path between two states");
        } else {
            List<Move> directions = new ArrayList<>();

            Node n = solution;
            while (n.parent != null) {
                directions.add(new Move(n.dir, false));

                n = n.parent;
            }

            Collections.reverse(directions);

            return directions;
        }
    }

    private Direction getDirection(Map map, State from, State to) {
        List<Integer> state1Crates = Arrays.stream(from.cratesIndices()).boxed().collect(Collectors.toList());
        List<Integer> state2Crates = Arrays.stream(to.cratesIndices()).boxed().collect(Collectors.toList());

        List<Integer> state1Copy = state1Crates.stream().toList();
        state1Crates.removeAll(state2Crates);
        state2Crates.removeAll(state1Copy);

        // crate position
        int mvt1X = (state1Crates.get(0) % map.getWidth());
        int mvt1Y = (state1Crates.get(0) / map.getWidth());

        // where it goes
        int mvt2X = (state2Crates.get(0) % map.getWidth());
        int mvt2Y = (state2Crates.get(0) / map.getWidth());

        int dirX = mvt2X - mvt1X;
        int dirY = mvt2Y - mvt1Y;

        return Direction.of(dirX, dirY);
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


    public static Solution fromJson(JsonReader jr, Level level) throws JsonException, IOException {
        SolverStatus status = SolverStatus.valueOf(jr.assertKeyEquals("status").nextString());

        jr.assertKeyEquals("parameters");
        SolverParameters parameters = SolverParameters.fromJson(jr, level);

        String key = jr.nextKey();

        List<State> states = null;
        if (key.equals("solution")) {
            jr.beginArray();

            states = new ArrayList<>();
            State last = null;
            while (!jr.isArrayEnd()) {
                jr.beginObject();

                int player = jr.assertKeyEquals("player").nextInt();
                jr.assertKeyEquals("crates");
                int[] crates = readIntArray(jr);

                State state = new State(player, crates, last);
                states.add(state);
                last = state;

                jr.endObject();
            }
            jr.endArray();

            jr.assertKeyEquals("statistics");
        } else if (!key.equals("statistics")) {
            throw new JsonException(String.format("Invalid key. Expected \"%s\" but was \"%s\"", "statistics", key));
        }

        SolverStatistics stats = SolverStatistics.fromJson(jr);

        return new Solution(parameters, stats, states, status);
    }

    private static int[] readIntArray(IJsonReader jr) throws JsonException, IOException {
        List<Integer> integers = new ArrayList<>();

        jr.beginArray();
        while (!jr.isArrayEnd()) {
            integers.add(jr.nextInt());
        }

        jr.endArray();

        int[] array = new int[integers.size()];
        for (int i = 0; i < integers.size(); i++) {
            array[i] = integers.get(i);
        }

        return array;
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
        return parameters.getSolver();
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

    public List<Move> getFullSolution() {
        return fullSolution;
    }

    public int numberOfPushes() {
        return states == null ? -1 : states.size() - 1;
    }

    public int numberOfMoves() {
        return fullSolution == null ? -1 : fullSolution.size();
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



    private record Node(Node parent, int playerX, int playerY, Direction dir) {

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Node node = (Node) o;

            if (playerX != node.playerX) return false;
            return playerY == node.playerY;
        }

        @Override
        public int hashCode() {
            int result = playerX;
            result = 31 * result + playerY;
            return result;
        }
    }
}
