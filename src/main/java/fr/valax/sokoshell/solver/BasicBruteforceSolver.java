package fr.valax.sokoshell.solver;

import java.util.ArrayDeque;

/**
 * This class serves as a base class for DFS and BFS solvers, as these class are nearly the same -- the only
 * difference being in the order in which they treat the states (LIFO for DFS and FIFO for BFS).
 */
public abstract class BasicBruteforceSolver extends BruteforceSolver<State> {

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
        final int i = map.topLeftReachablePosition(crateX, crateY, crateDestX, crateDestY);
        // The new player position is the crate position
        State s = toProcess.curCachedState().child(i, crateIndex,  crateDestY * map.getWidth() + crateDestX);

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

        protected State cur;

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
        public State popAndCacheState() {
            cur = popState();
            return cur;
        }

        @Override
        public State curCachedState() {
            return cur;
        }
    }

    private static class DFSSolver extends BasicBruteforceSolver  {

        @Override
        protected void createCollection() {
            toProcess = new DFSSolverCollection();
        }

        @Override
        public SolverType getSolverType() {
            return SolverType.DFS;
        }

        private static class DFSSolverCollection extends BasicBruteforceSolverCollection {

            @Override
            public State popState() {
                return collection.removeLast();
            }

            @Override
            public State topState() {
                return collection.peekLast();
            }
        }
    }

    private static class BFSSolver extends BasicBruteforceSolver {

        @Override
        protected void createCollection() {
            toProcess = new BFSSolverCollection();
        }

        @Override
        public SolverType getSolverType() {
            return SolverType.BFS;
        }

        private static class BFSSolverCollection extends BasicBruteforceSolverCollection {

            @Override
            public State popState() {
                return collection.removeFirst();
            }

            @Override
            public State topState() {
                return collection.peekFirst();
            }

        }
    }
}
