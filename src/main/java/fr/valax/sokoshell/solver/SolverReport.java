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

/**
 * An object representing the output of a solver. It contains the parameters given to the solver,
 * some statistics, the solver status and if the status is {@link SolverStatus#SOLUTION_FOUND},
 * it contains two representation of the solution: a sequence of {@link State} and a sequence of {@link Move}.
 *
 * @see SolverParameters
 * @see SolverStatistics
 * @see State
 * @see Move
 * @see SolverStatus
 * @author PoulpoGaz
 * @author darth-mole
 */
public class SolverReport {

    /**
     * Creates and returns a report that doesn't contain a solution
     *
     * @param params the parameters of the solver
     * @param stats the statistics
     * @param status the solver status
     * @return a report without a solution
     * @throws IllegalArgumentException if the state is {@link SolverStatus#SOLUTION_FOUND}
     */
    public static SolverReport withoutSolution(SolverParameters params, SolverStatistics stats, SolverStatus status) {
        if (status == SolverStatus.SOLUTION_FOUND) {
            throw new IllegalArgumentException("SolverStatus is SOLUTION_FOUND. You must give the solution");
        }

        return new SolverReport(params, stats, null, status);
    }

    /**
     * Creates and returns a report containing a solution. The solution is determined
     * from the final state.
     *
     * @param finalState the final state
     * @param params the parameters of the solver
     * @param stats the statistics
     * @return a report with a solution
     */
    public static SolverReport withSolution(State finalState, SolverParameters params, SolverStatistics stats) {
        List<State> solution = new ArrayList<>();

        State s = finalState;
        while (s.parent() != null)
        {
            solution.add(s);
            s = s.parent();
        }
        solution.add(s);
        Collections.reverse(solution);

        return new SolverReport(params, stats, solution, SolverStatus.SOLUTION_FOUND);
    }

    private final SolverParameters parameters;
    private final SolverStatistics statistics;

    private final List<State> states;

    private final SolverStatus status;

    private final List<Move> fullSolution;

    public SolverReport(SolverParameters parameters,
                        SolverStatistics statistics,
                        List<State> states,
                        SolverStatus status) {
        this.parameters = Objects.requireNonNull(parameters);
        this.statistics = Objects.requireNonNull(statistics);
        this.states = states;
        this.status = Objects.requireNonNull(status);

        if (states != null && status == SolverStatus.SOLUTION_FOUND) {
            fullSolution = createFullSolution();
        } else {
            fullSolution = null;
        }
    }


    /**
     * Deduce from solution's states all the moves needed to solve the sokoban
     *
     * @return the full solution
     */
    private List<Move> createFullSolution() {
        Level level = parameters.getLevel();
        Map map = level.getMap();

        List<Move> path = new ArrayList<>();

        int playerX = level.getPlayerX();
        int playerY = level.getPlayerY();

        for (int i = 0; i < states.size() - 1; i++) {
            State current = states.get(i);

            if (i != 0) {
                map.addStateCrates(current);
            }

            State next = states.get(i + 1);
            DirectionWithPosition dir = getDirection(map, current, next);

            int destX = dir.fromX - dir.dir().dirX();
            int destY = dir.fromY - dir.dir().dirY();

            if (playerX != destX || playerY != destY) {
                path.addAll(findPath(map, playerX, playerY, destX, destY));
            }

            path.add(new Move(dir.dir(), true));

            map.removeStateCrates(current);

            playerX = destX + dir.dir().dirX();
            playerY = destY + dir.dir().dirY();
        }

        return path;
    }

    /**
     * Find a path in the map between (fromX, fromY) and (destX, destY). This method doesn't
     * move any crates. It performs a simple graph traversal to find the path
     *
     * @return the path between the two points
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

    /**
     * This method compute the push made by the player between two states.
     * If the first state is:
     * <pre>
     *     #####
     *     #@  #
     *     ###$#
     *       # #
     *       ###
     * </pre>
     * and the second state:
     * <pre>
     *     #####
     *     #   #
     *     ###@#
     *       #$#
     *       ###
     * </pre>
     * This method will deduce that the player pushed the crate to the down
     *
     * @param map the map
     * @param from the first state
     * @param to the second state
     * @return the movement made by the player between two states
     */
    private DirectionWithPosition getDirection(Map map, State from, State to) {
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

        return new DirectionWithPosition(Direction.of(dirX, dirY), mvt1X, mvt1Y);
    }




    public void writeSolution(JsonPrettyWriter jpw) throws JsonException, IOException {
        jpw.field("status", status.status());
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


    public static SolverReport fromJson(JsonReader jr, Level level) throws JsonException, IOException {
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

        return new SolverReport(parameters, stats, states, status);
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

    /**
     * Returns the type of the solver used to produce this report
     *
     * @return the type of the solver used to produce this report
     */
    public SolverType getType() {
        return parameters.getSolver();
    }

    /**
     * Returns the parameters given to the solver that produce this report
     *
     * @return the parameters given to the solver
     */
    public SolverParameters getParameters() {
        return parameters;
    }

    /**
     * Returns the statistics produce by the solver that produce this report.
     * However, {@linkplain Solver solvers} are only capable of recording when
     * the research start and end. Others statistics are produced by {@link Tracker}
     *
     * @return the parameters given to the solver
     */
    public SolverStatistics getStatistics() {
        return statistics;
    }

    /**
     * If the sokoban was solved, this report contains the solution as a sequence
     * of states. It describes all pushes made by the player
     *
     * @return the solution or {@code null} if the sokoban wasn't solved
     */
    public List<State> getStates() {
        return states;
    }

    /**
     * If the sokoban was solved, this report contains the solution as a sequence
     * of moves. It describes all moves made by the player.
     *
     * @return the solution or {@code null} if the sokoban wasn't solved
     */
    public List<Move> getFullSolution() {
        return fullSolution;
    }

    /**
     * Returns the number of pushes the player made to solve the sokoban
     *
     * @return {@code -1} if the sokoban wasn't solved or the number of pushes the player made to solve the sokoban
     */
    public int numberOfPushes() {
        return states == null ? -1 : states.size() - 1;
    }

    /**
     * Returns the number of moves the player made to solve the sokoban
     *
     * @return {@code -1} if the sokoban wasn't solved or the number of moves the player made to solve the sokoban
     */
    public int numberOfMoves() {
        return fullSolution == null ? -1 : fullSolution.size();
    }


    /**
     * Returns {@code true} if this report contains a solution
     *
     * @return {@code true} if this report contains a solution
     */
    public boolean isSolved() {
        return status == SolverStatus.SOLUTION_FOUND;
    }

    /**
     * Returns {@code true} if this report doesn't contain a solution
     *
     * @return {@code true} if this report doesn't contain a solution
     */
    public boolean hasNoSolution() {
        return status != SolverStatus.SOLUTION_FOUND;
    }

    /**
     * Returns {@code true} if the solver was stopped by the user
     *
     * @return {@code true} if the solver was stopped by the user
     */
    public boolean isStopped() {
        return status == SolverStatus.STOPPED;
    }

    /**
     * TODO: documentation, cf SolverStatus
     */
    public SolverStatus getStatus() {
        return status;
    }


    /**
     * Used by {@link #findPath(Map, int, int, int, int)} to find a path. It represents
     * a node in a graph.
     *
     * @param parent the parent node
     * @param playerX player x
     * @param playerY player y
     * @param dir the direction made by the player to move from the parent node to this node
     */
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

    /**
     * Represents a movement of a player from (fromX, fromY).
     *
     * @param dir the direction taken by the player
     * @param fromX player x original position
     * @param fromY player y original position
     */
    private record DirectionWithPosition(Direction dir, int fromX, int fromY) {}
}
