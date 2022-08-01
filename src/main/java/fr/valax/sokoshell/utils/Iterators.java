package fr.valax.sokoshell.utils;

import java.util.*;

public class Iterators {

    private static final EmptyIterator EMPTY_ITERATOR = new EmptyIterator();

    @SuppressWarnings("unchecked")
    public static <T> Iterator<T> emptyIterator() {
        return (Iterator<T>) EMPTY_ITERATOR;
    }

    public static <T> Iterator<T> singleValueIterator(T value) {
        return new SingleObjectIterator<>(value);
    }

    public static <T> List<T> iteratorToList(Iterator<T> iterator) {
        if (!iterator.hasNext()) {
            return List.of();
        } else {
            List<T> list = new ArrayList<>();

            while (iterator.hasNext()) {
                list.add(iterator.next());
            }

            return list;
        }
    }

    private static class EmptyIterator implements Iterator<Object> {
        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public Object next() {
            throw new NoSuchElementException();
        }
    }

    private static class SingleObjectIterator<T> implements Iterator<T> {

        private final T value;
        private boolean hasNext = true;

        public SingleObjectIterator(T value) {
            this.value = value;
        }

        @Override
        public boolean hasNext() {
            return hasNext;
        }

        @Override
        public T next() {
            if (hasNext()) {
                hasNext = false;
                return value;
            }

            throw new NoSuchElementException();
        }
    }
}
