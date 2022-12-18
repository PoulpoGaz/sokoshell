package fr.valax.sokoshell.solver;

import fr.valax.sokoshell.utils.SizeOf;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * This class is the base for bruteforce-based solvers, i.e. solvers that use an exhaustive search to try and find a
 * solution.
 * @author darth-mole
 */
public abstract class BruteforceSolver<S extends State> extends AbstractSolver implements Trackable {

    protected SolverCollection<S> toProcess;
    protected final Set<State> processed = new HashSet<>();

    protected Map map;

    private boolean running = false;
    private boolean stopped = false;

    // statistics
    private long timeStart = -1;
    private long timeEnd = -1;
    private int nStateProcessed = -1;
    private int queueSize = -1;
    private Tracker tracker;
    
    public BruteforceSolver(String name) {
        super(name);
    }

    /**
     * Instantiates the {@link BruteforceSolver#toProcess} attribute, depending on the solver type:
     * <ul>
     *     <li>DFS: stack</li>
     *     <li>BFS: queue</li>
     *     <li>A*: priority queue</li>
     * </ul>
     */
    protected abstract void createCollection();

    @Override
    public SolverReport solve(SolverParameters params) {
        Objects.requireNonNull(params);

        // init statistics, timeout and stop
        String endStatus = null;

        running = true;
        stopped = false;

        long timeout = (long) params.get("timeout").getOrDefault();
        long maxRam = (long) params.get("max-ram").getOrDefault();
        boolean detailed = (boolean) params.get("detailed").getOrDefault();

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

        final State initialState = level.getInitialState();
        State finalState = null;

        int nState = initialState.cratesIndices().length;

        map = level.getMap();
        map.removeStateCrates(initialState);
        map.initForSolver();

        createCollection();
        processed.clear();

        addInitialState(level);

        while (!toProcess.isEmpty() && !stopped) {
            if (hasTimedOut(timeout)) {
                endStatus = SolverReport.TIMEOUT;
                break;
            }

            if (hasRamExceeded(maxRam, detailed, nState)) {
                endStatus = SolverReport.RAM_EXCEED;
                break;
            }

            S state = toProcess.peekAndCacheState();
            map.addStateCratesAndAnalyse(state);

            if (map.isCompletedWith(state)) {
                finalState = state;
                break;
            }

            if (!checkFreezeDeadlock(map, state)) {
                addChildrenStates();
            }

            map.removeStateCratesAndReset(state);
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

        System.out.println("END: " + finalState + " - " + endStatus);

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

    protected abstract void addInitialState(Level level);

    private void addChildrenStates() {
        S state = toProcess.cachedState();
        map.findReachableCases(state.playerPos());

        int[] cratesIndices = state.cratesIndices();
        for (int crateIndex = 0; crateIndex < cratesIndices.length; crateIndex++) {

            int crate = cratesIndices[crateIndex];
            int crateX = map.getX(crate);
            int crateY = map.getY(crate);

            TileInfo crateTile = map.getAt(crate);

            // check if the crate is already at his destination
            if (map.isGoalRoomLevel() && crateTile.isInARoom()) {
                Room r = crateTile.getRoom();

                if (r.isGoalRoom() && r.getPackingOrderIndex() >= 0) {
                    continue;
                }
            }

            Tunnel tunnel = crateTile.getTunnel();
            if (tunnel != null) {
                addChildrenStatesInTunnel(crateIndex, map.getAt(crateX, crateY));
            } else {
                addChildrenStatesDefault(crateIndex, crateX, crateY);
            }
        }
    }

    private void addChildrenStatesInTunnel(int crateIndex, TileInfo crate) {
        // the crate is in a tunnel. two possibilities: move to tunnel.startOut or tunnel.endOut
        // this part of the code assume that there is no other crate in the tunnel.
        // normally, this is impossible...

        for (Direction pushDir : Direction.VALUES) {
            TileInfo player = crate.adjacent(pushDir.negate());

            if (player.isReachable()) {
                TileInfo dest = crate.getTunnelExit().getExit(pushDir);

                if (dest != null && !dest.isSolid()) {
                    addStateCheckForGoalMacro(crateIndex, crate.getX(), crate.getY(), dest);
                }
            }
        }
    }

    private void addChildrenStatesDefault(int crateIndex, int crateX, int crateY) {
        for (Direction d : Direction.VALUES) {

            final int crateDestX = crateX + d.dirX();
            final int crateDestY = crateY + d.dirY();
            if (!map.caseExists(crateDestX, crateDestY)
                    || !map.isTileEmpty(crateDestX, crateDestY)) {
                continue; // The destination case is not empty
            }

            if (map.getAt(crateDestX, crateDestY).isDeadTile()) {
                continue; // Useless to push a crate on a dead position
            }

            final int playerX = crateX - d.dirX();
            final int playerY = crateY - d.dirY();
            if (!map.caseExists(playerX, playerY)
                    || !map.getAt(playerX, playerY).isReachable()
                    || !map.isTileEmpty(playerX, playerY)) {
                continue; // The player cannot reach the case to push the crate
            }


            TileInfo dest = map.getAt(crateDestX, crateDestY);

            // check for tunnel
            Tunnel tunnel = dest.getTunnel();

            // the crate will be pushed inside the tunnel
            if (tunnel != null) {
                if (tunnel.crateInside()) { // pushing inside will lead to a corral deadlock
                    continue;
                }

                // ie the crate can't be pushed to the other extremities of the tunnel
                // however, sometimes (boxxle 24) it is useful to push the crate inside
                // the tunnel. That's why the second addState is done (after this if)
                if (!tunnel.isPlayerOnlyTunnel()) {
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

                    if (newDest != null && !newDest.isDeadTile()) {
                        addStateCheckForGoalMacro(crateIndex, crateX, crateY, newDest);
                    }
                }
            }

            addStateCheckForGoalMacro(crateIndex, crateX, crateY, dest);
        }
    }

    private void addStateCheckForGoalMacro(int crateIndex, int crateX, int crateY, TileInfo dest) {
        Room room = dest.getRoom();
        if (room != null && map.isGoalRoomLevel() && room.getPackingOrderIndex() >= 0) {
            // goal macro!
            TileInfo newDest = room.getPackingOrder().get(room.getPackingOrderIndex());

            addState(crateIndex, crateX, crateY, newDest.getX(), newDest.getY());
        } else {
            addState(crateIndex, crateX, crateY, dest.getX(), dest.getY());
        }
    }

    /**
     * Add a state to the processed set. If it wasn't already added, it is added to
     * the toProcess queue. The move is unchecked
     *
     * @param crateIndex the crate's index that moves
     * @param crateX old crate x
     * @param crateY old crate y
     * @param crateDestX new crate x
     * @param crateDestY new crate y
     */
    protected abstract void addState(int crateIndex, int crateX, int crateY, int crateDestX, int crateDestY);

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

    @Override
    public List<SolverParameter> getParameters() {
        return List.of(new SolverParameter.Long("timeout", -1),
                new SolverParameter.RamParameter("max-ram", -1),
                new SolverParameter.Boolean("detailed", false));
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
    public State currentState() {
        return toProcess.cachedState();
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

}