package fr.valax.sokoshell.solver;

import javax.sound.midi.Track;
import java.util.*;

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

    // statistics
    private long timeStart = -1;
    private long timeEnd = -1;
    private int nStateProcessed = -1;
    private int queueSize = -1;
    private Tracker tracker;

    protected abstract State getNext();

    @Override
    public Solution solve(SolverParameters params) {
        Level level = params.getLevel();

        timeStart = System.currentTimeMillis();
        timeEnd = -1;

        Map map = new Map(level.getMap());
        State initialState = level.getInitialState();
        State finalState = null;

        map.removeStateCrates(initialState);

        reachableCases = new boolean[map.getHeight()][map.getWidth()];
        toProcess.clear();
        processed.clear();
        toProcess.add(initialState);

        while (!toProcess.isEmpty()) {
            State cur = getNext();
            map.addStateCrates(cur);

            /*if (checkFreezeDeadlock(map, cur)) {
                map.removeStateCrates(cur);
                continue;
            }*/

            if (map.isCompletedWith(cur)) {
                finalState = cur;
                break;
            }

            addChildrenStates(cur, map);

            map.removeStateCrates(cur);
        }

        timeEnd = System.currentTimeMillis();
        nStateProcessed = processed.size();
        queueSize = toProcess.size();

        // free ram
        processed.clear();
        toProcess.clear();

        if (finalState != null) {
            return buildSolution(finalState, params, getStatistics());
        } else {
            return createNoSolution(params, getStatistics());
        }
    }

    private void addChildrenStates(State cur, Map map) {
        findReachableCases(cur.playerPos(), map);

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

                int playerX = crateX - d.dirX();
                int playerY = crateY - d.dirY();
                if (playerX < 0 || playerX >= map.getWidth()
                 || playerY < 0 || playerY >= map.getHeight()
                 || !reachableCases[playerY][playerX]
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


    private SolverStatistics getStatistics() {
        SolverStatistics stats;

        if (tracker != null) {
            stats = Objects.requireNonNull(tracker.getStatistics());
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
