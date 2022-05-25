package fr.valax.sokoshell.solver;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * @author darth-mole
 * @author PoulpoGaz
 */
public abstract class AbstractSolver implements Solver {

    protected final Set<State> visitedStates = new HashSet<>();

    @Override
    public abstract SolverStatus solve(Level level, ArrayList<State> solution);

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void stop() {

    }
}
