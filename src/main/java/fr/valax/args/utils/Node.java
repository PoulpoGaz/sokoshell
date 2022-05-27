package fr.valax.args.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * A basic tree class.
 * Don't expect good performance from this
 * @author PoulpoGaz
 */
public class Node<V> implements INode<V> {

    /** contains {@code Node<V>}*/
    private final List<INode<V>> children;

    /** contains {@code Node<V>}*/
    private final List<INode<V>> immutableChildren;

    private INode<V> parent;

    private V value;

    public Node() {
        this(null);
    }

    public Node(V value) {
        this.value = value;
        children = new ArrayList<>();

        // it returns a read-only view of the List
        immutableChildren = Collections.unmodifiableList(children);
    }

    @Override
    public V getValue() {
        return value;
    }

    @Override
    public void setValue(V value) {
        this.value = value;
    }

    @Override
    public INode<V> getParent() {
        return parent;
    }

    @Override
    public List<INode<V>> getChildren() {
        return immutableChildren;
    }

    @Override
    public INode<V> addChild(V child) {
        return addChild(new Node<>(child));
    }

    @Override
    public INode<V> addChild(INode<V> child) {
        if (!(child instanceof Node<V>)) {
            throw new IllegalArgumentException("Not a Node<V>");
        } else if (isAncestor(child)) {
            throw new IllegalArgumentException("New child is an ancestor");
        } else {
            child.removeFromParent();
            children.add(child);
            ((Node<V>) child).parent = this;

            return child;
        }
    }

    @Override
    public INode<V> removeChild(int index) {
        if (index < 0 || index >= nChildren()) {
            throw new IndexOutOfBoundsException(index);
        }

        Node<V> child = (Node<V>) children.get(index);
        child.parent = null;
        children.remove(index);

        return child;
    }

    @Override
    public boolean removeChild(INode<V> child) {
        if (child == null) {
            return false;
        }

        int i = children.indexOf(child);

        return removeChild(i) == child;
    }

    @Override
    public boolean removeChild(V childValue) {
        for (int i = 0; i < children.size(); i++) {
            if (Objects.equals(children.get(i).getValue(), childValue)) {
                removeChild(i);
                return true;
            }
        }

        return false;
    }

    @Override
    public <R> INode<R> map(Function<V, R> mapper) {
        R ret = mapper.apply(value);

        Node<R> newNode = new Node<>(ret);

        for (INode<V> c : children) {
            Node<R> mappedChild = (Node<R>) c.map(mapper);

            mappedChild.parent = newNode;
            newNode.children.add(mappedChild);
        }

        return newNode;
    }

    @Override
    public <OUT, THROWABLE extends Throwable> INode<OUT> mapThrow(ThrowFunction<V, OUT, THROWABLE> mapper) throws THROWABLE {
        OUT ret = mapper.apply(value);

        Node<OUT> newNode = new Node<>(ret);

        for (INode<V> c : children) {
            Node<OUT> mappedChild = (Node<OUT>) c.mapThrow(mapper);

            mappedChild.parent = newNode;
            newNode.children.add(mappedChild);
        }

        return newNode;
    }

    /**
     * @return a copy of this node and his descendants
     */
    public ImmutableNode<V> immutableCopy() {
        return immutableCopy(null);
    }

    private ImmutableNode<V> immutableCopy(ImmutableNode<V> parent) {
        List<INode<V>> immutableChildren = new ArrayList<>();
        ImmutableNode<V> node = new ImmutableNode<>(parent, immutableChildren, value);

        for (INode<V> c : children) {
            Node<V> child = (Node<V>) c;

            immutableChildren.add(child.immutableCopy(node));
        }

        return node;
    }
}
