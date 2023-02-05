package fr.valax.sokoshell.solver;

import fr.valax.sokoshell.solver.board.Board;
import fr.valax.sokoshell.solver.collections.SolverPriorityQueue;
import fr.valax.sokoshell.solver.heuristic.GreedyHeuristic;
import fr.valax.sokoshell.solver.heuristic.Heuristic;
import fr.valax.sokoshell.solver.heuristic.SimpleHeuristic;

public abstract class AStarSolver extends BruteforceSolver<WeightedState> {

    public static AStarSolver newGreedyAStar() {
        return new AStarSolver("A*") {

            @Override
            protected Heuristic createHeuristic(Board board) {
                return new GreedyHeuristic(board);
            }
        };
    }

    public static AStarSolver newSimplerAStar() {
        return new AStarSolver("A* simple") {

            @Override
            protected Heuristic createHeuristic(Board board) {
                return new SimpleHeuristic(board);
            }
        };
    }


    private Heuristic heuristic;

    public AStarSolver(String name) {
        super(name);
    }

    @Override
    protected void init() {
        heuristic = createHeuristic(board);
        toProcess = new SolverPriorityQueue();
    }

    protected abstract Heuristic createHeuristic(Board board);

    @Override
    protected void addInitialState(Level level) {
        final State s = level.getInitialState();

        toProcess.addState(new WeightedState(s, 0, heuristic.compute(s)));
    }

    @Override
    protected void addState(int crateIndex, int crateX, int crateY, int crateDestX, int crateDestY) {
        final int i = board.topLeftReachablePosition(crateX, crateY, crateDestX, crateDestY);
        // The new player position is the crate position
        WeightedState s = toProcess.cachedState().child(i, crateIndex,
                                          crateDestY * board.getWidth() + crateDestX);
        s.setHeuristic(heuristic.compute(s));

        if (processed.add(s)) {
            toProcess.addState(s);
        }
    }
}
