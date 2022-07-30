package fr.valax.interval;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * A union doesn't contain a union.
 * It contains an ordered list of set sorted by the return of {@link Set#inf()}.
 * All sets should not overlap.
 *
 *
 */
public class Union implements Set {

    private final List<Set> sets;

    public Union(List<Set> sets) {
        List<Set> copy;
        if (sets instanceof ArrayList<Set>) {
            copy = sets;
        } else {
            copy = new ArrayList<>(sets);
        }

        checkValid(copy);
        this.sets = copy.stream()
                .sorted(Comparator.comparing(Set::inf))
                .toList();
    }

    public Union(Set... sets) {
        this(List.of(sets));
    }

    private void checkValid(List<Set> sets) {
        for (int i = 0; i < sets.size(); i++) {
            Set s = sets.get(i);

            if (s == null) {
                throw new NullPointerException("s is null");

            } else if (s instanceof Union u) {
                sets.addAll(i, u.getSets());
                sets.remove(u);
                i--;

            } else if (s instanceof Empty) {
                sets.remove(i);
                i--;

            } else {

                // check if s has an empty intersection with the previous sets.
                // The reverse loop avoid to check the instance of the set.
                for (int j = i - 1; j >= 0; j--) {
                    Set s2 = sets.get(j);

                    if (s.intersect(s2) != Empty.INSTANCE) {
                        throw new IllegalArgumentException("Non empty intersection");
                    }
                }
            }
        }

        if (sets.size() < 2) {
            throw new IllegalArgumentException("A union contains at least 2 elements");
        }
    }

    @Override
    public boolean contains(float real) {
        for (Set s : sets) {
            if (s.contains(real)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public Set union(Set set) {
        if (set == this) {
            return this;
        }

        if (inf() > set.sup() || set.inf() >= sup()) {
            return new Union(this, set);
        }

        List<Set> others;
        if (set instanceof Union u) {
            others = u.getSets();
        } else {
            others = List.of(set);
        }

        List<Set> output = new ArrayList<>();
        output.add(sets.get(0));

        int i = 1;
        int j = 0;
        while (i < sets.size() || j < others.size()) {
            Set last = output.get(output.size() - 1);
            Set toUnion;

            if (i < sets.size() && j < others.size()) {
                Set s1 = sets.get(i);
                Set s2 = others.get(j);

                if (s1.inf() < s2.inf()) {
                    toUnion = s1;
                    i++;
                } else {
                    toUnion = s2;
                    j++;
                }

            } else if (i < sets.size()) {
                toUnion = sets.get(i);
                i++;
            } else {
                toUnion = others.get(j);
                j++;
            }

            Set union = last.union(toUnion);
            if (union instanceof Union u) {
                output.add(u.getSets().get(1));
            } else {
                output.set(output.size() - 1, union);
            }
        }

        if (output.size() == 1) {
            return output.get(0);
        } else {
            return new Union(output);
        }
    }

    private void nextOverlapIndex(List<Set> others, IntWrapper i, IntWrapper j) {
        while (i.get() < sets.size() && j.get() < others.size()) {
            Set s = sets.get(i.get());
            Set s2 = others.get(j.get());

            if (s2.sup() < s.inf()) {
                j.increment();
            } else if (s.sup() < s2.inf()) {
                i.increment();
            } else {
                return; // END
            }
        }

        i.set(-1);
        j.set(-1);
    }

    @Override
    public Set intersect(Set set) {
        if (set == this) {
            return this;
        }

        if (inf() > set.sup() || set.inf() >= sup()) {
            return Empty.INSTANCE;
        }

        List<Set> others;
        if (set instanceof Union u) {
            others = u.getSets();
        } else {
            others = List.of(set);
        }

        List<Set> output = new ArrayList<>();

        IntWrapper i = new IntWrapper();
        IntWrapper j = new IntWrapper();

        nextOverlapIndex(others, i, j);
        while (i.get() != -1 && j.get() != -1) {
            Set s1 = sets.get(i.get());
            Set s2 = others.get(j.get());

            Set intersection = s1.intersect(s2);

            if (intersection != Empty.INSTANCE) {
                output.add(intersection);
            }

            if (s1.sup() < s2.sup()) {
                i.increment();
            } else {
                j.increment();
            }

            nextOverlapIndex(others, i, j);
        }

        if (output.size() == 0) {
            return Empty.INSTANCE;
        } else if (output.size() == 1) {
            return output.get(0);
        } else {
            return new Union(output);
        }
    }

    @Override
    public float sup() {
        return sets.get(sets.size() - 1).sup();
    }

    @Override
    public float inf() {
        return sets.get(0).inf();
    }

    public List<Set> getSets() {
        return sets;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Union union = (Union) o;

        return sets.equals(union.sets);
    }

    @Override
    public int hashCode() {
        return sets.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < sets.size() - 1; i++) {
            sb.append(sets.get(i));
            sb.append(" U ");
        }

        sb.append(sets.get(sets.size() - 1));

        return sb.toString();
    }
}
