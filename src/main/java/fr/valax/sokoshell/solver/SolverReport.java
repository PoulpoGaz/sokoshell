package fr.valax.sokoshell.solver;

import fr.poulpogaz.json.IJsonReader;
import fr.poulpogaz.json.JsonException;
import fr.poulpogaz.json.JsonPrettyWriter;
import fr.poulpogaz.json.JsonReader;
import fr.valax.graph.Label;
import fr.valax.graph.ScatterPlot;
import fr.valax.graph.Series;
import fr.valax.sokoshell.SokoShellHelper;
import fr.valax.sokoshell.graphics.MapRenderer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Queue;
import java.util.*;
import java.util.stream.Collectors;

/**
 * An object representing the output of a solver. It contains the parameters given to the solver,
 * some statistics, the solver status and if the status is {@link SolverReport#SOLUTION_FOUND},
 * it contains two representation of the solution: a sequence of {@link State} and a sequence of {@link Move}.
 *
 * @see SolverParameters
 * @see SolverStatistics
 * @see State
 * @see Move
 * @author PoulpoGaz
 * @author darth-mole
 */
public class SolverReport {

    public static final String NO_SOLUTION = "No solution";
    public static final String SOLUTION_FOUND = "Solution found";
    public static final String STOPPED = "Stopped";
    public static final String TIMEOUT = "Timeout";
    public static final String RAM_EXCEED = "Ram exceed";

    /**
     * Creates and returns a report that doesn't contain a solution
     *
     * @param params the parameters of the solver
     * @param stats the statistics
     * @param status the solver status
     * @return a report without a solution
     * @throws IllegalArgumentException if the state is {@link SolverReport#SOLUTION_FOUND}
     */
    public static SolverReport withoutSolution(SolverParameters params, SolverStatistics stats, String status) {
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

        return new SolverReport(params, stats, solution, SOLUTION_FOUND);
    }

    private final SolverParameters parameters;
    private final SolverStatistics statistics;

    private final List<State> states;

    private final String status;

    private final List<Move> fullSolution;

    public SolverReport(SolverParameters parameters,
                        SolverStatistics statistics,
                        List<State> states,
                        String status) {
        this.parameters = Objects.requireNonNull(parameters);
        this.statistics = Objects.requireNonNull(statistics);
        this.states = states;
        this.status = Objects.requireNonNull(status);

        if (status.equals(SOLUTION_FOUND)) {
            if (states == null) {
                throw new IllegalArgumentException("SolverStatus is SOLUTION_FOUND. You must give the solution");
            }

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

        ArrayList<Move> path = new ArrayList<>();
        List<Move> temp = new ArrayList<>();

        int playerX = level.getPlayerX();
        int playerY = level.getPlayerY();

        for (int i = 0; i < states.size() - 1; i++) {
            State current = states.get(i);

            if (i != 0) {
                map.addStateCrates(current);
            }

            State next = states.get(i + 1);
            StateDiff diff = getStateDiff(map, current, next);

            Node node = findPath(map, diff, playerX, playerY);
            if (node == null) {
                throw canFindPathException(map, current, next);
            }

            boolean newPlayerPosSet = false;
            while (node.parent != null) {
                if (!newPlayerPosSet) {
                    if (node.move.moveCrate()) {
                        playerX = node.playerX;
                        playerY = node.playerY;
                        temp.add(node.move);
                        newPlayerPosSet = true;
                    }
                } else {
                    temp.add(node.move);
                }

                node = node.parent;
            }

            path.ensureCapacity(path.size() + temp.size());
            for (int j = temp.size() - 1; j >= 0; j--) {
                path.add(temp.get(j));
            }
            temp.clear();

            map.removeStateCrates(current);
        }

        return path;
    }

    /**
     * Find the differences between two states:
     * <ul>
     *     <li>new player position</li>
     *     <li>old crate pos</li>
     *     <li>new crate pos</li>
     * </ul>
     *
     * @param map the map
     * @param from the first state
     * @param to the second state
     * @return a {@link StateDiff}
     */
    private StateDiff getStateDiff(Map map, State from, State to) {
        List<Integer> state1Crates = Arrays.stream(from.cratesIndices()).boxed().collect(Collectors.toList());
        List<Integer> state2Crates = Arrays.stream(to.cratesIndices()).boxed().collect(Collectors.toList());

        List<Integer> state1Copy = state1Crates.stream().toList();
        state1Crates.removeAll(state2Crates);
        state2Crates.removeAll(state1Copy);

        return new StateDiff(
                map.getX(to.playerPos()), map.getY(to.playerPos()),
                map.getX(state1Crates.get(0)), map.getY(state1Crates.get(0)),  // original crate pos
                map.getX(state2Crates.get(0)), map.getY(state2Crates.get(0))); // where it goes
    }

    /**
     * Find a path in the map from (playerX, playerY) so the state representing the map
     * is that same as the next state in {@link #createFullSolution()} ie move the player
     * to the new position stored in {@link StateDiff} and move at most one crate (original
     * and destination stored in {@link StateDiff})
     *
     * @param map the map
     * @param diff difference between two states: store player destination and old and new crate position
     * @param playerX current player x position
     * @param playerY current player y position
     * @return the path between the two points
     */
    private Node findPath(Map map, StateDiff diff, int playerX, int playerY) {
        Set<Node> visited = new HashSet<>();
        Queue<Node> queue = new ArrayDeque<>();
        queue.offer(new Node(null, playerX, playerY, diff.crateX(), diff.crateY(), null));
        visited.add(queue.peek());

        Node solution = null;
        while (!queue.isEmpty() && solution == null) {
            Node node = queue.poll();

            TileInfo player = map.getAt(node.playerX(), node.playerY());
            TileInfo crate = map.getAt(node.crateX(), node.crateY());
            crate.addCrate();

            for (Direction direction : Direction.VALUES) {
                TileInfo adj = player.adjacent(direction);

                Node child;
                if (adj.isSolid()) {
                    if (!crate.isAt(adj)) {
                        continue;
                    }

                    TileInfo adjAdj = adj.adjacent(direction);
                    if (adjAdj.isSolid()) {
                        continue;
                    }

                    child = new Node(node, adj.getX(), adj.getY(), adjAdj.getX(), adjAdj.getY(), new Move(direction, true));
                } else {
                    child = new Node(node, adj.getX(), adj.getY(), crate.getX(), crate.getY(), new Move(direction, false));
                }

                if (child.isEndNode(diff)) {
                    solution = child;
                    break;
                }

                if (visited.add(child)) {
                    queue.offer(child);
                }
            }

            crate.removeCrate();
        }

        map.getAt(diff.crateX, diff.crateY).addCrate();

        return solution;
    }

    /**
     * Create an exception indicating a path can't be found between two states.
     *
     * @param map the map which must be in the same state as current
     * @param current the current state
     * @param next the next state
     * @return an exception
     */
    private IllegalStateException canFindPathException(Map map, State current, State next) {
        MapRenderer mr = SokoShellHelper.INSTANCE.getRenderer();

        String map1 = mr.toString(map, map.getX(current.playerPos()), map.getY(current.playerPos()));
        map.removeStateCrates(current);
        map.addStateCrates(next);
        String map2 = mr.toString(map, map.getX(next.playerPos()), map.getY(next.playerPos()));

        return new IllegalStateException("""
                Can't find path between two states:
                %s
                and
                %s
                """.formatted(map1, map2));
    }




    public void writeSolution(JsonPrettyWriter jpw) throws JsonException, IOException {
        jpw.field("status", status);
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
        String status = jr.assertKeyEquals("status").nextString();

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
        return status.equals(SOLUTION_FOUND);
    }

    /**
     * Returns {@code true} if this report doesn't contain a solution
     *
     * @return {@code true} if this report doesn't contain a solution
     */
    public boolean hasNoSolution() {
        return !status.equals(SOLUTION_FOUND);
    }

    /**
     * Returns {@code true} if the solver was stopped by the user
     *
     * @return {@code true} if the solver was stopped by the user
     */
    public boolean isStopped() {
        return status.equals(STOPPED);
    }


    public String getStatus() {
        return status;
    }

    /**
     * Returns the level that was given to the solver
     *
     * @return the level that was given to the solver
     */
    public Level getLevel() {
        return parameters.getLevel();
    }


    /**
     * Returns the pack of the level that was given to the solver
     *
     * @return the pack of the level that was given to the solver
     */
    public Pack getPack() {
        return parameters.getLevel().getPack();
    }

    /**
     * Used by {@link #findPath(Map, StateDiff, int, int)} to find a path. It represents
     * a node in a graph.
     *
     * @param parent the parent node
     * @param playerX player x
     * @param playerY player y
     * @param crateX crate x
     * @param crateY crate y
     * @param move the move made by the player to move from the parent node to this node
     */
    private record Node(Node parent, int playerX, int playerY, int crateX, int crateY, Move move) {

        public boolean isEndNode(StateDiff diff) {
            return playerX == diff.destX() && playerY == diff.destY() &&
                    crateX == diff.crateDestX() && crateY == diff.crateDestY();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Node node)) return false;

            if (playerX != node.playerX) return false;
            if (playerY != node.playerY) return false;
            if (crateX != node.crateX) return false;
            return crateY == node.crateY;
        }

        @Override
        public int hashCode() {
            int result = playerX;
            result = 31 * result + playerY;
            result = 31 * result + crateX;
            result = 31 * result + crateY;
            return result;
        }
    }

    /**
     * Contains all differences between two states except the old player position.
     *
     * @param destX player destination x
     * @param destY player destination y
     * @param crateX old crate x
     * @param crateY old crate y
     * @param crateDestX new crate y
     * @param crateDestY new crate y
     */
    private record StateDiff(int destX, int destY, int crateX, int crateY, int crateDestX, int crateDestY) {}
}
