package fr.valax.sokoshell.solver;

import java.util.*;

/**
 * @author darth-mole
 *
 * This class is the base for bruteforce-based solvers, i.e. solvers that use an exhaustive search to try and find a
 * solution. It serves as a base class for DFS and BFS solvers, as these class are nearly the same -- the only
 * difference being in the order in which they treat the states (LIFO for DFS and FIFO for BFS).
 *
 */
public abstract class BasicBrutalSolver extends AbstractSolver {

    public static DFSSolver newDFSSolver() {
        return new DFSSolver();
    }

    public static BFSSolver newBFSSolver() {
        return new BFSSolver();
    }

    protected final ArrayDeque<State> toProcess = new ArrayDeque<>();
    protected final Set<State> processed = new HashSet<>();

    private Solution solution;

    protected abstract State getNext();

    @Override
    public SolverStatus solve(Level level) {
        solution = null;

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

            if (checkFreezeDeadlock(map, cur)) {
                map.removeStateCrates(cur);
                continue;
            }

            if (map.isCompletedWith(cur)) {
                finalState = cur;
                break;
            }

            addChildrenStates(cur, map);

            map.removeStateCrates(cur);
        }

        if (finalState == null) {
            return SolverStatus.NO_SOLUTION;
        } else {
            solution = buildSolution(finalState);

            return SolverStatus.SOLUTION_FOUND;
        }
    }

    private void addChildrenStates(State cur, Map map) {
        findAccessibleCases(cur.playerPos(), map);

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

    @Override
    public Solution getSolution() {
        return solution;
    }

    @Override
    public Set<State> getProcessed() {
        return Collections.unmodifiableSet(processed);
    }

    private static class DFSSolver extends BasicBrutalSolver {

        @Override
        protected State getNext() {
            return toProcess.removeLast(); // LIFO
        }
    }

    private static class BFSSolver extends BasicBrutalSolver {
        @Override
        protected State getNext() {
            return toProcess.removeFirst(); // FIFO
        }
    }
}
