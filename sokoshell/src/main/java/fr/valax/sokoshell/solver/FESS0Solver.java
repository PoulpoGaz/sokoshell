package fr.valax.sokoshell.solver;

import fr.valax.sokoshell.solver.board.Direction;
import fr.valax.sokoshell.solver.board.tiles.TileInfo;
import fr.valax.sokoshell.solver.collections.SolverCollection;
import fr.valax.sokoshell.solver.heuristic.GreedyHeuristic;
import fr.valax.sokoshell.solver.heuristic.Heuristic;

import java.util.PriorityQueue;

public class FESS0Solver extends AbstractSolver<FESS0Solver.FESS0State> {

    private Heuristic heuristic;
    private int lowerBound;

    public FESS0Solver() {
        super("fess0");
    }

    @Override
    protected void init(SolverParameters parameters) {
        heuristic = new GreedyHeuristic(board);
        toProcess = new SolverPriorityQueue();
    }

    @Override
    protected void addInitialState(Level level) {
        CorralDetector detector = board.getCorralDetector();
        State s = level.getInitialState();

        board.addStateCrates(s);
        detector.findCorral(board, s.playerPos() % level.getWidth(), s.playerPos() / level.getWidth());
        board.removeStateCrates(s);

        lowerBound = heuristic.compute(s);

        FESS0State state = new FESS0State(s, 0, lowerBound, detector.getRealNumberOfCorral(), countPackedCrate(s));

        toProcess.addState(state);
    }

    @Override
    protected void addState(TileInfo crate, TileInfo crateDest, Direction pushDir) {
        if (checkDeadlockBeforeAdding(crate, crateDest, pushDir)) {
            return;
        }

        final int i = board.topLeftReachablePosition(crate, crateDest);
        // The new player position is the crate position
        FESS0State s = toProcess.cachedState().child(i, crate.getCrateIndex(), crateDest.getIndex());
        s.setHeuristic(heuristic.compute(s));
        s.setConnectivity(board.getCorralDetector().getRealNumberOfCorral());
        s.setPacking(countPackedCrate(s));

        if (processed.add(s)) {
            toProcess.addState(s);
        }
    }

    private int countPackedCrate(State state) {
        int nPacked = 0;
        for (int crate : state.cratesIndices()) {
            TileInfo tile = board.getAt(crate);
            if (tile.isTarget() || tile.isCrateOnTarget()) {
                nPacked++;
            }
        }

        return nPacked;
    }

    @Override
    public int lowerBound() {
        return lowerBound;
    }

    private static class SolverPriorityQueue extends PriorityQueue<FESS0State>
            implements SolverCollection<FESS0State> {

        private FESS0State cachedState;

        @Override
        public void addState(FESS0State state) {
            offer(state);
        }

        @Override
        public FESS0State popState() {
            return poll();
        }

        @Override
        public FESS0State peekState() {
            return peek();
        }

        @Override
        public FESS0State peekAndCacheState() {
            cachedState = popState();
            return cachedState;
        }

        @Override
        public FESS0State cachedState() {
            return cachedState;
        }
    }

    protected static class FESS0State extends WeightedState implements Comparable<FESS0State> {

        private int connectivity;
        private int packing;

        public FESS0State(int playerPos, int[] cratesIndices, int hash, State parent, int cost, int heuristic) {
            super(playerPos, cratesIndices, hash, parent, cost, heuristic);
        }

        public FESS0State(State state, int cost, int heuristic, int connectivity, int packing) {
            super(state, cost, heuristic);
            this.connectivity = connectivity;
            this.packing = packing;
        }

        @Override
        public FESS0State child(int newPlayerPos, int crateToMove, int crateDestination) {
            return new FESS0State(super.child(newPlayerPos, crateToMove, crateDestination),
                    cost(), 0, 0, 0);
        }

        public int getConnectivity() {
            return connectivity;
        }

        public void setConnectivity(int connectivity) {
            this.connectivity = connectivity;
        }

        public int getPacking() {
            return packing;
        }

        public void setPacking(int packing) {
            this.packing = packing;
        }

        @Override
        public int compareTo(FESS0State o) {
            // compare in reverse order because
            // java PriorityQueue is a min-queue
            return compare(o, this);
        }

        private static int compare(FESS0State a, FESS0State b) {
            // -1 if this < o
            //  0 if this = o
            //  1 if this > o
            if (a.packing > b.packing) {
                return 1; // we want to maximize packing
            } else if (a.packing < b.packing) {
                return -1;
            } else {
                if (a.connectivity < b.connectivity) {
                    return 1; // we want to minimize connectivity
                } else if (a.connectivity > b.connectivity) {
                    return -1;
                } else {
                    return Integer.compare(a.weight(), b.weight());
                }
            }
        }
    }
}
