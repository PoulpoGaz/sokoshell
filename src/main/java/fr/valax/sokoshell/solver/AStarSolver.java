package fr.valax.sokoshell.solver;

public class AStarSolver extends BasicBrutalSolver {

    @Override
    public SolverType getSolverType() {
        return SolverType.ASTAR;
    }

    @Override
    protected void createCollection() {
        toProcess = new SolverPriorityQueue();
    }
}
