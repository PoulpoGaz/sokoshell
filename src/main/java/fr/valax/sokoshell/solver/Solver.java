package fr.valax.sokoshell.solver;

import java.util.ArrayList;
import java.util.List;

/**
 * @author darth-mole
 * @author PoulpoGaz
 */
public interface Solver {

    SolverStatus solve(Level level, List<State> solution);

    void pause();

    void resume();

    void stop();
}
