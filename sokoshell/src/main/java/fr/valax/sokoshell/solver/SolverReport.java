package fr.valax.sokoshell.solver;

import fr.poulpogaz.json.JsonException;
import fr.poulpogaz.json.JsonPrettyWriter;
import fr.poulpogaz.json.JsonReader;
import fr.valax.sokoshell.SokoShell;
import fr.valax.sokoshell.graphics.style.BoardStyle;
import fr.valax.sokoshell.solver.board.Board;
import fr.valax.sokoshell.solver.board.Move;
import fr.valax.sokoshell.solver.board.MutableBoard;
import fr.valax.sokoshell.solver.board.tiles.TileInfo;
import fr.valax.sokoshell.solver.pathfinder.CrateAStar;
import fr.valax.sokoshell.solver.pathfinder.Node;

import java.io.IOException;
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

    private final String status;

    private final List<Move> fullSolution;
    private final int numberOfPushes;

    public SolverReport(SolverParameters parameters,
                        SolverStatistics statistics,
                        List<State> states,
                        String status) {
        this.parameters = Objects.requireNonNull(parameters);
        this.statistics = Objects.requireNonNull(statistics);
        this.status = Objects.requireNonNull(status);

        if (status.equals(SOLUTION_FOUND)) {
            if (states == null) {
                throw new IllegalArgumentException("SolverStatus is SOLUTION_FOUND. You must give the solution");
            }

            fullSolution = createFullSolution(states);

            int n = 0;
            for (Move m : fullSolution) {
                if (m.moveCrate()) {
                    n++;
                }
            }
            numberOfPushes = n;
        } else {
            numberOfPushes = -1;
            fullSolution = null;
        }
    }

    private SolverReport(SolverParameters parameters,
                        SolverStatistics statistics,
                        String status,
                        List<Move> moves) {
        this.parameters = Objects.requireNonNull(parameters);
        this.statistics = Objects.requireNonNull(statistics);
        this.status = Objects.requireNonNull(status);

        if (status.equals(SOLUTION_FOUND)) {
            fullSolution = Objects.requireNonNull(moves);

            int n = 0;
            for (Move m : fullSolution) {
                if (m.moveCrate()) {
                    n++;
                }
            }
            numberOfPushes = n;
        } else {
            numberOfPushes = -1;
            fullSolution = null;
        }
    }


    /**
     * Deduce from solution's states all the moves needed to solve the sokoban
     *
     * @return the full solution
     */
    private List<Move> createFullSolution(List<State> states) {
        Level level = parameters.getLevel();
        Board board = new MutableBoard(level);

        ArrayList<Move> path = new ArrayList<>();
        List<Move> temp = new ArrayList<>();

        TileInfo player = board.getAt(level.getPlayerX(), level.getPlayerY());

        CrateAStar aStar = new CrateAStar(board);
        for (int i = 0; i < states.size() - 1; i++) {
            State current = states.get(i);

            if (i != 0) {
                board.addStateCrates(current);
            }

            State next = states.get(i + 1);
            StateDiff diff = getStateDiff(board, current, next);

            Node node = aStar.findPathAndComputeMoves(
                    player, null,
                    diff.crate(), diff.crateDest());

            if (node == null) {
                throw cannotFindPathException(board, current, next);
            }

            player = node.getPlayer();
            while (node.getParent() != null) {
                temp.add(node.getMove());
                node = node.getParent();
            }

            path.ensureCapacity(path.size() + temp.size());
            for (int j = temp.size() - 1; j >= 0; j--) {
                path.add(temp.get(j));
            }
            temp.clear();

            board.removeStateCrates(current);
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
     * @param board the board
     * @param from the first state
     * @param to the second state
     * @return a {@link StateDiff}
     */
    private StateDiff getStateDiff(Board board, State from, State to) {
        List<Integer> state1Crates = Arrays.stream(from.cratesIndices()).boxed().collect(Collectors.toList());
        List<Integer> state2Crates = Arrays.stream(to.cratesIndices()).boxed().collect(Collectors.toList());

        List<Integer> state1Copy = state1Crates.stream().toList();
        state1Crates.removeAll(state2Crates);
        state2Crates.removeAll(state1Copy);

        return new StateDiff(
                board.getAt(to.playerPos()),
                board.getAt(state1Crates.get(0)),  // original crate pos
                board.getAt(state2Crates.get(0))); // where it goes
    }

    /**
     * Create an exception indicating a path can't be found between two states.
     *
     * @param board the board which must be in the same state as current
     * @param current the current state
     * @param next the next state
     * @return an exception
     */
    private IllegalStateException cannotFindPathException(Board board, State current, State next) {
        BoardStyle style = SokoShell.INSTANCE.getBoardStyle();

        String str1 = style.drawToString(board, board.getX(current.playerPos()), board.getY(current.playerPos())).toAnsi();
        board.removeStateCrates(current);
        board.addStateCrates(next);
        String str2 = style.drawToString(board, board.getX(next.playerPos()), board.getY(next.playerPos())).toString();

        return new IllegalStateException("""
                Can't find path between two states:
                %s
                (%s)
                and
                %s
                (%s)
                """.formatted(str1, current, str2, next));
    }




    public void writeSolution(JsonPrettyWriter jpw) throws JsonException, IOException {
        jpw.field("status", status);
        jpw.key("parameters");
        parameters.append(jpw);

        if (fullSolution != null) {
            jpw.key("solution").beginArray();
            jpw.setInline(JsonPrettyWriter.Inline.ALL);

            for (Move m : fullSolution) {
                jpw.value(m.shortName());
            }

            jpw.endArray();
            jpw.setInline(JsonPrettyWriter.Inline.NONE);
        }

        jpw.key("statistics");
        statistics.writeStatistics(jpw);
    }


    public static SolverReport fromJson(JsonReader jr, Level level) throws JsonException, IOException {
        String status = jr.assertKeyEquals("status").nextString();

        jr.assertKeyEquals("parameters");
        SolverParameters parameters = SolverParameters.fromJson(jr, level);

        String key = jr.nextKey();

        List<Move> moves = null;
        if (key.equals("solution")) {
            jr.beginArray();

            moves = new ArrayList<>();
            while (!jr.isArrayEnd()) {
                String name = jr.nextString();
                Move move = Move.of(name);

                if (move == null) {
                    throw new IOException("Unknown move: " + name);
                }

                moves.add(move);
            }
            jr.endArray();

            jr.assertKeyEquals("statistics");
        } else if (!key.equals("statistics")) {
            throw new JsonException(String.format("Invalid key. Expected \"statistics\" but was \"%s\"", key));
        }

        SolverStatistics stats = SolverStatistics.fromJson(jr);

        return new SolverReport(parameters, stats, status, moves);
    }


    /**
     * Returns the type of the solver used to produce this report
     *
     * @return the type of the solver used to produce this report
     */
    public String getSolverName() {
        return parameters.getSolverName();
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
        return numberOfPushes;
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
     * Contains all differences between two states except the old player position.
     *
     * @param playerDest player destination
     * @param crate old crate position
     * @param crateDest crate destination
     */
    private record StateDiff(TileInfo playerDest, TileInfo crate, TileInfo crateDest) {}
}
