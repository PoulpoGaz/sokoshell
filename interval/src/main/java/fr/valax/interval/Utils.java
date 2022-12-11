package fr.valax.interval;

import java.util.Comparator;

public class Utils {

    public static <T> T min(T o1, T o2, Comparator<T> comparator) {
        int c = comparator.compare(o1, o2);

        return (c < 0) ? o1 : o2;
    }

    public static <T> T max(T o1, T o2, Comparator<T> comparator) {
        int c = comparator.compare(o1, o2);

        return (c > 0) ? o1 : o2;
    }
}
