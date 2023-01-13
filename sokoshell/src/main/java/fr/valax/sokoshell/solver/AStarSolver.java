package fr.valax.sokoshell.solver;

import fr.valax.sokoshell.solver.collections.SolverPriorityQueue;
import fr.valax.sokoshell.solver.heuristic.GreedyHeuristic;
import fr.valax.sokoshell.solver.heuristic.Heuristic;

public class AStarSolver extends BruteforceSolver<WeightedState> {

    private Heuristic heuristic;

    public AStarSolver() {
        super(A_STAR);
    }

    @Override
    protected void createCollection() {
        heuristic = new GreedyHeuristic(this.map);
        toProcess = new SolverPriorityQueue();
    }

    @Override
    protected void addInitialState(Level level) {
        final State s = level.getInitialState();

        toProcess.addState(new WeightedState(s, 0, heuristic.compute(s)));
    }

    @Override
    protected void addState(int crateIndex, int crateX, int crateY, int crateDestX, int crateDestY) {
        final int i = map.topLeftReachablePosition(crateX, crateY, crateDestX, crateDestY);
        // The new player position is the crate position
        WeightedState s = toProcess.cachedState().child(i, crateIndex,
                                          crateDestY * map.getWidth() + crateDestX);
        s.setHeuristic(heuristic.compute(s));

        if (processed.add(s)) {
            toProcess.addState(s);
        }
    }
}
