package fr.valax.sokoshell.solver.heuristic;

import fr.valax.sokoshell.solver.Map;

public abstract class AbstractHeuristic implements Heuristic {

    protected final Map map;

    public AbstractHeuristic(Map map) {
        this.map = map;
    }
}
