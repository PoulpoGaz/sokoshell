package fr.valax.sokoshell.solver;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public abstract class BasicBrutalSolver extends AbstractSolver {

    public static DFSSolver newDFSSolver() {
        return new DFSSolver();
    }

    public static BFSSolver newBFSSolver() {
        return new BFSSolver();
    }

    protected final ArrayDeque<State> toProcess = new ArrayDeque<>();
    protected boolean[][] accessibleCases;
    protected final HashMap<State, Boolean> processed = new HashMap<>();

    protected abstract State getNext();

    @Override
    public SolverStatus solve(Level level, ArrayList<State> solution) {
        Map map = level.getMap();
        State finalState = null;

        accessibleCases = new boolean[map.getHeight()][map.getWidth()];
        toProcess.clear();
        processed.clear();
        toProcess.add(level.getInitialState());

        while (!toProcess.isEmpty()) {
            State cur = getNext();
            map.addStateCrates(cur);
            if (map.isCompletedWith(cur)) {
                finalState = cur;
                break;
            }
            if (processed.containsKey(cur) && processed.get(cur))
                continue;
            processed.put(cur, true);
            addChildrenStates(cur, map);
            map.removeStateCrates(cur);
        }

        if (finalState == null) {
            return SolverStatus.NO_SOLUTION;
        } else {
            buildSolution(solution, finalState);
            return SolverStatus.SOLUTION_FOUND;
        }
    }

    private void buildSolution(ArrayList<State> solution, State finalState) {
        solution.clear();
        State s = finalState;
        while (s.parent() != null)
        {
            solution.add(s);
            s = s.parent();
        }
        Collections.reverse(solution);
    }

    private void addChildrenStates(State cur, Map map) {
        findAccessibleCases(cur.playerPos(), map);
        for (int crate : cur.cratesIndices()) {
            int crateX = map.getX(crate);
            int crateY = map.getY(crate);

            for (Direction d : Direction.values()) {
                int crateDestX = crateX + d.dirX();
                int crateDestY = crateY + d.dirY();
                int persoX = crateX - d.dirX();
                int persoY = crateY - d.dirY();
                if (crateDestX < 0 || crateDestX >= map.getWidth()
                 || crateDestY < 0 || crateDestY >= map.getHeight()
                 || persoX < 0 || persoX >= map.getWidth()
                 || persoY < 0 || persoY >= map.getHeight()) {
                    continue;
                }

                if (!accessibleCases[persoY][persoX]) {
                    continue;
                }

                if (map.isTileEmpty(crateX - d.dirX(), crateY - d.dirY())) {
                    addNewStateBasedOn(cur, map, crate, crateDestX, crateDestY);
                }
            }
        }
    }

    private void addNewStateBasedOn(State cur, Map map, int crate, int crateDestX, int crateDestY) {
        State s = new State(cur.playerPos(), cur.cratesIndices().clone(), cur);
        s.cratesIndices()[crate] = crateDestY * map.getWidth() + crateDestX;
        toProcess.add(s);
    }

    private void findAccessibleCases(int playerPos, Map map) {
        for (int i = 0; i < accessibleCases.length; i++) {
            for (int j = 0; j < accessibleCases[0].length; j++) {
                accessibleCases[j][i] = false;
            }
        }
        findAccessibleCases_aux(map.getX(playerPos), map.getY(playerPos), map);
    }

    private void findAccessibleCases_aux(int x, int y, Map map) {
        accessibleCases[y][x] = true;
        for (int i = x - 1; i <= x + 1; i++) {
            for (int j = y - 1; j <= y + 1; j++) {
                if (i < 0 || i >= map.getWidth()
                 || j < 0 || j >= map.getHeight()) {
                    continue;
                }
                /* the second part of the condition is here check if it has not already been
                   processed */
                if (map.isTileEmpty(i, j) && !accessibleCases[j][i]) {
                    findAccessibleCases_aux(i, j, map);
                }
            }
        }
    }

    private static class DFSSolver extends BasicBrutalSolver {
        @Override
        protected State getNext() {
            return toProcess.getLast();
        }
    }

    private static class BFSSolver extends BasicBrutalSolver {
        @Override
        protected State getNext() {
            return toProcess.getFirst();
        }
    }
}
