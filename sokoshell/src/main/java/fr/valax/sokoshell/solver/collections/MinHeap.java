package fr.valax.sokoshell.solver.collections;

import fr.valax.sokoshell.solver.WeightedState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MinHeap<T> {

    /**
     * Array of nodes.
     */
    private final List<Node<T>> nodes = new ArrayList<>();

    protected int leftChild(int i) {
        return 2 * i + 1;
    }

    protected int rightChild(int i) {
        return 2 * i + 2;
    }

    protected void moveNodeUp(int i) {
        if (i == 0) {
            return;
        }
        final int p = parent(i);
        if (nodes.get(i).hasPriorityOver(nodes.get(p))) {
            Collections.swap(nodes, i, p);
            moveNodeUp(p);
        }
    }

    protected void moveNodeDown(int i) {
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

    private int parent(int i) {
        assert i != 0;
        return (i - 1) / 2;
    }

    public void add(T content, int priority) {
        nodes.add(new Node<T>(content, priority));
        moveNodeUp(nodes.size() - 1);
    }

    public T pop() {
        final int j = nodes.size() - 1;
        Collections.swap(nodes, 0, j);
        final T content = nodes.remove(j).content;
        moveNodeDown(0);
        return content;
    }

    public T peek() {
        return nodes.get(0).content;
    }

    public void clear() {
        nodes.clear();
    }

    public boolean isEmpty() {
        return nodes.isEmpty();
    }

    public int size() {
        return nodes.size();
    }

    /**
     * Min heap (state, priority) couple.
     */
    private record Node<T>(T content, int priority) {
        public boolean hasPriorityOver(Node<T> o) {
            return priority < o.priority;
        }

        @Override
        public String toString() {
            return String.format("Node[priority=%d]", priority);
        }
    }
}
