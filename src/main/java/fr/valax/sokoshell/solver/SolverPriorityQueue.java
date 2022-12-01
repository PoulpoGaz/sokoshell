package fr.valax.sokoshell.solver;

import java.util.*;

/**
 * Priority queue of dynamic capacity. The priority are in <strong>ASCENDANT</strong> order, i.e. the element returned
 * by {@link SolverPriorityQueue#popState()} with the <strong>LOWEST</strong> priority.
 */
public class SolverPriorityQueue implements SolverCollection {

    /**
     * Array of nodes.
     */
    private final List<Node> nodes = new ArrayList<>();

    private int parent(int i) {
        assert i != 0;
        return (i - 1) / 2;
    }

    private int leftChild(int i) {
        return 2 * i + 1;
    }

    private int rightChild(int i) {
        return 2 * i + 2;
    }

    private void moveNodeUp(int i) {
        if (i == 0) {
            return;
        }
        final int p = parent(i);
        if (nodes.get(i).hasPriorityOver(nodes.get(p))) {
            Collections.swap(nodes, i, p);
            moveNodeUp(p);
        }
    }

    private void moveNodeDown(int i) {
        int j = i;
        final int l = leftChild(i), r = rightChild(i);
        if (l < nodes.size() && nodes.get(l).hasPriorityOver(nodes.get(i))) {
            j = l;
        }
        if (r < nodes.size() && nodes.get(r).hasPriorityOver(nodes.get(l))) {
            j = r;
        }

        if (i != j) {
            Collections.swap(nodes, i, j);
            moveNodeDown(j);
        }
    }

    @Override
    public void clear() {
        nodes.clear();
    }

    @Override
    public boolean isEmpty() {
        return nodes.isEmpty();
    }

    @Override
    public int size() {
        return nodes.size();
    }

    @Override
    public void addState(State state) {

    }

    public void addState(State state, int priority) {
        nodes.add(new Node(state, priority));
        moveNodeUp(nodes.size() - 1);
    }

    @Override
    public State popState() {
        final int j = nodes.size() - 1;
        Collections.swap(nodes, 0, j);
        final State s = nodes.remove(j).state;
        moveNodeDown(0);
        return s;
    }

    @Override
    public State topState() {
        return nodes.get(0).state;
    }

    /**
     * Priority queue (state, priority) couple.
     */
    private record Node(State state, int priority) {
        public boolean hasPriorityOver(Node o) {
            return priority < o.priority;
        }

        @Override
        public String toString() {
            return String.format("Node[priority=%d]", priority);
        }
    }
}

