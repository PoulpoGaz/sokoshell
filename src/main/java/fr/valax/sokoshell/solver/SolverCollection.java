package fr.valax.sokoshell.solver;

public interface SolverCollection<T extends State> {

    void clear();

    boolean isEmpty();

    int size();

    void addState(T state);

    T popState();

    T topState();

    void popAndCacheState();

    T curCachedState();
}
