package fr.valax.sokoshell.solver;

/**
 * @author darth-mole
 * @author PoulpoGaz
 */
public interface Solver {

    SolverStatus solve(Level level);

    void pause();

    void resume();

    void stop();
}
