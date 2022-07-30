package fr.valax.args.utils;

import fr.valax.args.api.TypeConverter;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author PoulpoGaz
 */
public class ArgsUtils {

    public static final String USER_HOME = System.getProperty("user.home");

    public static boolean contains(char[] array, char o) {
        for (char t : array) {
            if (o == t) {
                return true;
            }
        }

        return false;
    }

    public static <T> boolean contains(T[] array, T o) {
        if (o == null) {
            for (T t : array) {
                if (t == null) {
                    return true;
                }
            }
        } else {
            for (T t : array) {
                if (o.equals(t)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static <T, U extends Comparable<? super U>> Comparator<T> comparing(
            Function<? super T, ? extends U> keyExtractor) {
        Objects.requireNonNull(keyExtractor);
        return (c1, c2) -> {
            U u1 = keyExtractor.apply(c1);
            U u2 = keyExtractor.apply(c2);

            if (u1 == null && u2 == null) {
                return 0;
            } else if (u1 == null) {
                return -1;
            } else if (u2 == null) {
                return 1;
            } else {
                return u1.compareTo(u2);
            }
        };
    }

    public static void notNull(TypeConverter<?> converter, String err, Object... args) throws CommandLineException {
        if (converter == null) {
            thrExc(err, args);
        }
    }

    public static void thrExc(String format, Object... args) throws CommandLineException {
        throw new CommandLineException(format.formatted(args));
    }

    public static <T> T first(T[] array) {
        if (array == null || array.length == 0) {
            return null;
        } else {
            return array[0];
        }
    }

    public static char[] asCharArray(List<Character> chars) {
        char[] newChars = new char[chars.size()];

        for (int i = 0; i < chars.size(); i++) {
            newChars[i] = Objects.requireNonNull(chars.get(i));
        }

        return newChars;
    }
}