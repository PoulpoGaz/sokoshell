package fr.valax.args.utils;

import fr.valax.args.Tokenizer;
import fr.valax.args.api.TypeConverter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author PoulpoGaz
 */
public class ArgsUtils {

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

    public static void thrParseExc(String format, Object... args) throws ParseException {
        throw new ParseException(format.formatted(args));
    }

    public static String[] splitQuoted(String line) {
        List<String> strings = new ArrayList<>();

        Tokenizer t = new Tokenizer(line);
        while (t.hasNext()) {
            strings.add(t.next().value());
        }

        return strings.toArray(new String[0]);
    }

    public static <T> T first(T[] array) {
        if (array == null || array.length == 0) {
            return null;
        } else {
            return array[0];
        }
    }
}