package fr.valax.sokoshell.solver;

/**
 * How to name this class?
 *
 * @author darth-mole
 * @author PoulpoGaz
 */
public record SolverStatus(String status) {

    public static final SolverStatus NO_SOLUTION = new SolverStatus("No solution");
    public static final SolverStatus SOLUTION_FOUND = new SolverStatus("Solution found");
    public static final SolverStatus STOPPED = new SolverStatus("Stopped");
    public static final SolverStatus TIMEOUT = new SolverStatus("Timeout");

    private static final SolverStatus[] DEFAULT = new SolverStatus[] {
            NO_SOLUTION, SOLUTION_FOUND, STOPPED, TIMEOUT
    };

    public static SolverStatus valueOf(String status) {
        for (SolverStatus default_ : DEFAULT) {
            if (default_.status().equals(status)) {
                return default_;
            }
        }

        return new SolverStatus(status);
    }
}
