package fr.valax.interval;

public class Empty implements Set {

    public static final Empty INSTANCE = new Empty();

    private Empty() {

    }

    @Override
    public boolean contains(float real) {
        return false;
    }

    @Override
    public Set union(Set set) {
        return set;
    }

    @Override
    public Set intersect(Set set) {
        return this;
    }

    @Override
    public float sup() {
        throw new IllegalStateException();
    }

    @Override
    public float inf() {
        throw new IllegalStateException();
    }
}
