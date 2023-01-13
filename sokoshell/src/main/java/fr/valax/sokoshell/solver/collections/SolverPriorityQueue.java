package fr.valax.sokoshell.solver.collections;

import fr.valax.sokoshell.solver.WeightedState;

import java.util.*;

/**
 * Priority queue of dynamic capacity. The priority are in <strong>ASCENDANT</strong> order, i.e. the element returned
 * by {@link SolverPriorityQueue#popState()} with the <strong>LOWEST</strong> priority.
 */
public class SolverPriorityQueue implements SolverCollection<WeightedState> {

    private final MinHeap<WeightedState> heap = new MinHeap<>();

    private WeightedState cachedState;

    @Override
    public void addState(WeightedState state) {
        heap.add(state, state.weight());
    }

    @Override
    public WeightedState popState() {
        return heap.pop();
    }

    @Override
    public WeightedState peekState() {
        return heap.peek();
    }

    @Override
    public WeightedState peekAndCacheState() {
        cachedState = popState();
        return cachedState;
    }

    @Override
    public WeightedState cachedState() {
        return cachedState;
    }

    @Override
    public void clear() {
        heap.clear();
    }

    @Override
    public boolean isEmpty() {
        return heap.isEmpty();
    }

    @Override
    public int size() {
        return heap.size();
    }
}

