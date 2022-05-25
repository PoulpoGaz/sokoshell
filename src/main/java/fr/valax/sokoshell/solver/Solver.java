package fr.valax.sokoshell.solver;

import java.util.ArrayList;

public interface Solver {

    SolverStatus solve(Level level, ArrayList<State> solution);

    void pause();

    void resume();

    void stop();
}
