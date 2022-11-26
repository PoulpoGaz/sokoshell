package fr.valax.sokoshell.solver;

import fr.valax.sokoshell.utils.SizeOf;

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
    private Map map;

    private boolean running = false;
    private boolean stopped = false;

    // statistics
    private long timeStart = -1;
    private long timeEnd = -1;
    private int nStateProcessed = -1;
    private int queueSize = -1;
    private Tracker tracker;

    protected abstract State getNext();

    @Override
    public SolverReport solve(SolverParameters params) {
        Objects.requireNonNull(params);

        // init statistics, timeout and stop
        String endStatus = null;

        running = true;
        stopped = false;

        long timeout = params.getLong(SolverParameters.TIMEOUT, -1);
        long maxRam = params.getLong(SolverParameters.MAX_RAM, -1);
        boolean detailed = params.getBoolean("detailed", false);

        if (detailed) {
            SizeOf.initialize();
        }

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

        map = level.getMap();
        map.removeStateCrates(initialState);
        map.initForSolver();

        toProcess.clear();
        processed.clear();
        toProcess.add(initialState);

        while (!toProcess.isEmpty() && !stopped) {
            if (hasTimedOut(timeout)) {
                endStatus = SolverReport.TIMEOUT;
                break;
            }

            if (hasRamExceeded(maxRam, detailed, nState)) {
                endStatus = SolverReport.RAM_EXCEED;
                break;
            }

            State cur = getNext();
            map.addStateCratesAndAnalyse(cur);

            if (map.isCompletedWith(cur)) {
                finalState = cur;
                break;
            }

            if (!checkFreezeDeadlock(map, cur)) {
                addChildrenStates(cur);
            }

            map.removeStateCratesAndReset(cur);
        }

        // END OF RESEARCH

        timeEnd = System.currentTimeMillis();
        nStateProcessed = processed.size();
        queueSize = toProcess.size();

        // 'free' ram
        processed.clear();
        toProcess.clear();
        map = null;

        running = false;

        System.out.println("END: " + finalState);

        if (endStatus != null) {
            return SolverReport.withoutSolution(params, getStatistics(), endStatus);
        } else if (stopped) {
            return SolverReport.withoutSolution(params, getStatistics(), SolverReport.STOPPED);
        } else if (finalState != null) {
            return SolverReport.withSolution(finalState, params, getStatistics());
        } else {
            return SolverReport.withoutSolution(params, getStatistics(), SolverReport.NO_SOLUTION);
        }
    }

    private void addChildrenStates(State cur) {
        map.findReachableCases(cur.playerPos());

        int[] cratesIndices = cur.cratesIndices();
        for (int crateIndex = 0; crateIndex < cratesIndices.length; crateIndex++) {

            int crate = cratesIndices[crateIndex];
            int crateX = map.getX(crate);
            int crateY = map.getY(crate);

            Tunnel tunnel = map.getAt(crateX, crateY).getTunnel();
            if (tunnel != null) {
                addChildrenStatesInTunnel(cur, crateIndex, map.getAt(crateX, crateY));
            } else {
                addChildrenStatesDefault(cur, crateIndex, crateX, crateY);
            }
        }
    }


    private void addChildrenStatesInTunnel(State ancestor, int crateIndex, TileInfo crate) {
        // the crate is in a tunnel. two possibilities: move to tunnel.startOut or tunnel.endOut
        // this part of the code assume that there is no other crate in the tunnel.
        // normally, this is impossible...

        Tunnel t = crate.getTunnel();
        for (Direction pushDir : Direction.VALUES) {
            TileInfo player = crate.adjacent(pushDir.negate());

            if (player.isReachable()) {
                TileInfo dest = findEnd(t, crate, pushDir);

                if (dest != null && !dest.isSolid()) {
                    addState(ancestor, crateIndex, crate.getX(), crate.getY(), dest.getX(), dest.getY());
                }
            }
        }
    }

    private TileInfo findEnd(Tunnel tunnel, TileInfo start, Direction dir) {
        map.getMarkSystem().unmarkAll();
        start.adjacent(dir.negate()).mark();

        TileInfo end = start;
        while (end.getTunnel() == tunnel) {
            end.mark();

            boolean newEnd = false;
            for (Direction d : Direction.VALUES) {
                TileInfo t = end.adjacent(d);
                if (!t.isMarked() && !t.isSolid()) {
                    end = t;
                    newEnd = true;
                    break;
                }
            }

            if (!newEnd) {
                return null;
            }
        }

        return end;
    }

    private void addChildrenStatesDefault(State ancestor, int crateIndex, int crateX, int crateY) {
        for (Direction d : Direction.VALUES) {

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


            // check for tunnel
            TileInfo dest = map.getAt(crateDestX, crateDestY);
            Tunnel tunnel = dest.getTunnel();

            // the crate will be pushed inside the tunnel
            if (tunnel != null) {
                if (tunnel.isPlayerOnlyTunnel() || tunnel.crateInside()) {
                    continue;
                }

                TileInfo newDest = null;
                if (dest == tunnel.getStart()) {
                    if (tunnel.getEndOut() != null && !tunnel.getEndOut().anyCrate()) {
                        newDest = tunnel.getEndOut();
                    }
                } else {
                    if (tunnel.getStartOut() != null && !tunnel.getStartOut().anyCrate()) {
                        newDest = tunnel.getStartOut();
                    }
                }

                if (newDest != null) {
                    addState(ancestor, crateIndex, crateX, crateY, newDest.getX(), newDest.getY());
                }
            }

            addState(ancestor, crateIndex, crateX, crateY, crateDestX, crateDestY);
        }
    }

    /**
     * Add a state to the processed set. If it wasn't already added, it is added to
     * the toProcess queue. The move is unchecked
     *
     * @param ancestor the ancestor of the new state
     * @param crateIndex the crate's index that moves
     * @param crateX old crate x
     * @param crateY old crate y
     * @param crateDestX new crate x
     * @param crateDestY new crate y
     */
    private void addState(State ancestor, int crateIndex, int crateX, int crateY, int crateDestX, int crateDestY) {
        final int i = map.topLeftReachablePosition(crateX, crateY, crateDestX, crateDestY);
        // The new player position is the crate position
        State s = State.child(ancestor, i, crateIndex, crateDestY * map.getWidth() + crateDestX);

        if (processed.add(s)) {
            toProcess.add(s);
        }
    }

    protected boolean hasTimedOut(long timeout) {
        return timeout > 0 && timeout + timeStart < System.currentTimeMillis();
    }

    protected boolean hasRamExceeded(long maxRam, boolean detailed, int nCrate) {
        if (maxRam > 0) {
            if (detailed) {
                return SizeOf.approxSizeOf(processed, nCrate) >= maxRam;
            } else {
                return SizeOf.approxSizeOf2(processed, nCrate) >= maxRam;
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
