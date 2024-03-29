package fr.valax.sokoshell.solver;

import fr.valax.sokoshell.graphics.style.BasicStyle;
import fr.valax.sokoshell.solver.board.*;
import fr.valax.sokoshell.solver.board.tiles.TileInfo;
import fr.valax.sokoshell.solver.collections.SolverCollection;
import fr.valax.sokoshell.solver.pathfinder.CrateAStar;
import fr.valax.sokoshell.utils.SizeOf;

import java.io.IOException;
import java.nio.file.Path;
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

    protected final DeadlockTable table;

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

        try {
            table = DeadlockTable.read(Path.of("4x4.table"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

        board = new MutableBoard(level);
        board.removeStateCrates(initialState);
        board.initForSolver();
        board.getCorralDetector().setDeadlockTable(table);

        init(params);
        processed.clear();

        addInitialState(level);

        if (level.getPack().name().equals("XSokoban_90") && level.getIndex() == 3) {
            board.getAt(9, 10).setDeadTile(true);
        }

        while (!toProcess.isEmpty() && !stopped) {
            if (hasTimedOut(timeout)) {
                endStatus = SolverReport.TIMEOUT;
                break;
            }

            if (hasRamExceeded(maxRam, accurate)) {
                endStatus = SolverReport.RAM_EXCEED;
                break;
            }

            S state = toProcess.peekAndCacheState();
            board.addStateCrates(state);

            if (board.isCompletedWith(state)) {
                finalState = state;
                break;
            }

            int playerX = board.getX(state.playerPos());
            int playerY = board.getY(state.playerPos());

            CorralDetector detector = board.getCorralDetector();
            detector.findCorral(board, playerX, playerY);

            if (checkPICorralDeadlock(state)) {
                board.removeStateCrates(state);
                continue;
            }

            // compute after checking for corral deadlock, as corral deadlock deals with tunnels
            board.computeTunnelStatus(state);
            board.computePackingOrderProgress(state);

            addChildrenStates(board.getAt(playerX, playerY));
            board.removeStateCrates(state);
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
            if (corral.isDeadlock(state)) {
                return true;
            }
        }

        return false;
    }

    protected void addChildrenStates(TileInfo player) {
        Corral playerCorral = board.getCorralDetector().findCorral(player);

        List<TileInfo> crates = playerCorral.getCrates();
        for (int i = 0; i < crates.size(); i++) {
            TileInfo crateTile = crates.get(i);

            // check if the crate is already at his destination
            if (board.isGoalRoomLevel() && crateTile.isInARoom()) {
                Room r = crateTile.getRoom();

                if (r.isGoalRoom() && r.getPackingOrderIndex() >= 0) {
                    continue;
                } else {
                    tryGoalCut(crateTile);
                }
            }

            Tunnel tunnel = crateTile.getTunnel();
            if (tunnel != null) {
                addChildrenStatesInTunnel(crateTile);
            } else {
                addChildrenStatesDefault(crateTile);
            }
        }
    }

    protected void tryGoalCut(TileInfo crate) {
        TileInfo player = board.getAt(currentState().playerPos());

        // only works because rooms have one entry
        CrateAStar crateAStar = board.getCrateAStar();
        List<Room> rooms = board.getRooms();
        for (int i = 0; i < rooms.size(); i++) {
            Room r = rooms.get(i);

            Tunnel tunnel = r.getTunnels().get(0);
            TileInfo entrance;
            if (tunnel.getStartOut().getRoom() == r) {
                entrance = tunnel.getStartOut();
            } else {
                entrance = tunnel.getEndOut();
            }

            if (r.isGoalRoom() && r.getPackingOrderIndex() >= 0) {
                if (crateAStar.hasPath(player, null, crate, entrance)) {
                    addStateCheckForGoalMacro(crate, entrance, null);
                }
            }
        }
    }

    protected void addChildrenStatesInTunnel(TileInfo crate) {
        // the crate is in a tunnel. two possibilities: move to tunnel.startOut or tunnel.endOut
        // this part of the code assume that there is no other crate in the tunnel.
        // normally, this is impossible...

        for (Direction pushDir : Direction.VALUES) {
            TileInfo player = crate.adjacent(pushDir.negate());

            if (player.isReachable()) {
                TileInfo dest = crate.getTunnelExit().getExit(pushDir);

                if (dest != null && !dest.isSolid()) {
                    addStateCheckForGoalMacro(crate, dest, pushDir);
                }
            }
        }
    }

    protected void addChildrenStatesDefault(TileInfo crate) {
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
                    Direction pushDir = null;

                    if (crate == tunnel.getStartOut()) {
                        if (tunnel.getEndOut() != null && !tunnel.getEndOut().anyCrate()) {
                            newDest = tunnel.getEndOut();
                            pushDir = tunnel.getEnd().direction(tunnel.getEndOut());
                        }
                    } else {
                        if (tunnel.getStartOut() != null && !tunnel.getStartOut().anyCrate()) {
                            newDest = tunnel.getStartOut();
                            pushDir = tunnel.getStart().direction(tunnel.getStartOut());
                        }
                    }

                    if (newDest != null && !newDest.isDeadTile()) {
                        addStateCheckForGoalMacro(crate, newDest, pushDir);
                    }
                }

                if (tunnel.isOneway()) {
                    continue;
                }
            }

            addStateCheckForGoalMacro(crate, crateDest, d);
        }
    }

    protected void addStateCheckForGoalMacro(TileInfo crate, TileInfo dest, Direction pushDir) {
        Room room = dest.getRoom();
        if (room != null && board.isGoalRoomLevel() && room.getPackingOrderIndex() >= 0) {
            // goal macro!
            TileInfo newDest = room.getPackingOrder().get(room.getPackingOrderIndex());

            addState(crate, newDest, null);
        } else {
            addState(crate, dest, pushDir);
        }
    }

    /**
     * Check if the move leads to a deadlock.
     * Only for simple deadlock that don't require
     * lots of computation like PI Corral deadlock
     *
     * @param crate crate to move
     * @param crateDest crate destination
     * @param pushDir push dir of the player. If the move is a macro move,
     *                it is the last push done by the player. It can be null
     * @return true if deadlock
     */
    protected boolean checkDeadlockBeforeAdding(TileInfo crate, TileInfo crateDest, Direction pushDir) {
        crate.removeCrate();
        crateDest.addCrate();

        boolean deadlock = FreezeDeadlockDetector.checkFreezeDeadlock(crateDest);

        if (!deadlock && pushDir != null) {
            deadlock = table.isDeadlock(crateDest.adjacent(pushDir.negate()), pushDir);
        }

        crate.addCrate();
        crateDest.removeCrate();

        return deadlock;
    }

    /**
     * Add a state to the processed set. If it wasn't already added, it is added to
     * the toProcess queue. The move is unchecked
     *
     * @param crate crate to move
     * @param crateDest crate destination
     * @param pushDir push dir of the player. If the move is a macro move,
     *                it is the last push done by the player. It can be null
     */
    protected abstract void addState(TileInfo crate, TileInfo crateDest, Direction pushDir);

    protected boolean hasTimedOut(long timeout) {
        return timeout > 0 && timeout + timeStart < System.currentTimeMillis();
    }

    protected boolean hasRamExceeded(long maxRam, boolean accurate) {
        if (maxRam > 0) {
            State curr = currentState();

            if (curr != null) {
                long stateSize;
                long ramUsed;
                if (accurate) {
                    stateSize = curr.approxSizeOfAccurate();
                    ramUsed = SizeOf.approxSizeOfAccurate(processed, stateSize);
                } else {
                    stateSize = curr.approxSizeOf();
                    ramUsed = SizeOf.approxSizeOf(processed, stateSize);
                }

                return ramUsed + toProcess.size() * stateSize >= maxRam;
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
