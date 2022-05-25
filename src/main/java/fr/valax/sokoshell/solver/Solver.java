package fr.valax.sokoshell.solver;

import java.util.ArrayList;

/**
 * @author darth-mole
 * @author PoulpoGaz
 */
public interface Solver {

    SolverStatus solve(Level level, ArrayList<State> solution);

    void pause();

    void resume();

    void stop();
}
