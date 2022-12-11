package fr.valax.args.utils;

import java.util.*;
import java.util.function.Function;

public interface INode<V> extends Iterable<INode<V>> {

    V getValue();

    void setValue(V value);

    default int getDepth() {
        int depth = 0;
        INode<V> node = this;

        while (node.getParent() != null) {
            node = node.getParent();
            depth++;
        }

        return depth;
    }

    default int getHeight() {
        List<INode<V>> children = getChildren();

        if (children.size() == 0) {
            return 0;
        } else {
            int maxH = 0;

            for (INode<?> child : children) {
                maxH = Math.max(maxH, child.getHeight());
            }

            return 1 + maxH;
        }
    }

    default boolean isRoot() {
        return getParent() == null;
    }

    INode<V> getParent();

    default INode<V> getRoot() {
        INode<V> node = this;

        while (node.getParent() != null) {
            node = node.getParent();
        }

        return node;
    }

    List<INode<V>> getChildren();

    default INode<V> getChild(int index) {
        return getChildren().get(index);
    }

    default boolean hasChildren() {
        return !getChildren().isEmpty();
    }

    default int nChildren() {
        return getChildren().size();
    }

    default boolean isAncestor(INode<V> node) {
        INode<V> n = this;

        while (n != null) {
            if (n == node) {
                return true;
            } else {
                n = n.getParent();
            }
        }

        return false;
    }

    default boolean isDirectChild(INode<V> node) {
        return getChildren().contains(node);
    }

    INode<V> addChild(V child);

    INode<V> addChild(INode<V> child);

    INode<V> removeChild(int index);

    boolean removeChild(INode<V> child);

    boolean removeChild(V childValue);

    default boolean removeFromParent() {
        INode<V> parent = getParent();

        if (parent != null) {
            return parent.removeChild(this);
        } else {
            return false;
        }
    }

    <R> INode<R> map(Function<V, R> mapper);

    <OUT, THROWABLE extends Throwable> INode<OUT> mapThrow(ThrowFunction<V, OUT, THROWABLE> mapper) throws THROWABLE;

    default INode<V> find(V value) {
        Iterator<INode<V>> it = depthFirstIterator();

        while (it.hasNext()) {
            INode<V> next = it.next();

            if (Objects.equals(next.getValue(), value)) {
                return next;
            }
        }

        return null;
    }

    ImmutableNode<V> immutableCopy();

    default Iterator<INode<V>> depthFirstIterator() {
        return new TreeIterator<>(this) {
            @Override
            protected INode<V> poll() {
                return deque.pop();
            }
        };
    }

    default Iterator<INode<V>> breathFirstIterator() {
        return new TreeIterator<>(this) {
            @Override
            protected INode<V> poll() {
                return deque.poll();
            }
        };
    }

    @Override
    default Iterator<INode<V>> iterator() {
        return depthFirstIterator();
    }

    abstract class TreeIterator<V> implements Iterator<INode<V>> {

        protected final ArrayDeque<INode<V>> deque = new ArrayDeque<>();

        public TreeIterator(INode<V> root) {
            deque.offer(root);
        }

        @Override
        public boolean hasNext() {
            return !deque.isEmpty();
        }

        @Override
        public INode<V> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            INode<V> next = poll();

            List<INode<V>> nodes = next.getChildren();
            for (int i = nodes.size() - 1; i >= 0; i--) {
                INode<V> child = nodes.get(i);
                deque.offer(child);
            }

            return next;
        }

        protected abstract INode<V> poll();
    }

    @FunctionalInterface
    interface ThrowFunction<IN, OUT, THROWABLE extends Throwable> {

        OUT apply(IN input) throws THROWABLE;
    }
}
