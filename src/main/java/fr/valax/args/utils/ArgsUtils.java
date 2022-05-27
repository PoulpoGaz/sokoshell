package fr.valax.args.utils;

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
        List<String> split = new ArrayList<>();
        StringBuilder builder = new StringBuilder();

        boolean inQuotation = false;
        boolean escapeNext = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            boolean escape = escapeNext;
            escapeNext = false;

            if (escape) {
                builder.append(c);

            } else if (c == '\\') {
                escapeNext = true;

            } else if (c == '"') {
                inQuotation = !inQuotation;

            } else if (c == ' ' && !inQuotation) {
                if (!builder.isEmpty()) {
                    split.add(builder.toString());
                    builder.setLength(0);
                }

            } else {
                builder.append(c);
            }

        }

        if (!builder.isEmpty()) {
            split.add(builder.toString());
        }

        return split.toArray(new String[0]);
    }

    public static <T> T first(T[] array) {
        if (array == null || array.length == 0) {
            return null;
        } else {
            return array[0];
        }
    }
}