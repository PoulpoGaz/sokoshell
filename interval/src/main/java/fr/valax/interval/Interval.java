package fr.valax.interval;

import static fr.valax.interval.Bound.*;

public class Interval implements Set {

    private static final Interval ALL = new Interval(NEGATIVE_INFINITY, POSITIVE_INFINITY);

    public static Interval open(float min, float max) {
        checkRange(min, max);
        return new Interval(new OpenFinite(min), new OpenFinite(max));
    }

    public static Interval openClosed(float min, float max) {
        checkRange(min, max);
        return new Interval(new OpenFinite(min), new ClosedFinite(max));
    }

    public static Interval closedOpen(float min, float max) {
        checkRange(min, max);
        return new Interval(new ClosedFinite(min), new OpenFinite(max));
    }

    public static Interval closed(float min, float max) {
        checkRange(min, max);
        return new Interval(new ClosedFinite(min), new ClosedFinite(max));
    }

    public static Interval greaterThan(float min) {
        return new Interval(new ClosedFinite(min), POSITIVE_INFINITY);
    }

    public static Interval lessThan(float min) {
        return new Interval(NEGATIVE_INFINITY, new ClosedFinite(min));
    }

    public static Interval strictGreaterThan(float min) {
        return new Interval(new OpenFinite(min), POSITIVE_INFINITY);
    }

    public static Interval strictLessThan(float min) {
        return new Interval(NEGATIVE_INFINITY, new OpenFinite(min));
    }

    public static Interval all() {
        return ALL;
    }

    private static void checkRange(float min, float max) {
        if (min >= max) {
            throw new IllegalArgumentException("Bad range: " +
                    min + ">=" + max);
        }
    }

    private final Bound left;
    private final Bound right;

    private Interval(Bound left, Bound right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public boolean contains(float real) {
        return left.lessThan(real) && right.greaterThan(real);
    }

    /**
     * @return true if the intersection is non-empty
     */
    public boolean nonEmptyIntersection(Interval i) {
        Bound leftMax = Utils.max(left, i.left, Bound::valueCompare);
        Bound rightMin = Utils.min(right, i.right, Bound::valueCompare);

        if (leftMax.value() == rightMin.value() && leftMax.isFinite()) {
            return leftMax.isClosed() && rightMin.isClosed();
        } else {
            return rightMin.value() > leftMax.value();
        }
    }

    /**
     * @return true if the union with 'i' is not a union
     */
    public boolean isConnected(Interval i) {
        Bound leftMax = Utils.max(left, i.left, Bound::valueCompare);
        Bound rightMin = Utils.min(right, i.right, Bound::valueCompare);

        if (leftMax.value() == rightMin.value() && leftMax.isFinite()) {
            return leftMax.isClosed() || rightMin.isClosed();
        } else {
            return rightMin.value() > leftMax.value();
        }
    }

    private Set newInterval(Interval i, Bound min, Bound max) {
        if (min == left && max == right) {
            return this;
        } else if (min == i.left && max == i.right) {
            return i;
        } else if (min == NEGATIVE_INFINITY && max == POSITIVE_INFINITY) {
            return all();
        } else if (min.value() == max.value()) {
            return new Singleton(min.value());
        } else {
            return new Interval(min, max);
        }
    }

    private Bound min(Bound a, Bound b, boolean preferOpen) {
        if (a.value() < b.value()) {
            return a;
        } else if (a.value() > b.value()) {
            return b;
        } else if (a.isFinite()) { // so b is finite
            if (preferOpen) {
                return a.isOpen() ? a : b;
            } else {
                return a.isClosed() ? a : b;
            }
        } else { // a and b are POSITIVE_INFINITY or NEGATIVE_INFINITY
            return a;
        }
    }

    private Bound max(Bound a, Bound b, boolean preferOpen) {
        if (a.value() > b.value()) {
            return a;
        } else if (a.value() < b.value()) {
            return b;
        } else if (a.isFinite()) { // so b is finite
            if (preferOpen) {
                return a.isOpen() ? a : b;
            } else {
                return a.isClosed() ? a : b;
            }
        } else { // a and b are POSITIVE_INFINITY or NEGATIVE_INFINITY
            return a;
        }
    }

    @Override
    public Set union(Set set) {
        if (set instanceof Interval i) {

            if (isConnected(i)) {
                Bound min = min(left, i.left, false);
                Bound max = max(right, i.right, false);

                return newInterval(i, min, max);
            } else {
                return new Union(this, i);
            }
        } else {
            // redirect to Singleton or Union
            return set.union(this);
        }
    }

    @Override
    public Set intersect(Set set) {
        if (set == this) {
            return this;
        } else if (set instanceof Interval i) {

            if (nonEmptyIntersection(i)) {
                Bound min = max(left, i.left, true);
                Bound max = min(right, i.right, true);

                return newInterval(i, min, max);
            } else {
                return Empty.INSTANCE;
            }
        } else {
            // redirect to Singleton or Union
            return set.intersect(this);
        }
    }

    @Override
    public float sup() {
        return right.value();
    }

    @Override
    public float inf() {
        return left.value();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Interval interval = (Interval) o;

        if (!left.equals(interval.left)) return false;
        return right.equals(interval.right);
    }

    @Override
    public int hashCode() {
        int result = left.hashCode();
        result = 31 * result + right.hashCode();
        return result;
    }

    public Bound getLeftBound() {
        return left;
    }

    public Bound getRightBound() {
        return right;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        if (left.isInfinity()) {
            sb.append("]-inf");
        } else if (left.isClosed()) {
            sb.append("[").append(left.value());
        } else if (left.isOpen()) {
            sb.append("]").append(left.value());
        }

        sb.append("; ");

        if (right.isInfinity()) {
            sb.append("+inf[");
        } else if (right.isClosed()) {
            sb.append(right.value()).append("]");
        } else if (right.isOpen()) {
            sb.append(right.value()).append("[");
        }

        return sb.toString();
    }
}
