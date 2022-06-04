package fr.valax.sokoshell.solver;

import java.util.Set;

/**
 * @author darth-mole
 * @author PoulpoGaz
 */
public interface Solver {

    SolverStatus solve(Level level);

    Solution getSolution();

    void pause();

    void resume();

    void stop();

    Set<State> getProcessed();
}
