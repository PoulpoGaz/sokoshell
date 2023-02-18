package fr.valax.sokoshell.solver;

import fr.valax.sokoshell.solver.board.*;
import fr.valax.sokoshell.solver.board.tiles.TileInfo;
import fr.valax.sokoshell.solver.collections.SolverCollection;
import fr.valax.sokoshell.utils.SizeOf;

import java.util.*;

/**
 * This class is the base for bruteforce-based solvers, i.e. solvers that use an exhaustive search to try and find a
 * solution.
 * @author darth-mole
 */
public abstract class AbstractSolver<S extends State> implements Trackable, Solver {

    protected static final String TIMEOUT = "timeout";
    protected static final String MAX_RAM = "max-ram";
    protected static final String ACCURATE = "accurate";

    protected final String name;

    protected SolverCollection<S> toProcess;
    protected final Set<State> processed = new HashSet<>();

    protected MutableBoard board;

    private boolean running = false;
    private boolean stopped = false;

    // statistics
    private long timeStart = -1;
    private long timeEnd = -1;
    private int nStateProcessed = -1;
    private int queueSize = -1;
    private Tracker tracker;

    public AbstractSolver(String name) {
        this.name = name;
    }

    @Override
    public SolverReport solve(SolverParameters params) {
        Objects.requireNonNull(params);

        // init statistics, timeout and stop
        String endStatus = null;

        running = true;
        stopped = false;

        long timeout = params.getArgument(TIMEOUT);
        long maxRam = params.getArgument(MAX_RAM);
        boolean accurate = params.getArgument(ACCURATE);

        if (accurate) {
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

        State.initZobristValues(level.getWidth() * level.getHeight());

        final State initialState = level.getInitialState();
        State finalState = null;

        int nState = initialState.cratesIndices().length;

        board = new MutableBoard(level);
        board.removeStateCrates(initialState);
        board.initForSolver();

        init(params);
        processed.clear();

        addInitialState(level);

        while (!toProcess.isEmpty() && !stopped) {
            if (hasTimedOut(timeout)) {
                endStatus = SolverReport.TIMEOUT;
                break;
            }

            if (hasRamExceeded(maxRam, accurate, nState)) {
                endStatus = SolverReport.RAM_EXCEED;
                break;
            }

            S state = toProcess.peekAndCacheState();
            board.addStateCratesAndAnalyse(state);

            if (board.isCompletedWith(state)) {
                finalState = state;
                break;
            }

            if (FreezeDeadlockDetector.checkFreezeDeadlock(board, state)) {
                board.removeStateCratesAndReset(state);
                continue;
            }

            int playerX = board.getX(state.playerPos());
            int playerY = board.getY(state.playerPos());

            CorralDetector detector = board.getCorralDetector();
            detector.findCorral(board, playerX, playerY);

            if (checkPICorralDeadlock(state)) {
                board.removeStateCratesAndReset(state);
                continue;
            }

            addChildrenStates();
            board.removeStateCratesAndReset(state);
        }

        // END OF RESEARCH

        timeEnd = System.currentTimeMillis();
        nStateProcessed = processed.size();
        queueSize = toProcess.size();

        // 'free' ram
        processed.clear();
        toProcess.clear();
        board = null;

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

    /**
     * Initialize the solver. This method is called after the initialization of
     * the board
     */
    protected abstract void init(SolverParameters parameters);

    protected abstract void addInitialState(Level level);

    protected boolean checkPICorralDeadlock(State state) {
        CorralDetector detector = board.getCorralDetector();
        detector.findPICorral(board, state.cratesIndices());

        for (Corral corral : detector.getCorrals()) {
            if (corral.isPICorral()) {
                if (corral.isDeadlock(state)) {
                    return true;
                }
            }
        }

        return false;
    }

    protected void addChildrenStates() {
        S state = toProcess.cachedState();

        int[] cratesIndices = state.cratesIndices();
        for (int crateIndex = 0; crateIndex < cratesIndices.length; crateIndex++) {

            int crate = cratesIndices[crateIndex];

            TileInfo crateTile = board.getAt(crate);

            // check if the crate is already at his destination
            if (board.isGoalRoomLevel() && crateTile.isInARoom()) {
                Room r = crateTile.getRoom();

                if (r.isGoalRoom() && r.getPackingOrderIndex() >= 0) {
                    continue;
                }
            }

            Tunnel tunnel = crateTile.getTunnel();
            if (tunnel != null) {
                addChildrenStatesInTunnel(crateIndex, crateTile);
            } else {
                addChildrenStatesDefault(crateIndex, crateTile);
            }
        }
    }

    protected void addChildrenStatesInTunnel(int crateIndex, TileInfo crate) {
        // the crate is in a tunnel. two possibilities: move to tunnel.startOut or tunnel.endOut
        // this part of the code assume that there is no other crate in the tunnel.
        // normally, this is impossible...

        for (Direction pushDir : Direction.VALUES) {
            TileInfo player = crate.adjacent(pushDir.negate());

            if (player.isReachable()) {
                TileInfo dest = crate.getTunnelExit().getExit(pushDir);

                if (dest != null && !dest.isSolid()) {
                    addStateCheckForGoalMacro(crateIndex, crate, dest);
                }
            }
        }
    }

    protected void addChildrenStatesDefault(int crateIndex, TileInfo crate) {
        for (Direction d : Direction.VALUES) {

            TileInfo crateDest = crate.adjacent(d);
            if (crateDest.isSolid()) {
                continue; // The destination case is not empty
            }

            if (crateDest.isDeadTile()) {
                continue; // Useless to push a crate on a dead position
            }

            TileInfo player = crate.adjacent(d.negate());
            if (!player.isReachable()) {
                // The player cannot reach the case to push the crate
                // also checks if tile is solid: a solid tile is never reachable
                continue;
            }


            // check for tunnel
            Tunnel tunnel = crateDest.getTunnel();

            // the crate will be pushed inside the tunnel
            if (tunnel != null) {
                if (tunnel.crateInside()) { // pushing inside will lead to a corral deadlock
                    continue;
                }

                // ie the crate can't be pushed to the other extremities of the tunnel
                // however, sometimes (boxxle 24) it is useful to push the crate inside
                // the tunnel. That's why the second addState is done (after this if)
                // and only if this tunnel isn't oneway
                if (!tunnel.isPlayerOnlyTunnel()) {
                    TileInfo newDest = null;

                    if (crate == tunnel.getStartOut()) {
                        if (tunnel.getEndOut() != null && !tunnel.getEndOut().anyCrate()) {
                            newDest = tunnel.getEndOut();
                        }
                    } else {
                        if (tunnel.getStartOut() != null && !tunnel.getStartOut().anyCrate()) {
                            newDest = tunnel.getStartOut();
                        }
                    }

                    if (newDest != null && !newDest.isDeadTile()) {
                        addStateCheckForGoalMacro(crateIndex, crate, newDest);
                    }
                }

                if (tunnel.isOneway()) {
                    continue;
                }
            }

            addStateCheckForGoalMacro(crateIndex, crate, crateDest);
        }
    }

    protected void addStateCheckForGoalMacro(int crateIndex, TileInfo crate, TileInfo dest) {
        Room room = dest.getRoom();
        if (room != null && board.isGoalRoomLevel() && room.getPackingOrderIndex() >= 0) {
            // goal macro!
            TileInfo newDest = room.getPackingOrder().get(room.getPackingOrderIndex());

            addState(crateIndex, crate, newDest);
        } else {
            addState(crateIndex, crate, dest);
        }
    }

    /**
     * Add a state to the processed set. If it wasn't already added, it is added to
     * the toProcess queue. The move is unchecked
     *
     * @param crateIndex the crate's index that moves
     * @param crate crate to move
     * @param crateDest crate destination
     */
    protected abstract void addState(int crateIndex, TileInfo crate, TileInfo crateDest);

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
    public String getName() {
        return name;
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
        List<SolverParameter> params = new ArrayList<>();
        addParameters(params);
        return params;
    }

    /**
     * Add your parameters to the list returned by {@link #getParameters()}
     * @param parameters parameters that will be returned by {@link #getParameters()}
     */
    protected void addParameters(List<SolverParameter> parameters) {
        parameters.add(new SolverParameter.Long(TIMEOUT, "Maximal runtime of the solver", -1));
        parameters.add(new SolverParameter.RamParameter(MAX_RAM, -1));
        parameters.add(new SolverParameter.Boolean(ACCURATE, "Use a more accurate method to calculate ram usage", false));
    }

    private ISolverStatistics getStatistics() {
        ISolverStatistics stats;

        if (tracker != null) {
            stats = Objects.requireNonNull(tracker.getStatistics(this));
        } else {
            stats = new ISolverStatistics.Basic(timeStart, timeEnd);
        }

        return stats;
    }

    @Override
    public State currentState() {
        if (toProcess != null && running) {
            return toProcess.cachedState();
        } else {
            return null;
        }
    }

    @Override
    public Board staticBoard() {
        if (board != null && running) {
            return board.staticBoard();
        } else {
            return null;
        }
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
        } else if (timeEnd < 0 && toProcess != null) {
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
