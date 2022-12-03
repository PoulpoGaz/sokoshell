package fr.valax.sokoshell.solver;

public class AStarSolver extends BasicBrutalSolver<WeightedState> {

    @Override
    public SolverType getSolverType() {
        return SolverType.ASTAR;
    }

    @Override
    protected void createCollection() {
        toProcess = new SolverPriorityQueue();
    }

    @Override
    protected void addInitialState(Level level) {
        // TODO compute heuristic
        toProcess.addState(new WeightedState(level.getInitialState(), 0, 0));
    }

    @Override
    protected void addState(int crateIndex, int crateX, int crateY, int crateDestX, int crateDestY) {
        final int i = map.topLeftReachablePosition(crateX, crateY, crateDestX, crateDestY);
        // The new player position is the crate position
        WeightedState s = toProcess.curCachedState().child(i, crateIndex,crateDestY * map.getWidth() + crateDestX);

        if (processed.add(s)) {
            toProcess.addState(s);
        }
    }
}
