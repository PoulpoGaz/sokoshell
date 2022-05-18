package fr.valax.tipe;

public interface Solver {

    SolverStatus solve(Level level);

    void pause();

    void resume();

    void stop();
}
