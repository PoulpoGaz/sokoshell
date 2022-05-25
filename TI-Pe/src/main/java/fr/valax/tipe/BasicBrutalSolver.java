package fr.valax.tipe;

import java.util.ArrayDeque;
import java.util.HashMap;

public class BasicBrutalSolver extends AbstractSolver {
    private ArrayDeque<State> toProcess = new ArrayDeque<>();
    private HashMap<State, Boolean> processed = new HashMap<>();

    protected State getNext() { return null; }

    @Override
    public SolverStatus solve(Level level) {
        toProcess.clear();
        toProcess.add(level.getInitialState());
        while (!toProcess.isEmpty()) {
            State cur = getNext();
            if (processed.containsKey(cur) && processed.get(cur))
                continue;
            processed.put(cur, true);
            addChildrenStates(cur, level);
        }
        return SolverStatus.SOLUTION_FOUND;
    }

    private void addChildrenStates(State cur, Level level) {

    }
}
