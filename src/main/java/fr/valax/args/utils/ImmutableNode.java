package fr.valax.args.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class ImmutableNode<V> implements INode<V> {

    private final INode<V> parent;
    private final List<INode<V>> children;
    private final V value;

    ImmutableNode(INode<V> parent,
                  List<INode<V>> children,
                  V value) {
        this.parent = parent;
        this.children = Collections.unmodifiableList(children);
        this.value = value;
    }

    @Override
    public V getValue() {
        return value;
    }

    @Override
    public void setValue(V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public INode<V> getParent() {
        return parent;
    }

    @Override
    public List<INode<V>> getChildren() {
        return children;
    }

    @Override
    public INode<V> addChild(V child) {
        throw new UnsupportedOperationException();
    }

    @Override
    public INode<V> addChild(INode<V> child) {
        throw new UnsupportedOperationException();
    }

    @Override
    public INode<V> removeChild(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeChild(INode<V> child) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeChild(V childValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <R> INode<R> map(Function<V, R> mapper) {
        return map(mapper, null);
    }

    private <R> ImmutableNode<R> map(Function<V, R> mapper, ImmutableNode<R> parent) {
        List<INode<R>> immutableChildren = new ArrayList<>();
        ImmutableNode<R> node = new ImmutableNode<>(parent, immutableChildren, mapper.apply(value));

        for (INode<V> c : children) {
            ImmutableNode<V> child = (ImmutableNode<V>) c;

            immutableChildren.add(child.map(mapper, node));
        }

        return node;
    }

    @Override
    public <OUT, THROWABLE extends Throwable> INode<OUT> mapThrow(ThrowFunction<V, OUT, THROWABLE> mapper) throws THROWABLE {
        return mapThrow(mapper, null);
    }

    private <OUT, THROWABLE extends Throwable> ImmutableNode<OUT>
    mapThrow(ThrowFunction<V, OUT, THROWABLE> mapper, ImmutableNode<OUT> parent) throws THROWABLE {
        List<INode<OUT>> immutableChildren = new ArrayList<>();
        ImmutableNode<OUT> node = new ImmutableNode<>(parent, immutableChildren, mapper.apply(value));

        for (INode<V> c : children) {
            ImmutableNode<V> child = (ImmutableNode<V>) c;

            immutableChildren.add(child.mapThrow(mapper, node));
        }

        return node;
    }

    @Override
    public ImmutableNode<V> immutableCopy() {
        return this;
    }
}
