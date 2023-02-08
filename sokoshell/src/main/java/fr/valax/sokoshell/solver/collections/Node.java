package fr.valax.sokoshell.solver.collections;

public class Node<E> {

    protected Node<E> next;
    protected E value;

    public Node(E value) {
        this.value = value;
    }

    /**
     * Detach this node from the linked list. After this call
     * {@link #next()} will return null. If any node has for next
     * this node, it won't be detached from these nodes.
     *
     * @return next node
     */
    public Node<E> detach() {
        Node<E> oldNext = next;
        next = null;
        return oldNext;
    }

    /**
     * Makes the specified node the previous node of this node.
     *
     * @param node new parent
     */
    public void attach(Node<E> node) {
        node.next = this;
    }

    public Node<E> next() {
        return next;
    }

    public E getValue() {
        return value;
    }

    public void setValue(E value) {
        this.value = value;
    }
}
