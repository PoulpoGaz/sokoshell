package fr.valax.tipe.solver;

public interface Solver {

    SolverStatus solve(Level level);

    void pause();

    void resume();

    void stop();
}
