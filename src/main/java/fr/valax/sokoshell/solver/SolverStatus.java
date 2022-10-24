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
    public static final SolverStatus RAM_EXCEED = new SolverStatus("Ram exceed");

    private static final SolverStatus[] DEFAULT = new SolverStatus[] {
            NO_SOLUTION, SOLUTION_FOUND, STOPPED, TIMEOUT, RAM_EXCEED
    };

    public static SolverStatus valueOf(String status) {
        for (SolverStatus default_ : DEFAULT) {
            if (default_.status().equals(status)) {
                return default_;
            }
        }

        return new SolverStatus(status);
    }

    @Override
    public String toString() {
        return status;
    }
}
