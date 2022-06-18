package fr.valax.sokoshell.solver.tasks;

import fr.valax.sokoshell.solver.*;

import java.util.concurrent.CompletableFuture;

/**
 * A solver task is used to solve a sokoban in another thread.
 * If the solver implements {@link Trackable}, a {@link Tracker} is added
 * to the solver.
 */
public class SolverTask extends AbstractSolverTask<Solution> {

    private final SolverParameters parameters;

    public SolverTask(Solver solver, SolverParameters parameters) {
        super(solver);
        this.parameters = parameters;
    }

    @Override
    protected Tracker getTracker() {
        Object object = getParameters().get(Tracker.TRACKER_PARAM);

        if (object instanceof Tracker tracker) {
            return tracker;
        } else {
            return new BasicTracker();
        }
    }

    protected Solution solve() {
        try {
            return solver.solve(parameters);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        } finally {
            if (trackerFuture != null) {
                trackerFuture.cancel(false);
            }
        }
    }

    @Override
    public void stop() {
        if (solverFuture != null) {
            solver.stop();
        }

        if (trackerFuture != null) {
            trackerFuture.cancel(false);
        }
    }

    public CompletableFuture<Solution> getSolverFuture() {
        return solverFuture;
    }

    public Solver getSolver() {
        return solver;
    }

    public SolverParameters getParameters() {
        return parameters;
    }
}
