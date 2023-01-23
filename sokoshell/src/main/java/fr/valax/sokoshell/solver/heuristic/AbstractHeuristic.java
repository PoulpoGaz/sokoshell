package fr.valax.sokoshell.solver.heuristic;

import fr.valax.sokoshell.solver.board.MutableBoard;

/**
 * Base class for heuristic computing classes.
 * As there are different ways to compute the heuristic of a state, we provide a set of class each implementing
 * different heuristic calculation methods.
 */
public abstract class AbstractHeuristic implements Heuristic {

    protected final MutableBoard board;

    public AbstractHeuristic(MutableBoard board) {
        this.board = board;
    }
}
