package fr.valax.sokoshell.solver;

import fr.valax.sokoshell.solver.board.Direction;
import fr.valax.sokoshell.solver.board.tiles.TileInfo;
import fr.valax.sokoshell.solver.collections.SolverCollection;

import java.util.ArrayDeque;

/**
 * This class serves as a base class for DFS and BFS solvers, as these class are nearly the same -- the only
 * difference being in the order in which they treat the states (LIFO for DFS and FIFO for BFS).
 */
public abstract class BruteforceSolver extends AbstractSolver<State> {

    public BruteforceSolver(String name) {
        super(name);
    }

    public static DFSSolver newDFSSolver() {
        return new DFSSolver();
    }

    public static BFSSolver newBFSSolver() {
        return new BFSSolver();
    }

    @Override
    protected void addInitialState(Level level) {
        toProcess.addState(level.getInitialState());
    }

    @Override
    protected void addState(int crateIndex, TileInfo crate, TileInfo crateDest, Direction pushDir) {
        if (checkDeadlockBeforeAdding(crate, crateDest, pushDir)) {
            return;
        }

        final int i = board.topLeftReachablePosition(crate, crateDest);
        // The new player position is the crate position
        State s = toProcess.cachedState().child(i, crateIndex, crateDest.getIndex());

        if (processed.add(s)) {
            toProcess.addState(s);
        }
    }

    @Override
    public int lowerBound() {
        return -1;
    }

    /**
     * Base class for DFS and BFS solvers collection (both of them use {@link ArrayDeque}), the only difference being in
     * which side of the queue is used (end => FIFO => DFS, start => LIFO => BFS)
     */
    private static abstract class BasicBruteforceSolverCollection implements SolverCollection<State> {

        protected final ArrayDeque<State> collection = new ArrayDeque<>();

        protected State cachedState;

        @Override
        public void clear() {
            collection.clear();
        }

        @Override
        public boolean isEmpty() {
            return collection.isEmpty();
        }

        @Override
        public int size() {
            return collection.size();
        }

        @Override
        public void addState(State state) {
            collection.offer(state);
        }

        @Override
        public State peekAndCacheState() {
            cachedState = popState();
            return cachedState;
        }

        @Override
        public State cachedState() {
            return cachedState;
        }
    }

    private static class DFSSolver extends BruteforceSolver {

        public DFSSolver() {
            super(DFS);
        }

        @Override
        protected void init(SolverParameters parameters) {
            toProcess = new DFSSolverCollection();
        }

        private static class DFSSolverCollection extends BasicBruteforceSolverCollection {

            @Override
            public State popState() {
                return collection.removeLast();
            }

            @Override
            public State peekState() {
                return collection.peekLast();
            }
        }
    }

    private static class BFSSolver extends BruteforceSolver {

        public BFSSolver() {
            super(BFS);
        }

        @Override
        protected void init(SolverParameters parameters) {
            toProcess = new BFSSolverCollection();
        }

        private static class BFSSolverCollection extends BasicBruteforceSolverCollection {

            @Override
            public State popState() {
                return collection.removeFirst();
            }

            @Override
            public State peekState() {
                return collection.peekFirst();
            }

        }
    }
}
