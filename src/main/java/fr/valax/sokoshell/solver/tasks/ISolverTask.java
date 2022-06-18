package fr.valax.sokoshell.solver.tasks;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface ISolverTask<T> {

    void start();

    void stop();

    CompletableFuture<Void> onEnd(Consumer<T> consumer);
}
