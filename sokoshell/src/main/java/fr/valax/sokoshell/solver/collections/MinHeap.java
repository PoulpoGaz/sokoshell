package fr.valax.sokoshell.solver.collections;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MinHeap<T> {

    /**
     * Array of nodes.
     */
    protected final List<Node<T>> nodes;

    protected int currentSize;

    public MinHeap() {
        nodes = new ArrayList<>();
        currentSize = -1;
    }

    /**
     * Creates a min heap of fixed capacity.
     * This has 2 major consequences :
     * <ul>
     *     <li>this constructor instantiates empty object in each of the cases of the min heap array</li>
     *     <li>When {@link MinHeap#add(Object, int)} is called, no element is created nor added : the case where the
     *     new element goes is only updated with the new object values.</li>
     * </ul>
     * @param capacity The (fixed) capacity of the heap
     */
    public MinHeap(int capacity) {
        nodes = new ArrayList<>(capacity);
        for (int i = 0; i < capacity; i++) {
            nodes.add(i, new Node<T>());
        }
        currentSize = 0;
    }

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
        if (l < size() && nodes.get(l).hasPriorityOver(nodes.get(i))) {
            j = l;
        }
        if (r < size() && nodes.get(r).hasPriorityOver(nodes.get(l))) {
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
        int i = 0;
        if (currentSize == -1) {
            nodes.add(new Node<>(content, priority));
            moveNodeUp(nodes.size() - 1);
        } else {
            nodes.get(currentSize).set(content, priority);
            moveNodeUp(currentSize);
            currentSize++;
        }
    }

    public T pop() {
        final int i = size() - 1;
        Collections.swap(nodes, 0, i);
        T content;
        if (currentSize == -1) {
            content = nodes.remove(i).content();
        } else {
            content = nodes.get(i).content();
            currentSize--;
        }
        moveNodeDown(0);
        return content;
    }

    public T peek() {
        return nodes.get(0).content();
    }

    public void clear() {
        if (currentSize == - 1) {
            nodes.clear();
        } else {
            currentSize = 0;
        }
    }

    public boolean isEmpty() {
        return currentSize == -1 ? nodes.isEmpty() : (currentSize == 0);
    }

    public int size() {
        return currentSize == -1 ? nodes.size() : currentSize;
    }

    /**
     * Min heap (state, priority) couple.
     */
    protected static final class Node<T> {
        private T content;
        private int priority;

        public Node() {
            set(null, Integer.MAX_VALUE);
        }

        public Node(T content, int priority) {
            set(content, priority);
        }

        public boolean hasPriorityOver(Node<T> o) {
            return priority < o.priority;
        }

        @Override
        public String toString() {
            return String.format("Node[priority=%d]", priority);
        }

        public void set(T content, int priority) {
            this.content = content;
            this.priority = priority;
        }

        public T content() {
            return content;
        }

        public void setContent(T content) {
            this.content = content;
        }

        public int priority() {
            return priority;
        }

        public void setPriority(int priority) {
            this.priority = priority;
        }
    }
}
