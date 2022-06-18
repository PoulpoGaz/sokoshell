package fr.valax.sokoshell.solver;

/**
 * @author darth-mole
 * @author PoulpoGaz
 */
public interface Solver {

    Solution solve(SolverParameters params);

    SolverType getSolverType();

    void stop();
}
