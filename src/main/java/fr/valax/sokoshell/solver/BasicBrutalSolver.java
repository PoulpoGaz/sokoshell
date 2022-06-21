package fr.valax.sokoshell.solver;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author darth-mole
 *
 * This class is the base for bruteforce-based solvers, i.e. solvers that use an exhaustive search to try and find a
 * solution. It serves as a base class for DFS and BFS solvers, as these class are nearly the same -- the only
 * difference being in the order in which they treat the states (LIFO for DFS and FIFO for BFS).
 *
 */
public abstract class BasicBrutalSolver extends AbstractSolver implements Trackable {

    public static DFSSolver newDFSSolver() {
        return new DFSSolver();
    }

    public static BFSSolver newBFSSolver() {
        return new BFSSolver();
    }

    protected final ArrayDeque<State> toProcess = new ArrayDeque<>();
    protected final Set<State> processed = new HashSet<>();

    private boolean stopped = false;

    // statistics
    private long timeStart = -1;
    private long timeEnd = -1;
    private int nStateProcessed = -1;
    private int queueSize = -1;
    private Tracker tracker;

    protected abstract State getNext();

    @Override
    public Solution solve(SolverParameters params) {
        // init statistics, timeout and stop

        stopped = false;

        long timeout = getTimeout(params);
        boolean hasTimedOut = false;

        timeStart = System.currentTimeMillis();
        timeEnd = -1;
        nStateProcessed = 0;
        queueSize = 0;

        if (tracker != null) {
            tracker.reset();
        }

        // init the research

        Level level = params.getLevel();

        State initialState = level.getInitialState();
        State finalState = null;

        Map map = new Map(level.getMap());
        map.removeStateCrates(initialState);
        it.setMap(map);
        it.reset();

        map.computeDeadTiles();

        toProcess.clear();
        processed.clear();
        toProcess.add(initialState);

        while (!toProcess.isEmpty() && !stopped) {
            if (!checkTimeout(timeout)) {
                hasTimedOut = true;
                break;
            }

            State cur = getNext();
            map.addStateCrates(cur);

            if (map.isCompletedWith(cur)) {
                finalState = cur;
                break;
            }

            addChildrenStates(cur, map);

            map.removeStateCrates(cur);
        }


        // END OF RESEARCH

        timeEnd = System.currentTimeMillis();
        nStateProcessed = processed.size();
        queueSize = toProcess.size();

        // free ram
        processed.clear();
        toProcess.clear();

        if (hasTimedOut) {
            return create(params, getStatistics(), SolverStatus.TIMEOUT);
        } else if (stopped) {
            return create(params, getStatistics(), SolverStatus.STOPPED);
        } else if (finalState != null) {
            return buildSolution(finalState, params, getStatistics());
        } else {
            return create(params, getStatistics(), SolverStatus.NO_SOLUTION);
        }
    }

    private void addChildrenStates(State cur, Map map) {
        map.findReachableCases(cur.playerPos(), true);

        int[] cratesIndices = cur.cratesIndices();
        for (int crateIndex = 0; crateIndex < cratesIndices.length; crateIndex++) {

            int crate = cratesIndices[crateIndex];
            int crateX = map.getX(crate);
            int crateY = map.getY(crate);

            for (Direction d : Direction.values()) {

                int crateDestX = crateX + d.dirX();
                int crateDestY = crateY + d.dirY();
                if (crateDestX < 0 || crateDestX >= map.getWidth()
                 || crateDestY < 0 || crateDestY >= map.getHeight()
                 || !map.isTileEmpty(crateDestX, crateDestY)) {
                    continue; // The destination case is not empty
                }

                //if (map.getAt(crateX, crateY).isDeadTile()) {
                //    continue; // Useless to push a crate on a dead position
                //}

                int playerX = crateX - d.dirX();
                int playerY = crateY - d.dirY();
                if (playerX < 0 || playerX >= map.getWidth()
                 || playerY < 0 || playerY >= map.getHeight()
                 || !map.getAt(playerX, playerY).isReachable()
                 || !map.isTileEmpty(playerX, playerY)) {
                    continue; // The player cannot reach the case to push the crate
                }

                // The new player position is the crate position
                State s = new State(crate, cur.cratesIndices().clone(), cur);
                s.cratesIndices()[crateIndex] = crateDestY * map.getWidth() + crateDestX;

                if (processed.add(s)) {
                    toProcess.add(s);
                }
            }
        }
    }

    protected boolean checkTimeout(long timeout) {
        return timeout <= 0 || timeout + timeStart > System.currentTimeMillis();
    }

    @Override
    public void stop() {
        stopped = true;
    }

    private SolverStatistics getStatistics() {
        SolverStatistics stats;

        if (tracker != null) {
            stats = Objects.requireNonNull(tracker.getStatistics(this));
        } else {
            stats = new SolverStatistics();
            stats.setTimeStarted(timeStart);
            stats.setTimeEnded(timeEnd);
        }

        return stats;
    }

    @Override
    public int nStateExplored() {
        if (timeStart < 0) {
            return -1;
        } else if (timeEnd < 0) {
            return processed.size();
        } else {
            return nStateProcessed;
        }
    }

    @Override
    public int currentQueueSize() {
        if (timeStart < 0) {
            return -1;
        } else if (timeEnd < 0) {
            return toProcess.size();
        } else {
            return queueSize;
        }
    }

    @Override
    public long timeStarted() {
        return timeStart;
    }

    @Override
    public long timeEnded() {
        return timeEnd;
    }

    @Override
    public void setTacker(Tracker tracker) {
        this.tracker = tracker;
    }

    @Override
    public Tracker getTracker() {
        return null;
    }

    private static class DFSSolver extends BasicBrutalSolver {

        @Override
        protected State getNext() {
            return toProcess.removeLast(); // LIFO
        }

        @Override
        public SolverType getSolverType() {
            return SolverType.DFS;
        }
    }

    private static class BFSSolver extends BasicBrutalSolver {
        @Override
        protected State getNext() {
            return toProcess.removeFirst(); // FIFO
        }

        @Override
        public SolverType getSolverType() {
            return SolverType.BFS;
        }
    }
}
