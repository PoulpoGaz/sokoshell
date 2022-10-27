package fr.valax.sokoshell.solver;

/**
 * Defines the basics for all sokoban solver
 *
 * @author darth-mole
 * @author PoulpoGaz
 */
public interface Solver {

    /**
     * Try to solve the sokoban that is in the {@link SolverParameters}.
     * @param params non null solver parameters
     * @return a solution object
     * @see SolverReport
     * @see SolverParameters
     */
    SolverReport solve(SolverParameters params);

    /**
     * @return the type of solver
     * @see SolverType
     */
    SolverType getSolverType();

    /**
     * @return {@code true} if the solver is running
     */
    boolean isRunning();

    /**
     * Try to stop the solver if it is running.
     * When the solver is not running, it does nothing and returns {@code false}.
     * A solver that doesn't support stopping must return {@code false}
     * @return {@code true} if the solver was stopped, or if it registers the stop action.
     * Otherwise, it returns {@code false}.
     */
    boolean stop();
}
