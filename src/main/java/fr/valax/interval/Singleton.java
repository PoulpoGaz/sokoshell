package fr.valax.interval;

public record Singleton(float value) implements Set {

    public Singleton {
        if (Float.isInfinite(value) || Float.isNaN(value)) {
            throw new IllegalArgumentException("Can't create singleton of +-inf or NaN");
        }
    }

    @Override
    public boolean contains(float real) {
        return value == real;
    }

    @Override
    public Set union(Set set) {
        if (set.contains(value)) {
            return set;
        } else {
            return new Union(set, this);
        }
    }

    @Override
    public Set intersect(Set set) {
        if (set.contains(value)) {
            return this;
        } else {
            return Empty.INSTANCE;
        }
    }

    @Override
    public float sup() {
        return value;
    }

    @Override
    public float inf() {
        return value;
    }

    @Override
    public String toString() {
        return "{%s}".formatted(value);
    }
}