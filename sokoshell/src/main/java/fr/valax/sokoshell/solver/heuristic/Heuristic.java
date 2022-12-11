package fr.valax.sokoshell.solver.heuristic;

import fr.valax.sokoshell.solver.State;

/**
 * Heuristic computing class for guided-search (e.g. A*)
 */
public interface Heuristic {

    /**
     * Computes the heuristic of the given state.
     * @param s the state to compute the heuristic
     * @return the heuristic of the state
     */
    int compute(State s);

}
