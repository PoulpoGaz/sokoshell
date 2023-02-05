package fr.valax.sokoshell.solver;

import fr.valax.sokoshell.solver.collections.SolverCollection;

import java.util.ArrayDeque;

/**
 * This class serves as a base class for DFS and BFS solvers, as these class are nearly the same -- the only
 * difference being in the order in which they treat the states (LIFO for DFS and FIFO for BFS).
 */
public abstract class BasicBruteforceSolver extends BruteforceSolver<State> {

    public BasicBruteforceSolver(String name) {
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
    protected void addState(int crateIndex, int crateX, int crateY, int crateDestX, int crateDestY) {
        final int i = board.topLeftReachablePosition(crateX, crateY, crateDestX, crateDestY);
        // The new player position is the crate position
        State s = toProcess.cachedState().child(i, crateIndex, crateDestY * board.getWidth() + crateDestX);

        if (processed.add(s)) {
            toProcess.addState(s);
        }
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

    private static class DFSSolver extends BasicBruteforceSolver  {

        public DFSSolver() {
            super(DFS);
        }

        @Override
        protected void init() {
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

    private static class BFSSolver extends BasicBruteforceSolver {

        public BFSSolver() {
            super(BFS);
        }

        @Override
        protected void init() {
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
