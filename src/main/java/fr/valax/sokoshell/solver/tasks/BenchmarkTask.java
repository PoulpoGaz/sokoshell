package fr.valax.sokoshell.solver.tasks;

import fr.valax.sokoshell.SokoShellHelper;
import fr.valax.sokoshell.solver.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class BenchmarkTask extends AbstractSolverTask<List<Solution>> {

    private final Map<String, Object> params;
    private final Pack pack;

    public BenchmarkTask(Solver solver, Map<String, Object> params, Pack pack) {
        super(solver);
        this.params = params;
        this.pack = pack;
    }

    @Override
    protected Tracker getTracker() {
        Object object = params.get(Tracker.TRACKER_PARAM);

        if (object instanceof Tracker tracker) {
            return tracker;
        } else {
            return new BasicTracker();
        }
    }

    protected List<Solution> solve() {
        try {
            List<Level> levels = pack.levels();
            List<Solution> solutions = new ArrayList<>(levels.size());

            for (Level level : levels) {
                SokoShellHelper.INSTANCE.tryPrintln("Solving level n°" + (level.getIndex() + 1), 5, TimeUnit.MILLISECONDS);

                SolverParameters parameters = new SolverParameters(solver.getSolverType(), level, params);

                Solution solution = solver.solve(parameters);

                solutions.add(solution);
                if (solution.isStopped()) {
                    break;
                }
            }

            return solutions;
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

    @Override
    public CompletableFuture<Void> onEnd(Consumer<List<Solution>> consumer) {
        return solverFuture.thenAccept(consumer);
    }
}
