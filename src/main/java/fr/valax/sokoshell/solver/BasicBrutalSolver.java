package fr.valax.sokoshell.solver;

import fr.valax.sokoshell.utils.PerformanceMeasurer;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * This class is the base for bruteforce-based solvers, i.e. solvers that use an exhaustive search to try and find a
 * solution. It serves as a base class for DFS and BFS solvers, as these class are nearly the same -- the only
 * difference being in the order in which they treat the states (LIFO for DFS and FIFO for BFS).
 * @author darth-mole
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

    private boolean running = false;
    private boolean stopped = false;

    // statistics
    private long timeStart = -1;
    private long timeEnd = -1;
    private int nStateProcessed = -1;
    private int queueSize = -1;
    private Tracker tracker;

    // debug
    private final PerformanceMeasurer measurer = new PerformanceMeasurer();

    protected abstract State getNext();

    @Override
    public SolverReport solve(SolverParameters params) {
        Objects.requireNonNull(params);

        // init statistics, timeout and stop
        SolverStatus endStatus = null;

        running = true;
        stopped = false;

        long timeout = params.getLong(SolverParameters.TIMEOUT, -1);
        long maxRam = params.getLong(SolverParameters.MAX_RAM, -1);
        int stateSize = params.getInt(SolverParameters.STATE_SIZE, -1);
        int arraySize = params.getInt(SolverParameters.ARRAY_SIZE, -1);

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

        int nState = initialState.cratesIndices().length;

        Map map = level.getMap();
        map.removeStateCrates(initialState);

        map.computeDeadTiles();

        toProcess.clear();
        processed.clear();
        toProcess.add(initialState);

        measurer.reset();

        while (!toProcess.isEmpty() && !stopped) {
            if (hasTimedOut(timeout)) {
                endStatus = SolverStatus.TIMEOUT;
                break;
            }

            if (hasRamExceeded(maxRam, stateSize, arraySize, nState)) {
                endStatus = SolverStatus.RAM_EXCEED;
                break;
            }

            State cur = getNext();
            map.addStateCrates(cur);

            if (map.isCompletedWith(cur)) {
                finalState = cur;
                break;
            }

            measurer.start("freeze");
            boolean freeze = checkFreezeDeadlock(map, cur);
            measurer.end("freeze");

            if (!freeze) {
                addChildrenStates(cur, map);
            }

            map.removeStateCrates(cur);
        }

        // END OF RESEARCH

        System.out.println(measurer);

        timeEnd = System.currentTimeMillis();
        nStateProcessed = processed.size();
        queueSize = toProcess.size();

        // 'free' ram
        processed.clear();
        toProcess.clear();

        running = false;

        if (endStatus != null) {
            return SolverReport.withoutSolution(params, getStatistics(), endStatus);
        } else if (stopped) {
            return SolverReport.withoutSolution(params, getStatistics(), SolverStatus.STOPPED);
        } else if (finalState != null) {
            return SolverReport.withSolution(finalState, params, getStatistics());
        } else {
            return SolverReport.withoutSolution(params, getStatistics(), SolverStatus.NO_SOLUTION);
        }
    }

    private void addChildrenStates(State cur, Map map) {
        measurer.start("reachable");
        map.findReachableCases(cur.playerPos());
        measurer.end("reachable");

        measurer.start("child");
        int[] cratesIndices = cur.cratesIndices();
        for (int crateIndex = 0; crateIndex < cratesIndices.length; crateIndex++) {

            int crate = cratesIndices[crateIndex];
            int crateX = map.getX(crate);
            int crateY = map.getY(crate);

            for (Direction d : Direction.values()) {

                final int crateDestX = crateX + d.dirX();
                final int crateDestY = crateY + d.dirY();
                if (!map.caseExists(crateDestX, crateDestY)
                 || !map.isTileEmpty(crateDestX, crateDestY)) {
                    continue; // The destination case is not empty
                }

                if (map.getAt(crateX, crateY).isDeadTile()) {
                    continue; // Useless to push a crate on a dead position
                }

                final int playerX = crateX - d.dirX();
                final int playerY = crateY - d.dirY();
                if (!map.caseExists(playerX, playerY)
                 || !map.getAt(playerX, playerY).isReachable()
                 || !map.isTileEmpty(playerX, playerY)) {
                    continue; // The player cannot reach the case to push the crate
                }

                final int i = map.topLeftReachablePosition(crateX, crateY, crateDestX, crateDestY);
                // The new player position is the crate position
                State s = State.child(cur, i, crateIndex, crateDestY * map.getWidth() + crateDestX);

                measurer.start("add");
                if (processed.add(s)) {
                    toProcess.add(s);
                }
                measurer.end("add");
            }
        }

        measurer.end("child");
    }

    protected boolean hasTimedOut(long timeout) {
        return timeout > 0 && timeout + timeStart < System.currentTimeMillis();
    }

    protected boolean hasRamExceeded(long maxRam, int arraySize, int stateSize, int nCrate) {
        if (maxRam > 0) {
            long totalState = toProcess.size() + processed.size();

            if (arraySize > 0 && stateSize > 0) {
                return State.approxSize(arraySize, stateSize, nCrate) * totalState >= maxRam;
            } else {
                return State.approxSize(nCrate) * totalState >= maxRam;
            }
        }

        return false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public boolean stop() {
        stopped = true;
        return true;
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
        return tracker;
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

        @Override
        public State currentState() {
            return toProcess.peekLast();
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

        @Override
        public State currentState() {
            return toProcess.peekFirst();
        }
    }
}
