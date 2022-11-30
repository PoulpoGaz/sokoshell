package fr.valax.sokoshell.solver;

public interface SolverCollection {

    void clear();

    boolean isEmpty();

    int size();

    void addState(State state);

    State popState();

    public State topState();
}
