package fr.valax.sokoshell.solver.collections;

import fr.valax.sokoshell.solver.State;

public interface SolverCollection<T extends State> {

    void clear();

    boolean isEmpty();

    int size();

    void addState(T state);

    T popState();

    T peekState();

    T peekAndCacheState();

    T cachedState();
}
