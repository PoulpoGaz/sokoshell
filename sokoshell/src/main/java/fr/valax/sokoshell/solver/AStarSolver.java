package fr.valax.sokoshell.solver;

import fr.poulpogaz.json.IJsonReader;
import fr.poulpogaz.json.IJsonWriter;
import fr.poulpogaz.json.JsonException;
import fr.valax.sokoshell.commands.AbstractCommand;
import fr.valax.sokoshell.solver.board.tiles.TileInfo;
import fr.valax.sokoshell.solver.collections.SolverPriorityQueue;
import fr.valax.sokoshell.solver.heuristic.GreedyHeuristic;
import fr.valax.sokoshell.solver.heuristic.Heuristic;
import fr.valax.sokoshell.solver.heuristic.SimpleHeuristic;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;

import java.io.IOException;
import java.util.List;

public class AStarSolver extends AbstractSolver<WeightedState> {

    private Heuristic heuristic;

    public AStarSolver() {
        super(A_STAR);
    }

    @Override
    protected void init(SolverParameters parameters) {
        String heuristicName = parameters.getArgument("heuristic");

        if (heuristicName.equalsIgnoreCase("simple")) {
            heuristic = new SimpleHeuristic(board);
        } else {
            heuristic = new GreedyHeuristic(board);
        }

        toProcess = new SolverPriorityQueue();
    }

    @Override
    protected void addInitialState(Level level) {
        final State s = level.getInitialState();

        toProcess.addState(new WeightedState(s, 0, heuristic.compute(s)));
    }

    @Override
    protected void addState(int crateIndex, TileInfo crate, TileInfo crateDest) {
        final int i = board.topLeftReachablePosition(crate, crateDest);
        // The new player position is the crate position
        WeightedState s = toProcess.cachedState().child(i, crateIndex, crateDest.getIndex());
        s.setHeuristic(heuristic.compute(s));

        if (processed.add(s)) {
            toProcess.addState(s);
        }
    }

    @Override
    protected void addParameters(List<SolverParameter> parameters) {
        super.addParameters(parameters);
        parameters.add(new HeuristicParameter());
    }

    protected static class HeuristicParameter extends SolverParameter {

        private String value;

        public HeuristicParameter() {
            super("heuristic", "The heuristic the solver should use");
        }

        @Override
        public void set(String argument) throws AbstractCommand.InvalidArgument {
            if (argument.equalsIgnoreCase("greedy") || argument.equalsIgnoreCase("simple")) {
                this.value = argument;
            } else {
                throw new AbstractCommand.InvalidArgument("No such heuristic: " + argument);
            }
        }

        @Override
        public Object get() {
            return value;
        }

        @Override
        public Object getDefaultValue() {
            return "greedy";
        }

        @Override
        public void toJson(IJsonWriter jw) throws JsonException, IOException {
            jw.value(value);
        }

        @Override
        public void fromJson(IJsonReader jr) throws JsonException, IOException {
            value = jr.nextString();
        }

        @Override
        public void complete(LineReader reader, String argument, List<Candidate> candidates) {
            candidates.add(new Candidate("simple"));
            candidates.add(new Candidate("greedy"));
        }
    }
}
