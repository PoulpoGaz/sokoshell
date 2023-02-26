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

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * An object representing the output of a solver. It contains the parameters given to the solver,
 * some statistics, the solver status and if the status is {@link SolverReport#SOLUTION_FOUND},
 * it contains two representation of the solution: a sequence of {@link State} and a sequence of {@link Move}.
 *
 * @see SolverParameters
 * @see ISolverStatistics
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
    public static SolverReport withoutSolution(SolverParameters params, ISolverStatistics stats, String status) {
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
    public static SolverReport withSolution(State finalState, SolverParameters params, ISolverStatistics stats) {
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
    private final ISolverStatistics statistics;

    private final String status;

    /**
     * Solution packed in an int array.
     * Three bits are used for storing a move.
     * Move 1 is located at bit 0 of array 0,
     * Move 2 is located at bit 3 of array 0,
     * ...,
     * Move 10 is located at bit 27 of array 0,
     * Move 11 is located at bit 30 of array 0
     * and use the first bit of array 1.
     * Move 12 is located at bit 1 of array 1,
     * etc.
     * Bits are stored in little-endian fashion.
     */
    private final int[] solution;
    private final int numberOfMoves;
    private final int numberOfPushes;

    public SolverReport(SolverParameters parameters,
                        ISolverStatistics statistics,
                        List<State> states,
                        String status) {
        this.parameters = Objects.requireNonNull(parameters);
        this.statistics = Objects.requireNonNull(statistics);
        this.status = Objects.requireNonNull(status);

        if (status.equals(SOLUTION_FOUND)) {
            if (states == null) {
                throw new IllegalArgumentException("SolverStatus is SOLUTION_FOUND. You must give the solution");
            }

            SolutionBuilder builder = createFullSolution(states);

            numberOfPushes = builder.getNumberOfPushes();
            numberOfMoves = builder.getNumberOfMoves();
            solution = builder.getSolution();
        } else {
            numberOfMoves = -1;
            numberOfPushes = -1;
            solution = null;
        }
    }

    private SolverReport(SolverParameters parameters,
                         ISolverStatistics statistics,
                         String status,
                         SolutionBuilder builder) {
        this.parameters = Objects.requireNonNull(parameters);
        this.statistics = Objects.requireNonNull(statistics);
        this.status = Objects.requireNonNull(status);

        if (status.equals(SOLUTION_FOUND)) {
            numberOfPushes = builder.getNumberOfPushes();
            numberOfMoves = builder.getNumberOfMoves();
            solution = builder.getSolution();
        } else {
            numberOfMoves = -1;
            numberOfPushes = -1;
            solution = null;
        }
    }


    /**
     * Deduce from solution's states all the moves needed to solve the sokoban
     *
     * @return the full solution
     */
    private SolutionBuilder createFullSolution(List<State> states) {
        Level level = parameters.getLevel();
        Board board = new MutableBoard(level);

        SolutionBuilder sb = new SolutionBuilder(2 * states.size());
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

            sb.ensureCapacity(sb.getNumberOfMoves() + temp.size());
            for (int j = temp.size() - 1; j >= 0; j--) {
                sb.add(temp.get(j));
            }
            temp.clear();

            board.removeStateCrates(current);
        }

        return sb;
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
        String str2 = style.drawToString(board, board.getX(next.playerPos()), board.getY(next.playerPos())).toAnsi();

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

        if (solution != null) {
            jpw.key("solution").beginArray();
            jpw.setInline(JsonPrettyWriter.Inline.ALL);

            for (Move m : getFullSolution()) {
                jpw.value(m.shortName());
            }

            jpw.endArray();
            jpw.setInline(JsonPrettyWriter.Inline.NONE);
        }

        jpw.key("statistics");

        // probably not a good way to do that, but I don't know
        // how to easily serialize and deserialize ISolverStatistics
        // without having a factory...
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(statistics);
        oos.close();

        jpw.value(Base64.getEncoder().encodeToString(baos.toByteArray()));
    }


    public static SolverReport fromJson(JsonReader jr, Level level) throws JsonException, IOException {
        String status = jr.assertKeyEquals("status").nextString();

        jr.assertKeyEquals("parameters");
        SolverParameters parameters = SolverParameters.fromJson(jr, level);

        String key = jr.nextKey();

        SolutionBuilder sb = null;
        if (key.equals("solution")) {
            jr.beginArray();

            sb = new SolutionBuilder(32 * 5); // uses array of size 16
            while (!jr.isArrayEnd()) {
                String name = jr.nextString();
                Move move = Move.of(name);

                if (move == null) {
                    throw new IOException("Unknown move: " + name);
                }

                sb.add(move);
            }
            jr.endArray();

            jr.assertKeyEquals("statistics");
        } else if (!key.equals("statistics")) {
            throw new JsonException(String.format("Invalid key. Expected \"statistics\" but was \"%s\"", key));
        }

        // see writeSolution
        byte[] bytes = Base64.getDecoder().decode(jr.nextString());

        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
        ISolverStatistics stats;
        try {
            stats = (ISolverStatistics) ois.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        }
        ois.close();

        return new SolverReport(parameters, stats, status, sb);
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
    public ISolverStatistics getStatistics() {
        return statistics;
    }

    public SolutionIterator getSolutionIterator() {
        if (solution == null) {
            return null;
        }

        return new SolutionIterator();
    }

    /**
     * If the sokoban was solved, this report contains the solution as a sequence
     * of moves. It describes all moves made by the player.
     *
     * @return the solution or {@code null} if the sokoban wasn't solved
     */
    public List<Move> getFullSolution() {
        if (solution == null) {
            return null;
        }

        ListIterator<Move> it = getSolutionIterator();
        List<Move> moves = new ArrayList<>(numberOfMoves);

        while (it.hasNext()) {
            moves.add(it.next());
        }

        return moves;
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
        return numberOfMoves;
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

    /**
     * An object to iterate over a solution in forward and backward order.
     */
    public class SolutionIterator implements ListIterator<Move> {

        /**
         * Position in the array
         */
        private int arrayPos;

        /**
         * Position in solution[arrayPos]
         */
        private int bitPos;

        private int move;
        private int push;

        /**
         * @return read the next bit
         */
        private int readNext() {
            int bit = (solution[arrayPos] >> bitPos) & 0b1;

            bitPos++;
            if (bitPos == 32) {
                bitPos = 0;
                arrayPos++;
            }

            return bit;
        }

        /**
         * @return read the previous bit
         */
        private int readPrevious() {
            bitPos--;
            if (bitPos < 0) {
                bitPos = 31;
                arrayPos--;
            }

            return (solution[arrayPos] >> bitPos) & 0b1;
        }


        @Override
        public boolean hasNext() {
            return move < numberOfMoves;
        }

        @Override
        public Move next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            int first  = readNext();
            int second = readNext();
            int third  = readNext();

            int value = (third << 2) | (second << 1) | first;

            Move move = Move.values()[value];

            this.move++;
            if (move.moveCrate()) {
                push++;
            }

            return move;
        }

        @Override
        public boolean hasPrevious() {
            return move > 0;
        }

        @Override
        public Move previous() {
            if (!hasPrevious()) {
                throw new NoSuchElementException();
            }

            int third  = readPrevious();
            int second = readPrevious();
            int first  = readPrevious();

            int value = (third << 2) | (second << 1) | first;

            Move move = Move.values()[value];

            this.move--;
            if (move.moveCrate()) {
                push--;
            }

            return move;
        }

        @Override
        public int nextIndex() {
            return move;
        }

        @Override
        public int previousIndex() {
            return move - 1;
        }

        public void reset() {
            move = 0;
            arrayPos = 0;
            bitPos = 0;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void set(Move move) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(Move move) {
            throw new UnsupportedOperationException();
        }

        public int getMoveCount() {
            return move;
        }

        public int getPushCount() {
            return push;
        }
    }

    /**
     * A convenience object to convert a list of move to a solution array.
     */
    private static class SolutionBuilder {

        private int[] solution;

        private int arrayPos;
        private int bitPos;

        private int numberOfMoves;
        private int numberOfPushes;

        public SolutionBuilder(int estimatedNumberOfMove) {
            solution = new int[computeArraySize(estimatedNumberOfMove)];
        }

        private void write(int bit) {
            solution[arrayPos] = (bit & 0b1) << bitPos | solution[arrayPos];

            bitPos++;
            if (bitPos == 32) {
                bitPos = 0;
                arrayPos++;
            }
        }

        public void add(Move move) {
            if (bitPos + 3 >= 32 && arrayPos + 1 >= solution.length) {
                ensureCapacity(numberOfMoves * 2 + 1);
            }

            int value = move.ordinal();
            write(value & 0b1);
            write((value >> 1) & 0b1);
            write((value >> 2) & 0b1);
            numberOfMoves++;

            if (move.moveCrate()) {
                numberOfPushes++;
            }
        }

        public void ensureCapacity(int numberOfMove) {
            int minArraySize = computeArraySize(numberOfMove);

            if (minArraySize > solution.length) {
                solution = Arrays.copyOf(solution, minArraySize);
            }
        }

        public int getNumberOfMoves() {
            return numberOfMoves;
        }

        public int getNumberOfPushes() {
            return numberOfPushes;
        }

        public int[] getSolution() {
            int arraySize = computeArraySize(numberOfMoves);

            return Arrays.copyOf(solution, arraySize);
        }

        private int computeArraySize(int numberOfMove) {
            int nBits = 3 * numberOfMove;

            return nBits / 32 + 1;
        }
    }
}
