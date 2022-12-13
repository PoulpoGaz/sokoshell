package fr.valax.sokoshell.utils;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * This class supports the following wildcards:
 * <ul>
 *     <li>'*' matches zero or more {@link Character}</li>
 *     <li>'?' matches a single character</li>
 *     <li>'[a-z]' matches all characters between a and z</li>
 *     <li>'[abcdef]' matches 'a', 'b', 'c', 'd', 'e' or 'f'</li>
 *     <li>The two last can be combined and reversed with '[!...]'</li>
 * </ul>
 *
 * @param <T>
 */
public class GlobIterator<T> implements Iterator<T> {

    private static final String regexMetaChars = ".^$+{[]|()";
    private static final String globMetaChars = "\\*?[{";

    private static boolean isRegexMeta(char c) {
        return regexMetaChars.indexOf(c) != -1;
    }

    private static boolean isGlobMeta(char c) {
        return globMetaChars.indexOf(c) != -1;
    }

    private final Pattern pattern;
    private final Iterator<T> iterator;
    private final Function<T, String> toString;

    private T next;

    public GlobIterator(String glob, Iterable<T> iterable) {
        this(glob, iterable.iterator(), Object::toString);
    }

    public GlobIterator(String glob, Iterator<T> iterator) {
        this(glob, iterator, Object::toString);
    }

    public GlobIterator(String glob, Iterable<T> iterable, Function<T, String> toString) {
        this(globToRegex(glob), iterable.iterator(), toString);
    }

    public GlobIterator(String glob, Iterator<T> iterator, Function<T, String> toString) {
        this(globToRegex(glob), iterator, toString);
    }

    public GlobIterator(Pattern pattern, Iterable<T> iterable) {
        this(pattern, iterable, Object::toString);
    }

    public GlobIterator(Pattern pattern, Iterator<T> iterator) {
        this(pattern, iterator, Object::toString);
    }

    public GlobIterator(Pattern pattern, Iterable<T> iterable, Function<T, String> toString) {
        this(pattern, iterable.iterator(), toString);
    }

    public GlobIterator(Pattern pattern, Iterator<T> iterator, Function<T, String> toString) {
        this.pattern = Objects.requireNonNull(pattern);
        this.iterator = Objects.requireNonNull(iterator);
        this.toString = Objects.requireNonNull(toString);
    }

    private static Pattern globToRegex(String glob) {
        StringBuilder regex = new StringBuilder();
        regex.append("^");

        int i = 0;
        while (i < glob.length()) {
            char c = glob.charAt(i++);

            switch (c) {
                case '\\' -> {
                    if (i == glob.length()) {
                        throw new PatternSyntaxException("No character to escape", glob, i - 1);
                    }

                    char next = glob.charAt(i++);
                    if (isGlobMeta(next) || isRegexMeta(next)) {
                        regex.append('\\');
                    }
                    regex.append(next);
                }
                case '[' -> {
                    regex.append("\\[");

                    if (charAt(glob, i) == '!') {
                        regex.append("^");
                        i++;
                    }

                    while (i < glob.length()) {
                        c = glob.charAt(i);

                        if (c == '\\' && charAt(glob, i + 1) == ']') {
                            regex.append("\\]");
                            i += 2;
                        } else if (c == ']') {
                            break;
                        } else {
                            regex.append(c);
                            i++;
                        }
                    }

                    if (c != ']') {
                        throw new PatternSyntaxException("Missing ']", glob, i - 1);
                    }
                }
                case '?' -> regex.append('.');
                case '*' -> regex.append(".*");
                default -> {
                    if (isGlobMeta(c) || isRegexMeta(c)) {
                        regex.append('\\');
                    }
                    regex.append(c);
                }
            }
        }

        String regexStr = regex.append("$").toString();
        return Pattern.compile(regexStr);
    }

    private static char charAt(String str, int i) {
        if (i < str.length()) {
            return str.charAt(i);
        } else {
            return 0;
        }
    }

    private void fetchNext() {
        this.next = null;

        while (iterator.hasNext()) {
            T next = iterator.next();
            String str = toString.apply(next);

            if (pattern.matcher(str).matches()) {
                this.next = next;
                break;
            }
        }
    }

    @Override
    public boolean hasNext() {
        if (next == null) {
            fetchNext();
        }
        return next != null;
    }

    @Override
    public T next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        T n = next;
        next = null;
        return n;
    }
}
